package lambdamagus.ars_deco.network;

import lambdamagus.ars_deco.ArsDeco;
import lambdamagus.ars_deco.dye.ClientPlacedDyeColors;
import lambdamagus.ars_deco.dye.PlacedDyeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SyncChunkDyeColorsPayload(ChunkPos chunkPos, Map<BlockPos, PlacedDyeColors.PlacedColor> colors) implements CustomPacketPayload {
    public static final Type<SyncChunkDyeColorsPayload> TYPE = new Type<>(ArsDeco.prefix("sync_chunk_dye_colors"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncChunkDyeColorsPayload> STREAM_CODEC = StreamCodec.of(
            SyncChunkDyeColorsPayload::encode,
            SyncChunkDyeColorsPayload::decode
    );

    private static void encode(RegistryFriendlyByteBuf buffer, SyncChunkDyeColorsPayload payload) {
        buffer.writeChunkPos(payload.chunkPos);
        buffer.writeVarInt(payload.colors.size());
        for (Map.Entry<BlockPos, PlacedDyeColors.PlacedColor> entry : payload.colors.entrySet()) {
            buffer.writeBlockPos(entry.getKey());
            buffer.writeResourceLocation(entry.getValue().blockId());
            buffer.writeEnum(entry.getValue().color());
        }
    }

    private static SyncChunkDyeColorsPayload decode(RegistryFriendlyByteBuf buffer) {
        ChunkPos chunkPos = buffer.readChunkPos();
        int size = buffer.readVarInt();
        Map<BlockPos, PlacedDyeColors.PlacedColor> colors = new HashMap<>();
        for (int i = 0; i < size; i++) {
            BlockPos pos = buffer.readBlockPos();
            ResourceLocation blockId = buffer.readResourceLocation();
            DyeColor color = buffer.readEnum(DyeColor.class);
            colors.put(pos, new PlacedDyeColors.PlacedColor(blockId, color));
        }
        return new SyncChunkDyeColorsPayload(chunkPos, colors);
    }

    public static void handle(SyncChunkDyeColorsPayload payload, IPayloadContext context) {
        ClientPlacedDyeColors.applyChunk(payload.chunkPos, payload.colors);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
