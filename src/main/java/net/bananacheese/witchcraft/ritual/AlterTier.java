package net.bananacheese.witchcraft.ritual;

import net.bananacheese.witchcraft.block.WTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum AlterTier {
    TIER_1(1, "Basic Altar") {
        @Override
        public boolean isStructureValid(Level level, BlockPos pos) {
            return true;
        }
    },

    TIER_2(2, "Enhanced Altar") {
        @Override
        public boolean isStructureValid(Level level, BlockPos pos) {
            return check(level, pos, new int[][]{
                    {0, -1, 0},
                    {1, -1, 0},
                    {-1, -1, 0},
                    {0, -1, 1},
                    {0, -1, -1}
            }, Blocks.BLACKSTONE);
        }
    },

    TIER_3(3, "Ritualic Altar") {
        @Override
        public boolean isStructureValid(Level level, BlockPos pos) {
            boolean ring = check(level, pos, new int[][]{
                    {0, -1, 0},
                    {1, -1, 0},
                    {-1, -1, 0},
                    {0, -1, 1},
                    {0, -1, -1},
                    {1, -1, 1},
                    {1, -1, -1},
                    {-1, -1, 1},
                    {-1, -1, -1},
                    {2, -1, 2},
                    {-2, -1, 2},
                    {2, -1, -2},
                    {-2, -1, -2}
            }, Blocks.BLACKSTONE);

            boolean pedestals = check(level, pos, new int[][]{
                    {2, 0, 2},
                    {-2, 0, 2},
                    {2, 0, -2},
                    {-2, 0, -2}
            }, WTBlocks.PEDESTAL.get());

            return ring && pedestals;
        }
    },

    TIER_4(4, "Supreme Altar") {
        @Override
        public boolean isStructureValid(Level level, BlockPos pos) {
            boolean ring = check(level, pos, new int[][]{
                    {1, -1, 0},
                    {-1, -1, 0},
                    {0, -1, 1},
                    {0, -1, -1},
                    {1, -1, 1},
                    {1, -1, -1},
                    {-1, -1, 1},
                    {-1, -1, -1},
                    {2, -1, 0},
                    {-2, -1, 0},
                    {0, -1, 2},
                    {0, -1, -2},
                    {3, -1, 0},
                    {-3, -1, 0},
                    {0, -1, 3},
                    {0, -1, -3},
                    {2, -1, 2},
                    {2, -1, -2},
                    {-2, -1, 2},
                    {-2, -1, -2}
            }, Blocks.BLACKSTONE);

            boolean pedestals = check(level, pos, new int[][]{
                    {3, 0, 0},
                    {-3, 0, 0},
                    {0, 0, 3},
                    {0, 0, -3},
                    {2, 0, 2},
                    {2, 0, -2},
                    {-2, 0, 2},
                    {-2, 0, -2}
            }, WTBlocks.PEDESTAL.get());

            return ring && pedestals;
        }
    };

    private final int level;
    private final String displayName;

    AlterTier(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public abstract boolean isStructureValid(Level level, BlockPos pos);

    private static boolean check(
            Level level,
            BlockPos center,
            int[][] offsets,
            Block expected) {
        for (int[] offset : offsets) {
            BlockPos checkPos = center.offset(
                    offset[0], offset[1], offset[2]);

            if (!level.getBlockState(checkPos).is(expected)) {
                return false;
            }
        }

        return true;
    }

    public static AlterTier getTierByLevel(int level) {
        for (AlterTier tier : values()) {
            if (tier.level == level) {
                return tier;
            }
        }

        return TIER_1;
    }

    public static AlterTier getHighestValidTier(Level level, BlockPos pos) {
        AlterTier[] tiers = values();

        for (int i = tiers.length - 1; i >= 0; i--) {
            if (tiers[i].isStructureValid(level, pos)) {
                return tiers[i];
            }
        }

        return TIER_1;
    }
}
