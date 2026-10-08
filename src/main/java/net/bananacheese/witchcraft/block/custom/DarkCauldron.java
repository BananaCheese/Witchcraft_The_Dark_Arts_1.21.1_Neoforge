package net.bananacheese.witchcraft.block.custom;

import net.bananacheese.witchcraft.block.entity.custom.DarkCauldronBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DarkCauldron extends Block implements EntityBlock {
    public static final EnumProperty<CauldronPart> PART = EnumProperty.create("part", CauldronPart.class);

    /** How many of the four blocks under the cauldron must be heat sources for it to boil. */
    public static final int REQUIRED_HEAT_SOURCES = 1;

    private static final VoxelShape[] PART_SHAPES = createPartShapes();

    public DarkCauldron(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PART, CauldronPart.NORTH_WEST));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
    }

    // ------------------------------------------------------------------ shapes

    /** The original 1x1 cauldron outline (unchanged). */
    private static VoxelShape createSingleShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.125, 0.125, 0.125, 0.875, 0.1875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.125, 0.1875, 0.125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 0.1875, 0.1875, 0.125, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.125, 0.25, 0.125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0, 0.8125, 0.1875, 0.125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.8125, 0.25, 0.125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.0625, 0.75, 0.1875, 0.125, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0, 0.8125, 0.875, 0.125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.0625, 0.75, 0.875, 0.125, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.0625, 0.8125, 0.8125, 0.125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0, 0.125, 0.875, 0.125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.75, 0.0625, 0.125, 0.8125, 0.125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.0625, 0.1875, 0.875, 0.125, 0.25), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.3125, 0.0625, 0.875, 0.875, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.1875, 0.125, 0.8125, 0.3125, 0.1875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0.3125, 0.125, 0.9375, 0.875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.1875, 0.125, 0.875, 0.3125, 0.8125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.3125, 0.875, 0.875, 0.875, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.1875, 0.8125, 0.875, 0.3125, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 0.3125, 0.125, 0.125, 0.875, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.1875, 0.1875, 0.1875, 0.3125, 0.875), BooleanOp.OR);
        return shape;
    }

    /**
     * Builds the outline for each quarter by stretching the 1x1 outline to 2x2 (X/Z doubled, height kept,
     * matching the placeholder model), then slicing out each block's own 1x1 column.
     * If you change the big model, change this (or hand-write the four shapes) to match.
     */
    private static VoxelShape[] createPartShapes() {
        VoxelShape[] full = {Shapes.empty()};
        createSingleShape().forAllBoxes((x1, y1, z1, x2, y2, z2) ->
                full[0] = Shapes.or(full[0], Shapes.box(x1 * 2, y1, z1 * 2, x2 * 2, y2, z2 * 2)));

        VoxelShape[] shapes = new VoxelShape[CauldronPart.values().length];
        for (CauldronPart part : CauldronPart.values()) {
            VoxelShape column = Shapes.box(part.dx, 0, part.dz, part.dx + 1, 1, part.dz + 1);
            shapes[part.ordinal()] = Shapes.join(full[0], column, BooleanOp.AND).move(-part.dx, 0, -part.dz);
        }
        return shapes;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PART_SHAPES[state.getValue(PART).ordinal()];
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return PART_SHAPES[state.getValue(PART).ordinal()];
    }

    // ------------------------------------------------------------------ multiblock geometry

    /** The controller (north-west) block of the cauldron that {@code pos} belongs to. */
    public static BlockPos controllerPos(BlockPos pos, CauldronPart part) {
        return pos.offset(-part.dx, 0, -part.dz);
    }

    // ------------------------------------------------------------------ placement / removal

    /**
     * The 2x2 footprint extends away from the player (diagonally), so it lands "in front" of them.
     * Returns null (placement refused) unless all four spots are free.
     */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos origin = context.getClickedPos();

        int stepX;
        int stepZ;
        Player player = context.getPlayer();
        if (player != null) {
            Vec3 look = player.getLookAngle();
            stepX = direction(look.x, player.getX(), origin.getX() + 0.5);
            stepZ = direction(look.z, player.getZ(), origin.getZ() + 0.5);
        } else {
            stepX = context.getHorizontalDirection().getStepX() != 0 ? context.getHorizontalDirection().getStepX() : 1;
            stepZ = context.getHorizontalDirection().getStepZ() != 0 ? context.getHorizontalDirection().getStepZ() : 1;
        }

        for (int dx : new int[]{0, stepX}) {
            for (int dz : new int[]{0, stepZ}) {
                BlockPos p = origin.offset(dx, 0, dz);
                if (level.isOutsideBuildHeight(p) || !level.getBlockState(p).canBeReplaced(context)) {
                    return null;
                }
            }
        }

        // The placed block is the controller's +X / +Z neighbour whenever the footprint extends toward -X / -Z.
        CauldronPart part = CauldronPart.of(stepX > 0 ? 0 : 1, stepZ > 0 ? 0 : 1);
        return defaultBlockState().setValue(PART, part);
    }

    /** Look direction wins; if the player is looking along the other axis, fall back to which side they stand on. */
    private static int direction(double look, double playerPos, double blockCenter) {
        if (look > 0.15) return 1;
        if (look < -0.15) return -1;
        return playerPos <= blockCenter ? 1 : -1;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        BlockPos controller = controllerPos(pos, state.getValue(PART));
        for (CauldronPart part : CauldronPart.values()) {
            BlockPos other = controller.offset(part.dx, 0, part.dz);
            if (!other.equals(pos)) {
                level.setBlock(other, defaultBlockState().setValue(PART, part), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide) {
            CauldronPart part = state.getValue(PART);

            // Spill whatever was floating in the brew when the controller goes.
            if (part.isController() && level.getBlockEntity(pos) instanceof DarkCauldronBlockEntity kettle) {
                kettle.dropIngredients();
            }

            // Remove the other three quarters. They are set to air with drops suppressed, so breaking any
            // one block yields exactly one cauldron item (from the block that was actually broken).
            BlockPos controller = controllerPos(pos, part);
            for (CauldronPart other : CauldronPart.values()) {
                BlockPos otherPos = controller.offset(other.dx, 0, other.dz);
                if (otherPos.equals(pos)) continue;

                BlockState otherState = level.getBlockState(otherPos);
                if (otherState.is(this) && otherState.getValue(PART) == other) {
                    level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // ------------------------------------------------------------------ block entity / heat / interaction

    /** Only the controller has a block entity. */
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(PART).isController() ? new DarkCauldronBlockEntity(pos, state) : null;
    }

    private static boolean isHeatSource(BlockState state) {
        return state.is(Blocks.FIRE) ||
                state.is(Blocks.SOUL_FIRE) ||
                state.is(Blocks.CAMPFIRE) ||
                state.is(Blocks.SOUL_CAMPFIRE) ||
                state.is(Blocks.MAGMA_BLOCK) ||
                state.is(Blocks.LAVA);
    }

    /**
     * True if enough of the four blocks directly beneath the cauldron's footprint are heat sources.
     *
     * @param controllerPos the controller (north-west) block's position
     */
    public static boolean isHeatedUnderneath(Level level, BlockPos controllerPos) {
        int sources = 0;
        for (CauldronPart part : CauldronPart.values()) {
            if (isHeatSource(level.getBlockState(controllerPos.offset(part.dx, -1, part.dz)))) {
                sources++;
            }
        }
        return sources >= REQUIRED_HEAT_SOURCES;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // Any of the four quarters forwards to the controller's block entity.
        BlockPos controller = controllerPos(pos, state.getValue(PART));
        if (level.getBlockEntity(controller) instanceof DarkCauldronBlockEntity kettle) {
            if (!level.isClientSide()) {
                kettle.onPlayerInteract(player, hand, stack);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!state.getValue(PART).isController()) return null;

        return level.isClientSide()
                ? (lbl, pos, st, be) -> DarkCauldronBlockEntity.clientTick(lbl, pos, st, (DarkCauldronBlockEntity) be)
                : (lbl, pos, st, be) -> DarkCauldronBlockEntity.serverTick(lbl, pos, st, (DarkCauldronBlockEntity) be);
    }
}