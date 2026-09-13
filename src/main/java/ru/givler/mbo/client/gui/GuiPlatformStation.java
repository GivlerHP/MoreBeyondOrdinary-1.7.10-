package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import ru.givler.mbo.tileentity.TileEntityPlatformStation;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketPlatformStationConfig;

public final class GuiPlatformStation extends GuiScreen {
  private final TileEntityPlatformStation station;
  private int mode;
  private GuiTextField platformId;

  public GuiPlatformStation(TileEntityPlatformStation station) {
    this.station = station;
    mode = station.getMode();
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    buttonList.clear();
    int x = width / 2 - 150, y = height / 2 - 55;
    platformId = new GuiTextField(fontRendererObj, x, y, 300, 20);
    platformId.setMaxStringLength(36);
    platformId.setText(station.getPlatformIdText());
    buttonList.add(new GuiButton(0, x, y + 30, 300, 20, modeLabel()));
    buttonList.add(new GuiButton(1, x, y + 65, 147, 20, I18n.format("mbo.platform.save")));
    buttonList.add(new GuiButton(2, x + 153, y + 65, 147, 20, I18n.format("gui.cancel")));
  }

  private String modeLabel() {
    return I18n.format("mbo.platform.station.mode") + ": "
        + I18n.format("mbo.platform.station.mode." + mode);
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button.id == 0) {
      mode = (mode + 1) % TileEntityPlatformStation.MODE_COUNT;
      button.displayString = modeLabel();
    } else if (button.id == 1) {
      PacketManager.INSTANCE.sendToServer(
          new PacketPlatformStationConfig(
              station.xCoord, station.yCoord, station.zCoord, mode, platformId.getText()));
      mc.displayGuiScreen(null);
    } else if (button.id == 2) mc.displayGuiScreen(null);
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    int y = height / 2 - 88;
    drawCenteredString(fontRendererObj, I18n.format("mbo.platform.station.title"), width / 2, y, 0xffffff);
    drawString(fontRendererObj, I18n.format("mbo.platform.uuid"), width / 2 - 150, y + 18, 0xaaaaaa);
    platformId.drawTextBox();
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override
  protected void keyTyped(char character, int key) {
    if (!platformId.textboxKeyTyped(character, key)) super.keyTyped(character, key);
  }

  @Override
  protected void mouseClicked(int x, int y, int button) {
    super.mouseClicked(x, y, button);
    platformId.mouseClicked(x, y, button);
  }

  @Override
  public void updateScreen() { platformId.updateCursorCounter(); }

  @Override
  public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

  @Override
  public boolean doesGuiPauseGame() { return false; }
}
