package lambdamagus.ars_deco.mixin.client;

import lambdamagus.ars_deco.dye.DyeTextures;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import software.bernie.geckolib.renderer.GeoItemRenderer;

@Mixin(GeoItemRenderer.class)
public abstract class GeoItemRendererDyeTextureMixin {
    @Shadow
    public abstract ItemStack getCurrentItemStack();

    @Inject(method = "getTextureLocation", at = @At("HEAD"), cancellable = true)
    private void arsDeco$useDyedItemTexture(Item animatable, CallbackInfoReturnable<ResourceLocation> cir) {
        ItemStack stack = getCurrentItemStack();
        if (stack == null || stack.isEmpty()) {
            return;
        }

        DyeableArsBlocks.color(stack).flatMap(color ->
                DyeableArsBlocks.target(stack.getItem()).map(target -> DyeTextures.coloredTexture(target, color))
        ).ifPresent(cir::setReturnValue);
    }
}
