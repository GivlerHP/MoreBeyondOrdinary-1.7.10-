package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;
import ru.givler.mbo.movingplatform.PlatformDirection;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketPlatformAction;

public class GuiMovingPlatform extends GuiScreen {
  private final EntityMovingPlatform platform;
  private int direction, returnMode;
  private GuiTextField distance, seconds, delay;

  public GuiMovingPlatform(EntityMovingPlatform platform) {
    this.platform = platform;
    this.direction = platform.getDirectionIndex();
    this.returnMode = platform.getReturnMode();
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    buttonList.clear();
    int x = width / 2 - 100, y = height / 2 - 90;
    buttonList.add(new GuiButton(0, x, y, 200, 20, directionLabel()));
    distance = new GuiTextField(fontRendererObj, x, y + 35, 95, 20);
    distance.setText(String.valueOf(platform.getDistance()));
    seconds = new GuiTextField(fontRendererObj, x + 105, y + 35, 95, 20);
    seconds.setText(String.valueOf(Math.max(1, platform.getDurationTicks() / 20)));
    buttonList.add(new GuiButton(6, x, y + 60, 130, 20, returnLabel()));
    delay = new GuiTextField(fontRendererObj, x + 140, y + 60, 60, 20);
    delay.setText(String.valueOf(platform.getDelayTicks() / 20));
    buttonList.add(new GuiButton(1, x, y + 85, 98, 20, I18n.format("mbo.platform.save")));
    buttonList.add(new GuiButton(2, x + 102, y + 85, 98, 20, I18n.format("mbo.platform.rebuild")));
    buttonList.add(new GuiButton(3, x, y + 110, 98, 20, I18n.format("mbo.platform.goA")));
    buttonList.add(new GuiButton(4, x + 102, y + 110, 98, 20, I18n.format("mbo.platform.goB")));
    buttonList.add(new GuiButton(7, x, y + 135, 98, 20, I18n.format("mbo.platform.getA")));
    buttonList.add(new GuiButton(8, x + 102, y + 135, 98, 20, I18n.format("mbo.platform.getB")));
    buttonList.add(new GuiButton(5, x, y + 160, 98, 20, I18n.format("mbo.platform.reset")));
    buttonList.add(new GuiButton(9, x + 102, y + 160, 98, 20, I18n.format("mbo.platform.delete")));
    buttonList.add(new GuiButton(10, x, y + 185, 98, 20, I18n.format("mbo.platform.stop")));
    buttonList.add(new GuiButton(11, x + 102, y + 185, 98, 20, I18n.format("gui.cancel")));
  }

  private String directionLabel() {
    return I18n.format("mbo.platform.direction")
        + ": "
        + I18n.format(
            "mbo.platform.direction."
                + PlatformDirection.byOrdinal(direction).name().toLowerCase());
  }

  private String returnLabel() {
    return I18n.format("mbo.platform.returnMode")
        + ": "
        + I18n.format("mbo.platform.returnMode." + returnMode);
  }

  @Override
  protected void actionPerformed(GuiButton b) {
    if (b.id == 0) {
      direction = (direction + 1) % PlatformDirection.values().length;
      b.displayString = directionLabel();
      return;
    }
    if (b.id == 6) {
      returnMode = (returnMode + 1) % 3;
      b.displayString = returnLabel();
      return;
    }
    if (b.id == 11) {
      mc.displayGuiScreen(null);
      return;
    }
    int action =
        b.id == 1
            ? PacketPlatformAction.SAVE
            : b.id == 2
                ? PacketPlatformAction.REBUILD
                : b.id == 3
                    ? PacketPlatformAction.GO_A
                    : b.id == 4
                        ? PacketPlatformAction.GO_B
                        : b.id == 7
                            ? PacketPlatformAction.GET_A
                            : b.id == 8
                                ? PacketPlatformAction.GET_B
                                : b.id == 9
                                    ? PacketPlatformAction.DELETE
                                    : b.id == 10
                                        ? PacketPlatformAction.STOP
                                        : PacketPlatformAction.RESET;
    PacketManager.INSTANCE.sendToServer(
        new PacketPlatformAction(
            platform.getEntityId(),
            action,
            direction,
            number(distance.getText(), 3),
            number(seconds.getText(), 3),
            returnMode,
            number(delay.getText(), 0)));
    mc.displayGuiScreen(null);
  }

  private static int number(String s, int fallback) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return fallback;
    }
  }

  @Override
  protected void keyTyped(char c, int key) {
    if (distance.textboxKeyTyped(c, key)
        || seconds.textboxKeyTyped(c, key)
        || delay.textboxKeyTyped(c, key)) return;
    super.keyTyped(c, key);
  }

  @Override
  protected void mouseClicked(int x, int y, int button) {
    super.mouseClicked(x, y, button);
    distance.mouseClicked(x, y, button);
    seconds.mouseClicked(x, y, button);
    delay.mouseClicked(x, y, button);
  }

  @Override
  public void updateScreen() {
    distance.updateCursorCounter();
    seconds.updateCursorCounter();
    delay.updateCursorCounter();
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    drawDefaultBackground();
    int x = width / 2 - 100, y = height / 2 - 90;
    drawCenteredString(
        fontRendererObj, I18n.format("mbo.platform.title"), width / 2, y - 22, 0xffffff);
    drawString(fontRendererObj, I18n.format("mbo.platform.distance"), x, y + 25, 0xaaaaaa);
    drawString(fontRendererObj, I18n.format("mbo.platform.seconds"), x + 105, y + 25, 0xaaaaaa);
    drawString(fontRendererObj, I18n.format("mbo.platform.delay"), x + 140, y + 51, 0xaaaaaa);
    distance.drawTextBox();
    seconds.drawTextBox();
    delay.drawTextBox();
    super.drawScreen(mx, my, partial);
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }
}
