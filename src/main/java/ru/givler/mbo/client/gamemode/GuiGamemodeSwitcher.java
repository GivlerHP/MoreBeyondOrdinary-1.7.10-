package ru.givler.mbo.client.gamemode;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.spectator.SpectatorManager;

public final class GuiGamemodeSwitcher extends GuiScreen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
            "mbo", "textures/gui/gamemode_switcher.png");
    private static final int SLOT_SIZE = 25;
    private static final int SLOT_STEP = 30;

    private GamemodeSelection selected;
    private final boolean debugStateBefore;
    private boolean applyOnClose;
    private boolean closed;

    public GuiGamemodeSwitcher(GamemodeSelection selected, boolean debugStateBefore) {
        this.selected = selected;
        this.debugStateBefore = debugStateBefore;
    }

    public void cycle() { selected = selected.next(); }

    @Override
    public void updateScreen() {
        super.updateScreen();
        mc.gameSettings.showDebugInfo = false;
        if (!Keyboard.isKeyDown(Keyboard.KEY_F3)) {
            applyOnClose = true;
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char character, int keyCode) {
        if (keyCode == Keyboard.KEY_F4) {
            cycle();
        } else if (keyCode == Keyboard.KEY_ESCAPE) {
            applyOnClose = false;
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0) return;
        int startX = slotStartX();
        int y = height / 2 - 30;
        for (GamemodeSelection mode : GamemodeSelection.values()) {
            int x = startX + mode.ordinal() * SLOT_STEP;
            if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE) {
                selected = mode;
                applyOnClose = true;
                mc.displayGuiScreen(null);
                return;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.getTextureManager().bindTexture(TEXTURE);
        drawTexture(width / 2 - 62, height / 2 - 57, 0, 0, 125, 75);

        drawCenteredString(fontRendererObj, StatCollector.translateToLocal(selected.translationKey),
                width / 2, height / 2 - 50, 0xFFFFFF);
        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("mbo.gamemode.select_next"),
                width / 2, height / 2 + 5, 0xFFFFFF);

        int startX = slotStartX();
        int y = height / 2 - 30;
        // Draw every translucent slot before RenderItem gets a chance to alter GL state.
        for (GamemodeSelection mode : GamemodeSelection.values()) {
            int x = startX + mode.ordinal() * SLOT_STEP;
            minecraft.getTextureManager().bindTexture(TEXTURE);
            drawTexture(x, y, mode == selected ? 25 : 0, 75, SLOT_SIZE, SLOT_SIZE);
        }
        for (GamemodeSelection mode : GamemodeSelection.values()) {
            int x = startX + mode.ordinal() * SLOT_STEP;
            RenderItem.getInstance().renderItemAndEffectIntoGUI(fontRendererObj,
                    minecraft.getTextureManager(), mode.icon, x + 5, y + 5);
        }
    }

    private int slotStartX() {
        return width / 2 - ((GamemodeSelection.values().length - 1) * SLOT_STEP + SLOT_SIZE) / 2;
    }

    private static void drawTexture(int x, int y, int u, int v, int width, int height) {
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        func_146110_a(x, y, u, v, width, height, 128.0F, 128.0F);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void onGuiClosed() {
        if (closed) return;
        closed = true;
        mc.gameSettings.showDebugInfo = debugStateBefore;
        if (applyOnClose && mc.thePlayer != null) {
            // Stop client-side noclip immediately; the server confirmation follows in its state packet.
            if (selected != GamemodeSelection.SPECTATOR) {
                SpectatorManager.setClientState(mc.thePlayer.getUniqueID(), false);
                mc.thePlayer.noClip = false;
            }
            mc.thePlayer.sendChatMessage("/gamemode " + selected.command);
        }
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
