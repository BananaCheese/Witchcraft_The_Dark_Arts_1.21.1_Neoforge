package net.bananacheese.witchcraft.item.custom;

import net.bananacheese.witchcraft.potion.PotionSize;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class FlaskItem extends Item {
    private final PotionSize size;

    public FlaskItem(Properties properties, PotionSize size) {
        super(properties);
        this.size = size;
    }

    public PotionSize getSize() {
        return size;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Holds: §f" + size.getUnits() + (size.getUnits() == 1 ? " unit" : " units")));
    }
}
