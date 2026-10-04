package net.bananacheese.witchcraft.item.custom;

import net.bananacheese.witchcraft.inventory.PotionPouchItemHandler;
import net.bananacheese.witchcraft.inventory.PotionPouchMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;

public class PotionPouch extends Item {
    public PotionPouch(Properties properties) {
        super(properties.stacksTo(1)); // Pouch itself does not stack
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pouchStack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            PotionPouchItemHandler handler = new PotionPouchItemHandler();

            // Deserialize pouch contents from ItemStack NBT Data Component
            CustomData customData = pouchStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            handler.deserializeNBT(player.registryAccess(), customData.copyTag());

            int heldSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : -1;

            serverPlayer.openMenu(new SimpleMenuProvider((containerId, playerInventory, p) ->
                    new PotionPouchMenu(containerId, playerInventory, handler, heldSlot) {
                        @Override
                        public void removed(Player playerToRemove) {
                            super.removed(playerToRemove);
                            // Save pouch contents back to ItemStack when GUI is closed
                            CustomData updatedData = CustomData.of(handler.serializeNBT(playerToRemove.registryAccess()));
                            pouchStack.set(DataComponents.CUSTOM_DATA, updatedData);
                        }
                    }, Component.literal("Potion Pouch")));
        }

        return InteractionResultHolder.sidedSuccess(pouchStack, level.isClientSide());
    }
}
