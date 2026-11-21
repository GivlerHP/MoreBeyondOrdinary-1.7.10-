package ru.givler.mbo.client.texture;

import cpw.mods.fml.relauncher.ReflectionHelper;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;

/** Blends weather keyframes into a stable atlas slot, including opacity. */
public final class WeatherAtlasFilm {
  private final TextureAtlasSprite sprite;
  private final int[][][] frames;
  private final int[][] output;
  private int lastFrom = -1, lastTo = -1, lastWeight = -1, lastNight = -1;

  public WeatherAtlasFilm(TextureAtlasSprite sprite, int width, int levels, int[][] pixels) {
    this.sprite = sprite;
    frames = new int[pixels.length][][];
    for (int i = 0; i < pixels.length; i++) {
      int[][] source = new int[levels + 1][];
      source[0] = pixels[i];
      frames[i] = TextureUtil.generateMipmapData(levels, width, source);
    }
    output = new int[levels + 1][];
    for (int i = 0; i <= levels; i++) output[i] = new int[frames[0][i].length];
  }

  public static int mipmapLevels(TextureMap atlas) {
    return ReflectionHelper.<Integer, TextureMap>getPrivateValue(
        TextureMap.class, atlas, "mipmapLevels", "field_147636_j");
  }

  public int frameCount() { return frames.length; }

  public static BufferedImage read(String name) throws IOException {
    ResourceLocation location = new ResourceLocation("mbo", "textures/blocks/weather/" + name + ".png");
    try (InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream()) {
      BufferedImage image = ImageIO.read(stream);
      if (image == null) throw new IOException("Invalid weather texture " + location);
      return image;
    }
  }

  public static int[] pixels(String name, int width) throws IOException {
    BufferedImage image = read(name);
    if (image.getWidth() != width || image.getHeight() != width) {
      throw new IOException("Unexpected dimensions for weather texture " + name);
    }
    return image.getRGB(0, 0, width, width, null, 0, width);
  }

  public static void bind() {
    Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
  }

  public void update(int from, int to, float fraction) {
    update(from, to, fraction, 0);
  }

  /** The first snow interval starts from the current day/night frost mixture. */
  public void update(int from, int to, float fraction, float night) {
    int weight = Math.min(32, Math.max(0, Math.round(fraction * 32)));
    int nightWeight = Math.min(32 - weight, Math.max(0, Math.round(night * (32 - weight))));
    if (weight == 32) { from = to; weight = 0; }
    if (weight == 0) to = from;
    if (nightWeight == 32) { from = to = 1; nightWeight = 0; }
    if (from == lastFrom && to == lastTo && weight == lastWeight && nightWeight == lastNight) return;
    for (int level = 0; level < output.length; level++) {
      int[] a = frames[from][level], b = frames[to][level], out = output[level];
      if (from == to && nightWeight == 0) System.arraycopy(a, 0, out, 0, a.length);
      else {
        int[] n = frames[nightWeight == 0 ? from : 1][level];
        for (int p = 0; p < out.length; p++) out[p] = blend(a[p], b[p], n[p], weight, nightWeight);
      }
    }
    TextureInterpolation.upload(sprite, output);
    lastFrom = from;
    lastTo = to;
    lastWeight = weight;
    lastNight = nightWeight;
  }

  private static int blend(int a, int b, int n, int weight, int nightWeight) {
    int aa = (a >>> 24) * (32 - weight - nightWeight);
    int ba = (b >>> 24) * weight;
    int na = (n >>> 24) * nightWeight;
    int alpha = aa + ba + na;
    if (alpha == 0) return 0;
    int rgb = 0;
    for (int shift = 0; shift <= 16; shift += 8) {
      rgb |= ((((a >>> shift) & 255) * aa + ((b >>> shift) & 255) * ba + ((n >>> shift) & 255) * na) / alpha) << shift;
    }
    return ((alpha + 16) / 32 << 24) | rgb;
  }
}
