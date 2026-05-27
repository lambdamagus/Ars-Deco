package lambdamagus.ars_deco.dye;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClientPlacedDyeColors {
    private static final Map<Long, Map<Long, PlacedDyeColors.PlacedColor>> COLORS_BY_CHUNK = new HashMap<>();

    private ClientPlacedDyeColors() {
    }

    public static Optional<DyeColor> getColor(BlockPos pos, BlockState state) {
        Map<Long, PlacedDyeColors.PlacedColor> chunkColors = COLORS_BY_CHUNK.get(chunkKey(pos));
        if (chunkColors == null) {
            return Optional.empty();
        }

        PlacedDyeColors.PlacedColor color = chunkColors.get(pos.asLong());
        if (color == null) {
            return Optional.empty();
        }

        ResourceLocation blockId = DyeableArsBlocks.target(state.getBlock())
                .map(DyeTarget::blockId)
                .orElse(null);
        if (!color.blockId().equals(blockId)) {
            return Optional.empty();
        }
        return Optional.of(color.color());
    }

    public static void applyChunk(ChunkPos chunkPos, Map<BlockPos, PlacedDyeColors.PlacedColor> colors) {
        Map<Long, PlacedDyeColors.PlacedColor> packed = new HashMap<>();
        for (Map.Entry<BlockPos, PlacedDyeColors.PlacedColor> entry : colors.entrySet()) {
            packed.put(entry.getKey().asLong(), entry.getValue());
        }
        if (packed.isEmpty()) {
            COLORS_BY_CHUNK.remove(chunkPos.toLong());
        } else {
            COLORS_BY_CHUNK.put(chunkPos.toLong(), packed);
        }
    }

    public static void applySingle(BlockPos pos, @Nullable PlacedDyeColors.PlacedColor color) {
        long chunkKey = chunkKey(pos);
        if (color == null) {
            Map<Long, PlacedDyeColors.PlacedColor> chunkColors = COLORS_BY_CHUNK.get(chunkKey);
            if (chunkColors != null) {
                chunkColors.remove(pos.asLong());
                if (chunkColors.isEmpty()) {
                    COLORS_BY_CHUNK.remove(chunkKey);
                }
            }
        } else {
            COLORS_BY_CHUNK.computeIfAbsent(chunkKey, unused -> new HashMap<>()).put(pos.asLong(), color);
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            BlockState state = minecraft.level.getBlockState(pos);
            minecraft.level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public static void clearChunk(ChunkPos chunkPos) {
        COLORS_BY_CHUNK.remove(chunkPos.toLong());
    }

    private static long chunkKey(BlockPos pos) {
        return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
    }
}
