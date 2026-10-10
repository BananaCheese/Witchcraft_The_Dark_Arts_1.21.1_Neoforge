package net.bananacheese.witchcraft.item.custom;

import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.potion.ProceduralEffect;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

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

    // ---------------------------------------------------------------- drinking

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // Only start drinking if there is actually something to drink.
        if (!player.getItemInHand(hand).has(WTComponents.PROCEDURAL_POTION.get())) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    /**
     * The "drink" half: advancement trigger, effects, stat and game event. Does NOT consume the stack or
     * return a flask, so both the potion item itself and the Potion Pouch can share it.
     */
    public void applyDrink(ItemStack stack, LivingEntity entity) {
        Player player = entity instanceof Player p ? p : null;
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }

        ProceduralPotionData data = stack.get(WTComponents.PROCEDURAL_POTION.get());
        if (!entity.level().isClientSide && data != null) {
            data.applyTo(entity, player, player, 1.0);
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        entity.gameEvent(GameEvent.DRINK);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        Player player = entity instanceof Player p ? p : null;
        ProceduralPotionData data = stack.get(WTComponents.PROCEDURAL_POTION.get());

        applyDrink(stack, entity);

        // Leave behind the empty flask of the same size (falls back to a glass bottle for data-less potions).
        Item containerItem = data != null ? data.size().flask() : Items.GLASS_BOTTLE;
        boolean infinite = player != null && player.getAbilities().instabuild;
        if (!infinite) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                return new ItemStack(containerItem);
            }
            if (player != null) {
                player.getInventory().add(new ItemStack(containerItem));
            }
        }

        return stack;
    }

    // ---------------------------------------------------------------- tooltip

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ProceduralPotionData data = stack.get(WTComponents.PROCEDURAL_POTION.get());
        if (data == null) {
            tooltipComponents.add(Component.literal("Unbound Alchemy Liquid"));
            return;
        }

        tooltipComponents.add(Component.literal("§7Size: §f" + data.size().name()));

        if (!data.profile().isNone()) {
            var element = data.profile().element();
            String alignment = data.profile().harmful() ? "§cBane" : "§aBoon";
            tooltipComponents.add(Component.literal("§7Element: §f" + element.getLabel() + " §8(" + alignment + "§8)"));
            if (data.profile().harmful()) {
                tooltipComponents.add(Component.literal("§7Deals: §c" + data.elementSummary()));
                tooltipComponents.add(Component.literal("§8" + element.getHint()));
            }
        }

        if (data.effects().isEmpty()) {
            return;
        }
        tooltipComponents.add(Component.literal("§7Effects:"));

        for (ProceduralEffect effect : data.effects()) {
            int scaledDuration = (int) (effect.baseDurationTicks() * data.size().getDurationMultiplier());
            int scaledAmp = Math.round(effect.baseAmplifier() * data.size().getPotencyMultiplier());

            tooltipComponents.add(Component.literal(
                    " §8• §f" + ProceduralPotionData.effectDisplayName(effect.effectTypeId()) +
                            " §7(Lvl " + (scaledAmp + 1) + ") " +
                            "§8[" + (scaledDuration / 20) + "s]"
            ));
        }
    }
}