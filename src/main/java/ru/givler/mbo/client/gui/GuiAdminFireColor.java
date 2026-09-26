package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.block.BlockColourFire;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketAdminFireColor;

public final class GuiAdminFireColor extends GuiScreen {
  private final ItemStack stack;

  public GuiAdminFireColor(ItemStack stack) { this.stack = stack; }

  @Override
  public void initGui() {
    buttonList.clear();
    int left = width / 2 - 92, top = height / 2 - 50;
    for (int i = 0; i < 16; i++)
      buttonList.add(new ColorButton(
          i, left + (i & 3) * 46, top + (i >> 2) * 25, BlockColourFire.COLORS[i]));
    buttonList.add(new GuiButton(
        16, left, top + 102, 180, 20,
        StatCollector.translateToLocal("mbo.adminFire.vanilla")));
  }

  @Override
  protected void actionPerformed(GuiButton button) {
    if (button.id < 0 || button.id > 16) return;
    stack.setItemDamage(button.id == 16 ? 0 : button.id + 1);
    PacketManager.INSTANCE.sendToServer(new PacketAdminFireColor(button.id));
    mc.displayGuiScreen(null);
  }

  @Override
  public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    drawDefaultBackground();
    drawCenteredString(fontRendererObj, StatCollector.translateToLocal("mbo.adminFire.color"),
        width / 2, height / 2 - 72, 0xffffff);
    super.drawScreen(mouseX, mouseY, partialTicks);
  }

  @Override public boolean doesGuiPauseGame() { return false; }

  private static final class ColorButton extends GuiButton {
    private final int color;
    ColorButton(int id, int x, int y, int color) { super(id, x, y, 42, 20, ""); this.color = color; }
    @Override
    public void drawButton(net.minecraft.client.Minecraft mc, int mouseX, int mouseY) {
      super.drawButton(mc, mouseX, mouseY);
      GL11.glDisable(GL11.GL_TEXTURE_2D);
      drawRect(xPosition + 6, yPosition + 5, xPosition + width - 6, yPosition + height - 5,
          0xff000000 | color);
      GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
  }
}
