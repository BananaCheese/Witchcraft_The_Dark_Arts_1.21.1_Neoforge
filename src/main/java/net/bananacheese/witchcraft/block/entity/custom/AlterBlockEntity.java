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

public class AlterBlockEntity extends BlockEntity {
    private ItemStack heldItem = ItemStack.EMPTY;
    private int fluidAmount = 0;
    private static final int MAX_FLUID = 1000;
    private int creationTicks = 0;

    public AlterBlockEntity(BlockPos pos, BlockState state) {
        super(WTBlockEntities.ALTER_BE.get(), pos, state);
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(ItemStack stack) {
        this.heldItem = stack;
        setChanged();
    }

    public int getFluidAmount() {
        return fluidAmount;
    }

    public boolean addFluid(int amount) {
        if (fluidAmount >= MAX_FLUID) {
            return false;
        }
        fluidAmount = Math.min(MAX_FLUID, fluidAmount + amount);
        setChanged();
        return true;
    }

    public boolean consumeFluid(int amount) {
        if (fluidAmount < amount) {
            return false;
        }
        fluidAmount -= amount;
        setChanged();
        return true;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (creationTicks > 0) {
            creationTicks--;
            if (creationTicks == 0) {
                setChanged();
            }
        }
    }

    public void startCreationAnimation(int ticks) {
        this.creationTicks = ticks;
    }

    public boolean isCreating() {
        return creationTicks > 0;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fluidAmount = tag.getInt("FluidAmount");
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
        tag.putInt("FluidAmount", fluidAmount);
        tag.putInt("CreationTicks", creationTicks);
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
        return saveWithoutMetadata(registries);
    }
}