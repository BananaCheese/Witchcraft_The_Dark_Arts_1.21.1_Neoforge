package net.bananacheese.witchcraft.item.custom;

import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.inventory.PotionPouchItemHandler;
import net.bananacheese.witchcraft.inventory.PotionPouchMenu;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;

public class PotionPouch extends Item {
    public static final int SLOTS = 5;

    public PotionPouch(Properties properties) {
        super(properties.stacksTo(1)); // Pouch itself does not stack
    }

    // ------------------------------------------------------------------ storage helpers (shared client/server)

    /** Reads the pouch's slots out of its CUSTOM_DATA. Safe on the client (used for prediction and the HUD). */
    public static PotionPouchItemHandler readContents(ItemStack pouch, HolderLookup.Provider registries) {
        PotionPouchItemHandler handler = new PotionPouchItemHandler();
        CustomData data = pouch.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        handler.deserializeNBT(registries, data.copyTag());
        return handler;
    }

    /** SERVER: writes the slots back onto the pouch stack. */
    public static void writeContents(ItemStack pouch, PotionPouchItemHandler handler, HolderLookup.Provider registries) {
        pouch.set(DataComponents.CUSTOM_DATA, CustomData.of(handler.serializeNBT(registries)));
    }

    public static int getActiveSlot(ItemStack pouch) {
        return Mth.clamp(pouch.getOrDefault(WTComponents.POUCH_ACTIVE_SLOT.get(), 0), 0, SLOTS - 1);
    }

    public static void setActiveSlot(ItemStack pouch, int slot) {
        pouch.set(WTComponents.POUCH_ACTIVE_SLOT.get(), Mth.clamp(slot, 0, SLOTS - 1));
    }

    /** The pouch in the player's hands (main hand wins), or EMPTY. */
    public static ItemStack getHeldPouch(Player player) {
        if (player.getMainHandItem().getItem() instanceof PotionPouch) return player.getMainHandItem();
        if (player.getOffhandItem().getItem() instanceof PotionPouch) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }

    /** A pouch in the hands, otherwise the first one anywhere in the inventory, or EMPTY. */
    public static ItemStack findPouch(Player player) {
        ItemStack held = getHeldPouch(player);
        if (!held.isEmpty()) return held;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof PotionPouch) return stack;
        }
        return ItemStack.EMPTY;
    }

    /** SERVER: opens the pouch screen for the given pouch stack (which may be anywhere in the inventory). */
    public static void openMenu(ServerPlayer player, ItemStack pouchStack) {
        PotionPouchItemHandler handler = readContents(pouchStack, player.registryAccess());

        // Which inventory slot (0-35) holds the pouch, so the menu can stop it being picked up. -1 = offhand.
        int inventoryIndex = -1;
        NonNullList<ItemStack> items = player.getInventory().items;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == pouchStack) {
                inventoryIndex = i;
                break;
            }
        }
        final int pouchIndex = inventoryIndex;

        player.openMenu(new SimpleMenuProvider((containerId, playerInventory, p) ->
                new PotionPouchMenu(containerId, playerInventory, handler, pouchIndex) {
                    @Override
                    public void removed(Player playerToRemove) {
                        super.removed(playerToRemove);
                        // Save pouch contents back to the ItemStack when the GUI is closed
                        writeContents(pouchStack, handler, playerToRemove.registryAccess());
                    }
                }, Component.literal("Potion Pouch")));
    }

    // ------------------------------------------------------------------ right-click: use the active potion

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack pouch = player.getItemInHand(hand);

        // Sneak + right-click opens the screen (the Open Pouch keybind does the same from anywhere).
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                openMenu(serverPlayer, pouch);
            }
            return InteractionResultHolder.sidedSuccess(pouch, level.isClientSide());
        }

        int slot = getActiveSlot(pouch);
        PotionPouchItemHandler contents = readContents(pouch, player.registryAccess());
        ItemStack potion = contents.getStackInSlot(slot);

        if (potion.isEmpty() || !(potion.getItem() instanceof ProceduralPotion potionItem)) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.literal("Slot " + (slot + 1) + " is empty"), true);
            }
            return InteractionResultHolder.fail(pouch);
        }

        // Splash / lingering: throw straight away.
        if (potionItem instanceof ThrowableProceduralPotion throwable) {
            if (!level.isClientSide()) {
                throwable.throwFrom(level, player, potion);
                takeOne(pouch, contents, slot, player);
            }
            return InteractionResultHolder.sidedSuccess(pouch, level.isClientSide());
        }

        // Drinkable: start the drinking animation; finishUsingItem does the rest.
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pouch, Level level, LivingEntity entity) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return pouch;
        }

        // Re-read the slot now rather than trusting what it held when drinking started.
        int slot = getActiveSlot(pouch);
        PotionPouchItemHandler contents = readContents(pouch, level.registryAccess());
        ItemStack potion = contents.getStackInSlot(slot);

        if (!(potion.getItem() instanceof ProceduralPotion potionItem) || potionItem instanceof ThrowableProceduralPotion) {
            return pouch;
        }

        ProceduralPotionData data = potion.get(WTComponents.PROCEDURAL_POTION.get());
        potionItem.applyDrink(potion.copyWithCount(1), player);

        if (takeOne(pouch, contents, slot, player) && data != null) {
            // The emptied flask goes to the player, like drinking the potion directly.
            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(data.size().flask()));
        }
        return pouch;
    }

    /** SERVER: removes one potion from a slot and saves. Returns false (and removes nothing) in creative mode. */
    private static boolean takeOne(ItemStack pouch, PotionPouchItemHandler contents, int slot, Player player) {
        if (player.getAbilities().instabuild) return false;
        contents.extractItem(slot, 1, false);
        writeContents(pouch, contents, player.registryAccess());
        return true;
    }

    // ------------------------------------------------------------------ tooltip

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(Component.literal("Right-click: use the active potion").withStyle(ChatFormatting.GRAY));

        MutableComponent select = Component.literal("Hold ")
                .append(Component.keybind("key.wctda.pouch_select"))
                .append(" + scroll / 1-5: choose slot");
        tooltip.add(select.withStyle(ChatFormatting.GRAY));

        MutableComponent open = Component.keybind("key.wctda.pouch_open")
                .append(" or sneak + right-click: open");
        tooltip.add(open.withStyle(ChatFormatting.GRAY));
    }
}
