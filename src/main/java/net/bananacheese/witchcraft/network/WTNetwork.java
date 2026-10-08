package net.bananacheese.witchcraft.network;

import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.bananacheese.witchcraft.block.entity.custom.DarkCauldronBlockEntity;
import net.bananacheese.witchcraft.block.entity.custom.PedestalBlockEntity;
import net.bananacheese.witchcraft.item.custom.PotionPouch;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
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
                AlterSyncPayload.TYPE,
                AlterSyncPayload.STREAM_CODEC,
                WTNetwork::handleAlterSync);

        registrar.playToClient(
                DarkCauldronFluidSyncPayload.TYPE,
                DarkCauldronFluidSyncPayload.STREAM_CODEC,
                WTNetwork::handleDarkCauldronFluidSync);

        registrar.playToServer(
                PouchSelectSlotPayload.TYPE,
                PouchSelectSlotPayload.STREAM_CODEC,
                WTNetwork::handlePouchSelectSlot);

        registrar.playToServer(
                OpenPouchPayload.TYPE,
                OpenPouchPayload.STREAM_CODEC,
                WTNetwork::handleOpenPouch);
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

    private static void handleAlterSync(
            AlterSyncPayload payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {

        context.enqueueWork(() -> {
            if (context.player().level().isClientSide
                    && context.player().level().getBlockEntity(payload.pos())
                    instanceof AlterBlockEntity alter) {
                alter.setClientData(payload.heldItem(), payload.fluid());
            }
        });
    }

    private static void handlePouchSelectSlot(PouchSelectSlotPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemStack pouch = PotionPouch.getHeldPouch(context.player());
            if (!pouch.isEmpty()) {
                PotionPouch.setActiveSlot(pouch, payload.slot()); // clamped to 0-4
            }
        });
    }

    private static void handleOpenPouch(OpenPouchPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Ignore if the player already has another container open.
            if (context.player() instanceof ServerPlayer player && player.containerMenu == player.inventoryMenu) {
                ItemStack pouch = PotionPouch.findPouch(player);
                if (!pouch.isEmpty()) {
                    PotionPouch.openMenu(player, pouch);
                }
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
