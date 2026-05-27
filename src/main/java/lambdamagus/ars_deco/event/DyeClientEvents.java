package lambdamagus.ars_deco.event;

import lambdamagus.ars_deco.ArsDeco;
import lambdamagus.ars_deco.dye.DyeableArsBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Locale;

@EventBusSubscriber(modid = ArsDeco.MODID, value = Dist.CLIENT)
public final class DyeClientEvents {
    private DyeClientEvents() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        DyeColor color = DyeableArsBlocks.color(event.getItemStack()).orElse(null);
        if (color == null || !DyeableArsBlocks.isDyeable(event.getItemStack())) {
            return;
        }

        event.getToolTip().add(Component.translatable("tooltip.ars_deco.dye_color", displayName(color))
                .withStyle(ChatFormatting.GRAY));
    }

    private static String displayName(DyeColor color) {
        String[] parts = color.getName().split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(part.substring(0, 1).toUpperCase(Locale.ROOT));
            builder.append(part.substring(1));
        }
        return builder.toString();
    }
}
