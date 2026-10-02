package net.bananacheese.witchcraft.block.entity.custom;

import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.bananacheese.witchcraft.network.PedestalSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class PedestalBlockEntity extends BlockEntity {
    private ItemStack heldItem = ItemStack.EMPTY;
    private int creationTicks = 0;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(WTBlockEntities.PEDESTAL_BE.get(), pos, state);
    }

    public ItemStack getHeldItem() {
        if (level != null && level.isClientSide) {
            System.out.println(
                    "[WCTDA] CLIENT pedestal " +
                            worldPosition +
                            " heldItem = " +
                            heldItem);
        }

        return heldItem;
    }

    public void setHeldItem(ItemStack stack) {
        this.heldItem = stack;
        setChanged();

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(
                    serverLevel,
                    new net.minecraft.world.level.ChunkPos(worldPosition),
                    new PedestalSyncPayload(
                            worldPosition,
                            heldItem.copy()));
        }
    }

    public void setHeldItemClient(ItemStack stack) {
        this.heldItem = stack;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }

        if (creationTicks > 0) {
            creationTicks--;

            if (creationTicks == 0) {
                setChanged();
            }
        }
    }

    @Override
    public void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet,
            HolderLookup.Provider registries) {

        super.onDataPacket(connection, packet, registries);

        if (level != null && level.isClientSide) {
            System.out.println(
                    "[WCTDA] STANDARD pedestal packet received at " +
                            worldPosition +
                            " -> heldItem = " +
                            heldItem);
        }
    }

    @Override
    protected void loadAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries) {

        super.loadAdditional(tag, registries);

        creationTicks = tag.getInt("CreationTicks");

        if (tag.contains("HeldItem")) {
            heldItem = ItemStack.parseOptional(
                    registries,
                    tag.getCompound("HeldItem"));
        } else {
            heldItem = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag,
            HolderLookup.Provider registries) {

        super.saveAdditional(tag, registries);

        if (!heldItem.isEmpty()) {
            tag.put("HeldItem", heldItem.save(registries));
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(
            HolderLookup.Provider registries) {

        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }
}