package net.bananacheese.witchcraft.datagen;

import net.bananacheese.witchcraft.block.WTBlocks;
import net.bananacheese.witchcraft.item.WTItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class WTRecipeProvider extends RecipeProvider {

    public WTRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WTBlocks.ALTER.get())
                .pattern("COC")
                .pattern("COC")
                .pattern("CCC")
                .define('C', Items.COBBLESTONE)
                .define('O', Items.CRYING_OBSIDIAN)
                .unlockedBy("has_crying_obsidian", has(Items.CRYING_OBSIDIAN))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WTBlocks.PEDESTAL.get())
                .pattern("ORO")
                .pattern(" C ")
                .pattern("CCC")
                .define('C', Items.COBBLESTONE)
                .define('O', Items.CRYING_OBSIDIAN)
                .define('R', Items.RED_CARPET)
                .unlockedBy("has_crying_obsidian", has(Items.CRYING_OBSIDIAN))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WTItems.SOUL_SYRINGE.get())
                .pattern(" GI")
                .pattern(" XG")
                .pattern("I  ")
                .define('G', Items.GOLD_INGOT)
                .define('I', Items.IRON_INGOT)
                .define('X', Items.GLASS_PANE)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, WTBlocks.DARK_BARRIER.get())
                .pattern("BRB")
                .pattern("ADA")
                .pattern("BRB")
                .define('B', Items.BLACKSTONE)
                .define('R', Items.REDSTONE_BLOCK)
                .define('A', Items.AMETHYST_BLOCK)
                .define('D', WTItems.DARK_CRYSTAL)
                .unlockedBy("has_dark_crystal", has(WTItems.DARK_CRYSTAL))
                .save(output);

        // Dedicated ritual recipe datagen.
        WTRitualRecipeProvider.generate(output);
    }
}