package net.bananacheese.witchcraft.item.custom;

import net.bananacheese.witchcraft.block.custom.AlterBlock;
import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.ritual.AlterTier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class AlterAnalyzer extends Item {
    public AlterAnalyzer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        analyze(level, player, context.getClickedPos());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }

        HitResult hitResult = player.pick(20.0D, 1.0F, false);
        if (!(hitResult instanceof BlockHitResult blockHit)) {
            player.displayClientMessage(
                    Component.literal("§cYou must look at a block!"), true);
            return InteractionResultHolder.fail(stack);
        }

        analyze(level, player, blockHit.getBlockPos());
        return InteractionResultHolder.success(stack);
    }

    private void analyze(Level level, Player player, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        // 1. Verify the clicked block is actually an Altar component
        if (!(state.getBlock() instanceof AlterBlock)) { // Replace 'AlterBlock' with your actual Altar block class
            // Check offhand for item scanning if not clicking an Altar
            ItemStack offhandStack = player.getOffhandItem();
            if (!offhandStack.isEmpty()) {
                offhandStack.set(WTComponents.ANALYZED.get(), true);
                player.displayClientMessage(
                        Component.literal("§aAnalyzed " + offhandStack.getHoverName().getString() + "! Essence contents revealed."),
                        true
                );
            } else {
                player.displayClientMessage(Component.literal("§cNot looking at an Altar!"), true);
            }
            return;
        }

        AlterTier tier = AlterTier.getHighestValidTier(level, pos);

        player.displayClientMessage(
                Component.literal("§6═══════════════════════════"), false);
        player.displayClientMessage(
                Component.literal("§6Altar Analysis"), false);
        player.displayClientMessage(
                Component.literal("§7Position: §b" + pos.toShortString()), false);
        player.displayClientMessage(
                Component.literal("§7Current Tier: §b" + tier.getLevel()
                        + " §7- " + tier.getDisplayName()), false);
        player.displayClientMessage(
                Component.literal("§6═══════════════════════════"), false);

        AlterTier nextTier = tier == AlterTier.TIER_4
                ? null
                : AlterTier.values()[tier.ordinal() + 1];

        if (nextTier == null) {
            player.displayClientMessage(
                    Component.literal("§aAltar is at maximum tier!"), false);
            return;
        }

        player.displayClientMessage(
                Component.literal("§7To upgrade to §bTier "
                        + nextTier.getLevel() + "§7:"), false);
        player.displayClientMessage(
                Component.literal("§7" + nextTier.getDisplayName()), false);
    }

    private void analyzeItemInOffhand(Player player) {
        ItemStack offhandStack = player.getOffhandItem();
        if (!offhandStack.isEmpty()) {
            offhandStack.set(WTComponents.ANALYZED.get(), true);
            player.displayClientMessage(
                    Component.literal("§aAnalyzed " + offhandStack.getHoverName().getString() + "! Essence contents revealed."),
                    true
            );
        }
    }

    public void markItemAsAnalyzed(ItemStack stack) {
        stack.set(WTComponents.ANALYZED.get(), true);
    }
}

