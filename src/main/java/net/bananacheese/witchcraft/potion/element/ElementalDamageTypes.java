package net.bananacheese.witchcraft.potion.element;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.potion.synthesis.EssenceType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * One damage type per element. The types themselves are datapack JSON
 * ({@code data/wctda/damage_type/potion_*.json}); this class maps elements to them and back.
 */
public final class ElementalDamageTypes {
    private static final Map<EssenceType, ResourceKey<DamageType>> KEYS = new EnumMap<>(EssenceType.class);

    static {
        KEYS.put(EssenceType.FIRE, key("potion_fire"));
        KEYS.put(EssenceType.ICE, key("potion_ice"));
        KEYS.put(EssenceType.LIGHTNING, key("potion_lightning"));
        KEYS.put(EssenceType.POISON, key("potion_poison"));
        KEYS.put(EssenceType.VITALITY, key("potion_physical")); // Vitality's harmful side is physical damage
        KEYS.put(EssenceType.ORDER, key("potion_arcane"));      // Order is the arcane element
        KEYS.put(EssenceType.CHAOS, key("potion_chaos"));
    }

    private ElementalDamageTypes() {
    }

    private static ResourceKey<DamageType> key(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(WitchcraftTheDarkArts.MODID, path));
    }

    public static ResourceKey<DamageType> keyOf(EssenceType element) {
        return KEYS.get(element);
    }

    /** A damage source of the element's type. {@code direct} is the thrown potion (or null), {@code owner} the thrower. */
    public static DamageSource source(Level level, EssenceType element, @Nullable Entity direct, @Nullable Entity owner) {
        Holder<DamageType> type = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(KEYS.get(element));
        return new DamageSource(type, direct, owner);
    }

    /**
     * Which element's ward applies to this damage, or null if none does. Our own types map directly; a few
     * vanilla families map too (fire, freezing, lightning, magic) so wards protect against those as well.
     */
    @Nullable
    public static EssenceType elementOf(DamageSource source) {
        for (Map.Entry<EssenceType, ResourceKey<DamageType>> entry : KEYS.entrySet()) {
            if (source.is(entry.getValue())) return entry.getKey();
        }
        if (source.is(DamageTypeTags.IS_FIRE)) return EssenceType.FIRE;
        if (source.is(DamageTypeTags.IS_FREEZING)) return EssenceType.ICE;
        if (source.is(DamageTypeTags.IS_LIGHTNING)) return EssenceType.LIGHTNING;
        if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) return EssenceType.ORDER;
        return null;
    }
}
