package net.bananacheese.witchcraft.potion.element;

import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.bananacheese.witchcraft.potion.synthesis.EssenceType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Damage zones for lingering potions. The vanilla area-effect cloud only re-applies an effect list, so it
 * cannot deal typed elemental damage; every lingering bane/chaos potion also registers a zone here, which
 * pulses the element's damage at everyone standing in the cloud.
 *
 * <p>Zones live in memory only: if the server restarts mid-cloud the cloud survives but its pulses do not.
 */
public final class ElementalZones {
    /** Fraction of a full hit applied per pulse. Lingering is gentle per tick but lasts a long time. */
    public static final float PULSE_INTENSITY = 0.15f;
    public static final int PULSE_INTERVAL = 20;        // ticks
    public static final int CHAOS_PULSE_INTERVAL = 60;  // Chaos pulses less often: every outcome is a big deal
    public static final float RADIUS = 3.0f;

    private static final List<Zone> ZONES = new ArrayList<>();

    private static final class Zone {
        final ResourceKey<Level> dimension;
        final Vec3 pos;
        final ProceduralPotionData data;
        @Nullable final UUID owner;
        final int duration;
        final int interval;
        int age = 0;

        Zone(ResourceKey<Level> dimension, Vec3 pos, ProceduralPotionData data, @Nullable UUID owner, int duration, int interval) {
            this.dimension = dimension;
            this.pos = pos;
            this.data = data;
            this.owner = owner;
            this.duration = duration;
            this.interval = interval;
        }
    }

    private ElementalZones() {
    }

    /** Matches the lifetime the lingering cloud is given in ThrownProceduralPotion. */
    public static int cloudDuration(ProceduralPotionData data) {
        return Math.round(600 * data.size().getDurationMultiplier());
    }

    public static void add(ServerLevel level, Vec3 pos, ProceduralPotionData data, @Nullable Entity owner) {
        boolean chaos = data.profile().element() == EssenceType.CHAOS;
        // Beneficial non-chaos potions are handled entirely by the vanilla cloud (their wards are plain effects).
        if (!data.profile().harmful() && !chaos) return;

        ZONES.add(new Zone(level.dimension(), pos, data, owner == null ? null : owner.getUUID(),
                cloudDuration(data), chaos ? CHAOS_PULSE_INTERVAL : PULSE_INTERVAL));
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        if (ZONES.isEmpty()) return;
        MinecraftServer server = event.getServer();

        for (int i = ZONES.size() - 1; i >= 0; i--) {
            Zone zone = ZONES.get(i);
            zone.age++;
            if (zone.age >= zone.duration) {
                ZONES.remove(i);
                continue;
            }
            if (zone.age % zone.interval != 0) continue;

            ServerLevel level = server.getLevel(zone.dimension);
            if (level == null) {
                ZONES.remove(i);
                continue;
            }
            pulse(level, zone);
        }
    }

    private static void pulse(ServerLevel level, Zone zone) {
        // The cloud shrinks over its life; the zone shrinks with it (down to 40%).
        float radius = RADIUS * (1.0f - 0.6f * zone.age / zone.duration);
        AABB box = new AABB(zone.pos.x - radius, zone.pos.y - 0.5, zone.pos.z - radius,
                zone.pos.x + radius, zone.pos.y + 2.0, zone.pos.z + radius);
        Entity owner = zone.owner == null ? null : level.getEntity(zone.owner);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (!target.isAffectedByPotions()) continue;

            double dx = target.getX() - zone.pos.x;
            double dz = target.getZ() - zone.pos.z;
            if (dx * dx + dz * dz > radius * radius) continue;

            zone.data.applyElemental(target, null, owner, PULSE_INTENSITY);
        }
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        ZONES.clear(); // statics outlive a singleplayer world, so do not leak zones into the next one
    }
}
