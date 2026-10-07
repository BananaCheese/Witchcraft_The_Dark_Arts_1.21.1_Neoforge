package net.bananacheese.witchcraft.datagen;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.block.WTBlocks;
import net.bananacheese.witchcraft.item.WTItems;
import net.bananacheese.witchcraft.recipe.PotionResizeRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

import static org.openjdk.nashorn.internal.runtime.Debug.id;

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

        // ---- Flasks. Placeholder progression: a glass bottle + dark crystal makes the first small flask,
        // and empty flasks convert 1 medium <-> 2 small and 1 large <-> 2 medium (units are conserved).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, WTItems.MEDIUM_FLASK.get())
                .requires(Items.GLASS_BOTTLE)
                .requires(WTItems.DARK_CRYSTAL.get())
                .unlockedBy("has_dark_crystal", has(WTItems.DARK_CRYSTAL))
                .save(output, id("medium_flask_from_bottle"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, WTItems.MEDIUM_FLASK.get())
                .requires(WTItems.SMALL_FLASK.get(), 2)
                .unlockedBy("has_small_flask", has(WTItems.SMALL_FLASK))
                .save(output, id("medium_flask_from_small"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, WTItems.LARGE_FLASK.get())
                .requires(WTItems.MEDIUM_FLASK.get(), 2)
                .unlockedBy("has_medium_flask", has(WTItems.MEDIUM_FLASK))
                .save(output, id("large_flask_from_medium"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, WTItems.SMALL_FLASK.get(), 2)
                .requires(WTItems.MEDIUM_FLASK.get())
                .unlockedBy("has_medium_flask", has(WTItems.MEDIUM_FLASK))
                .save(output, id("small_flask_from_medium"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, WTItems.MEDIUM_FLASK.get(), 2)
                .requires(WTItems.LARGE_FLASK.get())
                .unlockedBy("has_large_flask", has(WTItems.LARGE_FLASK))
                .save(output, id("medium_flask_from_large"));

        // Pouring filled potions between flask sizes (special recipe: must copy the potion data component).
        SpecialRecipeBuilder.special(PotionResizeRecipe::new)
                .save(output, WitchcraftTheDarkArts.MODID + ":potion_resize");

        // Dedicated ritual recipe datagen.
        WTRitualRecipeProvider.generate(output);
    }
}