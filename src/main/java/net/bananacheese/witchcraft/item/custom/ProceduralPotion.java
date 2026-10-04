package net.bananacheese.witchcraft.item.custom;

import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.potion.ProceduralEffect;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class ProceduralPotion extends Item {
    public ProceduralPotion(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        ProceduralPotionData data = stack.get(WTComponents.PROCEDURAL_POTION.get());
        if (data != null) {
            return data.size().getDefaultMaxStack();
        }
        return super.getMaxStackSize(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ProceduralPotionData data = stack.get(WTComponents.PROCEDURAL_POTION.get());
        if (data == null) {
            tooltipComponents.add(Component.literal("Unbound Alchemy Liquid"));
            return;
        }

        tooltipComponents.add(Component.literal("§7Size: §f" + data.size().name()));
        tooltipComponents.add(Component.literal("§7Effects:"));

        for (ProceduralEffect effect : data.effects()) {
            int scaledDuration = (int) (effect.baseDurationTicks() * data.size().getDurationMultiplier());
            int scaledAmp = Math.round(effect.baseAmplifier() * data.size().getPotencyMultiplier());

            tooltipComponents.add(Component.literal(
                    " §8• §f" + effect.effectTypeId() +
                            " §7(Lvl " + (scaledAmp + 1) + ") " +
                            "§8[" + (scaledDuration / 20) + "s]"
            ));
        }
    }
}
