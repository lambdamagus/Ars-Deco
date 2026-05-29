package lambdamagus.ars_deco.dye;

import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import net.minecraft.world.item.DyeColor;

public final class DyeParticleColors {
    private DyeParticleColors() {
    }

    public static ParticleColor particleColor(DyeColor color) {
        return switch (color) {
            case WHITE -> ParticleColor.WHITE;
            case ORANGE -> ParticleColor.ORANGE;
            case MAGENTA -> ParticleColor.MAGENTA;
            case LIGHT_BLUE -> ParticleColor.LIGHT_BLUE;
            case YELLOW -> ParticleColor.YELLOW;
            case LIME -> ParticleColor.LIME;
            case PINK -> ParticleColor.PINK;
            case GRAY -> ParticleColor.GRAY;
            case LIGHT_GRAY -> ParticleColor.LIGHT_GRAY;
            case CYAN -> ParticleColor.CYAN;
            case PURPLE -> ParticleColor.PURPLE;
            case BLUE -> ParticleColor.BLUE;
            case BROWN -> ParticleColor.BROWN;
            case GREEN -> ParticleColor.GREEN;
            case RED -> ParticleColor.RED;
            case BLACK -> ParticleColor.BLACK;
        };
    }

    public static boolean shouldDyeSourceTransfer(DyeTarget target) {
        return switch (target.textureKey()) {
            case "source_relay", "source_splitter", "source_warp", "source_deposit", "source_collector" -> true;
            default -> false;
        };
    }
}
