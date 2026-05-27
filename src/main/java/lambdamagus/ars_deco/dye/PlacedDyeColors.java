package lambdamagus.ars_deco.dye;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlacedDyeColors extends SavedData {
    private static final String DATA_NAME = "ars_deco_placed_dye_colors";

    private final Map<Long, Map<Long, PlacedColor>> colorsByChunk = new HashMap<>();

    public static PlacedDyeColors get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PlacedDyeColors::new, PlacedDyeColors::load, null),
                DATA_NAME
        );
    }

    public Optional<PlacedColor> get(BlockPos pos) {
        Map<Long, PlacedColor> chunkColors = colorsByChunk.get(chunkKey(pos));
        if (chunkColors == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(chunkColors.get(pos.asLong()));
    }

    public Optional<DyeColor> getColor(BlockPos pos, ResourceLocation expectedBlockId) {
        return get(pos)
                .filter(color -> expectedBlockId.equals(color.blockId()))
                .map(PlacedColor::color);
    }

    public void set(BlockPos pos, ResourceLocation blockId, DyeColor color) {
        colorsByChunk.computeIfAbsent(chunkKey(pos), unused -> new HashMap<>())
                .put(pos.asLong(), new PlacedColor(blockId, color));
        setDirty();
    }

    public Optional<PlacedColor> clear(BlockPos pos) {
        long chunkKey = chunkKey(pos);
        Map<Long, PlacedColor> chunkColors = colorsByChunk.get(chunkKey);
        if (chunkColors == null) {
            return Optional.empty();
        }

        PlacedColor removed = chunkColors.remove(pos.asLong());
        if (chunkColors.isEmpty()) {
            colorsByChunk.remove(chunkKey);
        }
        if (removed != null) {
            setDirty();
        }
        return Optional.ofNullable(removed);
    }

    public Map<BlockPos, PlacedColor> getChunkColors(ChunkPos chunkPos) {
        Map<Long, PlacedColor> chunkColors = colorsByChunk.get(chunkPos.toLong());
        if (chunkColors == null || chunkColors.isEmpty()) {
            return Map.of();
        }

        Map<BlockPos, PlacedColor> result = new HashMap<>();
        for (Map.Entry<Long, PlacedColor> entry : chunkColors.entrySet()) {
            result.put(BlockPos.of(entry.getKey()), entry.getValue());
        }
        return result;
    }

    public static PlacedDyeColors load(CompoundTag tag, HolderLookup.Provider registries) {
        PlacedDyeColors colors = new PlacedDyeColors();
        ListTag entries = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            BlockPos pos = BlockPos.of(entry.getLong("pos"));
            ResourceLocation blockId = ResourceLocation.tryParse(entry.getString("block"));
            DyeColor color = DyeColor.byName(entry.getString("color"), null);
            if (blockId != null && color != null) {
                colors.colorsByChunk.computeIfAbsent(chunkKey(pos), unused -> new HashMap<>())
                        .put(pos.asLong(), new PlacedColor(blockId, color));
            }
        }
        return colors;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag entries = new ListTag();
        for (Map<Long, PlacedColor> chunkColors : colorsByChunk.values()) {
            for (Map.Entry<Long, PlacedColor> entry : chunkColors.entrySet()) {
                CompoundTag colorTag = new CompoundTag();
                colorTag.putLong("pos", entry.getKey());
                colorTag.putString("block", entry.getValue().blockId().toString());
                colorTag.putString("color", entry.getValue().color().getName());
                entries.add(colorTag);
            }
        }
        tag.put("entries", entries);
        return tag;
    }

    public record PlacedColor(ResourceLocation blockId, DyeColor color) {
    }

    private static long chunkKey(BlockPos pos) {
        return ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
    }
}
