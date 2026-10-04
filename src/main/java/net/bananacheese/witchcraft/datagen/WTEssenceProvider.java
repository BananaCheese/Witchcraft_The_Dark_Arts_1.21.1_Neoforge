package net.bananacheese.witchcraft.datagen;

import net.bananacheese.witchcraft.potion.synthesis.EssenceType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class WTEssenceProvider extends EssenceProvider {

    public WTEssenceProvider(PackOutput packOutput, String modId, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, modId, registries);
    }

    @Override
    protected void addEssences() {
        // Fire / Chaos
        add(Items.BLAZE_POWDER, Builder.create()
                .with(EssenceType.FIRE, 4.0f)
                .with(EssenceType.CHAOS, 1.0f));

        add(Items.MAGMA_CREAM, Builder.create()
                .with(EssenceType.FIRE, 2.5f)
                .with(EssenceType.VITALITY, 1.5f));

        // Ice / Freeze
        add(Items.ICE, Builder.create()
                .with(EssenceType.ICE, 2.0f));

        add(Items.BLUE_ICE, Builder.create()
                .with(EssenceType.ICE, 8.0f)
                .with(EssenceType.ORDER, 2.0f));

        add(Items.SNOWBALL, Builder.create()
                .with(EssenceType.ICE, 0.5f));

        // Vitality / Order
        add(Items.GLISTERING_MELON_SLICE, Builder.create()
                .with(EssenceType.VITALITY, 3.0f)
                .with(EssenceType.ORDER, 1.0f));

        add(Items.GOLDEN_CARROT, Builder.create()
                .with(EssenceType.VITALITY, 4.0f)
                .with(EssenceType.ORDER, 3.0f));

        // Poison / Malice
        add(Items.SPIDER_EYE, Builder.create()
                .with(EssenceType.POISON, 2.5f)
                .with(EssenceType.CHAOS, 1.0f));

        add(Items.FERMENTED_SPIDER_EYE, Builder.create()
                .with(EssenceType.POISON, 3.0f)
                .with(EssenceType.CHAOS, 4.0f));

        // Lightning / Speed
        add(Items.SUGAR, Builder.create()
                .with(EssenceType.LIGHTNING, 1.5f));

        add(Items.LIGHTNING_ROD, Builder.create()
                .with(EssenceType.LIGHTNING, 6.0f)
                .with(EssenceType.ORDER, 2.0f));
    }
}