package net.bananacheese.witchcraft;

import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.bananacheese.witchcraft.block.entity.renderer.AlterBlockEntityRenderer;
import net.bananacheese.witchcraft.block.entity.renderer.PedestalBlockEntityRenderer;
import net.bananacheese.witchcraft.client.BarrierParticleRenderer;
import net.bananacheese.witchcraft.init.WTMenuTypes;
import net.bananacheese.witchcraft.item.WTItems;
import net.bananacheese.witchcraft.item.custom.SoulSyringe;
import net.bananacheese.witchcraft.screen.PotionPouchScreen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = WitchcraftTheDarkArts.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = WitchcraftTheDarkArts.MODID, value = Dist.CLIENT)
public class WitchcraftTheDarkArtsClient {

    public static void init(IEventBus modBus) {
        modBus.addListener(WitchcraftTheDarkArtsClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                WTItems.SOUL_SYRINGE.get(),
                ResourceLocation.fromNamespaceAndPath(
                        WitchcraftTheDarkArts.MODID,
                        "syringe_fill_level"),
                (stack, level, entity, seed) -> SoulSyringe.getFillLevel(stack)));
    }

    public WitchcraftTheDarkArtsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        NeoForge.EVENT_BUS.addListener(BarrierParticleRenderer::onClientTick);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(WTMenuTypes.POTION_POUCH_MENU.get(), PotionPouchScreen::new);
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(WTBlockEntities.ALTER_BE.get(), AlterBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(WTBlockEntities.PEDESTAL_BE.get(), PedestalBlockEntityRenderer::new);
    }
}
