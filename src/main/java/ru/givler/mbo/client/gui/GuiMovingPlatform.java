package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.util.List;
import ru.givler.mbo.client.gui.lootcontainer.LootContainerSoundList;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;
import ru.givler.mbo.movingplatform.PlatformDirection;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketPlatformAction;

public class GuiMovingPlatform extends GuiScreen {
  private final EntityMovingPlatform platform;
  private int direction, returnMode;
  private GuiTextField platformId, distance, seconds, delay, sound;
  private List<String> sounds;
  private boolean soundListOpen;
  private boolean clipAboveSelection;
  private int soundScroll;
  private int leftX, columnWidth;

  public GuiMovingPlatform(EntityMovingPlatform platform) {
    this.platform = platform;
    this.direction = platform.getDirectionIndex();
    this.returnMode = platform.getReturnMode();
    this.clipAboveSelection = platform.isClipAboveSelection();
  }

  @Override
  public void initGui() {
    Keyboard.enableRepeatEvents(true);
    buttonList.clear();
    int gap = 12;
    columnWidth = Math.min(200, (width - 32 - gap) / 2);
    leftX = Math.max(10, (width - (columnWidth * 2 + gap)) / 2);
    int rightX = leftX + columnWidth + gap;
    int rightWidth = Math.min(columnWidth, width - rightX - 10);
    int halfWidth = (columnWidth - 8) / 2;
    platformId = new GuiTextField(fontRendererObj, leftX, 52, columnWidth, 20);
    platformId.setMaxStringLength(36);
    platformId.setText(platform.getPlatformId().toString());
    buttonList.add(new GuiButton(0, leftX, 79, columnWidth, 20, directionLabel()));
    distance = new GuiTextField(fontRendererObj, leftX, 126, halfWidth, 20);
    distance.setText(String.valueOf(platform.getDistance()));
    seconds = new GuiTextField(fontRendererObj, leftX + halfWidth + 8, 126, columnWidth - halfWidth - 8, 20);
    seconds.setText(secondsText(platform.getDurationTicks()));
    int delayWidth = 48;
    buttonList.add(new GuiButton(6, leftX, 162, columnWidth - delayWidth - 8, 20, returnLabel()));
    delay = new GuiTextField(fontRendererObj, leftX + columnWidth - delayWidth, 162, delayWidth, 20);
    delay.setText(secondsText(platform.getDelayTicks()));
    sound = new GuiTextField(fontRendererObj, leftX, 205, columnWidth - 25, 20);
    sound.setMaxStringLength(128);
    sound.setText(platform.getMovementSound());
    sounds = LootContainerSoundList.getSounds();
    buttonList.add(new GuiButton(12, leftX + columnWidth - 20, 205, 20, 20, "v"));
    buttonList.add(new GuiButton(13, leftX, 230, columnWidth, 20, clipLabel()));
    buttonList.add(
        new GuiButton(
            1, rightX, 32, rightWidth, 20,
            I18n.format(platform.isRebuildPending()
                ? "mbo.platform.saveRebuild"
                : "mbo.platform.save")));
    buttonList.add(new GuiButton(2, rightX, 56, rightWidth, 20, I18n.format("mbo.platform.rebuild")));
    buttonList.add(new GuiButton(3, rightX, 80, rightWidth, 20, I18n.format("mbo.platform.goA")));
    buttonList.add(new GuiButton(4, rightX, 104, rightWidth, 20, I18n.format("mbo.platform.goB")));
    buttonList.add(new GuiButton(7, rightX, 128, rightWidth, 20, I18n.format("mbo.platform.getStation")));
    buttonList.add(new GuiButton(5, rightX, 152, rightWidth, 20, I18n.format("mbo.platform.reset")));
    buttonList.add(new GuiButton(10, rightX, 176, rightWidth, 20, I18n.format("mbo.platform.stop")));
    buttonList.add(new GuiButton(9, rightX, 200, rightWidth, 20, I18n.format("mbo.platform.delete")));
    buttonList.add(new GuiButton(11, rightX, 224, rightWidth, 20, I18n.format("gui.cancel")));
  }

  private String directionLabel() {
    return I18n.format("mbo.platform.direction")
        + ": "
        + I18n.format(
            "mbo.platform.direction."
                + PlatformDirection.byOrdinal(direction).name().toLowerCase());
  }

  private String returnLabel() {
    return I18n.format("mbo.platform.returnMode." + returnMode);
  }

  private String clipLabel() {
    return I18n.format("mbo.platform.clipAbove", I18n.format(clipAboveSelection ? "options.on" : "options.off"));
  }

  private int dropdownTop() {
    return sound.yPosition - Math.min(8, matchingSounds().size()) * 12;
  }

  private List<String> matchingSounds() {
    return LootContainerSoundList.filter(sounds, sound.getText());
  }

  @Override
  protected void actionPerformed(GuiButton b) {
    if (b.id == 0) {
      direction = (direction + 1) % PlatformDirection.values().length;
      b.displayString = directionLabel();
      return;
    }
    if (b.id == 6) {
      returnMode = (returnMode + 1) % 4;
      b.displayString = returnLabel();
      return;
    }
    if (b.id == 12) { soundListOpen = !soundListOpen; soundScroll = 0; return; }
    if (b.id == 13) {
      clipAboveSelection = !clipAboveSelection;
      b.displayString = clipLabel();
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
                            ? PacketPlatformAction.GET_STATION
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
            decimal(seconds.getText(), 3D),
            returnMode,
            decimal(delay.getText(), 0D),
            platformId.getText(), sound.getText(), clipAboveSelection));
    mc.displayGuiScreen(null);
  }

  private static int number(String s, int fallback) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return fallback;
    }
  }

  private static double decimal(String value, double fallback) {
    try { return Double.parseDouble(value.replace(',', '.')); }
    catch (Exception ignored) { return fallback; }
  }

  private static String secondsText(int ticks) {
    double value = ticks / 20D;
    return value == Math.rint(value) ? String.valueOf((int) value) : String.valueOf(value);
  }

  @Override
  protected void keyTyped(char c, int key) {
    if (platformId.textboxKeyTyped(c, key)
        || distance.textboxKeyTyped(c, key)
        || seconds.textboxKeyTyped(c, key)
        || delay.textboxKeyTyped(c, key)
        || sound.textboxKeyTyped(c, key)) {
      soundScroll = 0;
      return;
    }
    super.keyTyped(c, key);
  }

  @Override
  protected void mouseClicked(int x, int y, int button) {
    if (soundListOpen && button == 0 && x >= leftX && x < leftX + columnWidth
        && y >= dropdownTop() && y < sound.yPosition) {
      int index = soundScroll + (y - dropdownTop()) / 12;
      List<String> matches = matchingSounds();
      if (index < matches.size()) sound.setText(matches.get(index));
      soundListOpen = false;
      soundScroll = 0;
      return;
    }
    if (soundListOpen && !(x >= leftX && x < leftX + columnWidth
        && y >= sound.yPosition && y < sound.yPosition + 20)) soundListOpen = false;
    super.mouseClicked(x, y, button);
    platformId.mouseClicked(x, y, button);
    distance.mouseClicked(x, y, button);
    seconds.mouseClicked(x, y, button);
    delay.mouseClicked(x, y, button);
    sound.mouseClicked(x, y, button);
  }

  @Override
  public void updateScreen() {
    platformId.updateCursorCounter();
    distance.updateCursorCounter();
    seconds.updateCursorCounter();
    delay.updateCursorCounter();
    sound.updateCursorCounter();
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
  }

  @Override
  public void drawScreen(int mx, int my, float partial) {
    drawDefaultBackground();
    drawCenteredString(
        fontRendererObj, I18n.format("mbo.platform.title"), width / 2, 12, 0xffffff);
    drawString(fontRendererObj, I18n.format("mbo.platform.uuid"), leftX, 40, 0xaaaaaa);
    platformId.drawTextBox();
    drawString(fontRendererObj, I18n.format("mbo.platform.distanceShort"), leftX, 114, 0xaaaaaa);
    drawString(fontRendererObj, I18n.format("mbo.platform.secondsShort"), seconds.xPosition, 114, 0xaaaaaa);
    drawString(fontRendererObj, I18n.format("mbo.platform.returnMode"), leftX, 150, 0xaaaaaa);
    drawString(fontRendererObj, I18n.format("mbo.platform.delayShort"), delay.xPosition, 150, 0xaaaaaa);
    distance.drawTextBox();
    seconds.drawTextBox();
    delay.drawTextBox();
    drawString(fontRendererObj, I18n.format("mbo.platform.sound"), leftX, sound.yPosition - 11, 0xaaaaaa);
    sound.drawTextBox();
    super.drawScreen(mx, my, partial);
    if (soundListOpen) {
      int top = dropdownTop();
      List<String> matches = matchingSounds();
      int visible = Math.min(8, matches.size());
      drawRect(leftX, top, leftX + columnWidth, top + visible * 12, 0xee101010);
      for (int row = 0; row < visible; row++) {
        int index = soundScroll + row;
        if (index >= matches.size()) break;
        if (my >= top + row * 12 && my < top + (row + 1) * 12 && mx >= leftX && mx < leftX + columnWidth)
          drawRect(leftX, top + row * 12, leftX + columnWidth, top + (row + 1) * 12, 0xff555555);
        drawString(fontRendererObj, fontRendererObj.trimStringToWidth(matches.get(index), columnWidth - 6), leftX + 3, top + row * 12 + 2, 0xffffff);
      }
    }
  }

  @Override
  public void handleMouseInput() {
    super.handleMouseInput();
    int wheel = Mouse.getEventDWheel();
    int mx = Mouse.getEventX() * width / mc.displayWidth;
    int my = height - Mouse.getEventY() * height / mc.displayHeight - 1;
    if (soundListOpen && wheel != 0 && mx >= leftX && mx < leftX + columnWidth
        && my >= dropdownTop() && my < sound.yPosition)
      soundScroll = Math.max(0, Math.min(Math.max(0, matchingSounds().size() - 8), soundScroll + (wheel < 0 ? 1 : -1)));
  }

  @Override
  public boolean doesGuiPauseGame() {
    return false;
  }
}
