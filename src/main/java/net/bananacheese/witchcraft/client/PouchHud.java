package net.bananacheese.witchcraft.client;

import net.bananacheese.witchcraft.component.WTComponents;
import net.bananacheese.witchcraft.inventory.PotionPouchItemHandler;
import net.bananacheese.witchcraft.item.custom.PotionPouch;
import net.bananacheese.witchcraft.item.custom.ThrowableProceduralPotion;
import net.bananacheese.witchcraft.potion.ProceduralPotionData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;


public final class PouchHud {
    private static final int SLOT_SIZE = 20;
    private static final int SLOT_GAP = 2;
    private static final int PADDING = 6;

    private static final int GOLD = 0xFFFFD34D;
    private static final int PANEL_BG = 0xC0101018;
    private static final int PANEL_BORDER = 0xFF7A5AA6;

    private static final ItemStack[] EMPTY_SLOTS = emptySlots();
    private static final ItemStack[] cache = emptySlots();
    private static CustomData cacheKey = null;

    private PouchHud() {
    }

    private static ItemStack[] emptySlots() {
        ItemStack[] slots = new ItemStack[PotionPouch.SLOTS];
        java.util.Arrays.fill(slots, ItemStack.EMPTY);
        return slots;
    }

    /**
     * The pouch contents live in CUSTOM_DATA NBT. Deserialising every frame would be wasteful, so re-read only
     * when the CustomData instance changes (it is immutable; a sync from the server produces a new one).
     */
    private static ItemStack[] slotsOf(ItemStack pouch, HolderLookup.Provider registries) {
        CustomData data = pouch.get(DataComponents.CUSTOM_DATA);
        if (data == null) return EMPTY_SLOTS;

        if (data != cacheKey) {
            PotionPouchItemHandler handler = PotionPouch.readContents(pouch, registries);
            for (int i = 0; i < PotionPouch.SLOTS; i++) {
                cache[i] = handler.getStackInSlot(i).copy();
            }
            cacheKey = data;
        }
        return cache;
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || mc.screen != null) return;

        ItemStack pouch = PotionPouch.getHeldPouch(player);
        if (pouch.isEmpty()) return;

        ItemStack[] slots = slotsOf(pouch, player.registryAccess());
        int active = PouchClientHandler.displayedSlot(pouch);

        if (PouchClientHandler.isSelecting()) {
            renderPopup(graphics, mc.font, slots, active);
        } else {
            renderIndicator(graphics, mc.font, slots, active);
        }
    }

    // ------------------------------------------------------------------ compact indicator

    private static void renderIndicator(GuiGraphics graphics, Font font, ItemStack[] slots, int active) {
        int x = graphics.guiWidth() / 2 + 14;
        int y = graphics.guiHeight() / 2 - 10;

        graphics.fill(x - 1, y - 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0x90000000);
        graphics.renderOutline(x - 1, y - 1, SLOT_SIZE, SLOT_SIZE, PANEL_BORDER);

        ItemStack stack = slots[active];
        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x + 1, y + 1);
            graphics.renderItemDecorations(font, stack, x + 1, y + 1);
        }
        drawSlotNumber(graphics, font, active + 1, x, y, stack.isEmpty() ? 0xFF777788 : 0xFFFFFFFF);
    }

    // ------------------------------------------------------------------ full popup

    private static void renderPopup(GuiGraphics graphics, Font font, ItemStack[] slots, int active) {
        int rowWidth = PotionPouch.SLOTS * SLOT_SIZE + (PotionPouch.SLOTS - 1) * SLOT_GAP;
        int panelWidth = rowWidth + PADDING * 2;

        List<Component> info = describe(slots[active]);
        int infoHeight = info.size() * (font.lineHeight + 1);
        int panelHeight = PADDING + SLOT_SIZE + 2 + font.lineHeight + 4 + infoHeight + PADDING - 2;

        int x0 = (graphics.guiWidth() - panelWidth) / 2;
        int y0 = graphics.guiHeight() / 2 + 22;

        graphics.fill(x0, y0, x0 + panelWidth, y0 + panelHeight, PANEL_BG);
        graphics.renderOutline(x0, y0, panelWidth, panelHeight, PANEL_BORDER);

        for (int i = 0; i < PotionPouch.SLOTS; i++) {
            int sx = x0 + PADDING + i * (SLOT_SIZE + SLOT_GAP);
            int sy = y0 + PADDING;
            boolean isActive = i == active;

            graphics.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, isActive ? 0xFF3A2A55 : 0xFF202030);

            ItemStack stack = slots[i];
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, sx + 2, sy + 2);
                graphics.renderItemDecorations(font, stack, sx + 2, sy + 2);
            }
            if (isActive) {
                graphics.renderOutline(sx - 1, sy - 1, SLOT_SIZE + 2, SLOT_SIZE + 2, GOLD);
            }

            graphics.drawCenteredString(font, String.valueOf(i + 1), sx + SLOT_SIZE / 2, sy + SLOT_SIZE + 2,
                    isActive ? GOLD : 0xFF9A9AAA);
        }

        int ty = y0 + PADDING + SLOT_SIZE + 2 + font.lineHeight + 4;
        for (Component line : info) {
            graphics.drawCenteredString(font, line, x0 + panelWidth / 2, ty, 0xFFFFFFFF);
            ty += font.lineHeight + 1;
        }
    }

    /** Name, effects and size/form of the active potion, as centred popup lines. */
    private static List<Component> describe(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        ProceduralPotionData data = stack.get(WTComponents.PROCEDURAL_POTION.get());
        if (stack.isEmpty() || data == null) {
            lines.add(Component.literal("Empty").withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
            return lines;
        }

        lines.add(stack.getHoverName().copy().withStyle(style -> style.withColor(TextColor.fromRgb(lighten(data.color())))));

        StringBuilder effects = new StringBuilder();
        if (data.profile().harmful()) {
            effects.append(data.elementSummary());
        }
        for (MobEffectInstance effect : data.toEffectInstances()) {
            if (!effects.isEmpty()) effects.append(", ");
            effects.append(Component.translatable(effect.getDescriptionId()).getString());
            if (effect.getAmplifier() > 0) effects.append(' ').append(effect.getAmplifier() + 1);
        }
        if (effects.isEmpty()) effects.append(data.profile().isNone() ? "No effects" : data.elementSummary());
        lines.add(Component.literal(Minecraft.getInstance().font.plainSubstrByWidth(effects.toString(), 150))
                .withStyle(net.minecraft.ChatFormatting.GRAY));

        String form = stack.getItem() instanceof ThrowableProceduralPotion t
                ? (t.isLingering() ? "Lingering" : "Splash") : "Drink";
        String size = data.size().name().charAt(0) + data.size().name().substring(1).toLowerCase();
        String element = data.profile().isNone() ? "" : " \u2022 " + data.profile().element().getLabel();
        lines.add(Component.literal(size + " \u2022 " + form + element).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        return lines;
    }

    // ------------------------------------------------------------------ helpers

    /** Brightens a potion colour toward white so dark potions stay readable as text. */
    private static int lighten(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r += (255 - r) / 2;
        g += (255 - g) / 2;
        b += (255 - b) / 2;
        return (r << 16) | (g << 8) | b;
    }

    /** Small slot number in the top-left of a slot, drawn above the item. */
    private static void drawSlotNumber(GuiGraphics graphics, Font font, int number, int x, int y, int color) {
        graphics.pose().pushPose();
        graphics.pose().translate(x + 1, y, 200);
        graphics.pose().scale(0.75f, 0.75f, 1f);
        graphics.drawString(font, String.valueOf(number), 0, 0, color, true);
        graphics.pose().popPose();
    }
}
