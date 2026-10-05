package net.bananacheese.witchcraft.block.entity.custom;

import net.bananacheese.witchcraft.block.custom.DarkCauldron;
import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.item.WTItems;
import net.bananacheese.witchcraft.network.DarkCauldronFluidSyncPayload;
import net.bananacheese.witchcraft.potion.PotionSize;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.bananacheese.witchcraft.potion.synthesis.EssenceData;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DarkCauldronBlockEntity extends BlockEntity {

    public boolean isFilled = false;
    public boolean clientIsBoiling =  false;
    private int heatTicks = 0;
    private static final int BOIL_THRESHOLD = 100;
    private final List<ItemStack> insertedIngredients = new ArrayList<>();

    public DarkCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(WTBlockEntities.DARK_CAULDRON_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DarkCauldronBlockEntity cauldron) {
        if (!cauldron.isFilled) return;

        // 1. Heat logic
        boolean previouslyBoiling = cauldron.isBoiling();
        boolean heated = DarkCauldron.isHeatedUnderneath(level, pos);

        if (heated) {
            if (cauldron.heatTicks < BOIL_THRESHOLD) cauldron.heatTicks++;
        } else {
            if (cauldron.heatTicks > 0) cauldron.heatTicks--;
        }

        boolean currentlyBoiling = cauldron.isBoiling();
        if (previouslyBoiling != currentlyBoiling) {
            cauldron.setChanged();
            cauldron.syncToClient();
        }

        // DEBUG: Print heat progression every 20 ticks (1 sec)
        if (level.getGameTime() % 20 == 0) {
            System.out.println("[Cauldron Debug] isFilled: " + cauldron.isFilled
                    + " | heated: " + heated
                    + " | heatTicks: " + cauldron.heatTicks
                    + " | isBoiling: " + currentlyBoiling);
        }

        // 2. Ingest ItemEntities floating inside or near the cauldron basin
        if (currentlyBoiling) {
            AABB basinBox = new AABB(
                    pos.getX() + 0.0625D, pos.getY() + 0.125D, pos.getZ() + 0.0625D,
                    pos.getX() + 0.9375D, pos.getY() + 1.25D,  pos.getZ() + 0.9375D
            );

            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, basinBox);
            if (!items.isEmpty()) {
                System.out.println("[Cauldron Debug] Found " + items.size() + " ItemEntity(s) inside basin box!");
            }

            for (ItemEntity itemEntity : items) {
                if (!itemEntity.isAlive()) continue;

                ItemStack stack = itemEntity.getItem();
                boolean valid = cauldron.isValidIngredient(stack);
                System.out.println("[Cauldron Debug] Checking item: " + stack.getHoverName().getString() + " | isValidIngredient: " + valid);

                if (valid) {
                    ItemStack singleIngredient = stack.copyWithCount(1);
                    cauldron.insertedIngredients.add(singleIngredient);

                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        itemEntity.discard();
                    }

                    level.playSound(
                            null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS,
                            0.5F, 1.2F + (level.random.nextFloat() * 0.4F)
                    );

                    cauldron.setChanged();
                    cauldron.syncToClient();
                    System.out.println("[Cauldron Debug] ABSORBED ITEM SUCCESSFULLY! Current ingredients count: " + cauldron.insertedIngredients.size());
                    break;
                }
            }
        }
    }

    public boolean isBoiling() {
        if (this.level != null && this.level.isClientSide()) {
            return this.clientIsBoiling;
        }
        return this.isFilled && this.heatTicks >= BOIL_THRESHOLD;
    }

    public void onPlayerInteract(Player player, InteractionHand hand, ItemStack heldItem) {
        if (level == null || level.isClientSide()) return;

        if (!isFilled && heldItem.is(Items.WATER_BUCKET)) {
            this.isFilled = true;
            this.heatTicks = 0;

            if (!player.isCreative()) {
                heldItem.shrink(1);

                ItemStack emptyBucket = new ItemStack(Items.BUCKET);
                if (heldItem.isEmpty()) {
                    player.setItemInHand(hand, emptyBucket);
                } else if (!player.getInventory().add(emptyBucket)) {
                    player.drop(emptyBucket, false);
                }
            }

            setChanged();
            syncToClient();
            return;
        }

        if (isFilled && heldItem.is(Items.BUCKET) && insertedIngredients.isEmpty()) {
            this.isFilled = false;
            this.heatTicks = 0;

            if (!player.isCreative()) {
                heldItem.shrink(1);
                ItemStack waterBucket = new ItemStack(Items.WATER_BUCKET);
                if (heldItem.isEmpty()) {
                    player.setItemInHand(hand, waterBucket);
                } else if (!player.getInventory().add(waterBucket)) {
                    player.drop(waterBucket, false);
                }
            }

            setChanged();
            syncToClient();
            return;
        }

        // Right-clicking with a Glass Bottle to finish brewing
        if (this.isFilled && this.isBoiling() && heldItem.is(Items.GLASS_BOTTLE) && !this.insertedIngredients.isEmpty()) {

            long seed = (this.level instanceof ServerLevel serverLevel) ? serverLevel.getSeed() : 0L;

            // Combine essences via EssenceDataLoader
            Map<EssenceType, Float> essencePool = EssenceDataLoader.combineIngredients(this.insertedIngredients);

            // Pass pool + world seed to SynthesisEngine
            ProceduralPotionData potionData = SynthesisEngine.synthesize(essencePool, seed, PotionSize.MEDIUM);

            ItemStack resultPotion = new ItemStack(WTItems.PROCEDURAL_POTION.get());
            resultPotion.set(WTComponents.PROCEDURAL_POTION, potionData);

            if (!player.isCreative()) {
                heldItem.shrink(1);
            }

            ItemHandlerHelper.giveItemToPlayer(player, resultPotion);

            // Reset cauldron state
            this.insertedIngredients.clear();
            this.isFilled = false;
            this.heatTicks = 0;

            level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

            setChanged();
            syncToClient();
        }
    }

    public boolean isValidIngredient(ItemStack stack) {
        if (this.insertedIngredients.size() >= 5) return false;
        if (stack.isEmpty()) return false;

        // Direct check against EssenceDataLoader registry or fallback item map
        EssenceData data = EssenceDataLoader.getEssenceData(stack);
        return data != null && !data.values().isEmpty();
    }

    public int getFluidColor() {
        if (!this.isFilled) return 0xFFFFFFFF;

        // Default water color if no ingredients inserted yet
        if (this.insertedIngredients.isEmpty()) {
            if (this.level != null) {
                IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(Fluids.WATER);
                int tint = ext.getTintColor(Fluids.WATER.defaultFluidState(), this.level, this.worldPosition);
                return tint | 0xFF000000; // Force 100% alpha
            }
            return 0xFF3F76E4;
        }

        // Use your loader's combineIngredients helper!
        Map<EssenceType, Float> essencePool = EssenceDataLoader.combineIngredients(this.insertedIngredients);
        if (essencePool.isEmpty()) return 0xFF3F76E4;

        float totalWeight = 0f;
        float r = 0, g = 0, b = 0;

        for (Map.Entry<EssenceType, Float> entry : essencePool.entrySet()) {
            float weight = entry.getValue();
            totalWeight += weight;
            int color = entry.getKey().getDefaultColor(); // Ensure EssenceType returns ARGB integer

            r += ((color >> 16) & 0xFF) * weight;
            g += ((color >> 8) & 0xFF) * weight;
            b += (color & 0xFF) * weight;
        }

        if (totalWeight <= 0f) return 0xFF3F76E4;

        int finalR = Math.min(255, Math.round(r / totalWeight));
        int finalG = Math.min(255, Math.round(g / totalWeight));
        int finalB = Math.min(255, Math.round(b / totalWeight));

        return (0xFF << 24) | (finalR << 16) | (finalG << 8) | finalB;
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DarkCauldronBlockEntity entity) {
        if (!entity.isFilled || !entity.isBoiling()) return;

        RandomSource rand = level.getRandom();

        if (rand.nextFloat() < 0.35f) {
            double px = pos.getX() + 0.2D + (rand.nextDouble() * 0.6D);
            double py = pos.getY() + 0.82D; // Slightly above fluid line
            double pz = pos.getZ() + 0.2D + (rand.nextDouble() * 0.6D);

            level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.BUBBLE_POP,
                    px, py, pz, 0.0D, 0.02D, 0.0D
            );

            if (rand.nextBoolean()) {
                level.addParticle(
                        net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        px, py, pz, 0.0D, 0.03D, 0.0D
                );
            }
        }

        if (rand.nextFloat() < 0.05f) {
            level.playLocalSound(
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    net.minecraft.sounds.SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    0.25F, 1.0F, false
            );
        }
    }

    private void syncToClient() {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }

        PacketDistributor.sendToPlayersTrackingChunk(
                serverLevel,
                new net.minecraft.world.level.ChunkPos(worldPosition),
                new DarkCauldronFluidSyncPayload(this.worldPosition, this.isFilled, this.isBoiling(), new ArrayList<>(this.insertedIngredients)));
    }

    public void setClientData(boolean isFilled, boolean isBoiling, List<ItemStack> ingredients) {
        this.isFilled = isFilled;
        this.clientIsBoiling = isBoiling;
        this.insertedIngredients.clear();
        this.insertedIngredients.addAll(ingredients);

        if (this.level != null && this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
            this.level.setBlocksDirty(this.worldPosition, getBlockState(), getBlockState());
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
}
