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
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

public record SyncBlockDyeColorPayload(BlockPos pos, @Nullable PlacedDyeColors.PlacedColor color) implements CustomPacketPayload {
    public static final Type<SyncBlockDyeColorPayload> TYPE = new Type<>(ArsDeco.prefix("sync_block_dye_color"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBlockDyeColorPayload> STREAM_CODEC = StreamCodec.of(
            SyncBlockDyeColorPayload::encode,
            SyncBlockDyeColorPayload::decode
    );

    private static void encode(RegistryFriendlyByteBuf buffer, SyncBlockDyeColorPayload payload) {
        buffer.writeBlockPos(payload.pos);
        buffer.writeBoolean(payload.color != null);
        if (payload.color != null) {
            buffer.writeResourceLocation(payload.color.blockId());
            buffer.writeEnum(payload.color.color());
        }
    }

    private static SyncBlockDyeColorPayload decode(RegistryFriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        if (!buffer.readBoolean()) {
            return new SyncBlockDyeColorPayload(pos, null);
        }
        ResourceLocation blockId = buffer.readResourceLocation();
        DyeColor color = buffer.readEnum(DyeColor.class);
        return new SyncBlockDyeColorPayload(pos, new PlacedDyeColors.PlacedColor(blockId, color));
    }

    public static void handle(SyncBlockDyeColorPayload payload, IPayloadContext context) {
        ClientPlacedDyeColors.applySingle(payload.pos, payload.color);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
