package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public final class GuiSign extends GuiScreen {
  private final TileEntitySign sign;
  private int updateCounter;
  private int line;
  private int cursor;
  private GuiButton doneButton;

  public GuiSign(TileEntitySign sign) {
    this.sign = sign;
  }

  @Override
  public void initGui() {
    buttonList.clear();
    Keyboard.enableRepeatEvents(true);
    doneButton = new GuiButton(0, width / 2 - 100, height / 4 + 120, StatCollector.translateToLocal("gui.done"));
    buttonList.add(doneButton);
    sign.setEditable(false);
    cursor = sign.signText[line].length();
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
    if (mc.getNetHandler() != null)
      mc.getNetHandler().addToSendQueue(
          new C12PacketUpdateSign(sign.xCoord, sign.yCoord, sign.zCoord, sign.signText));
    sign.setEditable(true);
    sign.lineBeingEdited = -1;
  }

  @Override
  public void updateScreen() {
    updateCounter++;
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (!button.enabled || button.id != 0) return;
    if (sign.signText[line].isEmpty()) sign.signText[line] = " ";
    sign.markDirty();
    mc.displayGuiScreen(null);
  }

  @Override
  protected void keyTyped(char typed, int key) {
    if (key == Keyboard.KEY_UP) {
      if (line > 0) line--;
      else if (sign.signText[3].isEmpty()) {
        copyLinesDown(0, 1);
        sign.signText[0] = "";
      }
      cursor = sign.signText[line].length();
    } else if (key == Keyboard.KEY_DOWN) {
      if (line == 0 && sign.signText[0].isEmpty()) {
        copyLinesUp(0, 1);
        sign.signText[3] = "";
      } else if (line < 3) line++;
      cursor = sign.signText[line].length();
    } else if (key == Keyboard.KEY_LEFT) {
      if (cursor > 0) cursor--;
      else if (line > 0) cursor = sign.signText[--line].length();
    } else if (key == Keyboard.KEY_RIGHT) {
      if (cursor < sign.signText[line].length()) cursor++;
      else if (line < 3) {
        line++;
        cursor = 0;
      }
    } else if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
      splitLine();
    } else if (key == Keyboard.KEY_BACK) {
      backspace();
    } else if (key == Keyboard.KEY_DELETE) {
      delete();
    } else if (isCtrlKeyDown() && key == Keyboard.KEY_V) {
      paste(getClipboardString());
    } else if (ChatAllowedCharacters.isAllowedCharacter(typed) && sign.signText[line].length() < 15) {
      String text = sign.signText[line];
      sign.signText[line] = text.substring(0, cursor) + typed + text.substring(cursor);
      cursor++;
    }
    if (key == Keyboard.KEY_ESCAPE) actionPerformed(doneButton);
    cursor = Math.min(cursor, sign.signText[line].length());
    updateCounter = 0;
  }

  private void splitLine() {
    if (line >= 3) return;
    copyLinesDown(line, 1);
    String text = sign.signText[line];
    sign.signText[line + 1] = text.substring(cursor);
    sign.signText[line] = text.substring(0, cursor);
    line++;
    cursor = 0;
  }

  private void backspace() {
    String text = sign.signText[line];
    if (cursor > 0) {
      sign.signText[line] = text.substring(0, cursor - 1) + text.substring(cursor);
      cursor--;
    } else if (line > 0) {
      int previousLength = sign.signText[line - 1].length();
      if (previousLength + text.length() <= 15) {
        sign.signText[line - 1] += text;
        copyLinesUp(line, 1);
        sign.signText[3] = "";
      }
      line--;
      cursor = sign.signText[line].length();
    } else if (text.isEmpty()) {
      copyLinesUp(0, 1);
      sign.signText[3] = "";
    }
  }

  private void delete() {
    String text = sign.signText[line];
    if (cursor < text.length()) {
      sign.signText[line] = text.substring(0, cursor) + text.substring(cursor + 1);
    } else if (line < 3) {
      String next = sign.signText[line + 1];
      if (text.length() + next.length() <= 15) {
        sign.signText[line] += next;
        copyLinesUp(line + 1, 1);
        sign.signText[3] = "";
      } else {
        line++;
        cursor = 0;
      }
    }
  }

  private void paste(String clipboard) {
    if (clipboard == null) return;
    String[] pasted = clipboard.split("\\r?\\n", -1);
    for (int i = 0; i < pasted.length; i++)
      pasted[i] = ChatAllowedCharacters.filerAllowedCharacters(pasted[i]);
    copyLinesDown(line, pasted.length - 1);
    String current = sign.signText[line];
    pasted[0] = current.substring(0, cursor) + pasted[0];
    int targetLine = line + pasted.length - 1;
    int finalCursor = pasted[pasted.length - 1].length();
    pasted[pasted.length - 1] += current.substring(cursor);
    for (int i = 0; i < pasted.length && line <= 3; i++, line++)
      sign.signText[line] = pasted[i].length() > 15 ? pasted[i].substring(0, 15) : pasted[i];
    line--;
    cursor = targetLine <= 3 ? Math.min(finalCursor, sign.signText[line].length()) : sign.signText[line].length();
  }

  private void copyLinesUp(int start, int amount) {
    for (int i = start; i + amount <= 3; i++) sign.signText[i] = sign.signText[i + amount];
  }

  private void copyLinesDown(int start, int amount) {
    for (int i = 3; i >= start + amount; i--) sign.signText[i] = sign.signText[i - amount];
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(fontRendererObj, StatCollector.translateToLocal("sign.edit"), width / 2, 40, 0xFFFFFF);
    GL11.glPushMatrix();
    GL11.glTranslatef(width / 2F, 0F, 50F);
    float scale = 93.75F;
    GL11.glScalef(-scale, -scale, -scale);
    GL11.glRotatef(180F, 0F, 1F, 0F);
    if (sign.getBlockType() == Blocks.standing_sign) {
      GL11.glRotatef(sign.getBlockMetadata() * 360F / 16F, 0F, 1F, 0F);
    } else {
      int metadata = sign.getBlockMetadata();
      float rotation = metadata == 2 ? 180F : metadata == 4 ? 90F : metadata == 5 ? -90F : 0F;
      GL11.glRotatef(rotation, 0F, 1F, 0F);
    }
    GL11.glTranslatef(0F, -1.0625F, 0F);
    sign.lineBeingEdited = ((updateCounter & 15) < 8 ? 1 : 0) | (line & Short.MAX_VALUE) << 1 | (cursor & Short.MAX_VALUE) << 16;
    TileEntityRendererDispatcher.instance.renderTileEntityAt(sign, -.5D, -.75D, -.5D, 0F);
    sign.lineBeingEdited = -1;
    GL11.glPopMatrix();
    super.drawScreen(mouseX, mouseY, partialTicks);
  }
}
