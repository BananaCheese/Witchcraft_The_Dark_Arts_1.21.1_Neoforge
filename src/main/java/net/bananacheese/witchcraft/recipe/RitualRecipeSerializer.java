package net.bananacheese.witchcraft.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;

public final class RitualRecipeSerializer implements RecipeSerializer<RitualRecipe> {
    public static final MapCodec<RitualRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.fieldOf("min_tier").forGetter(RitualRecipe::getMinTier),
            Ingredient.CODEC.fieldOf("altar_item").forGetter(RitualRecipe::getAltarItem),
            Codec.INT.fieldOf("fluid_amount").forGetter(RitualRecipe::getFluidAmount),
            Ingredient.CODEC.listOf().fieldOf("pedestal_items").forGetter(RitualRecipe::getPedestalItems),
            ItemStack.CODEC.fieldOf("result").forGetter(RitualRecipe::getResult),
            Codec.STRING.optionalFieldOf("effect", "").forGetter(RitualRecipe::getEffectName)
    ).apply(instance, RitualRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, RitualRecipe> STREAM_CODEC =
            StreamCodec.of(RitualRecipeSerializer::write, RitualRecipeSerializer::read);

    private static void write(RegistryFriendlyByteBuf buffer, RitualRecipe recipe) {
        buffer.writeVarInt(recipe.getMinTier());
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getAltarItem());
        buffer.writeVarInt(recipe.getFluidAmount());
        buffer.writeVarInt(recipe.getPedestalItems().size());

        for (Ingredient ingredient : recipe.getPedestalItems()) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
        }

        ItemStack.STREAM_CODEC.encode(buffer, recipe.getResult());
        buffer.writeUtf(recipe.getEffectName());
    }

    private static RitualRecipe read(RegistryFriendlyByteBuf buffer) {
        int minTier = buffer.readVarInt();
        Ingredient altarItem = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        int fluidAmount = buffer.readVarInt();
        int pedestalCount = buffer.readVarInt();
        ArrayList<Ingredient> pedestalItems = new ArrayList<>(pedestalCount);

        for (int i = 0; i < pedestalCount; i++) {
            pedestalItems.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
        }

        ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
        String effectName = buffer.readUtf();

        return new RitualRecipe(
                minTier,
                altarItem,
                fluidAmount,
                pedestalItems,
                result,
                effectName);
    }

    @Override
    public MapCodec<RitualRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, RitualRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
