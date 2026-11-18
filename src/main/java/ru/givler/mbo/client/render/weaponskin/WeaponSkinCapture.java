package ru.givler.mbo.client.render.weaponskin;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** Captures the regular first-person hand pass without changing either inventory. */
public final class WeaponSkinCapture {
    public static final WeaponSkinCapture INSTANCE = new WeaponSkinCapture();
    private static final Field HELD = ReflectionHelper.findField(ItemRenderer.class, "itemToRender", "field_78453_b");
    private static final Field EQUIP = ReflectionHelper.findField(ItemRenderer.class, "equippedProgress", "field_78454_c");
    private static final Field PREV = ReflectionHelper.findField(ItemRenderer.class, "prevEquippedProgress", "field_78451_d");
    public static final int SIZE = 256;

    private GuiScreen screen;
    private ItemStack requested, original;
    private Consumer<int[]> callback;
    private ItemRenderer renderer;
    private float equip, previousEquip;
    private int camera;
    private boolean frame;

    private WeaponSkinCapture() {}

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    public void request(GuiScreen screen, ItemStack stack, Consumer<int[]> callback) {
        this.screen = screen;
        requested = stack.copy();
        this.callback = callback;
    }

    public void cancel(GuiScreen owner) {
        if (screen == owner) finish(null);
    }

    @SubscribeEvent
    public void tick(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase == TickEvent.Phase.END) {
            if (frame) finish(null);
        } else if (requested != null) {
            if (mc.currentScreen != screen || mc.thePlayer == null || mc.renderViewEntity != mc.thePlayer) {
                finish(null);
                return;
            }
            camera = mc.gameSettings.thirdPersonView;
            mc.gameSettings.thirdPersonView = 0;
            frame = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void hand(RenderHandEvent event) {
        if (!frame || renderer != null) return;
        ItemRenderer hand = Minecraft.getMinecraft().entityRenderer.itemRenderer;
        try {
            original = (ItemStack) HELD.get(hand);
            equip = EQUIP.getFloat(hand);
            previousEquip = PREV.getFloat(hand);
            renderer = hand;
            HELD.set(hand, requested);
            EQUIP.setFloat(hand, 1F);
            PREV.setFloat(hand, 1F);
        } catch (IllegalAccessException error) {
            finish(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void beforeHud(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.ALL) capture();
    }

    /** Also handles runs with the HUD disabled. Called before the workbench draws. */
    public void beforeScreen(GuiScreen owner) {
        if (owner == screen) capture();
    }

    private void capture() {
        if (!frame) return;
        int[] pixels = null;
        try {
            if (renderer != null) pixels = readHandArea(Minecraft.getMinecraft());
        } finally {
            finish(pixels);
        }
    }

    private void finish(int[] pixels) {
        Minecraft mc = Minecraft.getMinecraft();
        if (frame) mc.gameSettings.thirdPersonView = camera;
        frame = false;
        try {
            if (renderer != null) {
                HELD.set(renderer, original);
                EQUIP.setFloat(renderer, equip);
                PREV.setFloat(renderer, previousEquip);
            }
        } catch (IllegalAccessException error) {
            throw new IllegalStateException("Could not restore the first-person item renderer", error);
        } finally {
            renderer = null;
            original = requested = null;
            screen = null;
            Consumer<int[]> receiver = callback;
            callback = null;
            if (receiver != null) receiver.accept(pixels);
        }
    }

    private static int[] readHandArea(Minecraft mc) {
        int width = mc.displayWidth, height = mc.displayHeight;
        int side = Math.min(width, height);
        if (side < 1) return null;
        ByteBuffer bytes = BufferUtils.createByteBuffer(side * side * 4);
        // The right-hand crop keeps the visible weapon at the actual in-game scale.
        GL11.glPushClientAttrib(GL11.GL_CLIENT_PIXEL_STORE_BIT);
        try {
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, 0);
            GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS, 0);
            GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, 0);
            GL11.glReadPixels(width - side, 0, side, side, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, bytes);
        } finally {
            GL11.glPopClientAttrib();
        }
        int[] output = new int[SIZE * SIZE];
        for (int y = 0; y < SIZE; y++) {
            int row = (side - 1 - y * side / SIZE) * side;
            for (int x = 0; x < SIZE; x++) {
                int p = (row + x * side / SIZE) * 4;
                output[y * SIZE + x] = 0xff000000 | (bytes.get(p + 2) & 255) << 16
                        | (bytes.get(p + 1) & 255) << 8 | bytes.get(p) & 255;
            }
        }
        return output;
    }
}

