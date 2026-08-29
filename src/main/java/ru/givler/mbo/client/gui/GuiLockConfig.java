package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import ru.givler.mbo.lockable.LockDifficulty;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketLockBarrierSettings;

public class GuiLockConfig extends GuiScreen {
    private final int x, y, z;
    private int difficulty, delay, radius;
    private GuiTextField delayField, radiusField;

    public GuiLockConfig(int x, int y, int z, int difficulty, int delay, int radius) {
        this.x = x; this.y = y; this.z = z;
        this.difficulty = difficulty; this.delay = delay; this.radius = radius;
    }

    @Override public void initGui() {
        Keyboard.enableRepeatEvents(true);
        int left = width / 2 - 100;
        buttonList.add(new GuiButton(1, left, height / 2 - 54, 200, 20, difficultyLabel()));
        delayField = numberField(left, height / 2 - 18, delay);
        radiusField = numberField(left, height / 2 + 18, radius);
        buttonList.add(new GuiButton(4, left, height / 2 + 48, 200, 20, I18n.format("gui.done")));
    }

    private GuiTextField numberField(int x, int y, int value) {
        GuiTextField field = new GuiTextField(fontRendererObj, x, y, 200, 18);
        field.setText(Integer.toString(value)); field.setMaxStringLength(8);
        return field;
    }

    private String difficultyLabel() {
        return I18n.format("mbo.lock.gui.difficulty.value",
                I18n.format("mbo.lock.difficulty." + LockDifficulty.byOrdinal(difficulty).name().toLowerCase()));
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            difficulty = (difficulty + 1) % 4; button.displayString = difficultyLabel();
        } else if (button.id == 4) {
            delay = value(delayField, delay, 0, 86400);
            radius = value(radiusField, radius, 0, 128);
            PacketManager.INSTANCE.sendToServer(new PacketLockBarrierSettings(x, y, z, difficulty, delay, radius));
            mc.displayGuiScreen(null);
        }
    }

    private int value(GuiTextField field, int fallback, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(field.getText()))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    @Override protected void mouseClicked(int mx, int my, int button) {
        super.mouseClicked(mx, my, button); delayField.mouseClicked(mx, my, button); radiusField.mouseClicked(mx, my, button);
    }

    @Override protected void keyTyped(char ch, int key) {
        if (numericKey(delayField, ch, key) || numericKey(radiusField, ch, key)) return;
        super.keyTyped(ch, key);
    }

    private boolean numericKey(GuiTextField field, char ch, int key) {
        if (!field.isFocused()) return false;
        if (key == Keyboard.KEY_ESCAPE) return false;
        if (Character.isDigit(ch) || key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE
                || key == Keyboard.KEY_LEFT || key == Keyboard.KEY_RIGHT || key == Keyboard.KEY_HOME || key == Keyboard.KEY_END
                || (isCtrlKeyDown() && (key == Keyboard.KEY_A || key == Keyboard.KEY_C || key == Keyboard.KEY_V || key == Keyboard.KEY_X)))
            field.textboxKeyTyped(ch, key);
        return true;
    }

    @Override public void updateScreen() { delayField.updateCursorCounter(); radiusField.updateCursorCounter(); }

    @Override public void drawScreen(int mx, int my, float partial) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("mbo.lock.gui.settings"), width / 2, height / 2 - 78, 0xffffff);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.autoClose.label"), width / 2 - 100, height / 2 - 29, 0xdddddd);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.playerRadius.label"), width / 2 - 100, height / 2 + 7, 0xdddddd);
        super.drawScreen(mx, my, partial); delayField.drawTextBox(); radiusField.drawTextBox();
    }

    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); super.onGuiClosed(); }
    @Override public boolean doesGuiPauseGame() { return false; }
}
