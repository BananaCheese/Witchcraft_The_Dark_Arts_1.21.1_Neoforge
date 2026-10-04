package net.bananacheese.witchcraft.component;

import com.mojang.serialization.Codec;
import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.UUID;

import static net.minecraft.util.datafix.fixes.References.DATA_COMPONENTS;

public final class WTComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, WitchcraftTheDarkArts.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SYRINGE_FILL_LEVEL =
            COMPONENTS.registerComponentType("syringe_fill_level", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> SYRINGE_TARGET_PLAYER =
            COMPONENTS.registerComponentType("syringe_target_player", builder -> builder
                    .persistent(Codec.STRING.xmap(UUID::fromString, UUID::toString))
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SYRINGE_TARGET_NAME =
            COMPONENTS.registerComponentType("syringe_target_name", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ProceduralPotionData>> PROCEDURAL_POTION =
            COMPONENTS.register("procedural_potion", () -> DataComponentType.<ProceduralPotionData>builder()
                    .persistent(ProceduralPotionData.CODEC)
                    .networkSynchronized(ProceduralPotionData.STREAM_CODEC)
                    .build());

    private WTComponents() {}
}
