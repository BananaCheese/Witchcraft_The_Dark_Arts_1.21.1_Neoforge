package net.bananacheese.witchcraft.block.entity.custom;

import net.bananacheese.witchcraft.block.custom.DarkCauldron;
import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.network.DarkCauldronFluidSyncPayload;
import net.bananacheese.witchcraft.potion.PotionSize;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.bananacheese.witchcraft.potion.synthesis.EssenceDataLoader;
import net.bananacheese.witchcraft.potion.synthesis.EssenceType;
import net.bananacheese.witchcraft.potion.synthesis.SynthesisEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DarkCauldronBlockEntity extends BlockEntity {

    public boolean isFilled = false;
    private FluidStack fluid = FluidStack.EMPTY;
    private int heatTicks = 0;
    private static final int BOIL_THRESHOLD = 100; // 5 seconds of heat to boil
    private final List<ItemStack> insertedIngredients = new ArrayList<>();

    public DarkCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(WTBlockEntities.DARK_CAULDRON_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DarkCauldronBlockEntity cauldron) {
        if (!cauldron.isFilled) return;

        boolean heated = DarkCauldron.isHeatedUnderneath(level, pos);
        if (heated) {
            if (cauldron.heatTicks < BOIL_THRESHOLD) {
                cauldron.heatTicks++;
            }
        } else {
            if (cauldron.heatTicks > 0) {
                cauldron.heatTicks--;
            }
        }
    }

    public boolean isBoiling() {
        return heatTicks >= BOIL_THRESHOLD;
    }

    public void onPlayerInteract(Player player, net.minecraft.world.InteractionHand hand, ItemStack heldItem) {
        if (level == null || level.isClientSide()) return;

        // 1. Fill vessel with Water Bucket
        if (!isFilled && heldItem.is(Items.WATER_BUCKET)) {
            isFilled = true;
            if (!player.isCreative()) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
            setChanged();
            return;
        }

        // 2. Add Ingredient (up to 5 items)
        if (isFilled && isBoiling() && insertedIngredients.size() < 5 && !heldItem.isEmpty()) {
            ItemStack added = heldItem.split(1);
            insertedIngredients.add(added);
            setChanged();
            return;
        }

        // 3. Extract Completed Potion with Flask/Glass Bottle
        if (isFilled && isBoiling() && !insertedIngredients.isEmpty() && heldItem.is(Items.GLASS_BOTTLE)) {
            // Safely retrieve world seed from ServerLevel
            long worldSeed = (level instanceof ServerLevel serverLevel) ? serverLevel.getSeed() : 0L;

            // Combine essence weights
            Map<EssenceType, Float> essencePool = EssenceDataLoader.combineIngredients(insertedIngredients);

            // Synthesize using world seed
            ProceduralPotionData synthesizedData = SynthesisEngine.synthesize(essencePool, worldSeed, PotionSize.MEDIUM);

            // Create potion stack
            ItemStack potionResult = new ItemStack(Items.POTION); // Or your custom ProceduralPotionItem
            potionResult.set(WTComponents.PROCEDURAL_POTION.get(), synthesizedData);

            heldItem.shrink(1);
            if (!player.getInventory().add(potionResult)) {
                player.drop(potionResult, false);
            }

            // Reset Kettle state
            insertedIngredients.clear();
            isFilled = false;
            heatTicks = 0;
            setChanged();
        }
    }

    public int getFluidColor() {
        if (!this.isFilled) {
            return 0xFFFFFFFF; // White / default
        }

        // 1. If no ingredients are added yet, render standard water color (with biome tinting)
        if (this.insertedIngredients.isEmpty()) {
            if (this.level != null) {
                IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(Fluids.WATER);
                int waterTint = ext.getTintColor(Fluids.WATER.defaultFluidState(), this.level, this.worldPosition);

                // Ensure full opacity (Alpha = 255)
                return waterTint | 0xFF000000;
            }
            return 0xFF3F76E4; // Standard Minecraft water blue fallback
        }

        // 2. If ingredients exist in the boiling water, blend their essence colors dynamically
        Map<EssenceType, Float> essencePool = EssenceDataLoader.combineIngredients(this.insertedIngredients);
        if (essencePool.isEmpty()) {
            return 0xFF3F76E4;
        }

        float totalWeight = 0f;
        float r = 0, g = 0, b = 0;

        for (Map.Entry<EssenceType, Float> entry : essencePool.entrySet()) {
            float weight = entry.getValue();
            totalWeight += weight;
            int color = entry.getKey().getDefaultColor();

            r += ((color >> 16) & 0xFF) * weight;
            g += ((color >> 8) & 0xFF) * weight;
            b += (color & 0xFF) * weight;
        }

        if (totalWeight <= 0f) return 0xFF3F76E4;

        int finalR = Math.min(255, Math.round(r / totalWeight));
        int finalG = Math.min(255, Math.round(g / totalWeight));
        int finalB = Math.min(255, Math.round(b / totalWeight));

        // Return ARGB format with 100% opacity (0xFF000000)
        return (0xFF << 24) | (finalR << 16) | (finalG << 8) | finalB;
    }

    public void setFluidClient(FluidStack stack) {
        fluid = stack.copy();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DarkCauldronBlockEntity entity) {
        if (!entity.isFilled || !entity.isBoiling()) return;

        RandomSource rand = level.getRandom();

        // 1. Spawn bubble and steam particles on top of fluid surface
        if (rand.nextFloat() < 0.35f) {
            double px = pos.getX() + 0.2D + (rand.nextDouble() * 0.6D);
            double py = pos.getY() + 0.82D; // Slightly above fluid line
            double pz = pos.getZ() + 0.2D + (rand.nextDouble() * 0.6D);

            // Water bubble popping at surface
            level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.BUBBLE_POP,
                    px, py, pz, 0.0D, 0.02D, 0.0D
            );

            // Gentle ambient steam rising
            if (rand.nextBoolean()) {
                level.addParticle(
                        net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        px, py, pz, 0.0D, 0.03D, 0.0D
                );
            }
        }

        // 2. Play ambient boiling sound periodically
        if (rand.nextFloat() < 0.05f) {
            level.playLocalSound(
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    net.minecraft.sounds.SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    0.25F, 1.0F, false
            );
        }
    }

    public void syncToTrackingClients() {
        if (this.level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(
                    serverLevel,
                    new ChunkPos(this.worldPosition),
                    new DarkCauldronFluidSyncPayload(this.worldPosition, this.isFilled, this.isBoiling(), new ArrayList<>(this.insertedIngredients))
            );
        }
    }

    /**
     * Called on the client side when packet is received
     */
    public void setClientData(boolean isFilled, boolean isBoiling, List<ItemStack> ingredients) {
        this.isFilled = isFilled;
        this.insertedIngredients.clear();
        this.insertedIngredients.addAll(ingredients);

        // Force a re-render on the client
        if (this.level != null && this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("IsFilled", isFilled);
        tag.putInt("HeatTicks", heatTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.isFilled = tag.getBoolean("IsFilled");
        this.heatTicks = tag.getInt("HeatTicks");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncToClient() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
