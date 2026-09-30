package net.bananacheese.witchcraft.block.entity;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.block.WTBlocks;
import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.bananacheese.witchcraft.block.entity.custom.PedestalBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WTBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, WitchcraftTheDarkArts.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlterBlockEntity>> ALTER_BE =
            BLOCK_ENTITY_TYPES.register("alter_be",
                    () -> BlockEntityType.Builder.of(AlterBlockEntity::new, WTBlocks.ALTER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PedestalBlockEntity>> PEDESTAL_BE =
            BLOCK_ENTITY_TYPES.register("pedestal_be",
                    () -> BlockEntityType.Builder.of(PedestalBlockEntity::new, WTBlocks.PEDESTAL.get()).build(null));

    private WTBlockEntities() {
    }
}
