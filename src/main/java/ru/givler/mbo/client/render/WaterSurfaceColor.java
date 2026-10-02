package ru.givler.mbo.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraftforge.client.event.TextureStitchEvent;

/** Gives shallow water a stronger blue body while retaining its animated texture. */
public final class WaterSurfaceColor {
  @SubscribeEvent
  public void onTextureStitch(TextureStitchEvent.Post event) {
    if (event.map.getTextureType() != 0) return;
    strengthen(event.map.getAtlasSprite("minecraft:water_still"));
    strengthen(event.map.getAtlasSprite("minecraft:water_flow"));
  }

  private static void strengthen(TextureAtlasSprite sprite) {
    if (sprite == null || !sprite.getIconName().startsWith("minecraft:water_")
        || sprite.getFrameCount() == 0) return;
    for (int frame = 0; frame < sprite.getFrameCount(); frame++) {
      int[][] levels = sprite.getFrameTextureData(frame);
      if (levels == null) continue;
      for (int[] pixels : levels) {
        if (pixels == null) continue;
        for (int i = 0; i < pixels.length; i++) {
          int color = pixels[i];
          int alpha = color >>> 24;
          if (alpha == 0) continue;
          // Higher coverage lets the blue water remain visible over pale riverbeds.
          int strongerAlpha = alpha + (255 - alpha) * 3 / 5;
          pixels[i] = (strongerAlpha << 24) | (color & 0x00ffffff);
        }
      }
    }

    // The atlas was uploaded before Post; refresh its first frame immediately.
    Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
    TextureUtil.uploadTextureMipmap(sprite.getFrameTextureData(0), sprite.getIconWidth(),
        sprite.getIconHeight(), sprite.getOriginX(), sprite.getOriginY(), false, false);
  }
}
