package lambdamagus.ars_deco.mixin;

import com.hollingsworth.arsnouveau.api.source.AbstractSourceMachine;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import lambdamagus.ars_deco.dye.ArsDecoDyeableBlockEntity;
import lambdamagus.ars_deco.dye.DyeParticleColors;
import lambdamagus.ars_deco.dye.DyeTarget;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractSourceMachine.class)
public abstract class AbstractSourceMachineDyeColorMixin {
    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true, remap = false)
    private void arsDeco$useDyedSourceTransferColor(CallbackInfoReturnable<ParticleColor> cir) {
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        DyeTarget target = DyeableArsBlocks.target(blockEntity.getBlockState().getBlock()).orElse(null);
        if (target == null || !DyeParticleColors.shouldDyeSourceTransfer(target)) {
            return;
        }
        if (!(this instanceof ArsDecoDyeableBlockEntity dyeable)) {
            return;
        }

        DyeColor color = dyeable.arsDeco$getColor().orElse(null);
        if (color != null) {
            cir.setReturnValue(DyeParticleColors.particleColor(color));
        }
    }
}
