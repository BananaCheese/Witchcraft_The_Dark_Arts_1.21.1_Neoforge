package net.bananacheese.witchcraft.potion.synthesis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class EssenceDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    // Global lookup registry mapping Item -> Essence Data
    private static final Map<Item, EssenceData> ITEM_ESSENCE_REGISTRY = new HashMap<>();

    public EssenceDataLoader() {
        super(GSON, "essences"); // Reads from data/<namespace>/essences/*.json
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        ITEM_ESSENCE_REGISTRY.clear();

        objectMap.forEach((location, json) -> {
            try {
                JsonObject jsonObject = json.getAsJsonObject();

                // Parse Target Item
                ResourceLocation itemId = ResourceLocation.parse(jsonObject.get("item").getAsString());
                Item item = BuiltInRegistries.ITEM.get(itemId);

                if (item != BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getDefaultKey())) {
                    // Parse Essence Data using Codec
                    EssenceData.CODEC.parse(JsonOps.INSTANCE, jsonObject)
                            .resultOrPartial(error -> System.err.println("Failed to parse Essence JSON [" + location + "]: " + error))
                            .ifPresent(data -> ITEM_ESSENCE_REGISTRY.put(item, data));
                }
            } catch (Exception e) {
                System.err.println("Error loading Essence file: " + location + " - " + e.getMessage());
            }
        });

        System.out.println("Loaded " + ITEM_ESSENCE_REGISTRY.size() + " ingredient essence mappings.");
    }

    /**
     * Look up the essence profile for a given item stack.
     */
    public static EssenceData getEssenceData(ItemStack stack) {
        return ITEM_ESSENCE_REGISTRY.getOrDefault(stack.getItem(), new EssenceData(Map.of()));
    }

    /**
     * Combines up to 5 ingredient items into a single unified essence map for synthesis.
     */
    public static Map<EssenceType, Float> combineIngredients(Iterable<ItemStack> ingredients) {
        // EnumMap: deterministic (ordinal) iteration order, required for seed-stable synthesis.
        Map<EssenceType, Float> combinedMap = new EnumMap<>(EssenceType.class);

        int count = 0;
        for (ItemStack stack : ingredients) {
            if (stack.isEmpty()) continue;
            if (count >= 5) break; // Hard limit at max 5 items

            EssenceData data = getEssenceData(stack);
            data.values().forEach((type, weight) ->
                    combinedMap.merge(type, weight * stack.getCount(), Float::sum)
            );

            count++;
        }

        return combinedMap;
    }
}

