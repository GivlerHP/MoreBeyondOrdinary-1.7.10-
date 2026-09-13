package ru.givler.mbo.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiPlayerInfo;
import net.minecraftforge.client.GuiIngameForge;
import ru.givler.mbo.config.PlayerPingConfig;

public final class PlayerPingRenderer {
  private static GuiPlayerInfo currentPlayer;

  private PlayerPingRenderer() {}

  public static void setCurrentPlayer(GuiPlayerInfo player) {
    currentPlayer = player;
  }

  public static void renderPing(
      GuiIngameForge gui, int x, int y, int textureX, int textureY, int width, int height) {
    GuiPlayerInfo player = currentPlayer;
    if (!PlayerPingConfig.enabled || player == null) {
      gui.drawTexturedModalRect(x, y, textureX, textureY, width, height);
      return;
    }
    int ping = player.responseTime;
    String text = PlayerPingConfig.format.replace("%d", Integer.toString(ping));
    FontRenderer font = Minecraft.getMinecraft().fontRenderer;
    if (PlayerPingConfig.showVanillaBars) {
      gui.drawTexturedModalRect(x, y, textureX, textureY, width, height);
      x -= font.getStringWidth(text) + 2;
    } else {
      x += width - font.getStringWidth(text);
    }
    font.drawStringWithShadow(
        text, x + PlayerPingConfig.xOffset, y, PlayerPingConfig.automaticColor ? colorFor(ping) : PlayerPingConfig.textColor);
  }

  private static int colorFor(int ping) {
    if (ping < 0) return 0x535353;
    if (ping < 150) return interpolate(0x00E676, 0xD6CF30, ping / 150F);
    return interpolate(0xD6CF30, 0xE53AB5, Math.min(ping - 150, 150) / 150F);
  }

  private static int interpolate(int start, int end, float amount) {
    int red = Math.round(((start >> 16) & 255) + (((end >> 16) & 255) - ((start >> 16) & 255)) * amount);
    int green = Math.round(((start >> 8) & 255) + (((end >> 8) & 255) - ((start >> 8) & 255)) * amount);
    int blue = Math.round((start & 255) + ((end & 255) - (start & 255)) * amount);
    return red << 16 | green << 8 | blue;
  }
}
