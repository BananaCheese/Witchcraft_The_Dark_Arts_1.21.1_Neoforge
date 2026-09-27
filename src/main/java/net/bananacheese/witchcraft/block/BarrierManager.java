package net.bananacheese.witchcraft.block;

import net.bananacheese.witchcraft.block.custom.BarrierFieldBlock;
import net.bananacheese.witchcraft.block.custom.DarkBarrierBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BarrierManager {
    private static final int BARRIER_RANGE = 2;

    private static final Map<BlockPos, Set<BlockPos>> BARRIER_SOURCES = new HashMap<>();

    public static void updateBarriers(Level level, BlockPos sourcePos, BlockState sourceState) {
        if (level.isClientSide) return;

        removeBarriersForSource(level, sourcePos);

        Set<BlockPos> controlledFields = new HashSet<>();

        for (Direction direction : Direction.values()) {
            boolean isActive = DarkBarrierBlock.isBarrierActive(sourceState, direction);
            if (isActive) {
                controlledFields.addAll(createBarrierInDirection(level, sourcePos, direction));
            }
        }

        if (!controlledFields.isEmpty()) {
            BARRIER_SOURCES.put(sourcePos, controlledFields);
        }
    }

    private static Set<BlockPos> createBarrierInDirection(Level level, BlockPos sourcePos, Direction direction) {
        Set<BlockPos> createdFields = new HashSet<>();

        for (int distance = 1; distance <= BARRIER_RANGE; distance++) {
            BlockPos fieldPos = sourcePos.relative(direction, distance);

            if (level.getBlockState(fieldPos).isAir() ||
                    level.getBlockState(fieldPos).getBlock() instanceof BarrierFieldBlock) {
                level.setBlock(fieldPos, WTBlocksRef.barrierField(), 3);
                createdFields.add(fieldPos);
            }
        }

        return createdFields;
    }

    private static void removeBarriersForSource(Level level, BlockPos sourcePos) {
        Set<BlockPos> controlledFields = BARRIER_SOURCES.remove(sourcePos);
        if (controlledFields != null) {
            for (BlockPos fieldPos : controlledFields) {
                if (level.getBlockState(fieldPos).getBlock() instanceof BarrierFieldBlock) {
                    boolean stillControlled = false;
                    for (Set<BlockPos> otherFields : BARRIER_SOURCES.values()) {
                        if (otherFields.contains(fieldPos)) {
                            stillControlled = true;
                            break;
                        }
                    }

                    if (!stillControlled) {
                        level.setBlock(fieldPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    public static void removeAllBarriers(Level level, BlockPos sourcePos) {
        removeBarriersForSource(level, sourcePos);
    }

    /**
     * Tiny indirection so this class doesn't need a direct static import of
     * WTBlocks (avoids a circular-looking dependency at a glance) — same
     * pattern as the original's DABlocks.BARRIER_FIELD reference, just
     * routed through one extra method.
     */
    private static final class WTBlocksRef {
        static BlockState barrierField() {
            return net.bananacheese.witchcraft.block.WTBlocks.BARRIER_FIELD.get().defaultBlockState();
        }
    }
}
