package lambdamagus.ars_deco.client.model;

import com.hollingsworth.arsnouveau.ArsNouveau;
import lambdamagus.ars_deco.ArsDeco;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;

@EventBusSubscriber(modid = ArsDeco.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class DyeModelEvents {
    private DyeModelEvents() {
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        for (Map.Entry<ModelResourceLocation, BakedModel> entry : event.getModels().entrySet()) {
            if (shouldWrap(entry.getKey())) {
                entry.setValue(new DyedBakedModel(entry.getValue(), event.getTextureGetter()));
            }
        }
    }

    private static boolean shouldWrap(ModelResourceLocation key) {
        String model = key.toString();
        if (!model.contains(ArsNouveau.MODID + ":")) {
            return false;
        }

        return DyeableArsBlocks.targets().stream()
                .anyMatch(target -> model.contains(ArsNouveau.MODID + ":" + target.blockId().getPath()));
    }
}
