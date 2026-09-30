package net.bananacheese.witchcraft.item;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WTItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WitchcraftTheDarkArts.MOD_ID);

    public static final DeferredItem<Item> DARK_CRYSTAL = ITEMS.registerSimpleItem("dark_crystal");
}
