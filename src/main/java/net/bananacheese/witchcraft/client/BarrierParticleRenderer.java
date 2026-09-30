package net.bananacheese.witchcraft.client;

import net.bananacheese.witchcraft.block.custom.BarrierFieldBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Ported from BarrierParticleRenderer. Renames: ClientTickEvents.END_CLIENT_TICK
 * (Fabric) -> ClientTickEvent.Post (NeoForge, fired on NeoForge.EVENT_BUS —
 * registered from the main mod class alongside WTCloudEvents),
 * MinecraftClient -> Minecraft, ClientWorld -> ClientLevel,
 * world.addParticleClient -> level.addParticle, BlockPos.iterate -> BlockPos.betweenClosed.
 *
 * UNVERIFIED: ClientTickEvent.Post's exact shape (whether it's a static
 * nested type fired directly, and how to get the current Minecraft
 * instance from it) — guessed as a no-arg-accessing event where we just
 * call Minecraft.getInstance() ourselves rather than reading from the
 * event, which should be safe either way.
 */
@OnlyIn(Dist.CLIENT)
public class BarrierParticleRenderer {
    private static final int PARTICLE_RANGE = 5;
    private static final int PARTICLE_CHECK_INTERVAL = 20;
    private static int tickCounter = 0;

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.level != null && client.player != null) {
            tickCounter++;
            if (tickCounter >= PARTICLE_CHECK_INTERVAL) {
                tickCounter = 0;
                spawnBarrierParticles(client);
            }
        }
    }

    private static void spawnBarrierParticles(Minecraft client) {
        ClientLevel level = client.level;
        if (level == null || client.player == null) return;

        BlockPos playerPos = client.player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(
                playerPos.offset(-PARTICLE_RANGE, -PARTICLE_RANGE, -PARTICLE_RANGE),
                playerPos.offset(PARTICLE_RANGE, PARTICLE_RANGE, PARTICLE_RANGE))) {

            if (level.getBlockState(pos).getBlock() instanceof BarrierFieldBlock) {
                spawnParticlesForBarrier(level, pos);
            }
        }
    }

    private static void spawnParticlesForBarrier(ClientLevel level, BlockPos pos) {
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();

        double density = 0.5;

        for (double dy = 0; dy <= 1; dy += density) {
            for (double dx = 0; dx <= 1; dx += density) {
                for (double dz = 0; dz <= 1; dz += density) {
                    if (isOnEdge(dx, dy, dz)) {
                        level.addParticle(
                                ParticleTypes.END_ROD,
                                x + dx,
                                y + dy,
                                z + dz,
                                0, 0, 0
                        );
                    }
                }
            }
        }
    }

    private static boolean isOnEdge(double x, double y, double z) {
        return x == 0 || x == 1 || y == 0 || y == 1 || z == 0 || z == 1;
    }
}
