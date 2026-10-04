package net.bananacheese.witchcraft.network;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record PedestalSyncPayload(BlockPos pos, ItemStack stack) implements CustomPacketPayload {

    public static final Type<PedestalSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(WitchcraftTheDarkArts.MODID, "pedestal_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PedestalSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    PedestalSyncPayload::pos,
                    ItemStack.OPTIONAL_STREAM_CODEC,
                    PedestalSyncPayload::stack,
                    PedestalSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
