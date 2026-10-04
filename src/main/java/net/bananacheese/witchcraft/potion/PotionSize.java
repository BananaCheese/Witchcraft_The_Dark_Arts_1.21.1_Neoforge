package net.bananacheese.witchcraft.potion;

public enum PotionSize {
    SMALL(1, 4, 32, 0.5f, 0.75f),
    MEDIUM(2, 2, 16, 1.0f, 1.0f),
    LARGE(4, 1, 8, 1.75f, 1.25f);

    private final int units;
    private final int defaultMaxStack;
    private final int pouchMaxStack;
    private final float durationMultiplier;
    private final float potencyMultiplier;

    PotionSize(int units, int defaultMaxStack, int pouchMaxStack, float durationMultiplier, float potencyMultiplier) {
        this.units = units;
        this.defaultMaxStack = defaultMaxStack;
        this.pouchMaxStack = pouchMaxStack;
        this.durationMultiplier = durationMultiplier;
        this.potencyMultiplier = potencyMultiplier;
    }

    public int getUnits() {
        return units;
    }

    public int getDefaultMaxStack() {
        return defaultMaxStack;
    }

    public int getPouchMaxStack() {
        return pouchMaxStack;
    }

    public float getDurationMultiplier() {
        return durationMultiplier;
    }

    public float getPotencyMultiplier() {
        return potencyMultiplier;
    }
}
