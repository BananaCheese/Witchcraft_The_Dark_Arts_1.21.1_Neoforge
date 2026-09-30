package net.bananacheese.witchcraft.recipe;

import net.bananacheese.witchcraft.item.custom.SoulSyringe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RevivalRitualRecipe implements Recipe<RitualRecipeInput> {
    private final int minTier;
    private final Ingredient altarItem;
    private final int fluidAmount;
    private final List<Ingredient> pedestalItems;

    public RevivalRitualRecipe(
            int minTier,
            Ingredient altarItem,
            int fluidAmount,
            List<Ingredient> pedestalItems) {
        this.minTier = minTier;
        this.altarItem = altarItem;
        this.fluidAmount = fluidAmount;
        this.pedestalItems = List.copyOf(pedestalItems);
    }

    @Override
    public boolean matches(RitualRecipeInput input, Level level) {
        if (input.getAltarTier() < minTier
                || !altarItem.test(input.getAltarItem())
                || input.getFluidAmount() < fluidAmount) {
            return false;
        }

        ItemStack syringe = input.getAltarItem();
        if (!(syringe.getItem() instanceof SoulSyringe)
                || SoulSyringe.getFillLevel(syringe) < 4) {
            return false;
        }

        UUID target = SoulSyringe.getTargetPlayer(syringe);
        if (target == null) {
            return false;
        }

        return matchesPedestals(input.getPedestalItems());
    }

    private boolean matchesPedestals(List<ItemStack> availableStacks) {
        if (availableStacks.size() < pedestalItems.size()) {
            return false;
        }

        List<ItemStack> remaining = new ArrayList<>(availableStacks);

        for (Ingredient ingredient : pedestalItems) {
            int match = -1;

            for (int i = 0; i < remaining.size(); i++) {
                if (ingredient.test(remaining.get(i))) {
                    match = i;
                    break;
                }
            }

            if (match < 0) {
                return false;
            }

            remaining.remove(match);
        }

        return true;
    }

    @Override
    public ItemStack assemble(RitualRecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WTRecipes.REVIVAL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return WTRecipes.REVIVAL_TYPE.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(altarItem);
        ingredients.addAll(pedestalItems);
        return ingredients;
    }

    public int getMinTier() {
        return minTier;
    }

    public Ingredient getAltarItem() {
        return altarItem;
    }

    public int getFluidAmount() {
        return fluidAmount;
    }

    public List<Ingredient> getPedestalItems() {
        return pedestalItems;
    }
}
