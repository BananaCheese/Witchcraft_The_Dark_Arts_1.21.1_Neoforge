package net.bananacheese.witchcraft.potion.synthesis;

import net.bananacheese.witchcraft.potion.PotionSize;
import net.bananacheese.witchcraft.potion.ProceduralEffect;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.minecraft.util.RandomSource;

import java.util.*;

public class SynthesisEngine {

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

        // 3. Determine Alignment (Beneficial vs Negative/Harmful)
        float chaosWeight = essenceInputs.getOrDefault(EssenceType.CHAOS, 0f);
        float orderWeight = essenceInputs.getOrDefault(EssenceType.ORDER, 0f);
        boolean isHarmful = (chaosWeight + random.nextFloat() * 2.0f) > (orderWeight + 1.0f);

        // 4. Generate Effects
        List<ProceduralEffect> generatedEffects = new ArrayList<>();

        // A. Dominant Primary Effect
        if (dominantEssence != null) {
            String effectId = isHarmful ? dominantEssence.getOffensiveEffect() : dominantEssence.getBeneficialEffect();

            // Duration & Potency scaling calculations
            int baseTicks = calculateBaseDuration(totalWeight, maxWeight, random);
            int baseAmplifier = calculateBaseAmplifier(maxWeight, totalWeight);
            float rawDamage = isHarmful ? (maxWeight * 1.5f) : 0f;

            generatedEffects.add(new ProceduralEffect(effectId, baseTicks, baseAmplifier, rawDamage));
        }

        // B. Secondary Trait Effects (Triggered if secondary essences meet thresholds)
        for (Map.Entry<EssenceType, Float> entry : essenceInputs.entrySet()) {
            EssenceType type = entry.getKey();
            float weight = entry.getValue();

            if (type != dominantEssence && weight >= 2.0f) {
                String secEffectId = isHarmful ? type.getOffensiveEffect() : type.getBeneficialEffect();
                int secTicks = (int) (calculateBaseDuration(weight, weight, random) * 0.5f);
                int secAmp = Math.max(0, calculateBaseAmplifier(weight, totalWeight) - 1);

                generatedEffects.add(new ProceduralEffect(secEffectId, secTicks, secAmp, 0f));
            }
        }

        // 5. Calculate Blended Color
        int blendedColor = blendEssenceColors(essenceInputs, totalWeight);

        // 6. Particle Identifier
        String particleId = selectParticleEffect(dominantEssence, isHarmful);

        return new ProceduralPotionData(generatedEffects, targetSize, blendedColor, particleId);
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