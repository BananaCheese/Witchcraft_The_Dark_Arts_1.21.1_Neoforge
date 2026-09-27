package net.bananacheese.witchcraft;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = WitchcraftTheDarkArts.MODID, dist = Dist.CLIENT)
public class WitchcraftTheDarkArtsClient {

    public static void init(IEventBus modBus) {
    }

    private WitchcraftTheDarkArtsClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
