package net.bananacheese.witchcraft.potion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bananacheese.witchcraft.potion.synthesis.EssenceType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * What a potion does elementally, decided once when it is brewed.
 *
 * @param element the dominant element
 * @param harmful true = deals {@code damage} of the element's type (a "bane"); false = grants a ward/buff (a "boon").
 *                A potion is one or the other, never both.
 * @param damage  base damage before size scaling (0 for boons)
 */
public record ElementalProfile(EssenceType element, boolean harmful, float damage) {

    /** Potions made before the element system existed: no elemental behaviour at all. */
    public static final ElementalProfile NONE = new ElementalProfile(EssenceType.ORDER, false, 0f);

    public static final Codec<ElementalProfile> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.xmap(EssenceType::valueOf, EssenceType::name).fieldOf("element").forGetter(ElementalProfile::element),
                    Codec.BOOL.fieldOf("harmful").forGetter(ElementalProfile::harmful),
                    Codec.FLOAT.fieldOf("damage").forGetter(ElementalProfile::damage)
            ).apply(instance, ElementalProfile::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ElementalProfile> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(EssenceType::valueOf, EssenceType::name), ElementalProfile::element,
            ByteBufCodecs.BOOL, ElementalProfile::harmful,
            ByteBufCodecs.FLOAT, ElementalProfile::damage,
            ElementalProfile::new
    );

    public boolean isNone() {
        return this.equals(NONE);
    }
}
