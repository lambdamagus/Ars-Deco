package lambdamagus.ars_deco.mixin;

import lambdamagus.ars_deco.dye.ArsDecoDyeableBlockEntity;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import lambdamagus.ars_deco.dye.DyeTarget;
import lambdamagus.ars_deco.dye.PlacedDyeColors;
import lambdamagus.ars_deco.network.SyncBlockDyeColorPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemPlacementMixin {
    @Unique
    private static final ThreadLocal<DyeColor> arsDeco$placingColor = new ThreadLocal<>();

    @Inject(method = "place", at = @At("HEAD"))
    private void arsDeco$captureColorBeforePlacement(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        arsDeco$placingColor.set(DyeableArsBlocks.color(context.getItemInHand()).orElse(null));
    }

    @Inject(method = "place", at = @At("RETURN"))
    private void arsDeco$copyColorToPlacedBlock(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        try {
            if (!cir.getReturnValue().consumesAction()) {
                return;
            }

            DyeColor color = arsDeco$placingColor.get();
            if (color == null) {
                return;
            }

            Level level = context.getLevel();
            BlockPos placedPos = context.getClickedPos();
            DyeTarget target = DyeableArsBlocks.target(level.getBlockState(placedPos).getBlock()).orElse(null);
            if (target == null) {
                return;
            }

            BlockEntity blockEntity = level.getBlockEntity(placedPos);
            if (blockEntity instanceof ArsDecoDyeableBlockEntity dyeable) {
                dyeable.arsDeco$setColor(color);
                blockEntity.setChanged();
                level.sendBlockUpdated(placedPos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
                if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                    PlacedDyeColors.PlacedColor placedColor = new PlacedDyeColors.PlacedColor(target.blockId(), color);
                    PlacedDyeColors.get(serverLevel).set(placedPos, target.blockId(), color);
                    PacketDistributor.sendToPlayersTrackingChunk(serverLevel, level.getChunk(placedPos).getPos(), new SyncBlockDyeColorPayload(placedPos, placedColor));
                }
            } else if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                PlacedDyeColors.PlacedColor placedColor = new PlacedDyeColors.PlacedColor(target.blockId(), color);
                PlacedDyeColors.get(serverLevel).set(placedPos, target.blockId(), color);
                level.sendBlockUpdated(placedPos, level.getBlockState(placedPos), level.getBlockState(placedPos), 3);
                PacketDistributor.sendToPlayersTrackingChunk(serverLevel, level.getChunk(placedPos).getPos(), new SyncBlockDyeColorPayload(placedPos, placedColor));
            }
        } finally {
            arsDeco$placingColor.remove();
        }
    }
}
