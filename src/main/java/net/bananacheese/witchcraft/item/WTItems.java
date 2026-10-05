package net.bananacheese.witchcraft.item;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.item.custom.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WTItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(WitchcraftTheDarkArts.MODID);

    public static final DeferredItem<Item> DARK_CRYSTAL =
            ITEMS.registerSimpleItem("dark_crystal");

    public static final DeferredItem<SoulSyringe> SOUL_SYRINGE =
            ITEMS.registerItem("soul_syringe", SoulSyringe::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<AlterAnalyzer> ALTER_ANALYZER =
            ITEMS.registerItem("alter_analyzer", AlterAnalyzer::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<ProceduralPotion> PROCEDURAL_POTION =
            ITEMS.registerItem("procedural_potion", ProceduralPotion::new, new Item.Properties().stacksTo(1));

    public static final DeferredItem<ThrowableProceduralPotion> PROCEDURAL_SPLASH_POTION =
            ITEMS.registerItem("procedural_splash_potion",
                    props -> new ThrowableProceduralPotion(props, false), new Item.Properties().stacksTo(1));

    public static final DeferredItem<ThrowableProceduralPotion> PROCEDURAL_LINGERING_POTION =
            ITEMS.registerItem("procedural_lingering_potion",
                    props -> new ThrowableProceduralPotion(props, true), new Item.Properties().stacksTo(1));

    public static final DeferredItem<PotionPouch> POTION_POUCH =
            ITEMS.registerItem("potion_pouch", PotionPouch::new, new Item.Properties().stacksTo(1));

    private WTItems() {
    }
}
