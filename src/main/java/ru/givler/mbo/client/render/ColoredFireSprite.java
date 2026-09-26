package ru.givler.mbo.client.render;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.util.ResourceLocation;

/** Builds a coloured atlas sprite in memory from one animated grayscale fire texture. */
public final class ColoredFireSprite extends TextureAtlasSprite {
  private final int layer;
  private final int color;
  private final boolean dark;
  private final boolean translucent;

  public ColoredFireSprite(String name, int layer, int color, boolean dark) {
    this(name, layer, color, dark, false);
  }

  public ColoredFireSprite(String name, int layer, int color, boolean dark, boolean translucent) {
    super(name);
    this.layer = layer;
    this.color = color;
    this.dark = dark;
    this.translucent = translucent;
  }

  @Override
  public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) { return true; }

  @Override
  public boolean load(IResourceManager manager, ResourceLocation ignored) {
    ResourceLocation source =
        new ResourceLocation("mbo", "textures/blocks/admin_fire/fire_layer_" + layer + ".png");
    try {
      IResource resource = manager.getResource(source);
      BufferedImage image;
      InputStream stream = resource.getInputStream();
      try {
        image = ImageIO.read(stream);
      } finally {
        stream.close();
      }
      tint(image);
      AnimationMetadataSection animation =
          (AnimationMetadataSection) resource.getMetadata("animation");
      loadSprite(new BufferedImage[] {image}, animation, false);
      return false;
    } catch (IOException exception) {
      throw new RuntimeException("Could not generate coloured admin fire", exception);
    }
  }

  private void tint(BufferedImage image) {
    int red = color >> 16 & 255, green = color >> 8 & 255, blue = color & 255;
    int peak = Math.max(red, Math.max(green, blue));
    float scale = dark ? .38F : 255F / Math.max(1, peak);
    for (int y = 0; y < image.getHeight(); y++)
      for (int x = 0; x < image.getWidth(); x++) {
        int pixel = image.getRGB(x, y);
        int alpha = pixel >>> 24;
        if (translucent) alpha = Math.round(alpha * .65F);
        int gray = pixel >> 16 & 255;
        int r = Math.min(255, Math.round(gray * red * scale / 255F));
        int g = Math.min(255, Math.round(gray * green * scale / 255F));
        int b = Math.min(255, Math.round(gray * blue * scale / 255F));
        image.setRGB(x, y, alpha << 24 | r << 16 | g << 8 | b);
      }
  }

  @Override
  @SuppressWarnings("unchecked")
  public void generateMipmaps(int levels) {
    for (int frame = 0; frame < framesTextureData.size(); frame++) {
      int[][] data = (int[][]) framesTextureData.get(frame);
      if (data.length < levels + 1)
        framesTextureData.set(frame, Arrays.copyOf(data, levels + 1));
    }
    super.generateMipmaps(levels);
  }
}
