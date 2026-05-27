package lambdamagus.ars_deco.mixin;

import lambdamagus.ars_deco.dye.ArsDecoDyeableBlockEntity;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import lambdamagus.ars_deco.dye.DyeTarget;
import lambdamagus.ars_deco.dye.PlacedDyeColors;
import lambdamagus.ars_deco.network.SyncBlockDyeColorPayload;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemPlacementMixin {
    @Inject(method = "place", at = @At("RETURN"))
    private void arsDeco$copyColorToPlacedBlock(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!cir.getReturnValue().consumesAction()) {
            return;
        }

        ItemStack stack = context.getItemInHand();
        DyeColor color = DyeableArsBlocks.color(stack).orElse(null);
        if (color == null) {
            return;
        }

        Level level = context.getLevel();
        DyeTarget target = DyeableArsBlocks.target(level.getBlockState(context.getClickedPos()).getBlock()).orElse(null);
        if (target == null) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (blockEntity instanceof ArsDecoDyeableBlockEntity dyeable) {
            dyeable.arsDeco$setColor(color);
            blockEntity.setChanged();
            level.sendBlockUpdated(context.getClickedPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        } else if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            PlacedDyeColors.PlacedColor placedColor = new PlacedDyeColors.PlacedColor(target.blockId(), color);
            PlacedDyeColors.get(serverLevel).set(context.getClickedPos(), target.blockId(), color);
            level.sendBlockUpdated(context.getClickedPos(), level.getBlockState(context.getClickedPos()), level.getBlockState(context.getClickedPos()), 3);
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, level.getChunk(context.getClickedPos()).getPos(), new SyncBlockDyeColorPayload(context.getClickedPos(), placedColor));
        }
    }
}
