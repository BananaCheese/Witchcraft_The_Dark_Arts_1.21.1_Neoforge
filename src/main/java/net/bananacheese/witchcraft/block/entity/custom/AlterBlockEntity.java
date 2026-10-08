package net.bananacheese.witchcraft.block.entity.custom;

import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.bananacheese.witchcraft.network.AlterSyncPayload;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class AlterBlockEntity extends BlockEntity {
    public static final int MAX_FLUID = 1000;

    private ItemStack heldItem = ItemStack.EMPTY;
    private FluidStack fluid = FluidStack.EMPTY;
    private int creationTicks = 0;
    private boolean powered = false;

    private final IFluidHandler fluidHandler = new AlterFluidHandler();

    public AlterBlockEntity(BlockPos pos, BlockState state) {
        super(WTBlockEntities.ALTER_BE.get(), pos, state);
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public void setHeldItemClient(ItemStack stack) {
        this.heldItem = stack;
    }

    public void setHeldItem(ItemStack stack) {
        this.heldItem = stack;
        setChanged();
        syncToClient();
    }

    public FluidStack getFluid() {
        return fluid;
    }

    public int getFluidAmount() {
        return fluid.getAmount();
    }

    public boolean addFluid(int amount) {
        if (amount <= 0) {
            return false;
        }

        return fillFluid(
                new FluidStack(net.minecraft.world.level.material.Fluids.WATER, amount),
                IFluidHandler.FluidAction.EXECUTE) > 0;
    }

    public boolean consumeFluid(int amount) {
        if (amount <= 0) {
            return false;
        }

        return drainFluid(amount, IFluidHandler.FluidAction.EXECUTE).getAmount() == amount;
    }

    public boolean wasPowered() {
        return powered;
    }

    public void setPowered(boolean powered) {
        this.powered = powered;
    }

    public IFluidHandler getFluidHandler() {
        return fluidHandler;
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

    public void startCreationAnimation(int ticks) {
        this.creationTicks = ticks;
        setChanged();
        syncToClient();
    }

    public boolean isCreating() {
        return creationTicks > 0;
    }

    private int fillFluid(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }

        if (!fluid.isEmpty()
                && !FluidStack.isSameFluidSameComponents(fluid, resource)) {
            return 0;
        }

        int amount = Math.min(resource.getAmount(), MAX_FLUID - fluid.getAmount());

        if (amount <= 0) {
            return 0;
        }

        if (action.simulate()) {
            return amount;
        }

        if (fluid.isEmpty()) {
            fluid = resource.copyWithAmount(amount);
        } else {
            fluid.grow(amount);
        }

        setChanged();
        syncToClient();
        return amount;
    }

    private FluidStack drainFluid(int amount, IFluidHandler.FluidAction action) {
        if (amount <= 0 || fluid.isEmpty()) {
            return FluidStack.EMPTY;
        }

        int drainedAmount = Math.min(amount, fluid.getAmount());
        FluidStack drained = fluid.copyWithAmount(drainedAmount);

        if (action.simulate()) {
            return drained;
        }

        fluid.shrink(drainedAmount);
        if (fluid.isEmpty()) {
            fluid = FluidStack.EMPTY;
        }

        setChanged();
        syncToClient();
        return drained;
    }

    private FluidStack drainFluid(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty()
                || fluid.isEmpty()
                || !FluidStack.isSameFluidSameComponents(fluid, resource)) {
            return FluidStack.EMPTY;
        }

        return drainFluid(resource.getAmount(), action);
    }

    private void syncToClient() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        PacketDistributor.sendToPlayersTrackingChunk(
                serverLevel,
                new net.minecraft.world.level.ChunkPos(worldPosition),
                new AlterSyncPayload(worldPosition, heldItem.copy(), fluid.copy()));
    }

    public void setFluidClient(FluidStack stack) {
        fluid = stack.copy();
    }

    /** CLIENT: applies a full sync from the server (held item + fluid). */
    public void setClientData(ItemStack heldItem, FluidStack fluid) {
        this.heldItem = heldItem.copy();
        this.fluid = fluid.copy();
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        creationTicks = tag.getInt("CreationTicks");
        powered = tag.getBoolean("Powered");

        if (tag.contains("HeldItem")) {
            heldItem = ItemStack.parseOptional(registries, tag.getCompound("HeldItem"));
        } else {
            heldItem = ItemStack.EMPTY;
        }

        if (tag.contains("Fluid")) {
            fluid = FluidStack.parseOptional(registries, tag.getCompound("Fluid"));
        } else {
            fluid = FluidStack.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putInt("CreationTicks", creationTicks);
        tag.putBoolean("Powered", powered);

        if (!heldItem.isEmpty()) {
            tag.put("HeldItem", heldItem.save(registries));
        }

        if (!fluid.isEmpty()) {
            tag.put("Fluid", fluid.save(registries));
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

    private class AlterFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? fluid.copy() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? MAX_FLUID : 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && !stack.isEmpty();
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return fillFluid(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return drainFluid(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return drainFluid(maxDrain, action);
        }
    }
}
