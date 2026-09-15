package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.container.ContainerPlatformStation;
import ru.givler.mbo.tileentity.TileEntityPlatformStation;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketPlatformStationConfig;

public final class GuiPlatformStation extends GuiContainer {
  private final TileEntityPlatformStation station;
  private int mode;
  private boolean priority;
  private GuiTextField platformId;

  public GuiPlatformStation(InventoryPlayer inventory, TileEntityPlatformStation station) {
    super(new ContainerPlatformStation(inventory, station));
    this.station = station;
    mode = station.getMode();
    priority = station.isPriority();
    xSize = 320;
    ySize = 160;
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
    buttonList.add(new GuiButton(3, x, y + 55, 300, 20, priorityLabel()));
    buttonList.add(new GuiButton(1, x, y + 90, 147, 20, I18n.format("mbo.platform.save")));
    buttonList.add(new GuiButton(2, x + 153, y + 90, 147, 20, I18n.format("gui.cancel")));
  }

  private String priorityLabel() {
    return I18n.format("mbo.platform.station.priority") + ": "
        + I18n.format(priority
            ? "mbo.platform.station.priority.immediate"
            : "mbo.platform.station.priority.arrival");
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
    } else if (button.id == 3) {
      priority = !priority;
      button.displayString = priorityLabel();
    } else if (button.id == 1) {
      PacketManager.INSTANCE.sendToServer(
          new PacketPlatformStationConfig(
              station.xCoord, station.yCoord, station.zCoord, mode, priority,
              platformId.getText()));
      mc.thePlayer.closeScreen();
    } else if (button.id == 2) mc.thePlayer.closeScreen();
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
  protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
    GL11.glColor4f(1F, 1F, 1F, 1F);
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
