package net.bananacheese.witchcraft.init;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.inventory.PotionPouchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WTMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, WitchcraftTheDarkArts.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<PotionPouchMenu>> POTION_POUCH_MENU =
            MENU_TYPES.register("potion_pouch", () -> IMenuTypeExtension.create((containerId, playerInv, extraData) -> new PotionPouchMenu(containerId, playerInv)));

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}