package net.bananacheese.witchcraft.potion.synthesis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record EssenceData(Map<EssenceType, Float> values) {

    public static final Codec<EssenceData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(
                            Codec.STRING.xmap(
                                    s -> EssenceType.valueOf(s.toUpperCase()),
                                    EssenceType::name
                            ),
                            Codec.FLOAT
                    ).fieldOf("essences").forGetter(EssenceData::values)
            ).apply(instance, EssenceData::new)
    );
}
