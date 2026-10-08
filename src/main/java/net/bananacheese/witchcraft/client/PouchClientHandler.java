package net.bananacheese.witchcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.bananacheese.witchcraft.WitchcraftTheDarkArts;
import net.bananacheese.witchcraft.item.custom.PotionPouch;
import net.bananacheese.witchcraft.network.OpenPouchPayload;
import net.bananacheese.witchcraft.network.PouchSelectSlotPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = WitchcraftTheDarkArts.MODID, value = Dist.CLIENT)
public final class PouchClientHandler {
    public static final String CATEGORY = "key.categories.wctda";

    public static final KeyMapping SELECT_KEY = new KeyMapping(
            "key.wctda.pouch_select", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Z, CATEGORY);

    public static final KeyMapping OPEN_KEY = new KeyMapping(
            "key.wctda.pouch_open", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);

    /** True while the select key is held with a pouch in hand and nothing else is going on. */
    private static boolean selecting = false;

    /**
     * The slot the player has picked this hold. The server echoes the real value back through the synced
     * component a moment later; while selecting we show this instead so fast scrolling does not flicker.
     */
    private static int pendingSlot = -1;

    private PouchClientHandler() {
    }

    public static boolean isSelecting() {
        return selecting;
    }

    /** The slot the HUD should highlight for this pouch. */
    public static int displayedSlot(ItemStack pouch) {
        return selecting && pendingSlot >= 0 ? pendingSlot : PotionPouch.getActiveSlot(pouch);
    }

    // ------------------------------------------------------------------ registration (mod bus)

    @SubscribeEvent
    static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(SELECT_KEY);
        event.register(OPEN_KEY);
    }

    @SubscribeEvent
    static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(WitchcraftTheDarkArts.MODID, "pouch_hud"),
                PouchHud::render);
    }

    // ------------------------------------------------------------------ input (game bus)

    /**
     * Runs at the very start of the client tick, BEFORE vanilla turns hotbar key presses into hotbar changes,
     * so consuming the 1-5 clicks here stops the hotbar from moving while we are selecting.
     */
    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            selecting = false;
            return;
        }

        ItemStack pouch = PotionPouch.getHeldPouch(player);
        boolean nowSelecting = SELECT_KEY.isDown() && mc.screen == null && !pouch.isEmpty() && !player.isUsingItem();
        if (nowSelecting && !selecting) {
            pendingSlot = PotionPouch.getActiveSlot(pouch);
        }
        selecting = nowSelecting;

        if (selecting) {
            for (int i = 0; i < PotionPouch.SLOTS; i++) {
                while (mc.options.keyHotbarSlots[i].consumeClick()) {
                    select(pouch, i);
                }
            }
        }

        // Always drain the open key so presses made while a screen was open do not fire later.
        while (OPEN_KEY.consumeClick()) {
            if (mc.screen == null && !PotionPouch.findPouch(player).isEmpty()) {
                PacketDistributor.sendToServer(new OpenPouchPayload());
            }
        }
    }

    /** While selecting, the scroll wheel cycles pouch slots instead of the hotbar. */
    @SubscribeEvent
    static void onScroll(InputEvent.MouseScrollingEvent event) {
        if (!selecting) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack pouch = PotionPouch.getHeldPouch(player);
        double delta = event.getScrollDeltaY();
        if (pouch.isEmpty() || delta == 0) return;

        int direction = delta > 0 ? -1 : 1; // scroll up = previous slot, like the hotbar
        select(pouch, Math.floorMod(pendingSlot + direction, PotionPouch.SLOTS));
        event.setCanceled(true);
    }

    private static void select(ItemStack pouch, int slot) {
        if (slot == pendingSlot) return;
        pendingSlot = slot;

        PotionPouch.setActiveSlot(pouch, slot); // immediate local prediction
        PacketDistributor.sendToServer(new PouchSelectSlotPayload(slot));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.4F));
    }
}
