package lambdamagus.ars_deco.dye;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public record DyeTarget(Block block, Item item, String textureKey, ResourceLocation blockId) {
}
