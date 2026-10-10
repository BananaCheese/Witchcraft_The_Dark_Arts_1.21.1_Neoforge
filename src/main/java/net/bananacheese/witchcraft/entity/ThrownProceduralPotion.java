package net.bananacheese.witchcraft.entity;

import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.init.WTEntities;
import net.bananacheese.witchcraft.item.WTItems;
import net.bananacheese.witchcraft.item.custom.ThrowableProceduralPotion;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.bananacheese.witchcraft.potion.element.ElementalEffects;
import net.bananacheese.witchcraft.potion.element.ElementalZones;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class ThrownProceduralPotion extends ThrowableItemProjectile {

    public ThrownProceduralPotion(EntityType<? extends ThrownProceduralPotion> type, Level level) {
        super(type, level);
    }

    public ThrownProceduralPotion(Level level, LivingEntity owner) {
        super(WTEntities.THROWN_PROCEDURAL_POTION.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return WTItems.PROCEDURAL_SPLASH_POTION.get();
    }

    /** Same arc as vanilla thrown potions (default throwable gravity is 0.03). */
    @Override
    protected double getDefaultGravity() {
        return 0.05;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level().isClientSide) return;

        ItemStack stack = getItem();
        ProceduralPotionData data = stack.get(WTComponents.PROCEDURAL_POTION.get());
        boolean lingering = stack.getItem() instanceof ThrowableProceduralPotion t && t.isLingering();

        if (data != null) {
            Entity directHit = result instanceof EntityHitResult ehr ? ehr.getEntity() : null;
            if (lingering) {
                spawnCloud(data);
            } else {
                applySplash(data, directHit);
            }

            if (level() instanceof ServerLevel serverLevel) {
                // Custom particle burst on top of vanilla's tinted splash (levelEvent below).
                serverLevel.sendParticles(data.particleOrDefault(),
                        getX(), getY() + 0.1D, getZ(), 24, 0.5D, 0.3D, 0.5D, 0.02D);

                // Element extras: fire blasts, vitality spikes, ...
                ElementalEffects.onImpact(data, serverLevel, position(), getOwner());
                // The vanilla cloud only re-applies effects; this zone adds the typed elemental damage.
                if (lingering) {
                    ElementalZones.add(serverLevel, position(), data, getOwner());
                }
            }
        }

        // 2002 = vanilla "splash potion" event: glass-break sound + coloured spell particles.
        level().levelEvent(2002, blockPosition(), data != null ? data.color() : 0xFFFFFF);
        discard();
    }

    private void applySplash(ProceduralPotionData data, Entity directHit) {
        AABB box = getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, box);
        for (LivingEntity target : targets) {
            if (!target.isAffectedByPotions()) continue;

            double distSq = distanceToSqr(target);
            if (distSq >= 16.0D) continue;

            double intensity = target == directHit ? 1.0D : 1.0D - Math.sqrt(distSq) / 4.0D;
            data.applyTo(target, this, getOwner(), intensity);
        }
    }

    private void spawnCloud(ProceduralPotionData data) {
        AreaEffectCloud cloud = new AreaEffectCloud(level(), getX(), getY(), getZ());
        if (getOwner() instanceof LivingEntity owner) {
            cloud.setOwner(owner);
        }
        cloud.setRadius(3.0F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        // Size scales cloud lifetime the same way it scales effect duration.
        cloud.setDuration(ElementalZones.cloudDuration(data));
        cloud.setRadiusPerTick(-cloud.getRadius() / (float) cloud.getDuration());
        cloud.setPotionContents(data.toPotionContents());
        data.particle().ifPresent(cloud::setParticle);
        level().addFreshEntity(cloud);
    }
}
