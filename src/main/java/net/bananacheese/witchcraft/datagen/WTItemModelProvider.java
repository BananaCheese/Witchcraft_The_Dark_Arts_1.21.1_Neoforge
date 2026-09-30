package net.bananacheese.witchcraft.datagen;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.block.WTBlocks;
import net.bananacheese.witchcraft.item.WTItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class WTItemModelProvider extends ItemModelProvider {

    public WTItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, WitchcraftTheDarkArts.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // TODO: register item models as the magic items are ported, e.g.
        //   handheldItem(WTItems.ALTER_ANALYZER.get());
        basicItem(WTItems.DARK_CRYSTAL.get());
    }
}
