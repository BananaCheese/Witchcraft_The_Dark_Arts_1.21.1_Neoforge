package net.bananacheese.witchcraft.item;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.item.custom.AlterAnalyzer;
import net.bananacheese.witchcraft.item.custom.SoulSyringe;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WTItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WitchcraftTheDarkArts.MODID);

    public static final DeferredItem<Item> DARK_CRYSTAL = ITEMS.registerSimpleItem("dark_crystal");

    public static final DeferredItem<SoulSyringe> SOUL_SYRINGE =
            ITEMS.registerItem(
                    "soul_syringe",
                    SoulSyringe::new,
                    new Item.Properties().stacksTo(1));

    public static final DeferredItem<AlterAnalyzer> ALTER_ANALYZER =
            ITEMS.registerItem(
                    "alter_analyzer",
                    AlterAnalyzer::new,
                    new Item.Properties().stacksTo(1));

    private WTItems() {
    }
}
