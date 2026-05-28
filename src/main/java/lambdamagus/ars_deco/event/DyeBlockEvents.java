package lambdamagus.ars_deco.event;

import lambdamagus.ars_deco.ArsDeco;
import lambdamagus.ars_deco.dye.ArsDecoDyeableBlockEntity;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import lambdamagus.ars_deco.dye.DyeTarget;
import lambdamagus.ars_deco.dye.PlacedDyeColors;
import lambdamagus.ars_deco.network.SyncBlockDyeColorPayload;
import lambdamagus.ars_deco.network.SyncChunkDyeColorsPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = ArsDeco.MODID)
public final class DyeBlockEvents {
    private static final Map<ServerLevel, Map<Long, DyeColor>> PENDING_BREAK_COLORS = new WeakHashMap<>();

    private DyeBlockEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack held = event.getItemStack();
        DyeColor color = DyeColor.getColor(held);
        if (color == null) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        DyeTarget target = DyeableArsBlocks.target(level.getBlockState(pos).getBlock()).orElse(null);
        if (target == null) {
            return;
        }

        event.setUseBlock(TriState.FALSE);
        event.setUseItem(TriState.FALSE);

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ArsDecoDyeableBlockEntity dyeable) {
            if (dyeable.arsDeco$getColor().filter(color::equals).isPresent()) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }

            if (!level.isClientSide) {
                dyeable.arsDeco$setColor(color);
                blockEntity.setChanged();
                level.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (level instanceof ServerLevel serverLevel) {
                    PlacedDyeColors.PlacedColor placedColor = new PlacedDyeColors.PlacedColor(target.blockId(), color);
                    PlacedDyeColors.get(serverLevel).set(pos, target.blockId(), color);
                    PacketDistributor.sendToPlayersTrackingChunk(serverLevel, level.getChunk(pos).getPos(), new SyncBlockDyeColorPayload(pos, placedColor));
                }

                Player player = event.getEntity();
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            }

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            PlacedDyeColors colors = PlacedDyeColors.get(serverLevel);
            if (colors.getColor(pos, target.blockId()).filter(color::equals).isPresent()) {
                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
                return;
            }

            PlacedDyeColors.PlacedColor placedColor = new PlacedDyeColors.PlacedColor(target.blockId(), color);
            colors.set(pos, target.blockId(), color);
            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, level.getChunk(pos).getPos(), new SyncBlockDyeColorPayload(pos, placedColor));

            Player player = event.getEntity();
            if (!player.getAbilities().instabuild) {
                held.shrink(1);
            }

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel) || DyeableArsBlocks.target(event.getState().getBlock()).isEmpty()) {
            return;
        }

        DyeColor color = colorAt(serverLevel, event.getPos(), event.getState(), serverLevel.getBlockEntity(event.getPos()));
        if (color != null) {
            PENDING_BREAK_COLORS.computeIfAbsent(serverLevel, level -> new HashMap<>()).put(event.getPos().asLong(), color);
        }
    }

    @SubscribeEvent
    public static void onBlockDrops(BlockDropsEvent event) {
        if (DyeableArsBlocks.target(event.getState().getBlock()).isEmpty()) {
            return;
        }
        DyeColor color = colorFromDropSource(event);
        if (color == null) {
            return;
        }

        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            if (DyeableArsBlocks.isDyeable(stack)) {
                DyeableArsBlocks.setColor(stack, color);
            }
        }

        PlacedDyeColors.get(event.getLevel()).clear(event.getPos());
        PacketDistributor.sendToPlayersTrackingChunk(event.getLevel(), event.getLevel().getChunk(event.getPos()).getPos(), new SyncBlockDyeColorPayload(event.getPos(), null));
    }

    @SubscribeEvent
    public static void onChunkSent(ChunkWatchEvent.Sent event) {
        var colors = PlacedDyeColors.get(event.getLevel()).getChunkColors(event.getPos());
        if (!colors.isEmpty()) {
            PacketDistributor.sendToPlayer(event.getPlayer(), new SyncChunkDyeColorsPayload(event.getPos(), colors));
        }
    }

    private static DyeColor colorFromDropSource(BlockDropsEvent event) {
        DyeColor color = colorAt(event.getLevel(), event.getPos(), event.getState(), event.getBlockEntity());
        if (color != null) {
            return color;
        }

        Map<Long, DyeColor> levelColors = PENDING_BREAK_COLORS.get(event.getLevel());
        return levelColors == null ? null : levelColors.remove(event.getPos().asLong());
    }

    private static DyeColor colorAt(ServerLevel level, BlockPos pos, net.minecraft.world.level.block.state.BlockState state, BlockEntity blockEntity) {
        if (blockEntity instanceof ArsDecoDyeableBlockEntity dyeable) {
            DyeColor color = dyeable.arsDeco$getColor().orElse(null);
            if (color != null) {
                return color;
            }
        }

        ResourceLocation blockId = DyeableArsBlocks.target(state.getBlock()).map(DyeTarget::blockId).orElse(null);
        if (blockId == null) {
            return null;
        }
        return PlacedDyeColors.get(level).getColor(pos, blockId).orElse(null);
    }

}
