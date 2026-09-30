package net.bananacheese.witchcraft.block.custom;

import com.mojang.serialization.MapCodec;
import net.bananacheese.witchcraft.block.base.ConnectedTextureBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CloudBlock extends ConnectedTextureBlock {
    public static final MapCodec<CloudBlock> CODEC = simpleCodec(CloudBlock::new);

    // Only the top surface is solid (like a lily pad)
    private static final VoxelShape TOP_SHAPE = Shapes.box(0, 0.875, 0, 1, 1, 1);

    private static final Map<UUID, Long> JUMP_COOLDOWNS = new HashMap<>();
    private static final long JUMP_COOLDOWN_MS = 500;

    public CloudBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entityContext) {
            Entity entity = entityContext.getEntity();
            if (entity != null && entity.isCrouching()) {
                return Shapes.empty();
            }

            if (entity != null) {
                double entityBottom = entity.getY();
                double blockTop = pos.getY() + 0.9375;

                if (entityBottom > blockTop) {
                    return TOP_SHAPE;
                }

                return Shapes.empty();
            }
        }

        return TOP_SHAPE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState otherState, Direction direction) {
        if (otherState.is(this)) {
            if (direction == Direction.UP) {
                return false;
            }
            return true;
        }
        return super.skipRendering(state, otherState, direction);
    }

    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.causeFallDamage((float) fallDistance, 0.1F, level.damageSources().fall());
    }

    public static void onPlayerJump(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return;

        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();

        Long lastJump = JUMP_COOLDOWNS.get(playerId);
        if (lastJump == null || (currentTime - lastJump) > JUMP_COOLDOWN_MS) {
            launchPlayerWithWindCharge(level, pos, player);
            JUMP_COOLDOWNS.put(playerId, currentTime);
        }
    }

    private static void launchPlayerWithWindCharge(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Vec3 playerPos = player.position();
        WindCharge windCharge = new WindCharge(player, level, playerPos.x, pos.getY() + 1, playerPos.z);

        windCharge.setDeltaMovement(0, -0.5, 0);
        level.addFreshEntity(windCharge);

        player.setDeltaMovement(player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z);
        player.hurtMarked = true;

        level.playSound(
                null,
                pos,
                SoundEvents.WIND_CHARGE_THROW,
                SoundSource.BLOCKS,
                1.0F,
                1.2F
        );

        serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                playerPos.x,
                pos.getY() + 1.0,
                playerPos.z,
                15,
                0.3, 0.1, 0.3,
                0.05
        );
    }
}

