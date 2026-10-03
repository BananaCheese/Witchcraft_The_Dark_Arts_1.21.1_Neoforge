package net.bananacheese.witchcraft.block.custom;

import com.mojang.serialization.MapCodec;
import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.bananacheese.witchcraft.ritual.RitualHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.Nullable;

public class AlterBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 13, 14);
    public static final MapCodec<AlterBlock> CODEC = simpleCodec(AlterBlock::new);

    public AlterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends AlterBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
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

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit) {

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(level.getBlockEntity(pos) instanceof AlterBlockEntity alter)) {
            return InteractionResult.PASS;
        }

        if (FluidUtil.interactWithFluidHandler(
                player,
                InteractionHand.MAIN_HAND,
                alter.getFluidHandler())) {
            return InteractionResult.SUCCESS;
        }

        ItemStack heldItem = player.getMainHandItem();

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
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean movedByPiston) {

        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        if (!level.isClientSide
                && level.getBlockEntity(pos) instanceof AlterBlockEntity alter) {
            boolean powered = level.hasNeighborSignal(pos);

            if (powered) {
                RitualHandler.attemptRitual(level, pos, alter);
            }
        }
    }
}

