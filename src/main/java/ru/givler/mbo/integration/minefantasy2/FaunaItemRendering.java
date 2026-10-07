package ru.givler.mbo.integration.minefantasy2;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import org.apache.logging.log4j.LogManager;

/** Locate MF2's separate haft layer and preserve the equipped renderer's weapon dimensions. */
public final class FaunaItemRendering {
  private static final float[] DEFAULT_GRIP = {.125F, .125F, -45F};
  private static final Map<IIcon, float[]> SPRITE_GRIPS = new WeakHashMap<IIcon, float[]>();
  private static final Map<IItemRenderer, float[]> TRANSFORMS =
      new IdentityHashMap<IItemRenderer, float[]>();

  private static final Set<String> LOGGED_ITEMS = new HashSet<String>();

  private FaunaItemRendering() {}

  public static IIcon spriteIcon(ItemStack stack, int pass) {
    return stack.getItem().getIcon(stack, pass);
  }

  public static void logMouthLayers(ItemStack stack, String path, float[] grip) {
    IIcon base = spriteIcon(stack, 0);
    if (base == null || !base.getIconName().startsWith("minefantasy2:")) return;
    String key = base.getIconName() + ":" + path;
    if (!LOGGED_ITEMS.add(key)) return;
    StringBuilder icons = new StringBuilder();
    int passes =
        stack.getItem().requiresMultipleRenderPasses()
            ? stack.getItem().getRenderPasses(stack.getItemDamage())
            : 1;
    for (int pass = 0; pass < passes; pass++) {
      IIcon icon = spriteIcon(stack, pass);
      if (pass > 0) icons.append(", ");
      icons.append(pass).append("=").append(icon == null ? "null" : icon.getIconName());
    }
    LogManager.getLogger("MBO fauna")
        .info(
            "[Fox mouth] item={} path={} grip=({}, {}) angle={} layers=[{}]",
            base.getIconName(),
            path,
            grip[0],
            grip[1],
            grip[2],
            icons);
  }

  public static float[] spriteGrip(ItemStack stack) {
    if (!stack.getItem().requiresMultipleRenderPasses()) return DEFAULT_GRIP;
    IIcon icon = spriteIcon(stack, 1);
    if (icon == null || !icon.getIconName().endsWith("_haft")) return DEFAULT_GRIP;
    float[] cached = SPRITE_GRIPS.get(icon);
    if (cached != null) return cached;
    float[] grip = DEFAULT_GRIP;
    ResourceLocation name = new ResourceLocation(icon.getIconName());
    ResourceLocation texture =
        new ResourceLocation(
            name.getResourceDomain(), "textures/items/" + name.getResourcePath() + ".png");
    try (InputStream input =
        Minecraft.getMinecraft().getResourceManager().getResource(texture).getInputStream()) {
      BufferedImage image = ImageIO.read(input);
      if (image != null) grip = gripFromImage(image);
    } catch (IOException exception) {
      // Missing/empty haft layers use the standard tool grip.
    }
    SPRITE_GRIPS.put(icon, grip);
    return grip;
  }

  public static float[] gripFromImage(BufferedImage image) {
    double weight = 0, x = 0, y = 0;
    for (int py = 0; py < image.getHeight(); py++) {
      for (int px = 0; px < image.getWidth(); px++) {
        double alpha = (image.getRGB(px, py) >>> 24) / 255D;
        if (alpha == 0) continue;
        double sx = (px + .5) / image.getWidth(), sy = 1 - (py + .5) / image.getHeight();
        weight += alpha;
        x += sx * alpha;
        y += sy * alpha;
      }
    }
    if (weight == 0) return DEFAULT_GRIP;
    x /= weight;
    y /= weight;
    // Use the same fixed orientation as the vanilla sword; only the grip follows the haft.
    return new float[] {(float) x, (float) y, -45F};
  }

  public static float[] grip(IItemRenderer renderer, float[] sprite) {
    float[] transform = TRANSFORMS.get(renderer);
    if (transform == null) {
      transform = new float[] {1, 1, 0, 0};
      String name = renderer.getClass().getName();
      if (name.equals("minefantasy.mf2.client.render.RenderHeavyWeapon")) {
        try {
          Field scaleField = renderer.getClass().getDeclaredField("scale");
          Field offsetField = renderer.getClass().getDeclaredField("offset");
          scaleField.setAccessible(true);
          offsetField.setAccessible(true);
          float scale = scaleField.getFloat(renderer), offset = offsetField.getFloat(renderer);
          transform = new float[] {scale, scale, .25F * offset - 1, -.25F * offset};
        } catch (ReflectiveOperationException exception) {
          // Other MF2 versions retain the standard equipped transform.
        }
      } else if (name.equals("minefantasy.mf2.client.render.RenderSpear")) {
        transform = new float[] {3, 3, -1, -1};
      } else if (name.equals("minefantasy.mf2.client.render.RenderRapier")) {
        transform = new float[] {1, 1, -.8F, -.4F};
      }
      TRANSFORMS.put(renderer, transform);
    }
    // MF2's renderItemIn2D calls mirror U, unlike the direct mouth sprite path.
    return new float[] {
      transform[2] + transform[0] * (1 - sprite[0]), transform[3] + transform[1] * sprite[1]
    };
  }
}
