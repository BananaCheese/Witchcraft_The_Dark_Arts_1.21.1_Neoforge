package net.bananacheese.witchcraft.network;

import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.bananacheese.witchcraft.block.entity.custom.DarkCauldronBlockEntity;
import net.bananacheese.witchcraft.block.entity.custom.PedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = "wctda")
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

        registrar.playToClient(
                AlterFluidSyncPayload.TYPE,
                AlterFluidSyncPayload.STREAM_CODEC,
                WTNetwork::handleAlterFluidSync);

        registrar.playToClient(
                DarkCauldronFluidSyncPayload.TYPE,
                DarkCauldronFluidSyncPayload.STREAM_CODEC,
                WTNetwork::handleDarkCauldronFluidSync);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                WTBlockEntities.ALTER_BE.get(),
                (alter, side) -> alter.getFluidHandler());
    }

    private static void handlePedestalSync(
            PedestalSyncPayload payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {

        context.enqueueWork(() -> {
            if (context.player().level().isClientSide
                    && context.player().level().getBlockEntity(payload.pos())
                    instanceof PedestalBlockEntity pedestal) {
                pedestal.setHeldItemClient(payload.stack());
            }
        });
    }

    private static void handleAlterFluidSync(
            AlterFluidSyncPayload payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {

        context.enqueueWork(() -> {
            if (context.player().level().isClientSide
                    && context.player().level().getBlockEntity(payload.pos())
                    instanceof AlterBlockEntity alter) {
                alter.setFluidClient(payload.fluid());
            }
        });
    }

    private static void handleDarkCauldronFluidSync(
            DarkCauldronFluidSyncPayload payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {

        context.enqueueWork(() -> {
            if (context.player().level().isClientSide
                    && context.player().level().getBlockEntity(payload.pos())
                    instanceof DarkCauldronBlockEntity cauldron) {
                cauldron.setClientData(payload.isFilled(), payload.isBoiling(), payload.ingredients(), payload.color());
            }
        });
    }
}
