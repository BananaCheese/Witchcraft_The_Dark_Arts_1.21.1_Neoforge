package net.bananacheese.witchcraft.init;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.potion.synthesis.EssenceType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public final class WTEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, WitchcraftTheDarkArts.MODID);

    /** A plain effect. Wards have no behaviour of their own: ElementalDamageHandler reads them when damage lands. */
    private static final class ElementalMobEffect extends MobEffect {
        ElementalMobEffect(MobEffectCategory category, int color) {
            super(category, color);
        }
    }

    // One ward per element except Chaos: Fire Ward, Ice Ward, Lightning Ward, Poison Ward, Vitality Ward, Order Ward.
    private static final Map<EssenceType, Holder<MobEffect>> WARDS = new EnumMap<>(EssenceType.class);

    static {
        for (EssenceType element : EssenceType.values()) {
            if (element.hasWard()) {
                WARDS.put(element, MOB_EFFECTS.register(element.wardName(),
                        () -> new ElementalMobEffect(MobEffectCategory.BENEFICIAL, element.getDefaultColor())));
            }
        }
    }

    /** Chaos's silly outcomes. Both are plain size changes via the vanilla scale attribute. */
    public static final Holder<MobEffect> CHAOS_GIANT = MOB_EFFECTS.register("chaos_giant",
            () -> new ElementalMobEffect(MobEffectCategory.NEUTRAL, 0xCC0000).addAttributeModifier(
                    Attributes.SCALE,
                    ResourceLocation.fromNamespaceAndPath(WitchcraftTheDarkArts.MODID, "effect.chaos_giant"),
                    1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

    public static final Holder<MobEffect> CHAOS_TINY = MOB_EFFECTS.register("chaos_tiny",
            () -> new ElementalMobEffect(MobEffectCategory.NEUTRAL, 0xCC0000).addAttributeModifier(
                    Attributes.SCALE,
                    ResourceLocation.fromNamespaceAndPath(WitchcraftTheDarkArts.MODID, "effect.chaos_tiny"),
                    -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

    private WTEffects() {
    }

    /** The ward for an element, or null for Chaos. */
    @Nullable
    public static Holder<MobEffect> ward(EssenceType element) {
        return WARDS.get(element);
    }
}
