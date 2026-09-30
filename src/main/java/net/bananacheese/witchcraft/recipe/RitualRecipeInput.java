package net.bananacheese.witchcraft.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.ArrayList;
import java.util.List;

public class RitualRecipeInput implements RecipeInput {
    private final ItemStack altarItem;
    private final int fluidAmount;
    private final int altarTier;
    private final List<ItemStack> pedestalItems;

    public RitualRecipeInput(ItemStack altarItem, int fluidAmount, int altarTier, List<ItemStack> pedestalItems) {
        this.altarItem = altarItem.copy();
        this.fluidAmount = fluidAmount;
        this.altarTier = altarTier;
        this.pedestalItems = new ArrayList<>();

        for (ItemStack stack : pedestalItems) {
            if (!stack.isEmpty()) {
                this.pedestalItems.add(stack.copy());
            }
        }
    }

    public ItemStack getAltarItem() {
        return altarItem;
    }

    public int getFluidAmount() {
        return fluidAmount;
    }

    public int getAltarTier() {
        return altarTier;
    }

    public List<ItemStack> getPedestalItems() {
        return pedestalItems;
    }

    @Override
    public ItemStack getItem(int index) {
        if (index == 0) {
            return altarItem;
        }

        int pedestalIndex = index - 1;
        if (pedestalIndex >= 0 && pedestalIndex < pedestalItems.size()) {
            return pedestalItems.get(pedestalIndex);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1 + pedestalItems.size();
    }
}
