package net.bananacheese.witchcraft.ritual;

import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.bananacheese.witchcraft.block.entity.custom.PedestalBlockEntity;
import net.bananacheese.witchcraft.item.WTItems;
import net.bananacheese.witchcraft.item.custom.SoulSyringe;
import net.bananacheese.witchcraft.recipe.RevivalRitualRecipe;
import net.bananacheese.witchcraft.recipe.RitualRecipe;
import net.bananacheese.witchcraft.recipe.RitualRecipeInput;
import net.bananacheese.witchcraft.recipe.WTRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class RitualHandler {
    public static boolean attemptRitual(
            Level level,
            BlockPos pos,
            AlterBlockEntity altar) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        int tier = AlterTier.getHighestValidTier(serverLevel, pos).getLevel();
        RitualRecipeInput input = createInput(serverLevel, pos, altar, tier);

        Optional<RecipeHolder<RitualRecipe>> ritual =
                serverLevel.getRecipeManager().getRecipeFor(
                        WTRecipes.RITUAL_TYPE.get(), input, serverLevel);

        if (ritual.isPresent()) {
            RitualRecipe recipe = ritual.get().value();
            consumeInputs(serverLevel, pos, altar, recipe.getFluidAmount(), recipe.getPedestalItems());
            altar.setHeldItem(recipe.getResult());
            RitualEffects.play(serverLevel, pos, recipe.getEffectName());

            ServerPlayer performer = findPerformer(serverLevel, pos);
            if (performer != null) {
                performer.sendSystemMessage(Component.literal("§a✓ Ritual complete!"));
            }

            return true;
        }

        Optional<net.minecraft.world.item.crafting.RecipeHolder<RevivalRitualRecipe>> revival =
                serverLevel.getRecipeManager().getRecipeFor(
                        WTRecipes.REVIVAL_TYPE.get(), input, serverLevel);

        if (revival.isPresent()) {
            return RevivalRitual.perform(
                    serverLevel,
                    pos,
                    altar,
                    revival.get().value().getFluidAmount(),
                    revival.get().value().getPedestalItems(),
                    findPerformer(serverLevel, pos));
        }

        ServerPlayer performer = findPerformer(serverLevel, pos);
        if (performer != null) {
            performer.displayClientMessage(
                    Component.literal("§c✗ No valid ritual found"), true);
        }

        return false;
    }

    private static RitualRecipeInput createInput(
            ServerLevel level,
            BlockPos pos,
            AlterBlockEntity altar,
            int tier) {
        List<ItemStack> pedestalItems = new ArrayList<>();

        for (BlockPos pedestalPos : pedestalPositions(pos)) {
            if (level.getBlockEntity(pedestalPos)
                    instanceof PedestalBlockEntity pedestal
                    && !pedestal.getHeldItem().isEmpty()) {
                pedestalItems.add(pedestal.getHeldItem().copy());
            }
        }

        return new RitualRecipeInput(
                altar.getHeldItem(),
                altar.getFluidAmount(),
                tier,
                pedestalItems);
    }

    private static void consumeInputs(
            ServerLevel level,
            BlockPos pos,
            AlterBlockEntity altar,
            int fluidAmount,
            List<Ingredient> ingredients) {
        if (fluidAmount > 0) {
            altar.consumeFluid(fluidAmount);
        }

        List<BlockPos> positions = pedestalPositions(pos);
        boolean[] used = new boolean[positions.size()];

        for (Ingredient ingredient : ingredients) {
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
            positions.add(altarPos.offset(offset[0], offset[1], offset[2]));
        }

        return positions;
    }

    private static ServerPlayer findPerformer(ServerLevel level, BlockPos pos) {
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
