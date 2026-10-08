package net.bananacheese.witchcraft.item.custom;

import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.entity.ThrownProceduralPotion;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class ThrowableProceduralPotion extends ProceduralPotion {
    private final boolean lingering;

    public ThrowableProceduralPotion(Properties properties, boolean lingering) {
        super(properties);
        this.lingering = lingering;
    }

    public boolean isLingering() {
        return lingering;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.has(WTComponents.PROCEDURAL_POTION.get())) {
            return InteractionResultHolder.pass(stack);
        }

        throwFrom(level, player, stack);
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * Throws one of {@code potion} from the player (sound, projectile, stat). Does NOT consume anything,
     * so the Potion Pouch can throw a potion out of one of its own slots.
     */
    public void throwFrom(Level level, Player player, ItemStack potion) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                lingering ? SoundEvents.LINGERING_POTION_THROW : SoundEvents.SPLASH_POTION_THROW,
                SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            ThrownProceduralPotion thrown = new ThrownProceduralPotion(level, player);
            thrown.setItem(potion.copyWithCount(1));
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
            level.addFreshEntity(thrown);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
    }
}
