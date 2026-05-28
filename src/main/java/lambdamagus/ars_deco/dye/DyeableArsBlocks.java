package lambdamagus.ars_deco.dye;

import com.hollingsworth.arsnouveau.ArsNouveau;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class DyeableArsBlocks {
    public static final String COLOR_TAG = "ars_deco_color";

    private static final String[][] TARGETS = {
            {"agronomic_sourcelink"},
            {"alchemical_sourcelink"},
            {"alteration_table"},
            {"arcane_core"},
            {"arcane_pedestal"},
            {"arcane_platform"},
            {"archwood_button", "archwood_planks"},
            {"archwood_door", "archwood_door_bottom"},
            {"archwood_fence", "archwood_planks"},
            {"archwood_fence_gate", "archwood_planks"},
            {"archwood_hanging_sign", "archwood_planks"},
            {"archwood_planks"},
            {"archwood_pressure_plate", "archwood_planks"},
            {"archwood_sconce"},
            {"archwood_sign", "archwood_planks"},
            {"archwood_slab", "archwood_planks"},
            {"archwood_stairs", "archwood_planks"},
            {"archwood_trapdoor"},
            {"basic_spell_turret"},
            {"brazier_relay"},
            {"enchanting_apparatus"},
            {"gilded_sourcestone_alternating"},
            {"gilded_sourcestone_alternating_slab", "gilded_sourcestone_alternating"},
            {"gilded_sourcestone_alternating_stairs", "gilded_sourcestone_alternating"},
            {"gilded_sourcestone_alternating_stone_cutterslab", "gilded_sourcestone_alternating"},
            {"gilded_sourcestone_alternating_stonecutter_stair", "gilded_sourcestone_alternating"},
            {"gilded_sourcestone_basketweave"},
            {"gilded_sourcestone_basketweave_slab", "gilded_sourcestone_basketweave"},
            {"gilded_sourcestone_basketweave_stairs", "gilded_sourcestone_basketweave"},
            {"gilded_sourcestone_basketweave_stone_cutterslab", "gilded_sourcestone_basketweave"},
            {"gilded_sourcestone_basketweave_stonecutter_stair", "gilded_sourcestone_basketweave"},
            {"gilded_sourcestone_large_bricks"},
            {"gilded_sourcestone_large_bricks_slab", "gilded_sourcestone_large_bricks"},
            {"gilded_sourcestone_large_bricks_stairs", "gilded_sourcestone_large_bricks"},
            {"gilded_sourcestone_large_bricks_stone_cutterslab", "gilded_sourcestone_large_bricks"},
            {"gilded_sourcestone_large_bricks_stonecutter_stair", "gilded_sourcestone_large_bricks"},
            {"gilded_sourcestone_mosaic"},
            {"gilded_sourcestone_mosaic_slab", "gilded_sourcestone_mosaic"},
            {"gilded_sourcestone_mosaic_stairs", "gilded_sourcestone_mosaic"},
            {"gilded_sourcestone_mosaic_stone_cutterslab", "gilded_sourcestone_mosaic"},
            {"gilded_sourcestone_mosaic_stonecutter_stair", "gilded_sourcestone_mosaic"},
            {"gilded_sourcestone_small_bricks"},
            {"gilded_sourcestone_small_bricks_slab", "gilded_sourcestone_small_bricks"},
            {"gilded_sourcestone_small_bricks_stairs", "gilded_sourcestone_small_bricks"},
            {"gilded_sourcestone_small_bricks_stone_cutterslab", "gilded_sourcestone_small_bricks"},
            {"gilded_sourcestone_small_bricks_stonecutter_stair", "gilded_sourcestone_small_bricks"},
            {"archwood_grate", "grate_archwood"},
            {"gold_grate", "grate_gold"},
            {"smooth_sourcestone_grate", "grate_smooth_sourcestone"},
            {"sourcestone_grate", "grate_sourcestone"},
            {"imbuement_chamber"},
            {"mycelial_sourcelink"},
            {"polished_sconce", "polished_sourcestone_sconce"},
            {"potion_melder"},
            {"repository"},
            {"ritual_brazier"},
            {"sconce"},
            {"scribes_table"},
            {"smooth_gilded_sourcestone_alternating"},
            {"smooth_gilded_sourcestone_alternating_slab", "smooth_gilded_sourcestone_alternating"},
            {"smooth_gilded_sourcestone_alternating_stairs", "smooth_gilded_sourcestone_alternating"},
            {"smooth_gilded_sourcestone_alternating_stone_cutterslab", "smooth_gilded_sourcestone_alternating"},
            {"smooth_gilded_sourcestone_alternating_stonecutter_stair", "smooth_gilded_sourcestone_alternating"},
            {"smooth_gilded_sourcestone_basketweave"},
            {"smooth_gilded_sourcestone_basketweave_slab", "smooth_gilded_sourcestone_basketweave"},
            {"smooth_gilded_sourcestone_basketweave_stairs", "smooth_gilded_sourcestone_basketweave"},
            {"smooth_gilded_sourcestone_basketweave_stone_cutterslab", "smooth_gilded_sourcestone_basketweave"},
            {"smooth_gilded_sourcestone_basketweave_stonecutter_stair", "smooth_gilded_sourcestone_basketweave"},
            {"smooth_gilded_sourcestone_large_bricks"},
            {"smooth_gilded_sourcestone_large_bricks_slab", "smooth_gilded_sourcestone_large_bricks"},
            {"smooth_gilded_sourcestone_large_bricks_stairs", "smooth_gilded_sourcestone_large_bricks"},
            {"smooth_gilded_sourcestone_large_bricks_stone_cutterslab", "smooth_gilded_sourcestone_large_bricks"},
            {"smooth_gilded_sourcestone_large_bricks_stonecutter_stair", "smooth_gilded_sourcestone_large_bricks"},
            {"smooth_gilded_sourcestone_mosaic"},
            {"smooth_gilded_sourcestone_mosaic_slab", "smooth_gilded_sourcestone_mosaic"},
            {"smooth_gilded_sourcestone_mosaic_stairs", "smooth_gilded_sourcestone_mosaic"},
            {"smooth_gilded_sourcestone_mosaic_stone_cutterslab", "smooth_gilded_sourcestone_mosaic"},
            {"smooth_gilded_sourcestone_mosaic_stonecutter_stair", "smooth_gilded_sourcestone_mosaic"},
            {"smooth_gilded_sourcestone_small_bricks"},
            {"smooth_gilded_sourcestone_small_bricks_slab", "smooth_gilded_sourcestone_small_bricks"},
            {"smooth_gilded_sourcestone_small_bricks_stairs", "smooth_gilded_sourcestone_small_bricks"},
            {"smooth_gilded_sourcestone_small_bricks_stone_cutterslab", "smooth_gilded_sourcestone_small_bricks"},
            {"smooth_gilded_sourcestone_small_bricks_stonecutter_stair", "smooth_gilded_sourcestone_small_bricks"},
            {"smooth_sourcestone"},
            {"smooth_sourcestone_slab", "smooth_sourcestone"},
            {"smooth_sourcestone_stairs", "smooth_sourcestone"},
            {"smooth_sourcestone_alternating"},
            {"smooth_sourcestone_alternating_slab", "smooth_sourcestone_alternating"},
            {"smooth_sourcestone_alternating_stairs", "smooth_sourcestone_alternating"},
            {"smooth_sourcestone_alternating_stone_cutterslab", "smooth_sourcestone_alternating"},
            {"smooth_sourcestone_alternating_stonecutter_stair", "smooth_sourcestone_alternating"},
            {"smooth_sourcestone_basketweave"},
            {"smooth_sourcestone_basketweave_slab", "smooth_sourcestone_basketweave"},
            {"smooth_sourcestone_basketweave_stairs", "smooth_sourcestone_basketweave"},
            {"smooth_sourcestone_basketweave_stone_cutterslab", "smooth_sourcestone_basketweave"},
            {"smooth_sourcestone_basketweave_stonecutter_stair", "smooth_sourcestone_basketweave"},
            {"smooth_sourcestone_large_bricks"},
            {"smooth_sourcestone_large_bricks_slab", "smooth_sourcestone_large_bricks"},
            {"smooth_sourcestone_large_bricks_stairs", "smooth_sourcestone_large_bricks"},
            {"smooth_sourcestone_large_bricks_stone_cutterslab", "smooth_sourcestone_large_bricks"},
            {"smooth_sourcestone_large_bricks_stonecutter_stair", "smooth_sourcestone_large_bricks"},
            {"smooth_sourcestone_mosaic"},
            {"smooth_sourcestone_mosaic_slab", "smooth_sourcestone_mosaic"},
            {"smooth_sourcestone_mosaic_stairs", "smooth_sourcestone_mosaic"},
            {"smooth_sourcestone_mosaic_stone_cutterslab", "smooth_sourcestone_mosaic"},
            {"smooth_sourcestone_mosaic_stonecutter_stair", "smooth_sourcestone_mosaic"},
            {"smooth_sourcestone_small_bricks"},
            {"smooth_sourcestone_small_bricks_slab", "smooth_sourcestone_small_bricks"},
            {"smooth_sourcestone_small_bricks_stairs", "smooth_sourcestone_small_bricks"},
            {"smooth_sourcestone_small_bricks_stone_cutterslab", "smooth_sourcestone_small_bricks"},
            {"smooth_sourcestone_small_bricks_stonecutter_stair", "smooth_sourcestone_small_bricks"},
            {"smooth_sourcestone_stone_cutterslab", "smooth_sourcestone"},
            {"smooth_sourcestone_stonecutter_stair", "smooth_sourcestone"},
            {"relay_collector", "source_collector"},
            {"relay_deposit", "source_deposit"},
            {"source_jar"},
            {"relay", "source_relay"},
            {"relay_splitter", "source_splitter"},
            {"relay_warp", "source_warp"},
            {"sourcestone"},
            {"sourcestone_slab", "sourcestone"},
            {"sourcestone_stairs", "sourcestone"},
            {"sourcestone_stone_cutterslab", "sourcestone"},
            {"sourcestone_stonecutter_stair", "sourcestone"},
            {"sourcestone_alternating"},
            {"sourcestone_alternating_slab", "sourcestone_alternating"},
            {"sourcestone_alternating_stairs", "sourcestone_alternating"},
            {"sourcestone_alternating_stone_cutterslab", "sourcestone_alternating"},
            {"sourcestone_alternating_stonecutter_stair", "sourcestone_alternating"},
            {"sourcestone_basketweave"},
            {"sourcestone_basketweave_slab", "sourcestone_basketweave"},
            {"sourcestone_basketweave_stairs", "sourcestone_basketweave"},
            {"sourcestone_basketweave_stone_cutterslab", "sourcestone_basketweave"},
            {"sourcestone_basketweave_stonecutter_stair", "sourcestone_basketweave"},
            {"sourcestone_large_bricks"},
            {"sourcestone_large_bricks_slab", "sourcestone_large_bricks"},
            {"sourcestone_large_bricks_stairs", "sourcestone_large_bricks"},
            {"sourcestone_large_bricks_stone_cutterslab", "sourcestone_large_bricks"},
            {"sourcestone_large_bricks_stonecutter_stair", "sourcestone_large_bricks"},
            {"sourcestone_mosaic"},
            {"sourcestone_mosaic_slab", "sourcestone_mosaic"},
            {"sourcestone_mosaic_stairs", "sourcestone_mosaic"},
            {"sourcestone_mosaic_stone_cutterslab", "sourcestone_mosaic"},
            {"sourcestone_mosaic_stonecutter_stair", "sourcestone_mosaic"},
            {"sourcestone_sconce"},
            {"sourcestone_small_bricks"},
            {"sourcestone_small_bricks_slab", "sourcestone_small_bricks"},
            {"sourcestone_small_bricks_stairs", "sourcestone_small_bricks"},
            {"sourcestone_small_bricks_stone_cutterslab", "sourcestone_small_bricks"},
            {"sourcestone_small_bricks_stonecutter_stair", "sourcestone_small_bricks"},
            {"spell_turret"},
            {"timer_spell_turret", "spell_turret_timer"},
            {"vitalic_sourcelink"},
            {"volcanic_sourcelink"},
            {"portal", "warp_portal"},
            {"wixie_cauldron"}
    };

    private static final Map<Item, DyeTarget> BY_ITEM = new HashMap<>();
    private static final Map<Block, DyeTarget> BY_BLOCK = new HashMap<>();
    private static boolean initialized;

    private DyeableArsBlocks() {
    }

    public static boolean isDyeable(ItemStack stack) {
        return !stack.isEmpty() && target(stack.getItem()).isPresent();
    }

    public static Optional<DyeTarget> target(Item item) {
        initialize();
        return Optional.ofNullable(BY_ITEM.get(item));
    }

    public static Optional<DyeTarget> target(Block block) {
        initialize();
        return Optional.ofNullable(BY_BLOCK.get(block));
    }

    public static Collection<DyeTarget> targets() {
        initialize();
        return BY_BLOCK.values();
    }

    public static Optional<DyeColor> color(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.BASE_COLOR));
    }

    public static void setColor(ItemStack stack, DyeColor color) {
        stack.set(DataComponents.BASE_COLOR, color);
    }

    public static Optional<DyeColor> placedColor(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ArsDecoDyeableBlockEntity dyeable) {
            return dyeable.arsDeco$getColor();
        }
        return Optional.empty();
    }

    private static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        for (String[] target : TARGETS) {
            String blockPath = target[0];
            String textureKey = target.length > 1 ? target[1] : blockPath;
            ResourceLocation blockId = ArsNouveau.prefix(blockPath);
            Block block = BuiltInRegistries.BLOCK.get(blockId);
            Item item = BuiltInRegistries.ITEM.get(blockId);

            if (block == null || item == null) {
                continue;
            }

            ResourceLocation resolvedBlockId = BuiltInRegistries.BLOCK.getKey(block);
            ResourceLocation resolvedItemId = BuiltInRegistries.ITEM.getKey(item);
            if (!blockId.equals(resolvedBlockId) || !blockId.equals(resolvedItemId)) {
                continue;
            }

            DyeTarget dyeTarget = new DyeTarget(block, item, textureKey, blockId);
            BY_ITEM.put(item, dyeTarget);
            BY_BLOCK.put(block, dyeTarget);
        }
    }
}
