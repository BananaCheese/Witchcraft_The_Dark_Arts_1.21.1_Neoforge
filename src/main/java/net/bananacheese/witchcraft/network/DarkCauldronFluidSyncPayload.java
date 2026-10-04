package net.bananacheese.witchcraft.network;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record DarkCauldronFluidSyncPayload(BlockPos pos, boolean isFilled, boolean isBoiling, List<ItemStack> ingredients) implements CustomPacketPayload {

    public static final Type<DarkCauldronFluidSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(WitchcraftTheDarkArts.MODID, "dark_cauldron_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, DarkCauldronFluidSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    DarkCauldronFluidSyncPayload::pos,
                    ByteBufCodecs.BOOL,
                    DarkCauldronFluidSyncPayload::isFilled,
                    ByteBufCodecs.BOOL,
                    DarkCauldronFluidSyncPayload::isBoiling,
                    ItemStack.OPTIONAL_LIST_STREAM_CODEC,
                    DarkCauldronFluidSyncPayload::ingredients,
                    DarkCauldronFluidSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}