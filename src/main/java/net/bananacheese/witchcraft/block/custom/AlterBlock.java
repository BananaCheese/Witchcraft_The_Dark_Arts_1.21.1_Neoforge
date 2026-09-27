package net.bananacheese.witchcraft.block.custom;

import com.mojang.serialization.MapCodec;
import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.bananacheese.witchcraft.ritual.RitualHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AlterBlock extends BaseEntityBlock {
    public static final MapCodec<AlterBlock> CODEC = simpleCodec(AlterBlock::new);

    public AlterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, WTBlockEntities.ALTER_BE.get(),
                (level1, pos, state1, blockEntity) -> blockEntity.tick(level1, pos, state1));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof AlterBlockEntity alter)) {
            return InteractionResult.PASS;
        }

        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() == Items.WATER_BUCKET) {
            if (alter.addFluid(1000)) {
                if (!player.getAbilities().instabuild) {
                    player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
                }
                player.displayClientMessage(Component.literal("Altar filled with water"), true);
            } else {
                player.displayClientMessage(Component.literal("Altar is already full"), true);
            }
            return InteractionResult.SUCCESS;
        }

        if (alter.getHeldItem().isEmpty()) {
            if (!heldItem.isEmpty()) {
                ItemStack toPlace = heldItem.copyWithCount(1);
                alter.setHeldItem(toPlace);
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
            }
        } else {
            ItemStack existing = alter.getHeldItem();
            alter.setHeldItem(ItemStack.EMPTY);
            if (!player.getInventory().add(existing)) {
                player.drop(existing, false);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AlterBlockEntity alter) {
            boolean powered = level.hasNeighborSignal(pos);
            if (powered) {
                RitualHandler.attemptRitual(level, pos, alter);
            }
        }
    }
}
