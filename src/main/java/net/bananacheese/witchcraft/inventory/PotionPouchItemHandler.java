package net.bananacheese.witchcraft.inventory;

import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class PotionPouchItemHandler extends ItemStackHandler {
    public PotionPouchItemHandler() {
        super(5); // 5 Slots
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        // Only allow items carrying procedural potion data
        return stack.has(WTComponents.PROCEDURAL_POTION.get());
    }

    @Override
    public int getSlotLimit(int slot) {
        return 32; // Overrides the default container slot stack cap
    }

    @Override
    public int getStackLimit(int slot, @NotNull ItemStack stack) {
        ProceduralPotionData data = stack.get(WTComponents.PROCEDURAL_POTION.get());
        if (data != null) {
            return data.size().getPouchMaxStack(); // 8 for Large, 16 for Medium, 32 for Small
        }
        return super.getStackLimit(slot, stack);
    }
}
