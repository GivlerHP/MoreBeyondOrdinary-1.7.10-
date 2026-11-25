package ru.givler.mbo.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.client.event.TextureStitchEvent;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.client.settings.ItemSpriteSettings;

/** Draws side faces only where a visible texel meets the outside of an atlas sprite. */
public final class ItemSpriteOutline {
  // TextureAtlasSprite.initSprite insets each endpoint of the face UV by 0.01 texel.
  private static final double ATLAS_INSET = 0.01;
  // Cover rounding at the shared edge between the side quad and the textured plane.
  private static final float SIDE_DEPTH_OVERLAP = 0.0005F;
  private static final Map<SpriteBounds, TextureAtlasSprite> BLOCK_SPRITES = new HashMap<>();
  private static final Map<SpriteBounds, TextureAtlasSprite> ITEM_SPRITES = new HashMap<>();
  private static final Map<TextureAtlasSprite, List<Edge>> OUTLINES =
      Collections.synchronizedMap(new WeakHashMap<>());
  private static TextureMap blockAtlas;
  private static TextureMap itemAtlas;
  private static boolean reportedFirstDraw;
  private static int firstPersonDepth;

  public static void enterFirstPerson() {
    firstPersonDepth++;
  }

  public static void exitFirstPerson() {
    firstPersonDepth--;
  }

  private static boolean isEnabledForCurrentRender() {
    ItemSpriteSettings.Mode mode = ItemSpriteSettings.mode();
    return mode == ItemSpriteSettings.Mode.ALL
        || (mode == ItemSpriteSettings.Mode.FIRST_PERSON && firstPersonDepth > 0);
  }

  public static float cacheThickness(float thickness) {
    return isEnabledForCurrentRender() && thickness > 0 ? -thickness : thickness;
  }

  public static void beforeClearFrames(Object value) {
    TextureAtlasSprite sprite = (TextureAtlasSprite) value;
    if (sprite.getFrameCount() != 1) return;
    List<Edge> outline = readOutline(sprite);
    if (outline != null) OUTLINES.put(sprite, outline);
  }

  @SubscribeEvent
  public void onTextureStitch(TextureStitchEvent.Post event) {
    if (Boolean.getBoolean("mbo.disableItemSpriteOutline")) return;
    TextureMap atlas = event.map;
    if (atlas.getTextureType() == 0) {
      indexAtlas(atlas, BLOCK_SPRITES);
      blockAtlas = atlas;
    } else if (atlas.getTextureType() == 1) {
      indexAtlas(atlas, ITEM_SPRITES);
      itemAtlas = atlas;
    }
  }

  private static void indexAtlas(TextureMap atlas, Map<SpriteBounds, TextureAtlasSprite> index) {
    index.clear();
    Map<?, ?> uploaded = ReflectionHelper.getPrivateValue(TextureMap.class, atlas,
        "mapUploadedSprites", "field_94252_e");
    Field rotated = ReflectionHelper.findField(TextureAtlasSprite.class, "rotated", "field_130222_e");
    for (Object value : uploaded.values()) {
      if (!(value instanceof TextureAtlasSprite)) continue;
      TextureAtlasSprite sprite = (TextureAtlasSprite) value;
      try {
        // Leave animated, rotated and custom atlas layouts on their existing rendering path.
        if (rotated.getBoolean(sprite)
            || (sprite.getFrameCount() != 1 && !OUTLINES.containsKey(sprite))) continue;
      } catch (IllegalAccessException exception) {
        throw new IllegalStateException("Could not read atlas sprite orientation", exception);
      }
      index.put(new SpriteBounds(sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV()), sprite);
    }
  }

  public static boolean drawSides(Object buffer, float leftU, float topV, float rightU,
      float bottomV, int width, int height, float thickness) {
    if (!isEnabledForCurrentRender()) return false;
    if (width <= 0 || height <= 0 || !(thickness > 0)
        || leftU == rightU || bottomV == topV) return false;

    SpriteBounds bounds = new SpriteBounds(Math.min(leftU, rightU), Math.max(leftU, rightU),
        Math.min(topV, bottomV), Math.max(topV, bottomV));
    TextureAtlasSprite block = BLOCK_SPRITES.get(bounds);
    TextureAtlasSprite item = ITEM_SPRITES.get(bounds);
    if (block == null && item == null) return false;
    int boundTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
    TextureAtlasSprite sprite = null;
    if (block != null && blockAtlas.getGlTextureId() == boundTexture) sprite = block;
    if (item != null && itemAtlas.getGlTextureId() == boundTexture) sprite = item;
    if (sprite == null || sprite.getIconWidth() != width || sprite.getIconHeight() != height) return false;

    List<Edge> outline = OUTLINES.get(sprite);
    if (outline == null && sprite.getFrameCount() == 1) {
      outline = readOutline(sprite);
      if (outline != null) OUTLINES.put(sprite, outline);
    }
    if (outline == null) return false;

    if (outline.isEmpty()) return true;
    boolean flipX = leftU > rightU;
    boolean flipY = bottomV > topV;
    Tessellator mesh = (Tessellator) buffer;
    for (Edge edge : outline) {
      edge.draw(mesh, bounds, width, height, thickness, flipX, flipY);
    }
    if (!reportedFirstDraw) {
      reportedFirstDraw = true;
      System.out.println("[MBO] Drawing item sprite outline for " + sprite.getIconName());
    }
    return true;
  }

  private static List<Edge> readOutline(TextureAtlasSprite sprite) {
    int width = sprite.getIconWidth();
    int height = sprite.getIconHeight();
    int[][] frame = sprite.getFrameTextureData(0);
    if (width <= 0 || height <= 0 || frame == null || frame.length == 0 || frame[0] == null
        || frame[0].length != (long) width * height) return null;
    return buildOutline(frame[0], width, height);
  }

  private static List<Edge> buildOutline(int[] pixels, int width, int height) {
    List<Edge> edges = new ArrayList<>();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (!visible(pixels[y * width + x])) continue;
        if (x == 0 || !visible(pixels[y * width + x - 1])) {
          edges.add(new Edge(x, y + 1, x, y, x, y));
        }
        if (x == width - 1 || !visible(pixels[y * width + x + 1])) {
          edges.add(new Edge(x + 1, y, x + 1, y + 1, x, y));
        }
        if (y == 0 || !visible(pixels[(y - 1) * width + x])) {
          edges.add(new Edge(x, y, x + 1, y, x, y));
        }
        if (y == height - 1 || !visible(pixels[(y + 1) * width + x])) {
          edges.add(new Edge(x + 1, y + 1, x, y + 1, x, y));
        }
      }
    }
    return edges;
  }

  private static boolean visible(int pixel) {
    // Item cutout rendering normally uses an alpha reference of 0.1.
    return (pixel >>> 24) > 25;
  }

  private static final class Edge {
    private final int x1, y1, x2, y2;
    private final int pixelX, pixelY;

    private Edge(int x1, int y1, int x2, int y2, int pixelX, int pixelY) {
      this.x1 = x1;
      this.y1 = y1;
      this.x2 = x2;
      this.y2 = y2;
      this.pixelX = pixelX;
      this.pixelY = pixelY;
    }

    private static double faceCoordinate(double texel, int size) {
      return Math.max(0, Math.min(1, (texel - ATLAS_INSET) / (size - 2 * ATLAS_INSET)));
    }

    private void draw(Tessellator mesh, SpriteBounds uv, int width, int height, float thickness,
        boolean flipX, boolean flipY) {
      double ax = faceCoordinate(flipX ? width - x1 : x1, width);
      double ay = faceCoordinate(flipY ? height - y1 : y1, height);
      double bx = faceCoordinate(flipX ? width - x2 : x2, width);
      double by = faceCoordinate(flipY ? height - y2 : y2, height);
      // Each side quad belongs to one texel. A constant UV prevents sampling a transparent neighbor.
      double u = uv.minU + (uv.maxU - uv.minU) * faceCoordinate(pixelX + 0.5, width);
      double v = uv.minV + (uv.maxV - uv.minV) * faceCoordinate(pixelY + 0.5, height);

      int dx = x2 - x1;
      int dy = y2 - y1;
      float back = -thickness - SIDE_DEPTH_OVERLAP;
      float front = SIDE_DEPTH_OVERLAP;
      mesh.setNormal(flipX ? -dy : dy, flipY ? dx : -dx, 0);
      if (flipX == flipY) {
        mesh.addVertexWithUV(ax, ay, back, u, v);
        mesh.addVertexWithUV(bx, by, back, u, v);
        mesh.addVertexWithUV(bx, by, front, u, v);
        mesh.addVertexWithUV(ax, ay, front, u, v);
      } else {
        mesh.addVertexWithUV(bx, by, back, u, v);
        mesh.addVertexWithUV(ax, ay, back, u, v);
        mesh.addVertexWithUV(ax, ay, front, u, v);
        mesh.addVertexWithUV(bx, by, front, u, v);
      }
    }
  }

  private static final class SpriteBounds {
    private final float minU, maxU, minV, maxV;

    private SpriteBounds(float minU, float maxU, float minV, float maxV) {
      this.minU = minU;
      this.maxU = maxU;
      this.minV = minV;
      this.maxV = maxV;
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof SpriteBounds)) return false;
      SpriteBounds bounds = (SpriteBounds) other;
      return minU == bounds.minU && maxU == bounds.maxU && minV == bounds.minV && maxV == bounds.maxV;
    }

    @Override
    public int hashCode() {
      int hash = Float.floatToIntBits(minU);
      hash = 31 * hash + Float.floatToIntBits(maxU);
      hash = 31 * hash + Float.floatToIntBits(minV);
      return 31 * hash + Float.floatToIntBits(maxV);
    }
  }
}
