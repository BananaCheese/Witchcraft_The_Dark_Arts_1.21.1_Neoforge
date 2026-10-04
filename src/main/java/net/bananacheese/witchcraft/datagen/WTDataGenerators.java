package net.bananacheese.witchcraft.datagen;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public final class WTDataGenerators {

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new WTBlockTagProvider(output, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), WTLootTableProvider.create(output, lookupProvider));
        generator.addProvider(event.includeServer(), new WTRecipeProvider(output, lookupProvider));

        generator.addProvider(event.includeClient(), new WTItemModelProvider(output, existingFileHelper));

        generator.addProvider(event.includeServer(), new WTEssenceProvider(output, WitchcraftTheDarkArts.MODID, lookupProvider));
    }

    private WTDataGenerators() {
    }
}
