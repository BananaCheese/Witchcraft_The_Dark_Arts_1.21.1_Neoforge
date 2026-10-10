package net.bananacheese.witchcraft.potion.synthesis;

import net.bananacheese.witchcraft.potion.ElementalProfile;
import net.bananacheese.witchcraft.potion.PotionSize;
import net.bananacheese.witchcraft.potion.ProceduralEffect;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.minecraft.util.RandomSource;

import java.util.*;

public class SynthesisEngine {

    /** Secondary essences below this weight add nothing; above it they add their support effect / ward. */
    private static final float SECONDARY_THRESHOLD = 2.0f;
    /** At most this many secondary elements contribute, so a kitchen-sink brew cannot become a wall of wards. */
    private static final int MAX_SECONDARIES = 2;

    public static ProceduralPotionData synthesize(Map<EssenceType, Float> rawInputs, long worldSeed, PotionSize targetSize) {
        if (rawInputs == null || rawInputs.isEmpty()) {
            return new ProceduralPotionData(List.of(), targetSize, 0x3F3F3F, "minecraft:bubble");
        }

        // Normalise to an EnumMap so iteration order is always ordinal order. HashMap with enum keys
        // iterates in identity-hash order, which changes between JVM runs and would make the
        // "deterministic per seed" hash, dominant-essence tie-break and RNG draw order unstable.
        Map<EssenceType, Float> essenceInputs = new EnumMap<>(EssenceType.class);
        essenceInputs.putAll(rawInputs);

        // 1. Calculate Essence Statistics
        float totalWeight = 0f;
        EssenceType dominantEssence = null;
        float maxWeight = -1f;

        for (Map.Entry<EssenceType, Float> entry : essenceInputs.entrySet()) {
            float weight = entry.getValue();
            totalWeight += weight;
            if (weight > maxWeight) {
                maxWeight = weight;
                dominantEssence = entry.getKey();
            }
        }

        // 2. Deterministic Seed Randomization
        // Mix world seed with the exact essence signature to keep formula outcomes static per seed
        long essenceSignature = computeEssenceHash(essenceInputs);
        long synthesisSeed = worldSeed ^ essenceSignature;
        RandomSource random = RandomSource.create(synthesisSeed);

        // 3. Alignment: bane (harmful) or boon (beneficial) - decided once, here, so a potion either
        // deals elemental damage or grants a ward, never both.
        // Each element has a "lean"; the brew's lean is the weight-average of its essences, and the
        // world-seeded roll decides. Pure Vitality brews are usually boons, pure Poison brews usually banes,
        // and anything in between can go either way - but always the same way for the same seed and recipe.
        float lean = 0f;
        for (Map.Entry<EssenceType, Float> entry : essenceInputs.entrySet()) {
            lean += entry.getValue() * entry.getKey().harmLean();
        }
        lean /= Math.max(totalWeight, 0.0001f);
        boolean isHarmful = random.nextFloat() < lean;

        return build(essenceInputs, totalWeight, dominantEssence, maxWeight, isHarmful, random, targetSize);
    }

    /**
     * A fixed, repeatable potion of one element, for the creative tab and testing. Not world-seeded.
     */
    public static ProceduralPotionData sample(EssenceType element, boolean harmful, PotionSize size) {
        Map<EssenceType, Float> inputs = new EnumMap<>(EssenceType.class);
        inputs.put(element, 8.0f);
        RandomSource random = RandomSource.create(element.ordinal() * 31L + (harmful ? 1L : 0L));
        return build(inputs, 8.0f, element, 8.0f, harmful, random, size);
    }

    private static ProceduralPotionData build(Map<EssenceType, Float> essenceInputs, float totalWeight,
                                              EssenceType dominantEssence, float maxWeight, boolean isHarmful,
                                              RandomSource random, PotionSize targetSize) {
        List<ProceduralEffect> generatedEffects = new ArrayList<>();

        // A. Dominant element: its ward / support effect.
        if (dominantEssence != null) {
            int baseTicks = calculateBaseDuration(totalWeight, maxWeight, random);
            int baseAmplifier = calculateBaseAmplifier(maxWeight, totalWeight);
            addElementEffects(generatedEffects, dominantEssence, isHarmful, baseTicks, baseAmplifier);
        }

        // B. Secondary elements (heaviest first, ties by ordinal so it stays deterministic).
        List<Map.Entry<EssenceType, Float>> secondaries = new ArrayList<>();
        for (Map.Entry<EssenceType, Float> entry : essenceInputs.entrySet()) {
            if (entry.getKey() != dominantEssence && entry.getValue() >= SECONDARY_THRESHOLD) {
                secondaries.add(entry);
            }
        }
        secondaries.sort((a, b) -> {
            int byWeight = Float.compare(b.getValue(), a.getValue());
            return byWeight != 0 ? byWeight : a.getKey().compareTo(b.getKey());
        });

        for (Map.Entry<EssenceType, Float> entry : secondaries.subList(0, Math.min(MAX_SECONDARIES, secondaries.size()))) {
            float weight = entry.getValue();
            int secTicks = (int) (calculateBaseDuration(weight, weight, random) * 0.5f);
            int secAmp = Math.max(0, calculateBaseAmplifier(weight, totalWeight) - 1);
            addElementEffects(generatedEffects, entry.getKey(), isHarmful, secTicks, secAmp);
        }

        // C. Elemental profile: the dominant element decides the damage type; banes carry the damage itself.
        float damage = isHarmful ? (maxWeight * 1.5f) : 0f;
        ElementalProfile profile = new ElementalProfile(
                dominantEssence != null ? dominantEssence : EssenceType.ORDER, isHarmful, damage);

        // D. Colour and particle
        int blendedColor = blendEssenceColors(essenceInputs, totalWeight);
        String particleId = selectParticleEffect(dominantEssence, isHarmful);

        return new ProceduralPotionData(generatedEffects, targetSize, blendedColor, particleId, profile);
    }

    /** Banes add the element's support debuff; boons add its ward plus its support buff. Duplicates are skipped. */
    private static void addElementEffects(List<ProceduralEffect> effects, EssenceType type, boolean harmful,
                                          int ticks, int amplifier) {
        if (harmful) {
            addUnique(effects, type.getOffensiveEffect(), ticks, amplifier);
        } else {
            if (type.hasWard()) {
                addUnique(effects, type.wardEffectId(), ticks, amplifier);
            }
            addUnique(effects, type.getBeneficialEffect(), ticks, amplifier);
        }
    }

    private static void addUnique(List<ProceduralEffect> effects, String effectId, int ticks, int amplifier) {
        if (effectId == null || effectId.isEmpty()) return;
        for (ProceduralEffect existing : effects) {
            if (existing.effectTypeId().equals(effectId)) return;
        }
        // rawElementalDamage is no longer used: damage now lives in the ElementalProfile.
        effects.add(new ProceduralEffect(effectId, ticks, amplifier, 0f));
    }

    private static long computeEssenceHash(Map<EssenceType, Float> inputs) {
        long hash = 7L;
        for (Map.Entry<EssenceType, Float> entry : inputs.entrySet()) {
            hash = 31L * hash + entry.getKey().ordinal();
            hash = 31L * hash + Float.floatToIntBits(entry.getValue());
        }
        return hash;
    }

    private static int calculateBaseDuration(float weight, float primaryWeight, RandomSource random) {
        // Base duration: 10 seconds (200 ticks) to 60 seconds (1200 ticks) scaled logarithmically by weight
        float durationScalar = (float) Math.log10(1.0f + weight) * 300.0f;
        float variance = (random.nextFloat() - 0.5f) * 40.0f; // +/- 2 seconds random variation per seed
        return Math.max(100, Math.round(200.0f + durationScalar + variance));
    }

    private static int calculateBaseAmplifier(float primaryWeight, float totalWeight) {
        // High concentrations of dominant essence unlock higher effect amplifiers (Level 1, 2, 3...)
        float concentration = primaryWeight / Math.max(1.0f, totalWeight);
        if (primaryWeight >= 10.0f && concentration > 0.6f) return 2; // Level III
        if (primaryWeight >= 5.0f && concentration > 0.4f) return 1;  // Level II
        return 0;                                                      // Level I
    }

    /** Weighted RGB average of the essence colours (no alpha). Public so the cauldron can preview it. */
    public static int blendEssenceColors(Map<EssenceType, Float> inputs, float totalWeight) {
        if (totalWeight <= 0f) return 0x3F76E4;
        float r = 0, g = 0, b = 0;

        for (Map.Entry<EssenceType, Float> entry : inputs.entrySet()) {
            float weightFactor = entry.getValue() / totalWeight;
            int color = entry.getKey().getDefaultColor();

            r += ((color >> 16) & 0xFF) * weightFactor;
            g += ((color >> 8) & 0xFF) * weightFactor;
            b += (color & 0xFF) * weightFactor;
        }

        return ((Math.round(r) & 0xFF) << 16) | ((Math.round(g) & 0xFF) << 8) | (Math.round(b) & 0xFF);
    }

    private static String selectParticleEffect(EssenceType dominant, boolean isHarmful) {
        if (dominant == null) return "minecraft:bubble";
        return switch (dominant) {
            case FIRE -> "minecraft:flame";
            case ICE -> "minecraft:snowflake";
            case LIGHTNING -> "minecraft:electric_spark";
            case POISON -> "minecraft:spore_blossom_dust";
            case VITALITY -> "minecraft:heart";
            case ORDER -> "minecraft:enchanted_hit";
            case CHAOS -> "minecraft:witch";
        };
    }
}
