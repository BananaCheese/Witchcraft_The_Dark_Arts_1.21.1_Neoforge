package net.bananacheese.witchcraft.block.custom;

import net.minecraft.util.StringRepresentable;

public enum CauldronPart implements StringRepresentable {
    NORTH_WEST("north_west", 0, 0),
    NORTH_EAST("north_east", 1, 0),
    SOUTH_WEST("south_west", 0, 1),
    SOUTH_EAST("south_east", 1, 1);

    public final int dx;
    public final int dz;
    private final String serializedName;

    CauldronPart(String serializedName, int dx, int dz) {
        this.serializedName = serializedName;
        this.dx = dx;
        this.dz = dz;
    }

    public boolean isController() {
        return this == NORTH_WEST;
    }

    public static CauldronPart of(int dx, int dz) {
        for (CauldronPart part : values()) {
            if (part.dx == dx && part.dz == dz) return part;
        }
        throw new IllegalArgumentException("Bad cauldron part offset " + dx + "," + dz);
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
