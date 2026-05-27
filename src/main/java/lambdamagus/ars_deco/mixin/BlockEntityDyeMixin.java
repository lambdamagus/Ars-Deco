package lambdamagus.ars_deco.mixin;

import lambdamagus.ars_deco.dye.ArsDecoDyeableBlockEntity;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(BlockEntity.class)
public abstract class BlockEntityDyeMixin implements ArsDecoDyeableBlockEntity {
    @Unique
    @Nullable
    private DyeColor arsDeco$color;

    @Override
    public Optional<DyeColor> arsDeco$getColor() {
        return Optional.ofNullable(arsDeco$color);
    }

    @Override
    public void arsDeco$setColor(@Nullable DyeColor color) {
        arsDeco$color = color;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void arsDeco$saveColor(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (arsDeco$color != null) {
            tag.putString(DyeableArsBlocks.COLOR_TAG, arsDeco$color.getName());
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void arsDeco$loadColor(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (tag.contains(DyeableArsBlocks.COLOR_TAG)) {
            arsDeco$color = DyeColor.byName(tag.getString(DyeableArsBlocks.COLOR_TAG), null);
        } else {
            arsDeco$color = null;
        }
    }
}
