package net.bananacheese.witchcraft.item;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.block.WTBlocks;
import net.bananacheese.witchcraft.potion.PotionForm;
import net.bananacheese.witchcraft.potion.PotionSize;
import net.bananacheese.witchcraft.potion.ProceduralEffect;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
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

                // Dev sample potions (one per form) for testing without a cauldron.
                ProceduralPotionData boon = new ProceduralPotionData(
                        List.of(new ProceduralEffect("minecraft:strength", 1200, 1, 0f),
                                new ProceduralEffect("minecraft:regeneration", 600, 0, 0f)),
                        PotionSize.MEDIUM, 0xFF3300, "minecraft:flame");
                ProceduralPotionData bane = new ProceduralPotionData(
                        List.of(new ProceduralEffect("minecraft:poison", 600, 1, 4f),
                                new ProceduralEffect("minecraft:slowness", 400, 0, 0f)),
                        PotionSize.MEDIUM, 0x00CC33, "minecraft:spore_blossom_dust");
                pOutput.accept(boon.createStack(PotionForm.DRINK));
                pOutput.accept(bane.createStack(PotionForm.SPLASH));
                pOutput.accept(bane.createStack(PotionForm.LINGERING));

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
