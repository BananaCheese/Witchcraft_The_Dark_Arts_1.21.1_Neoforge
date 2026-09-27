package net.bananacheese.witchcraft.block;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.block.custom.BarrierFieldBlock;
import net.bananacheese.witchcraft.block.custom.DarkBarrierBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WTBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(WitchcraftTheDarkArts.MODID);
    public static final DeferredRegister.Items BLOCK_ITEMS =
            DeferredRegister.createItems(WitchcraftTheDarkArts.MODID);

    public static final DeferredBlock<net.bananacheese.witchcraft.block.custom.AlterBlock> ALTER = BLOCKS.registerBlock(
            "alter", net.bananacheese.witchcraft.block.custom.AlterBlock::new,
            BlockBehaviour.Properties.of().noOcclusion().strength(2, 6));

    public static final DeferredBlock<net.bananacheese.witchcraft.block.custom.PedestalBlock> PEDESTAL = BLOCKS.registerBlock(
            "pedestal", net.bananacheese.witchcraft.block.custom.PedestalBlock::new,
            BlockBehaviour.Properties.of().noOcclusion().strength(2, 6));

    public static final DeferredBlock<DarkBarrierBlock> DARK_BARRIER = BLOCKS.registerBlock(
            "dark_barrier", DarkBarrierBlock::new,
            BlockBehaviour.Properties.of().strength(3, 9));

    // No block item — matches the original's registerBlockWithoutBlockItem.
    public static final DeferredBlock<BarrierFieldBlock> BARRIER_FIELD = BLOCKS.register(
            "barrier_field", () -> new BarrierFieldBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0f, 3600000.0f)
                    .noLootTable()
                    .noCollission()
                    .noOcclusion()));

    public static final DeferredHolder<Item, BlockItem> ALTER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(ALTER);
    public static final DeferredHolder<Item, BlockItem> PEDESTAL_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(PEDESTAL);
    public static final DeferredHolder<Item, BlockItem> DARK_BARRIER_ITEM = BLOCK_ITEMS.registerSimpleBlockItem(DARK_BARRIER);

    private WTBlocks() {
    }
}

