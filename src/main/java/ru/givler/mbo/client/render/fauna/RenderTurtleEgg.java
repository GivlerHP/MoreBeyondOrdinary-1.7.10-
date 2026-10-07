package ru.givler.mbo.client.render.fauna;

import com.google.gson.Gson;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import ru.givler.mbo.block.fauna.BlockTurtleEgg;

/** Original egg cuboids and per-face atlas regions, rather than standard block UVs. */
public final class RenderTurtleEgg implements ISimpleBlockRenderingHandler {
  private static final Definition[] MODELS = new Definition[4];

  static {
    for (int i = 0; i < MODELS.length; i++) {
      String path = "/assets/mbo/models/fauna/turtle_eggs_" + (i + 1) + ".json";
      try (InputStreamReader reader =
          new InputStreamReader(
              RenderTurtleEgg.class.getResourceAsStream(path), StandardCharsets.UTF_8)) {
        MODELS[i] = new Gson().fromJson(reader, Definition.class);
      } catch (Exception error) {
        throw new IllegalStateException("Missing turtle egg model: " + path, error);
      }
    }
  }

  private final int id;

  public RenderTurtleEgg(int id) {
    this.id = id;
  }

  public int getRenderId() {
    return id;
  }

  public boolean shouldRender3DInInventory(int modelId) {
    return false;
  }

  public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {}

  public boolean renderWorldBlock(
      IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
    int meta = world.getBlockMetadata(x, y, z);
    IIcon icon = block.getIcon(1, meta);
    Tessellator tess = Tessellator.instance;
    tess.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
    for (Element egg : MODELS[BlockTurtleEgg.count(meta) - 1].elements) {
      double a = x + egg.from[0] / 16D, b = y + egg.from[1] / 16D, c = z + egg.from[2] / 16D;
      double d = x + egg.to[0] / 16D, e = y + egg.to[1] / 16D, f = z + egg.to[2] / 16D;
      for (Map.Entry<String, Face> entry : egg.faces.entrySet()) {
        String side = entry.getKey();
        double[][] vertices;
        float light;
        if (side.equals("up")) {
          vertices = new double[][] {{a, e, c}, {a, e, f}, {d, e, f}, {d, e, c}};
          light = 1;
        } else if (side.equals("down")) {
          vertices = new double[][] {{a, b, f}, {a, b, c}, {d, b, c}, {d, b, f}};
          light = .5F;
        } else if (side.equals("north")) {
          vertices = new double[][] {{d, b, c}, {a, b, c}, {a, e, c}, {d, e, c}};
          light = .8F;
        } else if (side.equals("south")) {
          vertices = new double[][] {{a, b, f}, {d, b, f}, {d, e, f}, {a, e, f}};
          light = .8F;
        } else if (side.equals("west")) {
          vertices = new double[][] {{a, b, c}, {a, b, f}, {a, e, f}, {a, e, c}};
          light = .6F;
        } else {
          vertices = new double[][] {{d, b, f}, {d, b, c}, {d, e, c}, {d, e, f}};
          light = .6F;
        }
        tess.setColorOpaque_F(light, light, light);
        float[] uv = entry.getValue().uv;
        double u0 = icon.getInterpolatedU(uv[0]), v0 = icon.getInterpolatedV(uv[1]);
        double u1 = icon.getInterpolatedU(uv[2]), v1 = icon.getInterpolatedV(uv[3]);
        for (int i = 0; i < 4; i++)
          tess.addVertexWithUV(
              vertices[i][0],
              vertices[i][1],
              vertices[i][2],
              i == 0 || i == 3 ? u0 : u1,
              i < 2 ? v1 : v0);
      }
    }
    return true;
  }

  public static int modelEggCount(int count) {
    return MODELS[count - 1].elements.length;
  }

  private static final class Definition {
    Element[] elements;
  }

  private static final class Element {
    float[] from, to;
    Map<String, Face> faces;
  }

  private static final class Face {
    float[] uv;
  }
}
