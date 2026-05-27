package lambdamagus.ars_deco.crafting;

import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import lambdamagus.ars_deco.registry.ModRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DyeArsBlockRecipe extends CustomRecipe {
    public DyeArsBlockRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return findMatch(input).matches();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries) {
        Match match = findMatch(input);
        if (!match.matches()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = match.target().copy();
        result.setCount(1);
        DyeableArsBlocks.setColor(result, match.color());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRegistry.DYE_ARS_BLOCK_RECIPE.get();
    }

    private static Match findMatch(CraftingInput input) {
        ItemStack target = ItemStack.EMPTY;
        DyeColor color = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            DyeColor stackColor = DyeColor.getColor(stack);
            if (stackColor != null) {
                if (color != null) {
                    return Match.invalid();
                }
                color = stackColor;
                continue;
            }

            if (DyeableArsBlocks.isDyeable(stack)) {
                if (!target.isEmpty()) {
                    return Match.invalid();
                }
                target = stack;
                continue;
            }

            return Match.invalid();
        }

        if (target.isEmpty() || color == null) {
            return Match.invalid();
        }

        return new Match(target, color);
    }

    private record Match(ItemStack target, DyeColor color) {
        static Match invalid() {
            return new Match(ItemStack.EMPTY, null);
        }

        boolean matches() {
            return !target.isEmpty() && color != null;
        }
    }
}
