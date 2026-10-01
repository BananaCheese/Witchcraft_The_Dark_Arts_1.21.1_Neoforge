package net.bananacheese.witchcraft.datagen;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.item.WTItems;
import net.bananacheese.witchcraft.recipe.RevivalRitualRecipe;
import net.bananacheese.witchcraft.recipe.RitualRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public final class WTRitualRecipeProvider {

    public static void generate(RecipeOutput output) {

        // Altar Analyzer Creation (Tier 1)
        createRitualRecipe(
                output,
                "altar_analyzer_creation",
                1,
                Items.GOLD_INGOT,
                1000,
                List.of(),
                new ItemStack(WTItems.ALTER_ANALYZER.get()),
                "portal");

        // Dark Crystal Creation (Tier 4)
        createRitualRecipe(
                output,
                "dark_crystal_creation",
                4,
                Items.CRYING_OBSIDIAN,
                0,
                List.of(
                        Ingredient.of(Items.REDSTONE_BLOCK),
                        Ingredient.of(Items.REDSTONE_BLOCK),
                        Ingredient.of(Items.AMETHYST_BLOCK),
                        Ingredient.of(Items.AMETHYST_BLOCK),
                        Ingredient.of(Items.SCULK),
                        Ingredient.of(Items.SCULK),
                        Ingredient.of(Items.SCULK),
                        Ingredient.of(Items.SCULK)),
                new ItemStack(WTItems.DARK_CRYSTAL.get()),
                "dark");

        // Player Revival (Tier 4)
        createRevivalRecipe(
                output,
                "player_revival",
                4,
                WTItems.SOUL_SYRINGE.get(),
                1000,
                List.of(
                        Ingredient.of(Items.TOTEM_OF_UNDYING),
                        Ingredient.of(Items.NETHER_STAR),
                        Ingredient.of(Items.ENCHANTED_GOLDEN_APPLE),
                        Ingredient.of(Items.ENCHANTED_GOLDEN_APPLE),
                        Ingredient.of(Items.DIAMOND_BLOCK),
                        Ingredient.of(Items.DIAMOND_BLOCK),
                        Ingredient.of(Items.DIAMOND_BLOCK),
                        Ingredient.of(Items.DIAMOND_BLOCK)));
    }

    private static void createRitualRecipe(
            RecipeOutput output,
            String name,
            int minTier,
            Item altarItem,
            int fluidAmount,
            List<Ingredient> pedestalItems,
            ItemStack result,
            String effect) {

        RitualRecipe recipe = new RitualRecipe(
                minTier,
                Ingredient.of(altarItem),
                fluidAmount,
                pedestalItems,
                result,
                effect);

        output.accept(
                ResourceLocation.fromNamespaceAndPath(
                        WitchcraftTheDarkArts.MODID,
                        name),
                recipe,
                null);
    }

    private static void createRevivalRecipe(
            RecipeOutput output,
            String name,
            int minTier,
            Item altarItem,
            int fluidAmount,
            List<Ingredient> pedestalItems) {

        RevivalRitualRecipe recipe = new RevivalRitualRecipe(
                minTier,
                Ingredient.of(altarItem),
                fluidAmount,
                pedestalItems);

        output.accept(
                ResourceLocation.fromNamespaceAndPath(
                        WitchcraftTheDarkArts.MODID,
                        name),
                recipe,
                null);
    }

    private WTRitualRecipeProvider() {
    }
}
