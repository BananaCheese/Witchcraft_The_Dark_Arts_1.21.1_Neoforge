package net.bananacheese.witchcraft.block.custom;

import net.bananacheese.witchcraft.block.entity.custom.DarkCauldronBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class DarkCauldron extends Block implements EntityBlock {
    public DarkCauldron(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DarkCauldronBlockEntity(pos, state);
    }

    /**
     * Checks if the block directly underneath provides sufficient thermal energy to heat the liquid.
     */
    public static boolean isHeatedUnderneath(Level level, BlockPos pos) {
        BlockState stateBelow = level.getBlockState(pos.below());
        return stateBelow.is(Blocks.FIRE) ||
                stateBelow.is(Blocks.SOUL_FIRE) ||
                stateBelow.is(Blocks.CAMPFIRE) ||
                stateBelow.is(Blocks.SOUL_CAMPFIRE) ||
                stateBelow.is(Blocks.MAGMA_BLOCK) ||
                stateBelow.is(Blocks.LAVA);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof DarkCauldronBlockEntity cauldron) {
            cauldron.onPlayerInteract(player, hand, stack);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? (lbl, pos, st, be) -> DarkCauldronBlockEntity.clientTick(lbl, pos, st, (DarkCauldronBlockEntity) be)
                : (lbl, pos, st, be) -> DarkCauldronBlockEntity.serverTick(lbl, pos, st, (DarkCauldronBlockEntity) be);
    }
}