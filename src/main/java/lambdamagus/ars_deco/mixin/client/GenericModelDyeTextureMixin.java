package lambdamagus.ars_deco.mixin.client;

import com.hollingsworth.arsnouveau.client.renderer.tile.GenericModel;
import lambdamagus.ars_deco.dye.ArsDecoDyeableBlockEntity;
import lambdamagus.ars_deco.dye.DyeTextures;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.animatable.GeoAnimatable;

@Mixin(GenericModel.class)
public abstract class GenericModelDyeTextureMixin<T extends GeoAnimatable> {
    @Inject(method = "getTextureResource", at = @At("HEAD"), cancellable = true)
    private void arsDeco$useDyedBlockEntityTexture(T animatable, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!(animatable instanceof BlockEntity blockEntity)) {
            return;
        }
        if (!(blockEntity instanceof ArsDecoDyeableBlockEntity dyeable)) {
            return;
        }

        DyeColor color = dyeable.arsDeco$getColor().orElse(null);
        if (color == null) {
            return;
        }

        DyeableArsBlocks.target(blockEntity.getBlockState().getBlock())
                .map(target -> DyeTextures.coloredTexture(target, color))
                .ifPresent(cir::setReturnValue);
    }
}
