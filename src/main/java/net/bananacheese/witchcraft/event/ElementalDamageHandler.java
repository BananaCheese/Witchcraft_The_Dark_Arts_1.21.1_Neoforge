package net.bananacheese.witchcraft.event;

import net.bananacheese.witchcraft.init.WTEffects;
import net.bananacheese.witchcraft.potion.element.ElementalDamageTypes;
import net.bananacheese.witchcraft.potion.synthesis.EssenceType;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/** Wards: reduce incoming damage of their element. The reduction is hard-capped so nobody becomes immune. */
public final class ElementalDamageHandler {
    /** Reduction at ward level I; each further level adds {@link #WARD_PER_LEVEL}. */
    public static final float WARD_BASE = 0.30f;
    public static final float WARD_PER_LEVEL = 0.15f;
    /** No amount of ward level gets past this. (Arcane damage also dispels wards, as a second check.) */
    public static final float WARD_CAP = 0.75f;

    private ElementalDamageHandler() {
    }

    public static float reduction(int amplifier) {
        return Mth.clamp(WARD_BASE + WARD_PER_LEVEL * amplifier, 0.0f, WARD_CAP);
    }

    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        EssenceType element = ElementalDamageTypes.elementOf(event.getSource());
        if (element == null) return;

        Holder<MobEffect> ward = WTEffects.ward(element);
        if (ward == null) return;

        LivingEntity entity = event.getEntity();
        MobEffectInstance instance = entity.getEffect(ward);
        if (instance == null) return;

        event.setAmount(event.getAmount() * (1.0f - reduction(instance.getAmplifier())));
    }
}
