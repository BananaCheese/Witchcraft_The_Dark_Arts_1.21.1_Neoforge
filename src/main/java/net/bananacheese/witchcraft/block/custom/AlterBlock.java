package net.bananacheese.witchcraft.block.custom;

import com.mojang.serialization.MapCodec;
import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.bananacheese.witchcraft.ritual.RitualHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

        // setChanged() (called inside setHeldItem) only marks this dirty
        // for disk saving — it does NOT push the change to the client, so
        // without this the client-side block entity (what the floating-item
        // renderer actually reads from) never learns the held item changed.
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);

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
