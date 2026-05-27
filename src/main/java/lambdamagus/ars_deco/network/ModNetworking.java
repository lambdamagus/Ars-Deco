package lambdamagus.ars_deco.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private ModNetworking() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SyncBlockDyeColorPayload.TYPE, SyncBlockDyeColorPayload.STREAM_CODEC, SyncBlockDyeColorPayload::handle);
        registrar.playToClient(SyncChunkDyeColorsPayload.TYPE, SyncChunkDyeColorsPayload.STREAM_CODEC, SyncChunkDyeColorsPayload::handle);
    }
}
