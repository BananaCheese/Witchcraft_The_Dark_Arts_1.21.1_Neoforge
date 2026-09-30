package net.bananacheese.witchcraft.event;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class PlayerDeathHandler {

    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ServerLevel world = (ServerLevel) player.level();
        if (!world.getLevelData().isHardcore()) return;

        BlockPos deathPos = player.blockPosition();
        BlockPos headPos = findSuitableHeadPosition(world, deathPos);

        if (headPos != null) {
            world.setBlockAndUpdate(headPos, Blocks.PLAYER_HEAD.defaultBlockState());
            if (world.getBlockEntity(headPos) instanceof SkullBlockEntity skullEntity) {
                skullEntity.setOwner(new ResolvableProfile(player.getGameProfile()));
                WitchcraftTheDarkArts.LOGGER.info("Placed death marker head for player {} at {}",
                        player.getName().getString(), headPos);
            }
        }
    }

    private static BlockPos findSuitableHeadPosition(ServerLevel world, BlockPos startPos) {
        if (canPlaceHead(world, startPos)) return startPos;
        BlockPos upPos = startPos.above();
        if (canPlaceHead(world, upPos)) return upPos;
        BlockPos downPos = startPos.below();
        if (canPlaceHead(world, downPos)) return downPos;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                BlockPos adjacentPos = startPos.offset(x, 0, z);
                if (canPlaceHead(world, adjacentPos)) return adjacentPos;
            }
        }
        return null;
    }

    private static boolean canPlaceHead(ServerLevel world, BlockPos pos) {
        return world.getBlockState(pos).isAir() &&
                world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP);
    }
}