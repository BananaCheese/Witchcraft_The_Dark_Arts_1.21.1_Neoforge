package net.bananacheese.witchcraft.client;

import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.potion.synthesis.EssenceData;
import net.bananacheese.witchcraft.potion.synthesis.EssenceDataLoader;
import net.bananacheese.witchcraft.potion.synthesis.EssenceType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.Map;

@EventBusSubscriber(modid = WitchcraftTheDarkArts.MODID, value = Dist.CLIENT)
public class EssenceTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        EssenceData essenceData = EssenceDataLoader.getEssenceData(stack);
        if (essenceData == null || essenceData.values().isEmpty()) return;

        boolean isAnalyzed = Boolean.TRUE.equals(stack.get(WTComponents.ANALYZED.get()));

        if (!isAnalyzed) {
            event.getToolTip().add(Component.literal("Essences: ").withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("??? (Requires Analyzer)").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            return;
        }

        if (Screen.hasShiftDown()) {
            event.getToolTip().add(Component.literal("Analyzed Essences:").withStyle(ChatFormatting.GOLD));

            for (Map.Entry<EssenceType, Float> entry : essenceData.values().entrySet()) {
                EssenceType type = entry.getKey();
                float amount = entry.getValue();

                event.getToolTip().add(Component.literal("  • " + type.name() + ": " + amount)
                        .withStyle(getEssenceFormatting(type)));
            }
        } else {
            event.getToolTip().add(Component.literal("Essences: [Hold SHIFT]").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static ChatFormatting getEssenceFormatting(EssenceType type) {
        return switch (type) {
            case FIRE -> ChatFormatting.RED;
            case ICE -> ChatFormatting.AQUA;
            case LIGHTNING -> ChatFormatting.YELLOW;
            case POISON -> ChatFormatting.GREEN;
            case VITALITY -> ChatFormatting.LIGHT_PURPLE;
            case ORDER -> ChatFormatting.WHITE;
            case CHAOS -> ChatFormatting.DARK_PURPLE;
            default -> ChatFormatting.GRAY;
        };
    }

    private static boolean hasBeenAnalyzed(ItemStack stack) {
        // You can check a custom DataComponent or NBT tag here
        // e.g., stack.has(ModDataComponents.ANALYZED) or custom component check
        Boolean analyzed = stack.get(WTComponents.ANALYZED.get());
        return Boolean.TRUE.equals(analyzed);
    }
}