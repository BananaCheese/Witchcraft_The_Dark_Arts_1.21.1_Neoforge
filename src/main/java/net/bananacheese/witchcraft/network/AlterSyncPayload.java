package net.bananacheese.witchcraft.network;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public record AlterSyncPayload(BlockPos pos, ItemStack heldItem, FluidStack fluid) implements CustomPacketPayload {

    public static final Type<AlterSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(WitchcraftTheDarkArts.MODID, "alter_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlterSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    AlterSyncPayload::pos,
                    ItemStack.OPTIONAL_STREAM_CODEC,
                    AlterSyncPayload::heldItem,
                    FluidStack.OPTIONAL_STREAM_CODEC,
                    AlterSyncPayload::fluid,
                    AlterSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}