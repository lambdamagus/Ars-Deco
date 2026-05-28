package lambdamagus.ars_deco.mixin;

import com.hollingsworth.arsnouveau.api.item.inv.InteractType;
import com.hollingsworth.arsnouveau.api.item.inv.InventoryManager;
import com.hollingsworth.arsnouveau.api.item.inv.SlotReference;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.api.spell.SpellStats;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.TileCaster;
import com.hollingsworth.arsnouveau.api.util.IWololoable;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectWololo;
import lambdamagus.ars_deco.dye.DyeBlockColorApplier;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import lambdamagus.ars_deco.dye.DyeTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.function.Supplier;

@Mixin(EffectWololo.class)
public abstract class EffectWololoMixin {
    @Inject(method = "onResolveBlock", at = @At("HEAD"), cancellable = true)
    private void arsDeco$dyePlacedBlock(BlockHitResult rayTraceResult, Level world, @NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext, SpellResolver resolver, CallbackInfo ci) {
        if (!(world instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos pos = rayTraceResult.getBlockPos();
        DyeTarget target = DyeableArsBlocks.target(world.getBlockState(pos).getBlock()).orElse(null);
        if (target == null) {
            return;
        }

        ItemStack dyeStack = arsDeco$resolveDye(shooter, spellStats, spellContext);
        DyeColor color = DyeColor.getColor(dyeStack);
        if (color == null) {
            return;
        }

        DyeBlockColorApplier.apply(world, pos, target, color);

        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof IWololoable) {
            return;
        }

        world.playSound(null, pos, SoundEvents.EVOKER_PREPARE_WOLOLO, SoundSource.PLAYERS, spellContext.getSpell().sound().getVolume(), spellContext.getSpell().sound().getPitch());
        ci.cancel();
    }

    private static ItemStack arsDeco$resolveDye(@NotNull LivingEntity shooter, SpellStats spellStats, SpellContext spellContext) {
        if (spellContext.getCaster() instanceof TileCaster) {
            InventoryManager manager = spellContext.getCaster().getInvManager();
            SlotReference reference = manager.findItem(stack -> stack.getItem() instanceof DyeItem, InteractType.EXTRACT);
            if (!reference.isEmpty()) {
                return reference.getHandler().getStackInSlot(reference.getSlot());
            }
        } else {
            ItemStack stack = shooter.getOffhandItem();
            if (stack.getItem() instanceof DyeItem) {
                return stack;
            }
        }

        DyeItem dye = spellStats.isRandomized() ? arsDeco$randomDye(shooter) : arsDeco$closestDye(spellContext);
        return dye == null ? ItemStack.EMPTY : dye.getDefaultInstance();
    }

    private static DyeItem arsDeco$randomDye(LivingEntity shooter) {
        Object[] dyes = EffectWololo.vanillaColors.values().toArray();
        if (dyes.length == 0) {
            return null;
        }
        return (DyeItem) dyes[shooter.getRandom().nextInt(dyes.length)];
    }

    private static DyeItem arsDeco$closestDye(SpellContext spellContext) {
        ParticleColor spellColor = arsDeco$wololoColor(spellContext);
        ParticleColor targetColor = EffectWololo.vanillaColors.keySet().stream()
                .min(Comparator.comparingDouble(color -> color.euclideanDistance(spellColor)))
                .orElse(ParticleColor.WHITE);
        Item item = EffectWololo.vanillaColors.get(targetColor);
        return item instanceof DyeItem dyeItem ? dyeItem : null;
    }

    private static ParticleColor arsDeco$wololoColor(SpellContext spellContext) {
        try {
            Class<?> registry = Class.forName("com.hollingsworth.arsnouveau.api.registry.ParticleTimelineRegistry");
            Field timelineField = registry.getField("WOLOLO_TIMELINE");
            Object timelineHolder = timelineField.get(null);
            if (!(timelineHolder instanceof Supplier<?> supplier)) {
                return spellContext.getSpell().color();
            }

            Method particleTimeline = spellContext.getSpell().getClass().getMethod("particleTimeline");
            Object timelineMap = particleTimeline.invoke(spellContext.getSpell());
            Method get = timelineMap.getClass().getMethod("get", Supplier.class);
            Object wololoTimeline = get.invoke(timelineMap, supplier);
            Method getColor = wololoTimeline.getClass().getMethod("getColor");
            Object color = getColor.invoke(wololoTimeline);
            if (color instanceof ParticleColor particleColor) {
                return particleColor;
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Older Ars Nouveau builds did not expose per-effect particle timelines.
        }
        return spellContext.getSpell().color();
    }
}
