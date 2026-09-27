package net.bananacheese.witchcraft.datagen;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.item.WTItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class WTItemModelProvider extends ItemModelProvider {

    public WTItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, WitchcraftTheDarkArts.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        handheldItem(WTItems.DARK_CRYSTAL);
    }

    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(WitchcraftTheDarkArts.MODID,"item/" + item.getId().getPath()));
    }
}
