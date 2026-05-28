package lambdamagus.ars_deco.client.model;

import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import lambdamagus.ars_deco.dye.DyeTarget;

public final class DyeModelProperties {
    public static final ModelProperty<DyeColor> DYE_COLOR = new ModelProperty<>();
    public static final ModelProperty<DyeTarget> DYE_TARGET = new ModelProperty<>();

    private DyeModelProperties() {
    }
}
