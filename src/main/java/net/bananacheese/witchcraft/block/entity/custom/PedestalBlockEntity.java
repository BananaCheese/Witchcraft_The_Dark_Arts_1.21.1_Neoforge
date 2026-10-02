package net.bananacheese.witchcraft.block.entity.custom;

import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PedestalBlockEntity extends BlockEntity {
    private ItemStack heldItem = ItemStack.EMPTY;
    private int creationTicks = 0;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(WTBlockEntities.PEDESTAL_BE.get(), pos, state);
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(ItemStack stack) {
        this.heldItem = stack;
        setChanged();
        syncToClient();
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }

        if (creationTicks > 0) {
            creationTicks--;

            if (creationTicks == 0) {
                setChanged();
                syncToClient();
            }
        }
    }

    private void syncToClient() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        level.sendBlockUpdated(
                worldPosition,
                getBlockState(),
                getBlockState(),
                Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);

        ClientboundBlockEntityDataPacket packet =
                ClientboundBlockEntityDataPacket.create(this);

        ChunkPos chunkPos = new ChunkPos(worldPosition);

        serverLevel.getChunkSource().chunkMap
                .getPlayers(chunkPos, false)
                .forEach(player -> player.connection.send(packet));
    }

    @Override
    public void onDataPacket(
            Connection connection,
            ClientboundBlockEntityDataPacket packet,
            HolderLookup.Provider registries) {

        super.onDataPacket(connection, packet, registries);

        if (level != null && level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS | Block.UPDATE_IMMEDIATE);
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