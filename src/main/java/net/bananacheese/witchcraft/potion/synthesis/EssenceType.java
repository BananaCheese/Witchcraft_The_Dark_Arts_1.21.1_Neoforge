package net.bananacheese.witchcraft.potion.synthesis;

public enum EssenceType {
    FIRE(0xFF3300, "minecraft:instant_damage", "minecraft:strength"),
    ICE(0x0099FF, "minecraft:slowness", "minecraft:resistance"),
    LIGHTNING(0xFFFF00, "minecraft:speed", "minecraft:haste"),
    POISON(0x00CC33, "minecraft:poison", "minecraft:weakness"),
    VITALITY(0xFF3366, "minecraft:instant_health", "minecraft:regeneration"),
    ORDER(0x9933FF, "minecraft:absorption", "minecraft:resistance"),
    CHAOS(0xCC0000, "minecraft:wither", "minecraft:nausea");

    private final int defaultColor;
    private final String offensiveEffect;
    private final String beneficialEffect;

    EssenceType(int defaultColor, String offensiveEffect, String beneficialEffect) {
        this.defaultColor = defaultColor;
        this.offensiveEffect = offensiveEffect;
        this.beneficialEffect = beneficialEffect;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public String getOffensiveEffect() {
        return offensiveEffect;
    }

    public String getBeneficialEffect() {
        return beneficialEffect;
    }
}
