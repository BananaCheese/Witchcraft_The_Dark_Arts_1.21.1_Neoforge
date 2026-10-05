package net.bananacheese.witchcraft.potion;

import net.bananacheese.witchcraft.item.WTItems;
import net.minecraft.world.item.Item;

public enum PotionForm {
    DRINK,
    SPLASH,
    LINGERING;

    /** Resolved lazily so this enum can be referenced before items are registered. */
    public Item item() {
        return switch (this) {
            case DRINK -> WTItems.PROCEDURAL_POTION.get();
            case SPLASH -> WTItems.PROCEDURAL_SPLASH_POTION.get();
            case LINGERING -> WTItems.PROCEDURAL_LINGERING_POTION.get();
        };
    }
}
