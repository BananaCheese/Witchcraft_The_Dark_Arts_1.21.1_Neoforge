package net.bananacheese.witchcraft;

import net.bananacheese.witchcraft.block.WTBlocks;
import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.bananacheese.witchcraft.datagen.WTDataGenerators;
import net.bananacheese.witchcraft.event.PlayerDeathHandler;
import net.bananacheese.witchcraft.event.WTCloudEvents;
import net.bananacheese.witchcraft.item.WTItemGroup;
import net.bananacheese.witchcraft.item.WTItems;
import net.bananacheese.witchcraft.recipe.WTRecipes;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(WitchcraftTheDarkArts.MODID)
public class WitchcraftTheDarkArts {
    public static final String MODID = "wctda";
    public static final Logger LOGGER = LogUtils.getLogger();

    public WitchcraftTheDarkArts(IEventBus modEventBus, ModContainer modContainer) {

        WTItemGroup.register(modEventBus);

        WTItems.ITEMS.register(modEventBus);

        WTBlocks.BLOCKS.register(modEventBus);
        WTBlocks.BLOCK_ITEMS.register(modEventBus);
        WTBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);

        WTRecipes.RECIPE_TYPES.register(modEventBus);
        WTRecipes.RECIPE_SERIALIZERS.register(modEventBus);

        modEventBus.addListener(WTDataGenerators::gatherData);

        NeoForge.EVENT_BUS.addListener(WTCloudEvents::onLivingJump);

        NeoForge.EVENT_BUS.addListener(PlayerDeathHandler::onPlayerDeath);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            WitchcraftTheDarkArtsClient.init(modEventBus);
        }
    }
}
