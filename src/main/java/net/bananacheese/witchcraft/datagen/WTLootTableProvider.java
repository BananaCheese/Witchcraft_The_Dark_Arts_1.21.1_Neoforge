package net.bananacheese.witchcraft.datagen;

import net.bananacheese.witchcraft.block.WTBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class WTLootTableProvider {

    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)
        ), registries);
    }

    public static class Blocks extends BlockLootSubProvider {
        public Blocks(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            dropSelf(WTBlocks.ALTER.get());
            dropSelf(WTBlocks.PEDESTAL.get());
            dropSelf(WTBlocks.DARK_BARRIER.get());
            dropSelf(WTBlocks.CLOUD_BLOCK.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(WTBlocks.ALTER.get(), WTBlocks.PEDESTAL.get(), WTBlocks.DARK_BARRIER.get(), WTBlocks.CLOUD_BLOCK.get());
        }
    }
}
