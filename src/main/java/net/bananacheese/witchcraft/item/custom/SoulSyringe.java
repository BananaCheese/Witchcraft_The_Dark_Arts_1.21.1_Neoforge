package net.bananacheese.witchcraft.item.custom;

import net.bananacheese.witchcraft.component.WTComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SoulSyringe extends Item {
    private static final int MAX_FILL_LEVEL = 4;

    public SoulSyringe(Properties properties) {
        super(properties);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player
                && !player.level().isClientSide
                && getFillLevel(stack) == 0) {
            setFillLevel(stack, 1);
            player.displayClientMessage(
                    Component.literal("Soul Syringe: Slot 1 filled (Combat)"), true);
        }

        super.postHurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ItemStack offhand = player.getOffhandItem();

        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            int currentLevel = getFillLevel(stack);

            // Slot 1 -> Slot 2: sneak-right-click and sacrifice one heart.
            if (player.isShiftKeyDown() && currentLevel == 1) {
                setFillLevel(stack, 2);
                player.hurt(level.damageSources().magic(), 2.0F);
                player.displayClientMessage(
                        Component.literal("Soul Syringe: Slot 2 filled (Essence)"), true);
                return InteractionResultHolder.success(stack);
            }

            // Slot 3 -> Slot 4: provide the required biomass in the offhand.
            if (currentLevel == 3 && !offhand.isEmpty()) {
                BiomassType biomassType = getBiomassType(offhand);

                if (biomassType != BiomassType.NONE) {
                    int requiredAmount = biomassType.getRequiredAmount();

                    if (offhand.getCount() >= requiredAmount) {
                        offhand.shrink(requiredAmount);
                        setFillLevel(stack, 4);
                        player.displayClientMessage(
                                Component.literal("Soul Syringe: Slot 4 filled (Biomass)"), true);
                        return InteractionResultHolder.success(stack);
                    }

                    player.displayClientMessage(
                            Component.literal("§cNeed " + requiredAmount + " "
                                    + offhand.getHoverName().getString()), true);
                    return InteractionResultHolder.fail(stack);
                }
            }
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (level.isClientSide || player == null || getFillLevel(stack) != 2) {
            return InteractionResult.PASS;
        }

        if (!(level.getBlockState(pos).getBlock() instanceof PlayerHeadBlock)) {
            return InteractionResult.PASS;
        }

        if (!(level.getBlockEntity(pos) instanceof SkullBlockEntity skull)) {
            return InteractionResult.PASS;
        }

        var profile = skull.getOwnerProfile();
        if (profile == null || profile.id().isEmpty()) {
            player.displayClientMessage(
                    Component.literal("§cThis head has no owner data!"), true);
            return InteractionResult.FAIL;
        }

        UUID targetUuid = profile.id().get();
        String targetName = profile.name().orElse("Unknown");

        setTargetPlayer(stack, targetUuid);
        setTargetName(stack, targetName);
        setFillLevel(stack, 3);

        player.displayClientMessage(
                Component.literal("Soul Syringe: Slot 3 filled (Target: " + targetName + ")"), true);
        level.removeBlock(pos, false);

        return InteractionResult.SUCCESS;
    }

    public static int getFillLevel(ItemStack stack) {
        return stack.getOrDefault(WTComponents.SYRINGE_FILL_LEVEL, 0);
    }

    public static void setFillLevel(ItemStack stack, int level) {
        stack.set(WTComponents.SYRINGE_FILL_LEVEL, Math.min(level, MAX_FILL_LEVEL));
    }

    @Nullable
    public static UUID getTargetPlayer(ItemStack stack) {
        return stack.get(WTComponents.SYRINGE_TARGET_PLAYER);
    }

    public static void setTargetPlayer(ItemStack stack, UUID uuid) {
        stack.set(WTComponents.SYRINGE_TARGET_PLAYER, uuid);
    }

    @Nullable
    public static String getTargetName(ItemStack stack) {
        return stack.get(WTComponents.SYRINGE_TARGET_NAME);
    }

    public static void setTargetName(ItemStack stack, String name) {
        stack.set(WTComponents.SYRINGE_TARGET_NAME, name);
    }

    private static BiomassType getBiomassType(ItemStack stack) {
        if (stack.is(Items.BEEF)
                || stack.is(Items.COOKED_BEEF)
                || stack.is(Items.PORKCHOP)
                || stack.is(Items.COOKED_PORKCHOP)
                || stack.is(Items.MUTTON)
                || stack.is(Items.COOKED_MUTTON)
                || stack.is(Items.CHICKEN)
                || stack.is(Items.COOKED_CHICKEN)
                || stack.is(Items.RABBIT)
                || stack.is(Items.COOKED_RABBIT)
                || stack.is(Items.COD)
                || stack.is(Items.COOKED_COD)
                || stack.is(Items.SALMON)
                || stack.is(Items.COOKED_SALMON)
                || stack.is(Items.ROTTEN_FLESH)) {
            return BiomassType.MEAT;
        }

        if (stack.is(Items.WHEAT)
                || stack.is(Items.WHEAT_SEEDS)
                || stack.is(Items.CARROT)
                || stack.is(Items.POTATO)
                || stack.is(Items.BEETROOT)
                || stack.is(Items.BEETROOT_SEEDS)
                || stack.is(Items.APPLE)
                || stack.is(Items.MELON_SLICE)
                || stack.is(Items.PUMPKIN)
                || stack.is(Items.KELP)
                || stack.is(Items.SWEET_BERRIES)) {
            return BiomassType.PLANT;
        }

        return BiomassType.NONE;
    }

    private enum BiomassType {
        MEAT(32),
        PLANT(64),
        NONE(0);

        private final int requiredAmount;

        BiomassType(int requiredAmount) {
            this.requiredAmount = requiredAmount;
        }

        public int getRequiredAmount() {
            return requiredAmount;
        }
    }
}
