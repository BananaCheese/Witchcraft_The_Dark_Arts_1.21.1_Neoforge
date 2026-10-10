package net.bananacheese.witchcraft.item;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.block.WTBlocks;
import net.bananacheese.witchcraft.potion.PotionForm;
import net.bananacheese.witchcraft.potion.PotionSize;
import net.bananacheese.witchcraft.potion.ProceduralEffect;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.bananacheese.witchcraft.potion.synthesis.EssenceType;
import net.bananacheese.witchcraft.potion.synthesis.SynthesisEngine;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class WTItemGroup {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WitchcraftTheDarkArts.MODID);

    public static final Supplier<CreativeModeTab> WCTDA = CREATIVE_MODE_TABS.register("wctda",
            () -> CreativeModeTab.builder().title(Component.translatable("creativetab.wctda")).icon(() -> new ItemStack(WTItems.DARK_CRYSTAL.get())).displayItems((pParameters, pOutput) -> {

                pOutput.accept(WTItems.DARK_CRYSTAL);
                pOutput.accept(WTItems.SOUL_SYRINGE);
                pOutput.accept(WTItems.ALTER_ANALYZER);

                pOutput.accept(WTItems.SMALL_FLASK);
                pOutput.accept(WTItems.MEDIUM_FLASK);
                pOutput.accept(WTItems.LARGE_FLASK);

                pOutput.accept(WTItems.POTION_POUCH);

                // Dev sample potions for testing without a cauldron: per element, a bane (splash) and a boon (drink).
                for (EssenceType element : EssenceType.values()) {
                    pOutput.accept(SynthesisEngine.sample(element, true, PotionSize.MEDIUM).createStack(PotionForm.SPLASH));
                    pOutput.accept(SynthesisEngine.sample(element, false, PotionSize.MEDIUM).createStack(PotionForm.DRINK));
                }
                // A lingering bane and a large boon, for testing clouds and the size recipes.
                pOutput.accept(SynthesisEngine.sample(EssenceType.FIRE, true, PotionSize.MEDIUM).createStack(PotionForm.LINGERING));
                pOutput.accept(SynthesisEngine.sample(EssenceType.FIRE, false, PotionSize.LARGE).createStack(PotionForm.DRINK));

                pOutput.accept(WTBlocks.ALTER);
                pOutput.accept(WTBlocks.PEDESTAL);
                pOutput.accept(WTBlocks.DARK_BARRIER);
                pOutput.accept(WTBlocks.CLOUD_BLOCK);

                pOutput.accept(WTBlocks.DARK_CAULDRON);
            }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
