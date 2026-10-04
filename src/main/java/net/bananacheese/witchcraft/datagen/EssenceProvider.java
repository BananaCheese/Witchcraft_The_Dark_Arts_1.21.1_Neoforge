package net.bananacheese.witchcraft.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.bananacheese.witchcraft.potion.synthesis.EssenceData;
import net.bananacheese.witchcraft.potion.synthesis.EssenceType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EssenceProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final String modId;
    private final Map<Item, EssenceData> entries = new HashMap<>();

    public EssenceProvider(PackOutput packOutput, String modId, CompletableFuture<HolderLookup.Provider> registries) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "essences");
        this.registries = registries;
        this.modId = modId;
    }

    /**
     * Override this method to register all your item essences.
     */
    protected void addEssences() {

    }

    /**
     * Helper builder method to add an item with its essence values.
     */
    protected void add(ItemLike item, WTEssenceProvider.Builder builder) {
        entries.put(item.asItem(), builder.build());
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.registries.thenComposeAsync(provider -> {
            addEssences();
            List<CompletableFuture<?>> futures = new ArrayList<>();

            for (Map.Entry<Item, EssenceData> entry : entries.entrySet()) {
                Item item = entry.getKey();
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);

                // Encode using the EssenceData codec
                JsonElement json = EssenceData.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue())
                        .getOrThrow(msg -> new IllegalStateException("Failed to encode essence for " + itemId + ": " + msg));

                // Add the target item ID inside the JSON object
                json.getAsJsonObject().addProperty("item", itemId.toString());

                ResourceLocation filePath = ResourceLocation.fromNamespaceAndPath(modId, itemId.getPath());
                futures.add(DataProvider.saveStable(output, json, this.pathProvider.json(filePath)));
            }

            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Essences Provider: " + modId;
    }

    // Helper Builder for fluid method chaining
    public static class Builder {
        private final Map<EssenceType, Float> values = new HashMap<>();

        public static WTEssenceProvider.Builder create() {
            return new WTEssenceProvider.Builder();
        }

        public WTEssenceProvider.Builder with(EssenceType type, float weight) {
            values.put(type, weight);
            return this;
        }

        public EssenceData build() {
            return new EssenceData(Map.copyOf(values));
        }
    }
}
