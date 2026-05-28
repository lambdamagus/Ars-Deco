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
                "textures/block/" + target.textureKey() + "/" + color.getName() + ".png"
        );
    }

    public static ResourceLocation spriteLocation(DyeTarget target, DyeColor color) {
        return ResourceLocation.fromNamespaceAndPath(
                ASSET_NAMESPACE,
                "block/" + target.textureKey() + "/" + color.getName()
        );
    }

    public static ResourceLocation spriteLocation(DyeTarget target, DyeColor color, ResourceLocation originalSprite) {
        if ("source_jar".equals(target.textureKey()) && isSourceJarFill(originalSprite)) {
            return ResourceLocation.fromNamespaceAndPath(ASSET_NAMESPACE, "block/source_still/" + color.getName());
        }
        if ("archwood_door_bottom".equals(target.textureKey()) && isArchwoodDoorTop(originalSprite)) {
            return ResourceLocation.fromNamespaceAndPath(ASSET_NAMESPACE, "block/archwood_door_top/" + color.getName());
        }
        if ("sourcestone_door_bottom".equals(target.textureKey())) {
            if (isSourcestoneDoorTop(originalSprite)) {
                return ResourceLocation.fromNamespaceAndPath(ASSET_NAMESPACE, "block/sourcestone_door_top/" + color.getName());
            }
            if (isSourcestoneDoorItem(originalSprite)) {
                return ResourceLocation.fromNamespaceAndPath(ASSET_NAMESPACE, "item/sourcestone_door/" + color.getName());
            }
        }
        if ("polished_sourcestone_door_bottom".equals(target.textureKey())) {
            if (isPolishedSourcestoneDoorTop(originalSprite)) {
                return ResourceLocation.fromNamespaceAndPath(ASSET_NAMESPACE, "block/polished_sourcestone_door_top/" + color.getName());
            }
            if (isPolishedSourcestoneDoorItem(originalSprite)) {
                return ResourceLocation.fromNamespaceAndPath(ASSET_NAMESPACE, "item/polished_sourcestone_door/" + color.getName());
            }
        }
        return spriteLocation(target, color);
    }

    private static boolean isSourceJarFill(ResourceLocation originalSprite) {
        return "ars_nouveau".equals(originalSprite.getNamespace()) && "block/mana_still".equals(originalSprite.getPath());
    }

    private static boolean isArchwoodDoorTop(ResourceLocation originalSprite) {
        return "ars_nouveau".equals(originalSprite.getNamespace()) && "block/archwood_door_top".equals(originalSprite.getPath());
    }

    private static boolean isSourcestoneDoorTop(ResourceLocation originalSprite) {
        return "ars_additions".equals(originalSprite.getNamespace()) && "block/sourcestone_door_top".equals(originalSprite.getPath());
    }

    private static boolean isSourcestoneDoorItem(ResourceLocation originalSprite) {
        return "ars_additions".equals(originalSprite.getNamespace()) && "item/sourcestone_door".equals(originalSprite.getPath());
    }

    private static boolean isPolishedSourcestoneDoorTop(ResourceLocation originalSprite) {
        return "ars_additions".equals(originalSprite.getNamespace()) && "block/polished_sourcestone_door_top".equals(originalSprite.getPath());
    }

    private static boolean isPolishedSourcestoneDoorItem(ResourceLocation originalSprite) {
        return "ars_additions".equals(originalSprite.getNamespace()) && "item/polished_sourcestone_door".equals(originalSprite.getPath());
    }
}
