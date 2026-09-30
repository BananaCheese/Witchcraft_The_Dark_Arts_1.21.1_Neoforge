package net.bananacheese.witchcraft.event;

import net.bananacheese.witchcraft.block.custom.CloudBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public final class WTCloudEvents {

    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide) {
            return;
        }

        BlockPos belowPos = player.blockPosition().below();

        if (player.level().getBlockState(belowPos).getBlock() instanceof CloudBlock && player.onGround()) {
            CloudBlock.onPlayerJump(player.level(), belowPos, player);
        }
    }

    private WTCloudEvents() {
    }
}
