package net.bananacheese.witchcraft.network;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

public record AlterFluidSyncPayload(BlockPos pos, FluidStack fluid) implements CustomPacketPayload {

    public static final Type<AlterFluidSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(WitchcraftTheDarkArts.MODID, "alter_fluid_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlterFluidSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    AlterFluidSyncPayload::pos,
                    FluidStack.OPTIONAL_STREAM_CODEC,
                    AlterFluidSyncPayload::fluid,
                    AlterFluidSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}