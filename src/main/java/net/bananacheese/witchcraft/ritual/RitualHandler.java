package net.bananacheese.witchcraft.ritual;

import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.bananacheese.witchcraft.block.entity.custom.PedestalBlockEntity;
import net.bananacheese.witchcraft.item.WTItems;
import net.bananacheese.witchcraft.item.custom.SoulSyringe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class RitualHandler {
    private static final int REQUIRED_WATER = 1000;

    private static final List<Ingredient> DARK_CRYSTAL_INGREDIENTS = List.of(
            Ingredient.of(Items.REDSTONE_BLOCK),
            Ingredient.of(Items.REDSTONE_BLOCK),
            Ingredient.of(Items.AMETHYST_BLOCK),
            Ingredient.of(Items.AMETHYST_BLOCK),
            Ingredient.of(Items.SCULK),
            Ingredient.of(Items.SCULK),
            Ingredient.of(Items.SCULK),
            Ingredient.of(Items.SCULK)
    );

    public static boolean attemptRitual(
            Level level,
            BlockPos pos,
            AlterBlockEntity altar) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        ServerPlayer performer = findPerformer(serverLevel, pos);
        ItemStack heldItem = altar.getHeldItem();

        if (heldItem.getItem() instanceof SoulSyringe) {
            return RevivalRitual.perform(
                    serverLevel, pos, altar, performer);
        }

        if (heldItem.is(Items.GOLD_INGOT)
                && altar.getFluidAmount() >= REQUIRED_WATER) {
            return AlterAnalyzerRitual.perform(serverLevel, pos, altar);
        }

        if (canPerformDarkCrystalRitual(serverLevel, pos, altar)) {
            performDarkCrystalRitual(serverLevel, pos, altar, performer);
            return true;
        }

        if (performer != null) {
            performer.displayClientMessage(
                    Component.literal("§c✗ No valid ritual found"), true);
        }

        return false;
    }

    private static boolean canPerformDarkCrystalRitual(
            ServerLevel level,
            BlockPos pos,
            AlterBlockEntity altar) {
        return altar.getHeldItem().is(Items.CRYING_OBSIDIAN)
                && altar.getFluidAmount() == 0
                && AlterTier.getHighestValidTier(level, pos).getLevel() >= 4
                && hasPedestalIngredients(
                        level, pos, DARK_CRYSTAL_INGREDIENTS);
    }

    private static void performDarkCrystalRitual(
            ServerLevel level,
            BlockPos pos,
            AlterBlockEntity altar,
            ServerPlayer performer) {
        consumePedestals(level, pos, DARK_CRYSTAL_INGREDIENTS);
        altar.setHeldItem(new ItemStack(WTItems.DARK_CRYSTAL.get()));
        RitualEffects.play(level, pos, "dark");

        if (performer != null) {
            performer.sendSystemMessage(
                    Component.literal("§a✓ Ritual complete!"));
        }
    }

    private static boolean hasPedestalIngredients(
            ServerLevel level,
            BlockPos pos,
            List<Ingredient> required) {
        List<ItemStack> available = pedestalStacks(level, pos);

        if (available.size() < required.size()) {
            return false;
        }

        for (Ingredient ingredient : required) {
            int matchingIndex = -1;

            for (int i = 0; i < available.size(); i++) {
                if (ingredient.test(available.get(i))) {
                    matchingIndex = i;
                    break;
                }
            }

            if (matchingIndex < 0) {
                return false;
            }

            available.remove(matchingIndex);
        }

        return true;
    }

    private static void consumePedestals(
            ServerLevel level,
            BlockPos pos,
            List<Ingredient> required) {
        List<BlockPos> positions = pedestalPositions(pos);
        boolean[] used = new boolean[positions.size()];

        for (Ingredient ingredient : required) {
            for (int i = 0; i < positions.size(); i++) {
                if (used[i]) {
                    continue;
                }

                if (!(level.getBlockEntity(positions.get(i))
                        instanceof PedestalBlockEntity pedestal)) {
                    continue;
                }

                if (!ingredient.test(pedestal.getHeldItem())) {
                    continue;
                }

                pedestal.setHeldItem(ItemStack.EMPTY);
                used[i] = true;
                break;
            }
        }
    }

    private static List<ItemStack> pedestalStacks(
            ServerLevel level,
            BlockPos pos) {
        List<ItemStack> stacks = new ArrayList<>();

        for (BlockPos pedestalPos : pedestalPositions(pos)) {
            if (level.getBlockEntity(pedestalPos)
                    instanceof PedestalBlockEntity pedestal
                    && !pedestal.getHeldItem().isEmpty()) {
                stacks.add(pedestal.getHeldItem().copy());
            }
        }

        return stacks;
    }

    private static List<BlockPos> pedestalPositions(BlockPos altarPos) {
        int[][] offsets = {
                {3, 0, 0},
                {-3, 0, 0},
                {0, 0, 3},
                {0, 0, -3},
                {2, 0, 2},
                {2, 0, -2},
                {-2, 0, 2},
                {-2, 0, -2}
        };

        List<BlockPos> positions = new ArrayList<>(offsets.length);
        for (int[] offset : offsets) {
            positions.add(altarPos.offset(
                    offset[0], offset[1], offset[2]));
        }

        return positions;
    }

    private static ServerPlayer findPerformer(
            ServerLevel level,
            BlockPos pos) {
        ServerPlayer closestPlayer = null;
        double closestDistance = 64.0D;

        for (ServerPlayer player : level.players()) {
            double distance = player.distanceToSqr(
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5);

            if (distance < closestDistance) {
                closestPlayer = player;
                closestDistance = distance;
            }
        }

        return closestPlayer;
    }

    private RitualHandler() {
    }
}
