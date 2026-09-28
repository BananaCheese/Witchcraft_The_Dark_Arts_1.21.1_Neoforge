package net.bananacheese.witchcraft.datagen;

import net.bananacheese.witchcraft.block.WTBlocks;
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

        // TODO once WTItems exists:
        // SOUL_SYRINGE:  " GI" / " XG" / "I  "  with G=gold ingot, I=iron ingot, X=glass pane
        // DARK_BARRIER (x8): "BRB" / "ADA" / "BRB" with B=blackstone, R=redstone block,
        //                    A=amethyst block, D=dark crystal
    }
}
