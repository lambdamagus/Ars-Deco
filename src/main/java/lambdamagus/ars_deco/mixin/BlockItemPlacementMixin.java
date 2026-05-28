package lambdamagus.ars_deco.mixin;

import lambdamagus.ars_deco.dye.DyeBlockColorApplier;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import lambdamagus.ars_deco.dye.DyeTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
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

            DyeBlockColorApplier.apply(level, placedPos, target, color);
        } finally {
            arsDeco$placingColor.remove();
        }
    }
}
