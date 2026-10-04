package net.bananacheese.witchcraft.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ProceduralEffect(
        String effectTypeId,
        int baseDurationTicks,
        int baseAmplifier,
        float rawElementalDamage
) {
    public static final Codec<ProceduralEffect> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("effect_type").forGetter(ProceduralEffect::effectTypeId),
                    Codec.INT.fieldOf("duration").forGetter(ProceduralEffect::baseDurationTicks),
                    Codec.INT.fieldOf("amplifier").forGetter(ProceduralEffect::baseAmplifier),
                    Codec.FLOAT.fieldOf("raw_damage").forGetter(ProceduralEffect::rawElementalDamage)
            ).apply(instance, ProceduralEffect::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ProceduralEffect> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ProceduralEffect::effectTypeId,
            ByteBufCodecs.VAR_INT, ProceduralEffect::baseDurationTicks,
            ByteBufCodecs.VAR_INT, ProceduralEffect::baseAmplifier,
            ByteBufCodecs.FLOAT, ProceduralEffect::rawElementalDamage,
            ProceduralEffect::new
    );
}
