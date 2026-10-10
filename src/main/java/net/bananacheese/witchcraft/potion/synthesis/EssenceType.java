package net.bananacheese.witchcraft.potion.synthesis;

/**
 * The seven elements. Each one defines how a potion of that element behaves when it is
 * HARMFUL (typed damage + a support debuff) and when it is BENEFICIAL (a resistance "ward" + a support buff).
 *
 * <p>Support effects are vanilla effect ids; an empty string means "none". The element's own damage and
 * its ward are handled by {@code potion.element.ElementalEffects} and {@code init.WTEffects}.
 *
 * <p>Names (FIRE, ORDER, ...) are used in the essence data files, so they must not be renamed.
 */
public enum EssenceType {
    //        colour    harmful support           beneficial support        harmLean variance label        damage label  hint
    FIRE      (0xFF3300, "",                      "minecraft:strength",     0.65f,   0.15f,  "Fire",      "Fire",       "Strong vs frozen, weak vs wet; may explode"),
    ICE       (0x0099FF, "minecraft:slowness",    "minecraft:resistance",   0.50f,   0.15f,  "Ice",       "Frost",      "Strong vs burning and wet; chills"),
    LIGHTNING (0xFFFF00, "",                      "minecraft:speed",        0.55f,   0.20f,  "Lightning", "Lightning",  "Strong vs wet targets"),
    POISON    (0x00CC33, "minecraft:poison",      "",                       0.80f,   0.15f,  "Poison",    "Poison",     "Strong vs poisoned; harmless to undead"),
    VITALITY  (0xFF3366, "",                      "minecraft:regeneration", 0.30f,   0.15f,  "Vitality",  "Physical",   "Shatters frozen targets"),
    ORDER     (0x9933FF, "minecraft:weakness",    "minecraft:absorption",   0.35f,   0.10f,  "Arcane",    "Arcane",     "Ignores armor; dispels beneficial effects"),
    CHAOS     (0xCC0000, "",                      "",                       0.50f,   0.60f,  "Chaos",     "Chaos",      "Unpredictable");

    private final int defaultColor;
    private final String offensiveEffect;
    private final String beneficialEffect;
    private final float harmLean;
    private final float damageVariance;
    private final String label;
    private final String damageLabel;
    private final String hint;

    EssenceType(int defaultColor, String offensiveEffect, String beneficialEffect, float harmLean,
                float damageVariance, String label, String damageLabel, String hint) {
        this.defaultColor = defaultColor;
        this.offensiveEffect = offensiveEffect;
        this.beneficialEffect = beneficialEffect;
        this.harmLean = harmLean;
        this.damageVariance = damageVariance;
        this.label = label;
        this.damageLabel = damageLabel;
        this.hint = hint;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    /** Vanilla support effect added to HARMFUL potions of this element ("" = none). */
    public String getOffensiveEffect() {
        return offensiveEffect;
    }

    /** Vanilla support effect added to BENEFICIAL potions of this element ("" = none). */
    public String getBeneficialEffect() {
        return beneficialEffect;
    }

    /**
     * How strongly this element pulls a brew toward being harmful (0 = always helpful, 1 = always harmful).
     * Brews average this over their essences and then roll against it with the world-seeded RNG.
     */
    public float harmLean() {
        return harmLean;
    }

    /** +/- fraction applied to this element's damage every hit (Chaos is wildly swingy). */
    public float getDamageVariance() {
        return damageVariance;
    }

    /** Player-facing element name (ORDER shows as "Arcane"). */
    public String getLabel() {
        return label;
    }

    /** Player-facing damage name, e.g. "Frost", "Physical". */
    public String getDamageLabel() {
        return damageLabel;
    }

    /** One-line description of this element's interactions, shown in tooltips of harmful potions. */
    public String getHint() {
        return hint;
    }

    /** Chaos has no ward: its beneficial side is a random lucky outcome instead. */
    public boolean hasWard() {
        return this != CHAOS;
    }

    /** Registry path of this element's ward effect, e.g. "fire_ward". */
    public String wardName() {
        return name().toLowerCase() + "_ward";
    }

    /** Full effect id of this element's ward, e.g. "wctda:fire_ward". */
    public String wardEffectId() {
        return "wctda:" + wardName();
    }
}
