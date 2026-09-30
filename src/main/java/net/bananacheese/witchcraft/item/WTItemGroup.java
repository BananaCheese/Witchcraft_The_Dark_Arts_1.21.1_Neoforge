package net.bananacheese.witchcraft.item;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.block.WTBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class WTItemGroup {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WitchcraftTheDarkArts.MOD_ID);

    public static final Supplier<CreativeModeTab> WCTDA = CREATIVE_MODE_TABS.register("wctda",
            () -> CreativeModeTab.builder().title(Component.translatable("creativetab.wctda")).icon(() -> new ItemStack(WTItems.DARK_CRYSTAL.get())).displayItems((pParameters, pOutput) -> {

                pOutput.accept(WTItems.DARK_CRYSTAL);

                pOutput.accept(WTBlocks.ALTER);
                pOutput.accept(WTBlocks.PEDESTAL);
                pOutput.accept(WTBlocks.DARK_BARRIER);
                pOutput.accept(WTBlocks.CLOUD_BLOCK);
            }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
