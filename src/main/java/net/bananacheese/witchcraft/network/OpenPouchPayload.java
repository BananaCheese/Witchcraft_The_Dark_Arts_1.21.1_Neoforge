package net.bananacheese.witchcraft.network;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenPouchPayload() implements CustomPacketPayload {

    public static final Type<OpenPouchPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(WitchcraftTheDarkArts.MODID, "open_pouch"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenPouchPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenPouchPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}