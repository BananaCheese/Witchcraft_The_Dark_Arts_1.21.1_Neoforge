package net.bananacheese.witchcraft.recipe;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class WTRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, WitchcraftTheDarkArts.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, WitchcraftTheDarkArts.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<RitualRecipe>> RITUAL_TYPE =
            RECIPE_TYPES.register("ritual", () -> RecipeType.simple(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                            WitchcraftTheDarkArts.MODID, "ritual")));

    public static final DeferredHolder<RecipeType<?>, RecipeType<RevivalRitualRecipe>> REVIVAL_TYPE =
            RECIPE_TYPES.register("revival_ritual", () -> RecipeType.simple(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                            WitchcraftTheDarkArts.MODID, "revival_ritual")));

    public static final DeferredHolder<RecipeSerializer<?>, RitualRecipeSerializer> RITUAL_SERIALIZER =
            RECIPE_SERIALIZERS.register("ritual", RitualRecipeSerializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RevivalRitualRecipeSerializer> REVIVAL_SERIALIZER =
            RECIPE_SERIALIZERS.register("revival_ritual", RevivalRitualRecipeSerializer::new);

    private WTRecipes() {
    }
}
