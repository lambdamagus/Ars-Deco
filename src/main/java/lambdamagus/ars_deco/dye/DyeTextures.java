package lambdamagus.ars_deco.dye;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public final class DyeTextures {
    public static final String ASSET_NAMESPACE = "ars_deco";

    private DyeTextures() {
    }

    public static ResourceLocation coloredTexture(DyeTarget target, DyeColor color) {
        return ResourceLocation.fromNamespaceAndPath(
                ASSET_NAMESPACE,
                "textures/" + target.textureKey() + "/" + color.getName() + ".png"
        );
    }

    public static ResourceLocation spriteLocation(DyeTarget target, DyeColor color) {
        return ResourceLocation.fromNamespaceAndPath(
                ASSET_NAMESPACE,
                target.textureKey() + "/" + color.getName()
        );
    }
}
