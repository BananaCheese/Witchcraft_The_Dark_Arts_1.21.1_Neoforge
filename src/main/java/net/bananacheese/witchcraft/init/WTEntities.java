package net.bananacheese.witchcraft.init;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.entity.ThrownProceduralPotion;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class WTEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, WitchcraftTheDarkArts.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownProceduralPotion>> THROWN_PROCEDURAL_POTION =
            ENTITY_TYPES.register("thrown_procedural_potion", () ->
                    EntityType.Builder.<ThrownProceduralPotion>of(ThrownProceduralPotion::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("thrown_procedural_potion"));

    private WTEntities() {
    }
}
