package lambdamagus.ars_deco.mixin.client;

import com.hollingsworth.arsnouveau.client.renderer.tile.RepositoryModel;
import com.hollingsworth.arsnouveau.common.block.tile.RepositoryTile;
import lambdamagus.ars_deco.dye.ArsDecoDyeableBlockEntity;
import lambdamagus.ars_deco.dye.ClientPlacedDyeColors;
import lambdamagus.ars_deco.dye.DyeTextures;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RepositoryModel.class)
public abstract class RepositoryModelDyeTextureMixin {
    @Inject(method = "getTextureResource(Lcom/hollingsworth/arsnouveau/common/block/tile/RepositoryTile;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true, remap = false)
    private void arsDeco$useDyedRepositoryTexture(RepositoryTile repository, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!(repository instanceof ArsDecoDyeableBlockEntity dyeable)) {
            return;
        }

        DyeColor color = dyeable.arsDeco$getColor().orElse(null);
        if (color == null) {
            color = ClientPlacedDyeColors.getColor(repository.getBlockPos(), repository.getBlockState()).orElse(null);
            if (color == null) {
                return;
            }
        }

        DyeColor resolvedColor = color;
        DyeableArsBlocks.target(repository.getBlockState().getBlock())
                .map(target -> DyeTextures.coloredTexture(target, resolvedColor))
                .ifPresent(cir::setReturnValue);
    }
}
