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

    public static ResourceLocation spriteLocation(DyeTarget target, DyeColor color, ResourceLocation originalSprite) {
        if ("source_jar".equals(target.textureKey()) && isSourceJarFill(originalSprite)) {
            return ResourceLocation.fromNamespaceAndPath(ASSET_NAMESPACE, "source_still/" + color.getName());
        }
        if ("archwood_door_bottom".equals(target.textureKey()) && isArchwoodDoorTop(originalSprite)) {
            return ResourceLocation.fromNamespaceAndPath(ASSET_NAMESPACE, "archwood_door_top/" + color.getName());
        }
        return spriteLocation(target, color);
    }

    private static boolean isSourceJarFill(ResourceLocation originalSprite) {
        return "ars_nouveau".equals(originalSprite.getNamespace()) && "block/mana_still".equals(originalSprite.getPath());
    }

    private static boolean isArchwoodDoorTop(ResourceLocation originalSprite) {
        return "ars_nouveau".equals(originalSprite.getNamespace()) && "block/archwood_door_top".equals(originalSprite.getPath());
    }
}
