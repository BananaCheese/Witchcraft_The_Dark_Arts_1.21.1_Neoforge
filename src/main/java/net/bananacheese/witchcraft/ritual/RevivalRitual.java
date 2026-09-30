package net.bananacheese.witchcraft.ritual;

import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.bananacheese.witchcraft.block.entity.custom.PedestalBlockEntity;
import net.bananacheese.witchcraft.item.custom.SoulSyringe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class RevivalRitual {
    public static boolean perform(
            ServerLevel level,
            BlockPos pos,
            AlterBlockEntity altar,
            int requiredFluid,
            List<Ingredient> requiredPedestalItems,
            @Nullable ServerPlayer performer) {

        ItemStack syringe = altar.getHeldItem();
        UUID targetId = SoulSyringe.getTargetPlayer(syringe);

        if (targetId == null) {
            message(performer, "§c✗ The Soul Syringe has no target player!");
            return false;
        }

        ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
        if (target == null) {
            message(performer, "§c✗ Target player is not online!");
            return false;
        }

        if (!target.isSpectator()) {
            message(performer,
                    "§c✗ " + target.getName().getString() + " is not dead!");
            return false;
        }

        if (!hasPedestalIngredients(level, pos, requiredPedestalItems)) {
            message(performer, "§c✗ The revival ritual is missing its ingredients!");
            return false;
        }

        if (altar.getFluidAmount() < requiredFluid) {
            message(performer, "§c✗ The altar needs " + requiredFluid + " mB of water!");
            return false;
        }

        altar.consumeFluid(requiredFluid);
        consumePedestals(level, pos, requiredPedestalItems);
        altar.setHeldItem(ItemStack.EMPTY);

        target.setGameMode(GameType.SURVIVAL);
        target.setHealth(2.0F);
        target.teleportTo(
                pos.getX() + 0.5,
                pos.getY() + 1.5,
                pos.getZ() + 0.5);

        RitualEffects.play(level, pos, "revival");

        String performerName = performer == null
                ? "the ritual"
                : performer.getName().getString();

        target.sendSystemMessage(Component.literal(
                "§a✓ You have been revived by " + performerName + "!"));

        if (performer != null) {
            performer.sendSystemMessage(Component.literal(
                    "§a✓ Successfully revived "
                            + target.getName().getString() + "!"));
        }

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("§6✦ " + target.getName().getString()
                        + " has been brought back from the dead!"), false);

        return true;
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

    private static List<ItemStack> pedestalStacks(ServerLevel level, BlockPos pos) {
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
            positions.add(altarPos.offset(offset[0], offset[1], offset[2]));
        }

        return positions;
    }

    private static void message(@Nullable ServerPlayer player, String text) {
        if (player != null) {
            player.sendSystemMessage(Component.literal(text));
        }
    }

    private RevivalRitual() {
    }
}
