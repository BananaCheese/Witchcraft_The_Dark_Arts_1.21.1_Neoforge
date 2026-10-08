package net.bananacheese.witchcraft.block.entity.custom;

import net.bananacheese.witchcraft.block.custom.DarkCauldron;
import net.bananacheese.witchcraft.block.entity.WTBlockEntities;
import net.bananacheese.witchcraft.item.custom.FlaskItem;
import net.bananacheese.witchcraft.network.DarkCauldronFluidSyncPayload;
import net.bananacheese.witchcraft.potion.PotionForm;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.bananacheese.witchcraft.potion.synthesis.EssenceData;
import net.bananacheese.witchcraft.potion.synthesis.EssenceDataLoader;
import net.bananacheese.witchcraft.potion.synthesis.EssenceType;
import net.bananacheese.witchcraft.potion.synthesis.SynthesisEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DarkCauldronBlockEntity extends BlockEntity {

    public boolean isFilled = false;
    public boolean clientIsBoiling = false;
    private int heatTicks = 0;
    private static final int BOIL_THRESHOLD = 100;
    public static final int MAX_INGREDIENTS = 5;

    private final List<ItemStack> insertedIngredients = new ArrayList<>();

    /**
     * Blended liquid colour as 0xRRGGBB, computed on the SERVER and synced, because the essence
     * registry only exists server-side (it is filled by a reload listener). 0 = "no potion colour,
     * render plain water".
     */
    private int potionColor = 0;

    /** Which item the next glass bottle will produce. Server-side only; reset when brewing finishes. */
    private PotionForm form = PotionForm.DRINK;

    public DarkCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(WTBlockEntities.DARK_CAULDRON_BE.get(), pos, state);
    }

    // ------------------------------------------------------------------ ticking

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

        // 2. Ingest ItemEntities floating inside or near the cauldron basin
        if (currentlyBoiling && cauldron.insertedIngredients.size() < MAX_INGREDIENTS) {
            // The controller sits at the north-west corner; the basin spans the full 2x2 footprint.
            AABB basinBox = new AABB(
                    pos.getX() + 0.125D, pos.getY() + 0.125D, pos.getZ() + 0.125D,
                    pos.getX() + 1.875D, pos.getY() + 1.25D, pos.getZ() + 1.875D
            );

            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, basinBox);
            for (ItemEntity itemEntity : items) {
                if (!itemEntity.isAlive()) continue;

                ItemStack stack = itemEntity.getItem();
                if (!cauldron.isValidIngredient(stack)) continue;

                cauldron.insertedIngredients.add(stack.copyWithCount(1));
                cauldron.recomputeColor();

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
                break; // one ingredient per tick
            }
        }
    }

    public boolean isBoiling() {
        if (this.level != null && this.level.isClientSide()) {
            return this.clientIsBoiling;
        }
        return this.isFilled && this.heatTicks >= BOIL_THRESHOLD;
    }

    // ------------------------------------------------------------------ interaction

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

        boolean brewing = this.isFilled && this.isBoiling() && !this.insertedIngredients.isEmpty();

        // Form modifiers (placeholder catalysts, same as vanilla): gunpowder -> splash, dragon's breath -> lingering.
        if (brewing && heldItem.is(Items.GUNPOWDER) && this.form == PotionForm.DRINK) {
            if (!player.isCreative()) heldItem.shrink(1);
            this.form = PotionForm.SPLASH;
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.4F);
            setChanged();
            return;
        }
        if (brewing && heldItem.is(Items.DRAGON_BREATH) && this.form != PotionForm.LINGERING) {
            if (!player.isCreative()) {
                heldItem.shrink(1);
                ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(Items.GLASS_BOTTLE));
            }
            this.form = PotionForm.LINGERING;
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 0.8F);
            setChanged();
            return;
        }

        // Right-clicking with an empty flask finishes the brew; the flask's size decides the potion's size.
        if (brewing && heldItem.getItem() instanceof FlaskItem flask) {

            long seed = (this.level instanceof ServerLevel serverLevel) ? serverLevel.getSeed() : 0L;

            Map<EssenceType, Float> essencePool = EssenceDataLoader.combineIngredients(this.insertedIngredients);
            ProceduralPotionData potionData = SynthesisEngine.synthesize(essencePool, seed, flask.getSize());

            // Stack-size is handled by ProceduralPotion#getMaxStackSize, so no component patching needed.
            ItemStack resultPotion = potionData.createStack(this.form);

            if (!player.isCreative()) {
                heldItem.shrink(1);
            }

            ItemHandlerHelper.giveItemToPlayer(player, resultPotion);

            // Reset cauldron state
            this.insertedIngredients.clear();
            this.potionColor = 0;
            this.form = PotionForm.DRINK;
            this.isFilled = false;
            this.heatTicks = 0;

            level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);

            setChanged();
            syncToClient();
        }
    }

    public boolean isValidIngredient(ItemStack stack) {
        if (this.insertedIngredients.size() >= MAX_INGREDIENTS) return false;
        if (stack.isEmpty()) return false;

        EssenceData data = EssenceDataLoader.getEssenceData(stack);
        return data != null && !data.values().isEmpty();
    }

    // ------------------------------------------------------------------ colour

    /** SERVER ONLY: recompute the blended colour from the current ingredients. */
    private void recomputeColor() {
        if (insertedIngredients.isEmpty()) {
            potionColor = 0;
            return;
        }
        Map<EssenceType, Float> pool = EssenceDataLoader.combineIngredients(insertedIngredients);
        float total = 0f;
        for (float w : pool.values()) total += w;
        if (pool.isEmpty() || total <= 0f) {
            potionColor = 0;
            return;
        }
        int rgb = SynthesisEngine.blendEssenceColors(pool, total) & 0xFFFFFF;
        potionColor = rgb == 0 ? 0x000001 : rgb; // 0 is reserved for "plain water"
    }

    /** CLIENT: ARGB colour for the liquid surface. */
    public int getFluidColor() {
        if (!this.isFilled) return 0xFFFFFFFF;

        if (this.potionColor == 0) {
            if (this.level != null) {
                IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(Fluids.WATER);
                int tint = ext.getTintColor(Fluids.WATER.defaultFluidState(), this.level, this.worldPosition);
                return tint | 0xFF000000; // Force 100% alpha
            }
            return 0xFF3F76E4;
        }
        return 0xFF000000 | this.potionColor;
    }

    /** SERVER: spill the floating ingredients (called when the cauldron is broken). */
    public void dropIngredients() {
        if (level == null || level.isClientSide()) return;
        for (ItemStack stack : insertedIngredients) {
            net.minecraft.world.level.block.Block.popResource(level, worldPosition.above(), stack.copy());
        }
        insertedIngredients.clear();
        potionColor = 0;
    }

    /** Read-only view for the renderer (floating ingredient items). */
    public List<ItemStack> getIngredients() {
        return Collections.unmodifiableList(insertedIngredients);
    }

    // ------------------------------------------------------------------ client particles

    public static void clientTick(Level level, BlockPos pos, BlockState state, DarkCauldronBlockEntity entity) {
        if (!entity.isFilled || !entity.isBoiling()) return;

        RandomSource rand = level.getRandom();

        if (rand.nextFloat() < 0.35f) {
            double px = pos.getX() + 0.4D + (rand.nextDouble() * 1.2D);
            double py = pos.getY() + 0.82D; // Slightly above fluid line
            double pz = pos.getZ() + 0.4D + (rand.nextDouble() * 1.2D);

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
                    pos.getX() + 1.0D, pos.getY() + 0.5D, pos.getZ() + 1.0D,
                    net.minecraft.sounds.SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT,
                    net.minecraft.sounds.SoundSource.BLOCKS,
                    0.25F, 1.0F, false
            );
        }
    }

    // ------------------------------------------------------------------ sync / persistence

    private void syncToClient() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        PacketDistributor.sendToPlayersTrackingChunk(
                serverLevel,
                new net.minecraft.world.level.ChunkPos(worldPosition),
                new DarkCauldronFluidSyncPayload(this.worldPosition, this.isFilled, this.isBoiling(),
                        new ArrayList<>(this.insertedIngredients), this.potionColor));
    }

    public void setClientData(boolean isFilled, boolean isBoiling, List<ItemStack> ingredients, int color) {
        this.isFilled = isFilled;
        this.clientIsBoiling = isBoiling;
        this.potionColor = color;
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
        tag.putInt("PotionColor", potionColor);
        tag.putString("Form", form.name());

        ListTag list = new ListTag();
        for (ItemStack stack : insertedIngredients) {
            if (!stack.isEmpty()) list.add(stack.save(registries));
        }
        tag.put("Ingredients", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.isFilled = tag.getBoolean("IsFilled");
        this.heatTicks = tag.getInt("HeatTicks");
        this.potionColor = tag.getInt("PotionColor");

        try {
            this.form = PotionForm.valueOf(tag.getString("Form"));
        } catch (IllegalArgumentException e) {
            this.form = PotionForm.DRINK;
        }

        this.insertedIngredients.clear();
        ListTag list = tag.getList("Ingredients", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack.parse(registries, list.getCompound(i)).ifPresent(this.insertedIngredients::add);
        }
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
