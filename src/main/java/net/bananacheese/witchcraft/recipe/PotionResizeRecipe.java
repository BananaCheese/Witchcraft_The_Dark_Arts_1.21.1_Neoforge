package net.bananacheese.witchcraft.recipe;

import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.item.custom.FlaskItem;
import net.bananacheese.witchcraft.item.custom.ProceduralPotion;
import net.bananacheese.witchcraft.potion.PotionSize;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Pours potions between flask sizes. Sizes are measured in "units" (small 1, medium 2, large 4),
 * and units are always conserved.
 *
 * <p><b>Split</b> - one potion + empty flasks that are all one smaller size, as many as fit:
 * <br>Medium potion + 2 small flasks -> 2 small potions, medium flask returned.
 * <br>Large potion + 2 medium flasks -> 2 medium potions, large flask returned.
 * <br>Large potion + 4 small flasks -> 4 small potions, large flask returned.
 *
 * <p><b>Merge</b> - several identical potions of one size + exactly one empty flask of the matching larger size:
 * <br>2 small potions + medium flask -> medium potion, 2 small flasks returned (and so on).
 *
 * <p>The potion's form (drink / splash / lingering) is preserved, and merging requires the same form
 * and the same brew.
 */
public class PotionResizeRecipe extends CustomRecipe {

    public PotionResizeRecipe(CraftingBookCategory category) {
        super(category);
    }

    /** The planned result: the output stack plus what each grid slot leaves behind. */
    private record Plan(ItemStack result, List<ItemStack> leftovers) {}

    private static Plan plan(CraftingInput input) {
        List<Integer> potionSlots = new ArrayList<>();
        List<Integer> flaskSlots = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof ProceduralPotion && stack.has(WTComponents.PROCEDURAL_POTION.get())) {
                potionSlots.add(i);
            } else if (stack.getItem() instanceof FlaskItem) {
                flaskSlots.add(i);
            } else {
                return null; // anything else in the grid -> not our recipe
            }
        }

        if (potionSlots.size() == 1 && !flaskSlots.isEmpty()) {
            return planSplit(input, potionSlots.get(0), flaskSlots);
        }
        if (potionSlots.size() >= 2 && flaskSlots.size() == 1) {
            return planMerge(input, potionSlots, flaskSlots.get(0));
        }
        return null;
    }

    private static Plan planSplit(CraftingInput input, int potionSlot, List<Integer> flaskSlots) {
        ItemStack potion = input.getItem(potionSlot);
        ProceduralPotionData data = potion.get(WTComponents.PROCEDURAL_POTION.get());
        if (data == null) return null;

        // All flasks must be the same size.
        PotionSize target = ((FlaskItem) input.getItem(flaskSlots.get(0)).getItem()).getSize();
        for (int slot : flaskSlots) {
            if (((FlaskItem) input.getItem(slot).getItem()).getSize() != target) return null;
        }

        PotionSize source = data.size();
        if (target.getUnits() >= source.getUnits()) return null;
        if (source.getUnits() % target.getUnits() != 0) return null;
        int count = source.getUnits() / target.getUnits();
        if (flaskSlots.size() != count) return null;

        ItemStack result = potion.copyWithCount(count);
        result.set(WTComponents.PROCEDURAL_POTION.get(), data.withSize(target));

        List<ItemStack> leftovers = emptyLeftovers(input.size());
        leftovers.set(potionSlot, new ItemStack(source.flask())); // the emptied big flask comes back
        return new Plan(result, leftovers);
    }

    private static Plan planMerge(CraftingInput input, List<Integer> potionSlots, int flaskSlot) {
        ItemStack first = input.getItem(potionSlots.get(0));
        ProceduralPotionData firstData = first.get(WTComponents.PROCEDURAL_POTION.get());
        if (firstData == null) return null;

        for (int slot : potionSlots) {
            ItemStack other = input.getItem(slot);
            ProceduralPotionData otherData = other.get(WTComponents.PROCEDURAL_POTION.get());
            if (otherData == null
                    || other.getItem() != first.getItem()       // same form
                    || otherData.size() != firstData.size()      // same size
                    || !otherData.isSameBrew(firstData)) {       // same brew
                return null;
            }
        }

        PotionSize small = firstData.size();
        PotionSize big = ((FlaskItem) input.getItem(flaskSlot).getItem()).getSize();
        if (big.getUnits() != small.getUnits() * potionSlots.size()) return null;

        ItemStack result = first.copyWithCount(1);
        result.set(WTComponents.PROCEDURAL_POTION.get(), firstData.withSize(big));

        List<ItemStack> leftovers = emptyLeftovers(input.size());
        for (int slot : potionSlots) {
            leftovers.set(slot, new ItemStack(small.flask())); // each emptied small flask comes back
        }
        return new Plan(result, leftovers);
    }

    private static List<ItemStack> emptyLeftovers(int size) {
        List<ItemStack> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add(ItemStack.EMPTY);
        return list;
    }

    // ------------------------------------------------------------------ CustomRecipe

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return plan(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        Plan plan = plan(input);
        return plan == null ? ItemStack.EMPTY : plan.result().copy();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        Plan plan = plan(input);
        if (plan == null) return super.getRemainingItems(input);

        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int i = 0; i < input.size(); i++) {
            remaining.set(i, plan.leftovers().get(i).copy());
        }
        return remaining;
    }

    /** Smallest valid recipe is a medium potion + 2 small flasks (3 slots); the largest needs 5. */
    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WTRecipes.POTION_RESIZE_SERIALIZER.get();
    }
}
