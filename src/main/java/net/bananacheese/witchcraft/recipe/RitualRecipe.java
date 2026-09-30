package net.bananacheese.witchcraft.recipe;

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

public class RitualRecipe implements Recipe<RitualRecipeInput> {
    private final int minTier;
    private final Ingredient altarItem;
    private final int fluidAmount;
    private final List<Ingredient> pedestalItems;
    private final ItemStack result;
    private final String effectName;

    public RitualRecipe(
            int minTier,
            Ingredient altarItem,
            int fluidAmount,
            List<Ingredient> pedestalItems,
            ItemStack result,
            String effectName) {
        this.minTier = minTier;
        this.altarItem = altarItem;
        this.fluidAmount = fluidAmount;
        this.pedestalItems = List.copyOf(pedestalItems);
        this.result = result.copy();
        this.effectName = effectName;
    }

    @Override
    public boolean matches(RitualRecipeInput input, Level level) {
        if (input.getAltarTier() < minTier) {
            return false;
        }

        if (!altarItem.test(input.getAltarItem())) {
            return false;
        }

        if (input.getFluidAmount() < fluidAmount) {
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
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WTRecipes.RITUAL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return WTRecipes.RITUAL_TYPE.get();
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

    public ItemStack getResult() {
        return result.copy();
    }

    public String getEffectName() {
        return effectName;
    }
}
