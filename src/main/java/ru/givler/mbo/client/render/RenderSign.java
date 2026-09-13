package ru.givler.mbo.client.render;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.client.model.ModelSign;

public final class RenderSign extends TileEntitySpecialRenderer {
  private static final ResourceLocation TEXTURE =
      new ResourceLocation("mbo", "textures/entity/sign.png");
  private final ModelSign model = new ModelSign();

  public void renderTileEntitySignAt(
      TileEntitySign sign, double x, double y, double z, float partialTicks) {
    GL11.glPushMatrix();
    float modelScale = 2F / 3F;
    boolean standing = sign.getBlockType() == Blocks.standing_sign;
    if (standing) {
      GL11.glTranslatef((float) x + .5F, (float) y + .75F * modelScale, (float) z + .5F);
      GL11.glRotatef(-(sign.getBlockMetadata() * 360F / 16F), 0F, 1F, 0F);
    } else {
      int metadata = sign.getBlockMetadata();
      float rotation = metadata == 2 ? 180F : metadata == 4 ? 90F : metadata == 5 ? -90F : 0F;
      GL11.glTranslatef((float) x + .5F, (float) y + .75F * modelScale, (float) z + .5F);
      GL11.glRotatef(-rotation, 0F, 1F, 0F);
      GL11.glTranslatef(0F, -.3125F, -.4375F);
    }
    bindTexture(TEXTURE);
    GL11.glPushMatrix();
    GL11.glScalef(modelScale, -modelScale, -modelScale);
    model.render(standing);
    GL11.glPopMatrix();

    FontRenderer font = func_147498_b();
    float textScale = (1F / 60F) * modelScale;
    GL11.glTranslatef(0F, .5F * modelScale, .07F * modelScale);
    GL11.glDepthMask(false);
    int lastLine = lastVisibleLine(sign);
    int packedCursor = sign.lineBeingEdited;
    int selectedLine = packedCursor < 0 ? -1 : packedCursor >> 1 & Short.MAX_VALUE;
    int cursorPosition = packedCursor < 0 ? 0 : packedCursor >> 16 & Short.MAX_VALUE;
    boolean cursorVisible = packedCursor >= 0 && (packedCursor & 1) != 0;
    if (selectedLine > lastLine) lastLine = selectedLine;
    if (lastLine < 2) {
      if (sign.signText[0].length() < 9 && sign.signText[1].length() < 9) textScale *= 1.8F;
      else if (sign.signText[0].length() < 11 && sign.signText[1].length() < 11)
        textScale *= 1.4F;
    }
    GL11.glScalef(textScale, -textScale, textScale);
    GL11.glNormal3f(0F, 0F, -textScale);
    int top = -5 * (lastLine + 1);
    for (int line = 0; line <= lastLine; line++) {
      String text = sign.signText[line];
      if (packedCursor >= 0) {
        String edge = text.length() < 15 ? EnumChatFormatting.GRAY.toString() : EnumChatFormatting.DARK_RED.toString();
        String decorated = edge + "- " + EnumChatFormatting.RESET + text + edge + " -";
        font.drawString(decorated, -font.getStringWidth(decorated) / 2, top + line * 10, 0xFFFFFF);
      } else {
        font.drawString(text, -font.getStringWidth(text) / 2, top + line * 10, 0xFFFFFF);
      }
      if (line == selectedLine && cursorVisible) {
        int safeCursor = Math.min(cursorPosition, text.length());
        int cursorX = -font.getStringWidth(text) / 2 + font.getStringWidth(text.substring(0, safeCursor));
        int cursorY = top + line * 10 - 1;
        Gui.drawRect(cursorX, cursorY, cursorX + 1, cursorY + font.FONT_HEIGHT + 1, 0xFFD0D0D0);
      }
    }
    GL11.glDepthMask(true);
    GL11.glColor4f(1F, 1F, 1F, 1F);
    GL11.glPopMatrix();
  }

  private static int lastVisibleLine(TileEntitySign sign) {
    int line = sign.signText.length - 1;
    while (line > 0 && sign.signText[line].isEmpty()) line--;
    return line;
  }

  @Override
  public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
    renderTileEntitySignAt((TileEntitySign) tile, x, y, z, partialTicks);
  }
}
