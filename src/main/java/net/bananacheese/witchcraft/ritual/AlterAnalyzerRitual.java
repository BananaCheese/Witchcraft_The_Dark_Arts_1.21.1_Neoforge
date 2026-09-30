package net.bananacheese.witchcraft.ritual;

import net.bananacheese.witchcraft.block.entity.custom.AlterBlockEntity;
import net.bananacheese.witchcraft.item.WTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class AlterAnalyzerRitual {
    private static final int REQUIRED_FLUID = 1000;

    public static boolean perform(ServerLevel level, BlockPos pos, AlterBlockEntity altar) {
        if (AlterTier.getHighestValidTier(level, pos).getLevel() < 1) {
            return false;
        }

        if (!altar.getHeldItem().is(Items.GOLD_INGOT)
                || altar.getFluidAmount() < REQUIRED_FLUID) {
            return false;
        }

        altar.setHeldItem(new ItemStack(WTItems.ALTER_ANALYZER.get()));
        altar.consumeFluid(REQUIRED_FLUID);
        RitualEffects.play(level, pos, "portal");

        level.players().stream()
                .filter(player -> player.distanceToSqr(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5) <= 64)
                .forEach(player -> player.displayClientMessage(
                        Component.literal("§a✓ Altar Analyzer created!"), true));

        return true;
    }

    private AlterAnalyzerRitual() {
    }
}
