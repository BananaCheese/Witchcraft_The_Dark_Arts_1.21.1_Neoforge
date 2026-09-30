package net.bananacheese.witchcraft.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * Visual and audio effects shared by the ritual system.
 */
public final class RitualEffects {
    public static void play(ServerLevel level, BlockPos pos, String effectName) {
        switch (effectName.toLowerCase()) {
            case "water" -> playWater(level, pos);
            case "enchanting" -> playEnchanting(level, pos);
            case "soul" -> playSoul(level, pos);
            case "fire" -> playFire(level, pos);
            case "portal" -> playPortal(level, pos);
            case "revival" -> playRevival(level, pos);
            case "dark" -> playDark(level, pos);
            case "holy" -> playHoly(level, pos);
            case "lightning" -> playLightning(level, pos);
            case "nature" -> playNature(level, pos);
            case "blood" -> playBlood(level, pos);
            default -> playBasic(level, pos);
        }
    }

    private static void playBasic(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.AMETHYST_BLOCK_STEP, 1.0F, 1.2F);
        spawnCircle(level, pos, ParticleTypes.ENCHANT, 20, 1.5D, 0.1D);
        spawn(level, ParticleTypes.GLOW, pos, 15, 0.2D, 0.2D, 0.2D, 0.1D, 1.5D);
    }

    private static void playWater(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.AMBIENT_UNDERWATER_ENTER, 1.0F, 1.0F);
        spawnCircle(level, pos, ParticleTypes.SPLASH, 30, 1.5D, 0.1D);
        spawn(level, ParticleTypes.BUBBLE_COLUMN_UP, pos, 20,
                0.3D, 0.5D, 0.3D, 0.1D, 0.5D);
    }

    private static void playEnchanting(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
        spawnCircle(level, pos, ParticleTypes.ENCHANT, 50, 1.5D, 0.05D);
        spawn(level, ParticleTypes.PORTAL, pos, 30,
                0.2D, 0.2D, 0.2D, 0.3D, 1.5D);
    }

    private static void playSoul(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.SOUL_ESCAPE.value(), 1.0F, 0.8F);
        spawnCircle(level, pos, ParticleTypes.SOUL, 40, 2.0D, 0.03D);
        spawn(level, ParticleTypes.SOUL, pos, 50,
                0.3D, 0.3D, 0.3D, 0.1D, 1.0D);
    }

    private static void playFire(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.FIRECHARGE_USE, 1.0F, 1.0F);
        spawnCircle(level, pos, ParticleTypes.FLAME, 30, 1.8D, 0.05D);
        spawn(level, ParticleTypes.LAVA, pos, 20,
                0.3D, 0.1D, 0.3D, 0.0D, 1.0D);
    }

    private static void playPortal(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.PORTAL_TRIGGER, 1.0F, 1.0F);
        spawnCircle(level, pos, ParticleTypes.PORTAL, 100, 1.5D, 0.5D);
    }

    private static void playRevival(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.ENDER_DRAGON_GROWL, 1.0F, 0.8F);
        playSound(level, pos, SoundEvents.PORTAL_TRIGGER, 0.5F, 1.5F);
        spawnCircle(level, pos, ParticleTypes.SOUL, 50, 2.0D, 0.02D);
        spawn(level, ParticleTypes.PORTAL, pos, 100,
                0.3D, 0.3D, 0.3D, 0.5D, 1.5D);
        spawn(level, ParticleTypes.TOTEM_OF_UNDYING, pos, 30,
                0.5D, 0.5D, 0.5D, 0.2D, 1.0D);
    }

    private static void playDark(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.WITHER_SPAWN, 0.5F, 0.5F);
        spawnCircle(level, pos, ParticleTypes.SMOKE, 40, 2.0D, 0.03D);
        spawn(level, ParticleTypes.SQUID_INK, pos, 50,
                0.3D, 0.3D, 0.3D, 0.1D, 1.5D);
    }

    private static void playHoly(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.5F);
        spawn(level, ParticleTypes.END_ROD, pos, 40,
                0.5D, 0.5D, 0.5D, 0.05D, 2.0D);
    }

    private static void playLightning(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.TRIDENT_THUNDER.value(), 0.8F, 1.0F);
        spawn(level, ParticleTypes.ELECTRIC_SPARK, pos, 60,
                1.5D, 1.5D, 1.5D, 0.1D, 1.5D);
    }

    private static void playNature(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.BONE_MEAL_USE, 1.0F, 1.2F);
        spawn(level, ParticleTypes.HAPPY_VILLAGER, pos, 40,
                1.0D, 1.0D, 1.0D, 0.05D, 1.0D);
    }

    private static void playBlood(ServerLevel level, BlockPos pos) {
        playSound(level, pos, SoundEvents.PLAYER_HURT, 0.8F, 0.5F);
        spawn(level, ParticleTypes.DAMAGE_INDICATOR, pos, 50,
                1.0D, 1.0D, 1.0D, 0.1D, 1.0D);
    }

    private static void playSound(
            ServerLevel level,
            BlockPos pos,
            net.minecraft.sounds.SoundEvent sound,
            float volume,
            float pitch) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, volume, pitch);
    }

    private static void spawnCircle(
            ServerLevel level,
            BlockPos pos,
            ParticleOptions particle,
            int count,
            double radius,
            double speed) {
        for (int i = 0; i < count; i++) {
            double angle = i / (double) count * Math.PI * 2.0D;
            double x = pos.getX() + 0.5D + Math.cos(angle) * radius;
            double z = pos.getZ() + 0.5D + Math.sin(angle) * radius;

            level.sendParticles(
                    particle,
                    x,
                    pos.getY() + 0.5D,
                    z,
                    1,
                    0.0D,
                    0.1D,
                    0.0D,
                    speed);
        }
    }

    private static void spawn(
            ServerLevel level,
            ParticleOptions particle,
            BlockPos pos,
            int count,
            double offsetX,
            double offsetY,
            double offsetZ,
            double speed,
            double yOffset) {
        level.sendParticles(
                particle,
                pos.getX() + 0.5D,
                pos.getY() + yOffset,
                pos.getZ() + 0.5D,
                count,
                offsetX,
                offsetY,
                offsetZ,
                speed);
    }

    private RitualEffects() {
    }
}
