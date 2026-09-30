package net.bananacheese.witchcraft;

import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.bananacheese.witchcraft.block.entity.renderer.AlterBlockEntityRenderer;
import net.bananacheese.witchcraft.block.entity.renderer.PedestalBlockEntityRenderer;
import net.bananacheese.witchcraft.client.BarrierParticleRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = WitchcraftTheDarkArts.MOD_ID, dist = Dist.CLIENT)
public class WitchcraftTheDarkArtsClient {

    public static void init(IEventBus modBus) {
    }

    public WitchcraftTheDarkArtsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        NeoForge.EVENT_BUS.addListener(BarrierParticleRenderer::onClientTick);
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(WTBlockEntities.ALTER_BE.get(), AlterBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(WTBlockEntities.PEDESTAL_BE.get(), PedestalBlockEntityRenderer::new);
    }
}
