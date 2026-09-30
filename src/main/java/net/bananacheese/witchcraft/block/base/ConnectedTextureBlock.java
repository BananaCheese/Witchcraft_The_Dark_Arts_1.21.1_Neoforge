package net.bananacheese.witchcraft.block.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class ConnectedTextureBlock extends Block {

    public static final BooleanProperty CONNECTED_NORTH = BooleanProperty.create("connected_north");
    public static final BooleanProperty CONNECTED_SOUTH = BooleanProperty.create("connected_south");
    public static final BooleanProperty CONNECTED_EAST = BooleanProperty.create("connected_east");
    public static final BooleanProperty CONNECTED_WEST = BooleanProperty.create("connected_west");
    public static final BooleanProperty CONNECTED_UP = BooleanProperty.create("connected_up");
    public static final BooleanProperty CONNECTED_DOWN = BooleanProperty.create("connected_down");

    public ConnectedTextureBlock(Properties properties) {
        super(properties);
        registerDefaultState(getConnectionDefaultState(getStateDefinition().any()));
    }

    protected BlockState getConnectionDefaultState(BlockState state) {
        return state
                .setValue(CONNECTED_NORTH, false)
                .setValue(CONNECTED_SOUTH, false)
                .setValue(CONNECTED_EAST, false)
                .setValue(CONNECTED_WEST, false)
                .setValue(CONNECTED_UP, false)
                .setValue(CONNECTED_DOWN, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONNECTED_NORTH, CONNECTED_SOUTH, CONNECTED_EAST,
                CONNECTED_WEST, CONNECTED_UP, CONNECTED_DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState state = defaultBlockState();
        return updateConnections(state, ctx.getLevel(), ctx.getClickedPos());
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
                                                LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return updateConnections(state, level, pos);
    }

    protected BlockState updateConnections(BlockState state, LevelAccessor level, BlockPos pos) {
        return state
                .setValue(CONNECTED_NORTH, shouldConnect(level, pos, Direction.NORTH))
                .setValue(CONNECTED_SOUTH, shouldConnect(level, pos, Direction.SOUTH))
                .setValue(CONNECTED_EAST, shouldConnect(level, pos, Direction.EAST))
                .setValue(CONNECTED_WEST, shouldConnect(level, pos, Direction.WEST))
                .setValue(CONNECTED_UP, shouldConnect(level, pos, Direction.UP))
                .setValue(CONNECTED_DOWN, shouldConnect(level, pos, Direction.DOWN));
    }

    protected boolean shouldConnect(LevelAccessor level, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.relative(direction);
        BlockState neighborState = level.getBlockState(neighborPos);
        return canConnectTo(neighborState);
    }

    protected boolean canConnectTo(BlockState neighborState) {
        return neighborState.getBlock() == this;
    }

    protected void updateNeighborConnections(LevelAccessor level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.getBlock() instanceof ConnectedTextureBlock ctb) {
                level.setBlock(neighborPos, ctb.updateConnections(neighborState, level, neighborPos), 3);
            }
        }
    }

    public static BooleanProperty getConnectionProperty(Direction direction) {
        return switch (direction) {
            case NORTH -> CONNECTED_NORTH;
            case SOUTH -> CONNECTED_SOUTH;
            case EAST -> CONNECTED_EAST;
            case WEST -> CONNECTED_WEST;
            case UP -> CONNECTED_UP;
            case DOWN -> CONNECTED_DOWN;
        };
    }
}
