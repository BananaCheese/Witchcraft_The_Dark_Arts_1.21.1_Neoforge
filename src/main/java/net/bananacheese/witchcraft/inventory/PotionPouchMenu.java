package net.bananacheese.witchcraft.inventory;

import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.init.WTMenuTypes;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class PotionPouchMenu extends AbstractContainerMenu {
    private final PotionPouchItemHandler pouchHandler;
    private final int pouchSlotIndex;

    // Client-side constructor
    public PotionPouchMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new PotionPouchItemHandler(), playerInventory.selected);
    }

    // Server-side constructor
    public PotionPouchMenu(int containerId, Inventory playerInventory, PotionPouchItemHandler handler, int pouchSlotIndex) {
        super(WTMenuTypes.POTION_POUCH_MENU.get(), containerId);
        this.pouchHandler = handler;
        this.pouchSlotIndex = pouchSlotIndex;

        // 1. Add 5 Pouch Slots (Centered horizontally in GUI)
        for (int i = 0; i < 5; i++) {
            this.addSlot(new SlotItemHandler(pouchHandler, i, 44 + (i * 18), 36) {
                @Override
                public int getMaxStackSize(ItemStack stack) {
                    ProceduralPotionData data = stack.get(WTComponents.PROCEDURAL_POTION.get());
                    if (data != null) {
                        return data.size().getPouchMaxStack(); // 8 (Large), 16 (Medium), 32 (Small)
                    }
                    return super.getMaxStackSize(stack);
                }
            });
        }

        // 2. Add Main Player Inventory Slots (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int inventoryIndex = col + row * 9 + 9;
                this.addSlot(new Slot(playerInventory, inventoryIndex, 8 + col * 18, 84 + row * 18) {
                    @Override
                    public boolean mayPickup(Player player) {
                        // The pouch may now be opened from anywhere in the inventory (keybind), not only the hotbar.
                        return inventoryIndex != pouchSlotIndex;
                    }
                });
            }
        }

        // 3. Add Player Hotbar Slots
        for (int col = 0; col < 9; col++) {
            int slotIdx = col;
            this.addSlot(new Slot(playerInventory, slotIdx, 8 + col * 18, 142) {
                @Override
                public boolean mayPickup(Player player) {
                    // Prevent player from picking up the currently held pouch item itself while open
                    return slotIdx != pouchSlotIndex;
                }
            });
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Prevent moving the active pouch item via hotbar swap keys
        if (clickType == ClickType.SWAP && button == pouchSlotIndex) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 5) {
                // Move from Pouch -> Player Inventory
                if (!this.moveItemStackTo(itemstack1, 5, 41, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Move from Player Inventory -> Pouch
                if (itemstack1.has(WTComponents.PROCEDURAL_POTION.get())) {
                    if (!this.moveItemStackTo(itemstack1, 0, 5, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}