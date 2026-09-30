package net.bananacheese.witchcraft.ritual;

import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.bananacheese.witchcraft.item.custom.SoulSyringe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class RevivalRitual {
    private static final int REQUIRED_FLUID = 1000;

    public static boolean perform(
            ServerLevel level,
            BlockPos pos,
            AlterBlockEntity altar,
            @Nullable ServerPlayer performer) {

        if (AlterTier.getHighestValidTier(level, pos).getLevel() < 4) {
            message(performer,
                    "§c✗ This altar is not powerful enough! Required: Tier 4 (Supreme Altar)");
            return false;
        }

        ItemStack syringe = altar.getHeldItem();
        if (!(syringe.getItem() instanceof SoulSyringe)
                || SoulSyringe.getFillLevel(syringe) < 4) {
            message(performer,
                    "§c✗ The Soul Syringe is not fully charged! (4/4 required)");
            return false;
        }

        if (altar.getFluidAmount() < REQUIRED_FLUID) {
            message(performer,
                    "§c✗ The altar needs 1000 mB of water!");
            return false;
        }

        UUID targetId = SoulSyringe.getTargetPlayer(syringe);
        if (targetId == null) {
            message(performer,
                    "§c✗ The Soul Syringe has no target player!");
            return false;
        }

        ServerPlayer target = level.getServer().getPlayerList().getPlayer(targetId);
        if (target == null) {
            message(performer,
                    "§c✗ Target player is not online!");
            return false;
        }

        if (!target.isSpectator()) {
            message(performer,
                    "§c✗ " + target.getName().getString() + " is not dead!");
            return false;
        }

        altar.consumeFluid(REQUIRED_FLUID);
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

    private static void message(@Nullable ServerPlayer player, String text) {
        if (player != null) {
            player.sendSystemMessage(Component.literal(text));
        }
    }

    private RevivalRitual() {
    }
}
