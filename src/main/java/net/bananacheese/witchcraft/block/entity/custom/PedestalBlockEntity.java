package net.bananacheese.witchcraft.block.entity.custom;

import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
        if (level == null || level.isClientSide) {
            return;
        }

        level.sendBlockUpdated(
                worldPosition,
                getBlockState(),
                getBlockState(),
                3);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        
        creationTicks = tag.getInt("CreationTicks");

        if (tag.contains("HeldItem")) {
            heldItem = ItemStack.parseOptional(registries, tag.getCompound("HeldItem"));

        } else {
            heldItem = ItemStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }
}