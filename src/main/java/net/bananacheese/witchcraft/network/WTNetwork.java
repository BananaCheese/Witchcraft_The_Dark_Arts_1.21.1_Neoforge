package net.bananacheese.witchcraft.network;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.block.entity.custom.PedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = WitchcraftTheDarkArts.MODID)
public final class WTNetwork {

    private WTNetwork() {
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                PedestalSyncPayload.TYPE,
                PedestalSyncPayload.STREAM_CODEC,
                WTNetwork::handlePedestalSync);
    }

    private static void handlePedestalSync(
            PedestalSyncPayload payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {

        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level == null) {
                return;
            }

            if (Minecraft.getInstance().level.getBlockEntity(payload.pos())
                    instanceof PedestalBlockEntity pedestal) {

                pedestal.setHeldItemClient(payload.stack());
            }
        });
    }
}
