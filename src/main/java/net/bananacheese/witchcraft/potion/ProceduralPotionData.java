package net.bananacheese.witchcraft.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

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
}