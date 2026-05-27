package lambdamagus.ars_deco.registry;

import lambdamagus.ars_deco.item.StarHatCosmetic;
import lambdamagus.ars_deco.crafting.DyeArsBlockRecipe;
import com.hollingsworth.arsnouveau.api.sound.SpellSound;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import static lambdamagus.ars_deco.ArsDeco.MODID;
import static lambdamagus.ars_deco.ArsDeco.prefix;
import static net.minecraft.core.registries.Registries.RECIPE_SERIALIZER;
import static net.minecraft.core.registries.Registries.SOUND_EVENT;

public class ModRegistry {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(SOUND_EVENT, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(RECIPE_SERIALIZER, MODID);


    public static void registerRegistries(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        SOUNDS.register(bus);
        RECIPE_SERIALIZERS.register(bus);
    }

    public static final DeferredHolder<Item, ? extends Item> STARBUNCLE_HAT;
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<DyeArsBlockRecipe>> DYE_ARS_BLOCK_RECIPE =
            RECIPE_SERIALIZERS.register("dye_ars_block", () -> new SimpleCraftingRecipeSerializer<>(DyeArsBlockRecipe::new));

    public static DeferredHolder<SoundEvent, SoundEvent> DECO_SOUND_FAMILY = SOUNDS.register("deco_sound", () -> makeSound("deco_sound"));
    public static SpellSound DECO_SPELL_SOUND = new SpellSound(ModRegistry.DECO_SOUND_FAMILY, Component.literal("Ars Deco"), prefix("deco_random_sound"));


    static {
        STARBUNCLE_HAT = ITEMS.register("star_hat", () -> new StarHatCosmetic(new Item.Properties()));
    }

    static SoundEvent makeSound(@NotNull String name) {
        return SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MODID, name));
    }
}
