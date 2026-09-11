package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketDungeonAreaDelete;
import ru.givler.mbo.network.packet.PacketDungeonEditorSettings;

public final class GuiDungeonEditor extends GuiScreen {
  private int type;
  private String areaId = "";
  private int restoreMode;
  private int initialRestoreSeconds = 10;
  private GuiButton restoreButton;
  private GuiTextField restoreSeconds;

  public GuiDungeonEditor() {
    ItemStack stack =
        net.minecraft.client.Minecraft.getMinecraft().thePlayer.getCurrentEquippedItem();
    if (stack != null && stack.hasTagCompound()) {
      type = Math.max(0, Math.min(3, stack.getTagCompound().getInteger("AreaType")));
      restoreMode = Math.max(0, Math.min(2, stack.getTagCompound().getInteger("RestoreMode")));
      if (stack.getTagCompound().hasKey("RestoreSeconds"))
        initialRestoreSeconds = Math.max(1, stack.getTagCompound().getInteger("RestoreSeconds"));
    }
  }

  public GuiDungeonEditor(String areaId, int type, int restoreMode, int restoreSeconds) {
    this.areaId = areaId == null ? "" : areaId;
    this.type = Math.max(0, Math.min(3, type));
    this.restoreMode = Math.max(0, Math.min(2, restoreMode));
    this.initialRestoreSeconds = Math.max(1, restoreSeconds);
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    buttonList.clear();
    int x = width / 2 - 100, y = height / 2 - 70;
    GuiButton typeButton = new GuiButton(0, x, y, 200, 20, typeLabel());
    typeButton.enabled = areaId.isEmpty();
    buttonList.add(typeButton);
    restoreButton = new GuiButton(1, x, y + 28, 200, 20, restoreLabel());
    buttonList.add(restoreButton);
    restoreSeconds = new GuiTextField(fontRendererObj, x, y + 70, 200, 20);
    restoreSeconds.setText(String.valueOf(initialRestoreSeconds));
    buttonList.add(new GuiButton(2, x, y + 100, 98, 20, I18n.format("mbo.platform.save")));
    buttonList.add(new GuiButton(3, x + 102, y + 100, 98, 20, I18n.format("gui.cancel")));
    if (!areaId.isEmpty())
      buttonList.add(new GuiButton(4, x, y + 124, 200, 20, I18n.format("mbo.dungeon.delete")));
    updateTypeControls();
  }

  private String typeLabel() {
    return I18n.format("mbo.dungeon.type") + ": " + I18n.format("mbo.dungeon.type." + type);
  }

  private String restoreLabel() {
    return I18n.format("mbo.dungeon.restore")
        + ": "
        + I18n.format("mbo.dungeon.restore." + restoreMode);
  }

  private void updateTypeControls() {
    boolean illusion = type == 1;
    restoreButton.visible = illusion;
    restoreSeconds.setVisible(illusion && restoreMode == 1);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button.id == 0) {
      initialRestoreSeconds = number(restoreSeconds.getText(), initialRestoreSeconds);
      type = (type + 1) % 4;
      button.displayString = typeLabel();
      updateTypeControls();
      return;
    }
    if (button.id == 1) {
      restoreMode = (restoreMode + 1) % 3;
      button.displayString = restoreLabel();
      updateTypeControls();
      return;
    }
    if (button.id == 2 && type == 3 && areaId.isEmpty()) {
      mc.displayGuiScreen(new GuiDungeonTriggerEditor());
      return;
    }
    if (button.id == 2)
      PacketManager.INSTANCE.sendToServer(
          new PacketDungeonEditorSettings(
              areaId, type, restoreMode, number(restoreSeconds.getText(), initialRestoreSeconds)));
    if (button.id == 4) PacketManager.INSTANCE.sendToServer(new PacketDungeonAreaDelete(areaId));
    mc.displayGuiScreen(null);
  }

  private static int number(String value, int fallback) {
    try {
      return Integer.parseInt(value);
    } catch (Exception ignored) {
      return fallback;
    }
  }

  @Override
  protected void keyTyped(char c, int key) {
    if (restoreSeconds.getVisible() && restoreSeconds.textboxKeyTyped(c, key)) return;
    super.keyTyped(c, key);
  }

  @Override
  protected void mouseClicked(int x, int y, int button) {
    super.mouseClicked(x, y, button);
    if (restoreSeconds.getVisible()) restoreSeconds.mouseClicked(x, y, button);
  }

  @Override
  public void updateScreen() {
    restoreSeconds.updateCursorCounter();
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    drawDefaultBackground();
    int x = width / 2 - 100, y = height / 2 - 70;
    drawCenteredString(
        fontRendererObj, I18n.format("mbo.dungeon.settings"), width / 2, y - 24, 0xffffff);
    if (restoreSeconds.getVisible()) {
      drawString(fontRendererObj, I18n.format("mbo.dungeon.restoreSeconds"), x, y + 59, 0xaaaaaa);
      restoreSeconds.drawTextBox();
    }
    super.drawScreen(mx, my, partial);
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }
}
