package net.bananacheese.witchcraft;

import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.bananacheese.witchcraft.block.entity.renderer.AlterBlockEntityRenderer;
import net.bananacheese.witchcraft.block.entity.renderer.DarkCauldronBlockEntityRenderer;
import net.bananacheese.witchcraft.block.entity.renderer.PedestalBlockEntityRenderer;
import net.bananacheese.witchcraft.client.BarrierParticleRenderer;
import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.init.WTEntities;
import net.bananacheese.witchcraft.init.WTMenuTypes;
import net.bananacheese.witchcraft.item.WTItems;
import net.bananacheese.witchcraft.item.custom.SoulSyringe;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.bananacheese.witchcraft.screen.PotionPouchScreen;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
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

        ResourceLocation sizeProperty = ResourceLocation.fromNamespaceAndPath(WitchcraftTheDarkArts.MODID, "potion_size");
        for (Item potion : new Item[]{
                WTItems.PROCEDURAL_POTION.get(),
                WTItems.PROCEDURAL_SPLASH_POTION.get(),
                WTItems.PROCEDURAL_LINGERING_POTION.get()}) {
            ItemProperties.register(potion, sizeProperty, (stack, level, entity, seed) -> {
                ProceduralPotionData data = stack.get(WTComponents.PROCEDURAL_POTION.get());
                return data == null ? 0f : data.size().ordinal();
            });
        }
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
    static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
                    if (tintIndex != 0) return -1;
                    ProceduralPotionData data = stack.get(WTComponents.PROCEDURAL_POTION.get());
                    return data == null ? 0xFFFFFFFF : (0xFF000000 | data.color());
                }, WTItems.PROCEDURAL_POTION.get(),
                WTItems.PROCEDURAL_SPLASH_POTION.get(),
                WTItems.PROCEDURAL_LINGERING_POTION.get());
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(WTBlockEntities.ALTER_BE.get(), AlterBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(WTBlockEntities.PEDESTAL_BE.get(), PedestalBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(WTBlockEntities.DARK_CAULDRON_BE.get(), DarkCauldronBlockEntityRenderer::new);
        event.registerEntityRenderer(WTEntities.THROWN_PROCEDURAL_POTION.get(), ThrownItemRenderer::new);
    }
}
