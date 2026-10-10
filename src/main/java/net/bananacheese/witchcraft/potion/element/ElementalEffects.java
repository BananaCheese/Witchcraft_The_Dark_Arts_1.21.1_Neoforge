package net.bananacheese.witchcraft.potion.element;

import net.bananacheese.witchcraft.init.WTEffects;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.bananacheese.witchcraft.potion.synthesis.EssenceType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * What elemental potions actually do to a target: typed damage, element-vs-element interactions,
 * status side effects, and Chaos's random outcomes.
 *
 * <p>Everything here is a tunable constant on purpose - this is the balance sheet.
 */
public final class ElementalEffects {

    // ---- Chaos
    /** Chance a Chaos potion does the OPPOSITE of what it was brewed as (a bane helps, a boon hurts). */
    public static final float CHAOS_FLIP_CHANCE = 0.25f;

    // ---- Impact (thrown potions only)
    public static final float FIRE_BLAST_CHANCE = 0.30f;

    // ---- Interactions (damage multipliers)
    public static final float FIRE_VS_FROZEN = 1.30f;      // melts frozen targets (and thaws them)
    public static final float FIRE_VS_WET = 0.70f;         // steam: weaker against wet targets
    public static final float ICE_VS_BURNING = 1.25f;      // thermal shock (and puts the fire out)
    public static final float ICE_VS_WET = 1.25f;          // flash freeze
    public static final float LIGHTNING_VS_WET = 1.50f;    // water conducts
    public static final float POISON_VS_POISONED = 1.25f;  // venom compounds
    public static final float VITALITY_VS_FROZEN = 1.50f;  // shatter (and thaws them)
    public static final float ORDER_BONUS_PER_DISPEL = 0.10f;
    public static final float ORDER_MAX_DISPEL_BONUS = 0.50f;

    private ElementalEffects() {
    }

    // ======================================================================== banes

    /** Apply a harmful potion's elemental half to one target. */
    public static void applyHarm(ProceduralPotionData data, LivingEntity target, @Nullable Entity source,
                                 @Nullable Entity owner, double intensity) {
        EssenceType element = data.profile().element();
        RandomSource random = target.getRandom();

        if (element == EssenceType.CHAOS) {
            if (random.nextFloat() < CHAOS_FLIP_CHANCE) {
                chaosBoon(data, target, owner, intensity, random);   // lucky: the bane turned out kind
            } else {
                chaosBane(data, target, source, owner, intensity, random);
            }
            return;
        }

        float damage = data.scaledDamage() * (float) intensity;
        damage *= 1.0f + (random.nextFloat() * 2.0f - 1.0f) * element.getDamageVariance();
        damage = applyInteractions(element, target, damage);

        if (damage > 0.0f) {
            DamageSource damageSource = ElementalDamageTypes.source(target.level(), element, source, owner);
            target.hurt(damageSource, damage);
        }
        if (target.isAlive()) {
            afterHit(element, target, data, intensity);
        }
    }

    /**
     * Element-vs-state interactions. May change the target (thaw, extinguish, dispel) as a side effect.
     *
     * @return the adjusted damage
     */
    private static float applyInteractions(EssenceType element, LivingEntity target, float damage) {
        boolean wet = target.isInWaterRainOrBubble();

        switch (element) {
            case FIRE -> {
                if (target.getTicksFrozen() > 0) {
                    damage *= FIRE_VS_FROZEN;
                    target.setTicksFrozen(0);
                }
                if (wet) damage *= FIRE_VS_WET;
            }
            case ICE -> {
                if (target.isOnFire()) {
                    damage *= ICE_VS_BURNING;
                    target.clearFire();
                }
                if (wet) damage *= ICE_VS_WET;
            }
            case LIGHTNING -> {
                if (wet) damage *= LIGHTNING_VS_WET;
            }
            case POISON -> {
                if (target.getType().is(EntityTypeTags.UNDEAD)) return 0.0f; // the dead do not mind
                Holder<MobEffect> poison = vanilla("poison");
                if (poison != null && target.hasEffect(poison)) damage *= POISON_VS_POISONED;
            }
            case VITALITY -> {
                if (target.isFullyFrozen()) {
                    damage *= VITALITY_VS_FROZEN;
                    target.setTicksFrozen(0);
                }
            }
            case ORDER -> {
                int dispelled = dispel(target);
                damage *= 1.0f + Math.min(ORDER_MAX_DISPEL_BONUS, ORDER_BONUS_PER_DISPEL * dispelled);
            }
            default -> {
            }
        }
        return damage;
    }

    /** Arcane damage strips beneficial effects (wards included). Returns how many were removed. */
    private static int dispel(LivingEntity target) {
        List<Holder<MobEffect>> beneficial = target.getActiveEffects().stream()
                .filter(instance -> instance.getEffect().value().isBeneficial())
                .map(MobEffectInstance::getEffect)
                .toList();
        for (Holder<MobEffect> effect : beneficial) {
            target.removeEffect(effect);
        }
        return beneficial.size();
    }

    /** Status effects that follow the damage. */
    private static void afterHit(EssenceType element, LivingEntity target, ProceduralPotionData data, double intensity) {
        switch (element) {
            case FIRE -> {
                int ticks = (int) (80 * data.size().getPotencyMultiplier() * intensity);
                if (ticks > 0 && !target.fireImmune()) {
                    target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), ticks));
                }
            }
            case ICE -> {
                if (target.canFreeze()) {
                    int frozen = target.getTicksFrozen() + (int) (100 * intensity);
                    target.setTicksFrozen(Math.min(frozen, target.getTicksRequiredToFreeze() + 40));
                }
            }
            case VITALITY -> {
                // The spikes jab upward.
                target.push(0.0, 0.35, 0.0);
                target.hurtMarked = true;
            }
            default -> {
            }
        }
    }

    // ======================================================================== boons

    /** Apply a beneficial potion's elemental half. Only Chaos has one (its wards are ordinary effects). */
    public static void applyBoon(ProceduralPotionData data, LivingEntity target, @Nullable Entity owner, double intensity) {
        if (data.profile().element() != EssenceType.CHAOS) return;

        RandomSource random = target.getRandom();
        if (random.nextFloat() < CHAOS_FLIP_CHANCE) {
            chaosBane(data, target, null, owner, intensity, random);   // unlucky: the boon bites
        } else {
            chaosBoon(data, target, owner, intensity, random);
        }
    }

    // ======================================================================== chaos

    private static void chaosBane(ProceduralPotionData data, LivingEntity target, @Nullable Entity source,
                                  @Nullable Entity owner, double intensity, RandomSource random) {
        float base = data.scaledDamage() * (float) intensity;
        int ticks = Math.max(40, (int) (160 * data.size().getDurationMultiplier() * intensity));
        float potency = data.size().getPotencyMultiplier();
        int roll = random.nextInt(100);

        if (roll < 20) {
            hurtChaos(target, source, owner, base * (0.4f + random.nextFloat() * 1.2f));
            announce(target, "Wild!");
        } else if (roll < 35) {
            hurtChaos(target, source, owner, base * 2.0f);
            announce(target, "Critical chaos!");
        } else if (roll < 55) {
            launch(target, 1.1 * potency, random);
            announce(target, "Launched!");
        } else if (roll < 65) {
            teleport(target, random);
            announce(target, "Warped!");
        } else if (roll < 75) {
            give(target, owner, vanilla("levitation"), 100, 0);
            announce(target, "Floating!");
        } else if (roll < 85) {
            give(target, owner, vanilla("nausea"), ticks, 0);
            announce(target, "Dizzy!");
        } else if (roll < 93) {
            target.level().explode(owner, target.getX(), target.getY(0.5), target.getZ(), 1.6f,
                    Level.ExplosionInteraction.NONE);
            announce(target, "Boom!");
        } else {
            give(target, owner, WTEffects.CHAOS_TINY, ticks * 2, 0);
            announce(target, "Tiny!");
        }
    }

    private static void chaosBoon(ProceduralPotionData data, LivingEntity target, @Nullable Entity owner,
                                  double intensity, RandomSource random) {
        int ticks = Math.max(40, (int) (200 * data.size().getDurationMultiplier() * intensity));
        float potency = data.size().getPotencyMultiplier();
        int roll = random.nextInt(100);

        if (roll < 20) {
            launch(target, 0.9 * potency, random);
            give(target, owner, vanilla("slow_falling"), ticks * 2, 0);
            announce(target, "Skyward!");
        } else if (roll < 40) {
            give(target, owner, vanilla("jump_boost"), ticks, 4);
            announce(target, "Springy!");
        } else if (roll < 55) {
            give(target, owner, vanilla("speed"), ticks, 2);
            announce(target, "Hasty!");
        } else if (roll < 70) {
            give(target, owner, WTEffects.CHAOS_GIANT, ticks * 2, 0);
            announce(target, "Giant!");
        } else if (roll < 80) {
            target.heal(6.0f * potency);
            announce(target, "Mended!");
        } else if (roll < 90) {
            teleport(target, random);
            announce(target, "Warped!");
        } else {
            give(target, owner, WTEffects.CHAOS_TINY, ticks * 2, 0);
            announce(target, "Tiny!");
        }
    }

    private static void hurtChaos(LivingEntity target, @Nullable Entity source, @Nullable Entity owner, float damage) {
        if (damage > 0.0f) {
            target.hurt(ElementalDamageTypes.source(target.level(), EssenceType.CHAOS, source, owner), damage);
        }
    }

    private static void launch(LivingEntity target, double strength, RandomSource random) {
        Vec3 motion = target.getDeltaMovement();
        target.setDeltaMovement(
                motion.x + (random.nextDouble() - 0.5) * 0.4,
                0.9 + strength * 0.6,
                motion.z + (random.nextDouble() - 0.5) * 0.4);
        target.hurtMarked = true; // makes the server send the new velocity to players
    }

    private static void teleport(LivingEntity target, RandomSource random) {
        Level level = target.level();
        for (int attempt = 0; attempt < 16; attempt++) {
            double x = target.getX() + (random.nextDouble() - 0.5) * 16.0;
            double z = target.getZ() + (random.nextDouble() - 0.5) * 16.0;
            double y = Mth.clamp(target.getY() + (random.nextInt(16) - 8),
                    level.getMinBuildHeight(), level.getMaxBuildHeight() - 1);
            if (target.randomTeleport(x, y, z, true)) {
                level.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
                return;
            }
        }
    }

    // ======================================================================== impact (thrown potions)

    /** One-off effects at the point a harmful potion shatters (not per target). */
    public static void onImpact(ProceduralPotionData data, ServerLevel level, Vec3 pos, @Nullable Entity owner) {
        if (!data.profile().harmful()) return;
        RandomSource random = level.getRandom();

        switch (data.profile().element()) {
            case FIRE -> {
                if (random.nextFloat() < FIRE_BLAST_CHANCE) {
                    level.explode(owner, pos.x, pos.y + 0.2, pos.z, 1.1f, Level.ExplosionInteraction.NONE);
                }
            }
            case LIGHTNING -> level.playSound(null, BlockPos.containing(pos),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.6f, 1.4f);
            case VITALITY -> {
                // Placeholder for the spike VFX: a burst of pointed-dripstone shards.
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                        pos.x, pos.y + 0.1, pos.z, 30, 0.9, 0.2, 0.9, 0.1);
                level.playSound(null, BlockPos.containing(pos),
                        SoundEvents.POINTED_DRIPSTONE_LAND, SoundSource.PLAYERS, 1.0f, 0.8f);
            }
            default -> {
            }
        }
    }

    // ======================================================================== helpers

    @Nullable
    private static Holder<MobEffect> vanilla(String path) {
        return BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse("minecraft:" + path)).orElse(null);
    }

    private static void give(LivingEntity target, @Nullable Entity owner, @Nullable Holder<MobEffect> effect,
                             int ticks, int amplifier) {
        if (effect != null) {
            target.addEffect(new MobEffectInstance(effect, ticks, amplifier), owner);
        }
    }

    /** Tell players what Chaos just did to them; part of the fun is working it out. */
    private static void announce(LivingEntity target, String text) {
        if (target instanceof Player player) {
            player.displayClientMessage(Component.literal("\u00A7d" + text), true);
        }
    }
}
