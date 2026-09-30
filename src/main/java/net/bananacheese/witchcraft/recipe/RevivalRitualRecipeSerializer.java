package net.bananacheese.witchcraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;

public final class RevivalRitualRecipeSerializer implements RecipeSerializer<RevivalRitualRecipe> {
    public static final MapCodec<RevivalRitualRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("min_tier").forGetter(RevivalRitualRecipe::getMinTier),
            Ingredient.CODEC.fieldOf("altar_item").forGetter(RevivalRitualRecipe::getAltarItem),
            Codec.INT.fieldOf("fluid_amount").forGetter(RevivalRitualRecipe::getFluidAmount),
            Ingredient.CODEC.listOf().fieldOf("pedestal_items").forGetter(RevivalRitualRecipe::getPedestalItems)
    ).apply(instance, RevivalRitualRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RevivalRitualRecipe> STREAM_CODEC =
            StreamCodec.of(RevivalRitualRecipeSerializer::write, RevivalRitualRecipeSerializer::read);

    private static void write(RegistryFriendlyByteBuf buffer, RevivalRitualRecipe recipe) {
        buffer.writeVarInt(recipe.getMinTier());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getAltarItem());
        buffer.writeVarInt(recipe.getFluidAmount());
        buffer.writeVarInt(recipe.getPedestalItems().size());

        for (Ingredient ingredient : recipe.getPedestalItems()) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
        }
    }

    private static RevivalRitualRecipe read(RegistryFriendlyByteBuf buffer) {
        int minTier = buffer.readVarInt();
        Ingredient altarItem = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        int fluidAmount = buffer.readVarInt();
        int pedestalCount = buffer.readVarInt();
        ArrayList<Ingredient> pedestalItems = new ArrayList<>(pedestalCount);

        for (int i = 0; i < pedestalCount; i++) {
            pedestalItems.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
        }

        return new RevivalRitualRecipe(minTier, altarItem, fluidAmount, pedestalItems);
    }

    @Override
    public MapCodec<RevivalRitualRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RevivalRitualRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
