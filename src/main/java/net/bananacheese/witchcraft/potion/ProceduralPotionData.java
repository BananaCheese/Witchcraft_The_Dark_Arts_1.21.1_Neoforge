package net.bananacheese.witchcraft.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.component.WTComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record ProceduralPotionData(
        List<ProceduralEffect> effects,
        PotionSize size,
        int color,
        String particleTypeId
) {
    public static final Codec<ProceduralPotionData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ProceduralEffect.CODEC.listOf().fieldOf("effects").forGetter(ProceduralPotionData::effects),
                    Codec.STRING.xmap(PotionSize::valueOf, PotionSize::name).fieldOf("size").forGetter(ProceduralPotionData::size),
                    Codec.INT.fieldOf("color").forGetter(ProceduralPotionData::color),
                    Codec.STRING.fieldOf("particle_type").forGetter(ProceduralPotionData::particleTypeId)
            ).apply(instance, ProceduralPotionData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ProceduralPotionData> STREAM_CODEC = StreamCodec.composite(
            ProceduralEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), ProceduralPotionData::effects,
            ByteBufCodecs.STRING_UTF8.map(PotionSize::valueOf, PotionSize::name), ProceduralPotionData::size,
            ByteBufCodecs.VAR_INT, ProceduralPotionData::color,
            ByteBufCodecs.STRING_UTF8, ProceduralPotionData::particleTypeId,
            ProceduralPotionData::new
    );

    // ------------------------------------------------------------------------------------
    // Balance knobs for ProceduralEffect.rawElementalDamage (previously unused).
    // Set RAW_DAMAGE_SCALE to 0 to disable the direct-damage component entirely.
    // ------------------------------------------------------------------------------------
    public static final float RAW_DAMAGE_SCALE = 1.0f;
    public static final float RAW_DAMAGE_CAP = 20.0f;

    /** Builds a ready-to-use stack of the given form carrying this data. */
    public ItemStack createStack(PotionForm form) {
        ItemStack stack = new ItemStack(form.item());
        stack.set(WTComponents.PROCEDURAL_POTION.get(), this);
        return stack;
    }

    /**
     * Single source of truth for size-scaled effects. Used by the tooltip, drinking,
     * splashing and the lingering cloud so they can never drift apart.
     * Unknown effect ids are skipped (and logged) rather than crashing.
     */
    public List<MobEffectInstance> toEffectInstances() {
        List<MobEffectInstance> out = new ArrayList<>();
        for (ProceduralEffect e : effects) {
            ResourceLocation id = ResourceLocation.tryParse(e.effectTypeId());
            Holder<MobEffect> holder = id == null ? null : BuiltInRegistries.MOB_EFFECT.getHolder(id).orElse(null);
            if (holder == null) {
                WitchcraftTheDarkArts.LOGGER.warn("Procedural potion references unknown effect '{}'", e.effectTypeId());
                continue;
            }
            int duration = Math.max(1, Math.round(e.baseDurationTicks() * size.getDurationMultiplier()));
            int amplifier = Math.max(0, Math.round(e.baseAmplifier() * size.getPotencyMultiplier()));
            out.add(new MobEffectInstance(holder, duration, amplifier));
        }
        return out;
    }

    /** Total direct (non-effect) damage this potion deals at full intensity. */
    public float totalRawDamage() {
        float sum = 0f;
        for (ProceduralEffect e : effects) sum += e.rawElementalDamage();
        sum *= size.getPotencyMultiplier() * RAW_DAMAGE_SCALE;
        return Math.min(sum, RAW_DAMAGE_CAP);
    }

    /**
     * Applies this potion to a target.
     *
     * @param source    the direct source (the thrown entity, or the drinker); may be null
     * @param owner     the owning entity (thrower / drinker); may be null
     * @param intensity 1.0 = full strength (drinking, direct splash hit); lower for splash falloff
     */
    public void applyTo(LivingEntity target, @Nullable Entity source, @Nullable Entity owner, double intensity) {
        for (MobEffectInstance instance : toEffectInstances()) {
            MobEffect effect = instance.getEffect().value();
            if (effect.isInstantenous()) {
                effect.applyInstantenousEffect(source, owner, target, instance.getAmplifier(), intensity);
            } else {
                int duration = (int) (intensity * instance.getDuration() + 0.5);
                if (duration > 20) {
                    target.addEffect(new MobEffectInstance(instance.getEffect(), duration, instance.getAmplifier()), source);
                }
            }
        }

        float damage = (float) (totalRawDamage() * intensity);
        if (damage > 0f) {
            DamageSource damageSource = source != null
                    ? target.damageSources().indirectMagic(source, owner)
                    : target.damageSources().magic();
            target.hurt(damageSource, damage);
        }
    }

    /** Adapter for vanilla systems (AreaEffectCloud) that only understand PotionContents. */
    public PotionContents toPotionContents() {
        return new PotionContents(Optional.empty(), Optional.of(color), toEffectInstances());
    }

    /** Resolves {@link #particleTypeId()} to a simple particle (flame, snowflake, ...). Falls back to nothing. */
    public Optional<ParticleOptions> particle() {
        ResourceLocation id = ResourceLocation.tryParse(particleTypeId);
        if (id == null) return Optional.empty();
        return BuiltInRegistries.PARTICLE_TYPE.getOptional(id)
                .filter(type -> type instanceof SimpleParticleType)
                .map(type -> (ParticleOptions) type);
    }

    /** Particle used for the splash burst; defaults to witch sparkles if the id is unusable. */
    public ParticleOptions particleOrDefault() {
        return particle().orElse(ParticleTypes.WITCH);
    }
}