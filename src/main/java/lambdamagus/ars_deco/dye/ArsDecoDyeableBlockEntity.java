package lambdamagus.ars_deco.dye;

import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface ArsDecoDyeableBlockEntity {
    Optional<DyeColor> arsDeco$getColor();

    void arsDeco$setColor(@Nullable DyeColor color);
}
