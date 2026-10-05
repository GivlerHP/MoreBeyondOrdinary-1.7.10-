package ru.givler.mbo.client.render;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.WorldEvent;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.waterlogging.ClientWaterloggedBlocks;
import ru.givler.mbo.waterlogging.WaterloggedBlockSupport;
import ru.givler.mbo.waterlogging.WaterloggedGeometry;
import ru.givler.mbo.core.WaterloggingCameraHooks;

public final class WaterloggedBlockRenderer {
  private static boolean renderedInTranslucentPass;
  private static final double EDGE = 0.001D;
  private static final double SOURCE_SURFACE = WaterloggedLiquidHeightHooks.sourceSurface();
  private static final ThreadLocal<double[]> CHUNK_QUAD = new ThreadLocal<double[]>();
  private static final ThreadLocal<Integer> CHUNK_VERTEX = new ThreadLocal<Integer>();

  public static void renderPaneWaterInChunk(World world, int x, int y, int z) {
    CHUNK_QUAD.set(new double[20]);
    CHUNK_VERTEX.set(0);
    try {
      renderWater(world, new ClientWaterloggedBlocks.Position(x, y, z));
    } finally {
      CHUNK_QUAD.remove();
      CHUNK_VERTEX.remove();
    }
  }

  private static void addFaceVertex(Tessellator tessellator, double x, double y, double z, double u, double v) {
    tessellator.addVertexWithUV(x, y, z, u, v);
    double[] quad = CHUNK_QUAD.get();
    if (quad == null) return;
    int vertex = CHUNK_VERTEX.get();
    int offset = vertex * 5;
    quad[offset] = x; quad[offset + 1] = y; quad[offset + 2] = z;
    quad[offset + 3] = u; quad[offset + 4] = v;
    if (++vertex == 4) {
      // Chunk rendering uses face culling; water needs the reversed quad underwater too.
      for (int i = 3; i >= 0; --i) {
        int n = i * 5;
        tessellator.addVertexWithUV(quad[n], quad[n + 1], quad[n + 2], quad[n + 3], quad[n + 4]);
      }
      vertex = 0;
    }
    CHUNK_VERTEX.set(vertex);
  }

  @SubscribeEvent
  public void unload(WorldEvent.Unload event) {
    if (event.world.isRemote) {
      ClientWaterloggedBlocks.clear();
    }
  }

  @SubscribeEvent
  public void underwaterFog(EntityViewRenderEvent.FogDensity event) {
    if (event.entity != Minecraft.getMinecraft().renderViewEntity) return;
    if (event.block.getMaterial() != Material.water) return;
    // Use the same underwater distance for terrain, sky and particles. The
    // former 96-block range made shallow rivers almost indistinguishable from air.
    float end = Math.min(48.0F,
        Math.max(16.0F, Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16.0F - 8.0F));
    GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
    GL11.glFogf(GL11.GL_FOG_START, 0.0F);
    GL11.glFogf(GL11.GL_FOG_END, end);
    event.setCanceled(true);
  }

  @SubscribeEvent
  public void underwaterColor(EntityViewRenderEvent.FogColors event) {
    if (event.entity != Minecraft.getMinecraft().renderViewEntity
        || event.block.getMaterial() != Material.water) return;
    // The 1.7.10 water branch is a very dark, height-dependent blue. Keep the
    // clear colour and fog colour identical while the camera is underwater.
    event.red = 0.055F;
    event.green = 0.145F;
    event.blue = 0.235F;
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void render(RenderWorldLastEvent event) {
    if (renderedInTranslucentPass) {
      renderedInTranslucentPass = false;
      return;
    }
    renderNow(event.partialTicks);
  }

  public static void renderInTranslucentPass(float partialTicks) {
    if (renderedInTranslucentPass) return;
    renderedInTranslucentPass = true;
    renderNow(partialTicks);
  }

  public static void renderNow(float partialTicks) {
    Minecraft minecraft = Minecraft.getMinecraft();
    World world = minecraft.theWorld;
    if (world == null || minecraft.renderViewEntity == null) return;
    int dimension = world.provider.dimensionId;
    double cameraX =
        minecraft.renderViewEntity.lastTickPosX
            + (minecraft.renderViewEntity.posX - minecraft.renderViewEntity.lastTickPosX)
                * partialTicks;
    double cameraY =
        minecraft.renderViewEntity.lastTickPosY
            + (minecraft.renderViewEntity.posY - minecraft.renderViewEntity.lastTickPosY)
                * partialTicks;
    double cameraZ =
        minecraft.renderViewEntity.lastTickPosZ
            + (minecraft.renderViewEntity.posZ - minecraft.renderViewEntity.lastTickPosZ)
                * partialTicks;

    EntityRenderer entityRenderer = minecraft.entityRenderer;
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    try {
      minecraft.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
      entityRenderer.enableLightmap(partialTicks);
      GL11.glColor4f(1F, 1F, 1F, 1F);
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glDisable(GL11.GL_CULL_FACE);
      GL11.glDepthMask(false);

      Tessellator tessellator = Tessellator.instance;
      tessellator.startDrawingQuads();
      tessellator.setTranslation(-cameraX, -cameraY, -cameraZ);
      for (ClientWaterloggedBlocks.Position position : ClientWaterloggedBlocks.all(dimension)) {
        double dx = position.x + 0.5D - cameraX;
        double dy = position.y + 0.5D - cameraY;
        double dz = position.z + 0.5D - cameraZ;
        if (dx * dx + dy * dy + dz * dz > 96D * 96D
            || !world.blockExists(position.x, position.y, position.z)) continue;
        if (world.getBlock(position.x, position.y, position.z).getMaterial() != Material.water
            && !(world.getBlock(position.x, position.y, position.z) instanceof BlockPane)
            && WaterloggedBlockSupport.canWaterlog(world, position.x, position.y, position.z))
          renderWater(world, position);
      }
      tessellator.setTranslation(0, 0, 0);
      tessellator.draw();
    } finally {
      Tessellator.instance.setTranslation(0, 0, 0);
      entityRenderer.disableLightmap(partialTicks);
      GL11.glPopAttrib();
    }
  }

  private static void renderWater(World world, ClientWaterloggedBlocks.Position position) {
    double[] heights = new double[4];
    for (int x = 0; x < 2; ++x) for (int z = 0; z < 2; ++z)
      heights[x*2+z] = (double)WaterloggedLiquidHeightHooks.cornerHeight(
          world,position.x+x,position.y,position.z+z) - (double)0.001F;
    SURFACE_CORNERS.set(heights);
    try { renderWaterBody(world,position); }
    finally { SURFACE_CORNERS.remove(); }
  }

  private static final ThreadLocal<double[]> SURFACE_CORNERS = new ThreadLocal<double[]>();

  private static double surfaceY(ClientWaterloggedBlocks.Position position, double x, double z, double maxY) {
    double[] heights = SURFACE_CORNERS.get();
    if (heights == null || maxY <= 0.5D) return position.y+maxY;
    double localX = x-position.x, localZ = z-position.z;
    return position.y + (heights[0]*(1-localZ)+heights[1]*localZ)*(1-localX)
        + (heights[2]*(1-localZ)+heights[3]*localZ)*localX;
  }

  private static void renderWaterBody(World world, ClientWaterloggedBlocks.Position position) {
    boolean[] free =
        WaterloggedGeometry.waterCellsForRender(world, position.x, position.y, position.z);
    if (world.getBlock(position.x, position.y, position.z) instanceof BlockPane
        && renderMergedPaneWater(world, position, free)) return;
    if (world.getBlock(position.x, position.y, position.z) instanceof BlockPane
        && renderPaneColumns(world, position, free)) return;
    boolean[] mergedTop = new boolean[2];
    IIcon still = Blocks.water.getIcon(1, 0);
    for (int cellY = 0; cellY < 2; cellY++) {
      boolean fullLayer = true;
      for (int cellX = 0; cellX < 2; cellX++)
        for (int cellZ = 0; cellZ < 2; cellZ++)
          fullLayer &= free[WaterloggedGeometry.index(cellX, cellY, cellZ)];
      double mergedHeight = cellMaxY(world, position, 0, cellY, 0);
      for (int cellX = 0; cellX < 2; cellX++)
        for (int cellZ = 0; cellZ < 2; cellZ++)
          fullLayer &= cellMaxY(world, position, cellX, cellY, cellZ) == mergedHeight;
      if (fullLayer && shouldRenderFace(world, position, 0, cellY, 0, 0, 1, 0)) {
        double minY = cellY == 0 ? 0.0D : 0.5D;
        double maxY = mergedHeight;
        setFaceLighting(world, position, 1, Blocks.water.colorMultiplier(world, position.x, position.y, position.z));
        renderFace(position, 0.0D, minY, 0.0D, 1.0D, maxY, 1.0D, 1, still);
        mergedTop[cellY] = true;
      }
    }
    for (int cellX = 0; cellX < 2; cellX++)
      for (int cellY = 0; cellY < 2; cellY++)
        for (int cellZ = 0; cellZ < 2; cellZ++) {
          if (!free[WaterloggedGeometry.index(cellX, cellY, cellZ)]) continue;
          renderCell(world, position, cellX, cellY, cellZ, mergedTop[cellY]);
        }
  }

  private static boolean renderMergedPaneWater(
      World world, ClientWaterloggedBlocks.Position position, boolean[] wet) {
    int minX = 2, minZ = 2, maxX = -1, maxZ = -1;
    for (int x = 0; x < 2; ++x) for (int z = 0; z < 2; ++z) {
      if (wet[WaterloggedGeometry.index(x, 0, z)] != wet[WaterloggedGeometry.index(x, 1, z)]) return false;
      if (!wet[WaterloggedGeometry.index(x, 0, z)]) continue;
      minX = Math.min(minX, x); maxX = Math.max(maxX, x);
      minZ = Math.min(minZ, z); maxZ = Math.max(maxZ, z);
    }
    if (maxX < 0) return true;
    double height = cellMaxY(world, position, minX, 1, minZ);
    for (int x = minX; x <= maxX; ++x) for (int z = minZ; z <= maxZ; ++z)
      if (!wet[WaterloggedGeometry.index(x, 0, z)]
          || cellMaxY(world, position, x, 1, z) != height) return false;
    int[][] directions = {{0,-1,0}, {0,1,0}, {0,0,-1}, {0,0,1}, {-1,0,0}, {1,0,0}};
    boolean[] visible = new boolean[6];
    for (int side = 0; side < 6; ++side) {
      boolean first = true;
      int[] d = directions[side];
      for (int x = minX; x <= maxX; ++x) for (int y = 0; y < 2; ++y) for (int z = minZ; z <= maxZ; ++z) {
        if (d[0] < 0 && x != minX || d[0] > 0 && x != maxX
            || d[1] < 0 && y != 0 || d[1] > 0 && y != 1
            || d[2] < 0 && z != minZ || d[2] > 0 && z != maxZ) continue;
        boolean cellVisible = shouldRenderFace(world, position, x, y, z, d[0], d[1], d[2]);
        if (!first && cellVisible != visible[side]) return false;
        visible[side] = cellVisible; first = false;
      }
    }
    double[] low = WaterloggedGeometry.paneWaterCellBounds(wet, minX, 0, minZ);
    double[] high = WaterloggedGeometry.paneWaterCellBounds(wet, maxX, 0, maxZ);
    int color = Blocks.water.colorMultiplier(world, position.x, position.y, position.z);
    // One quad per face instead of independently sorted half-cell tiles. Stained
    // glass is translucent too; small tiles otherwise interleave with its large faces.
    for (int side = 0; side < 6; ++side) if (visible[side]) {
      setFaceLighting(world, position, side, color);
      renderFace(position, low[0], 0D, low[2], high[1], height, high[3], side,
          Blocks.water.getIcon(side < 2 ? 1 : 2, 0));
    }
    return true;
  }

  private static boolean renderPaneColumns(World world, ClientWaterloggedBlocks.Position position, boolean[] wet) {
    int[][] directions = {{0,-1,0},{0,1,0},{0,0,-1},{0,0,1},{-1,0,0},{1,0,0}};
    java.util.List<double[]> faces = new java.util.ArrayList<double[]>();
    for (int x = 0; x < 2; ++x) for (int z = 0; z < 2; ++z) {
      if (wet[WaterloggedGeometry.index(x,0,z)] != wet[WaterloggedGeometry.index(x,1,z)]) return false;
      if (!wet[WaterloggedGeometry.index(x,0,z)]) continue;
      double[] bounds = WaterloggedGeometry.paneWaterCellBounds(wet,x,0,z);
      for (int side = 0; side < 6; ++side) {
        int[] d = directions[side];
        boolean visible = shouldRenderFace(world,position,x,side == 1 ? 1 : 0,z,d[0],d[1],d[2]);
        if (side > 1 && visible != shouldRenderFace(world,position,x,1,z,d[0],d[1],d[2])) return false;
        if (visible) faces.add(new double[] {bounds[0],0,bounds[2],bounds[1],
            cellMaxY(world,position,x,1,z),bounds[3],side});
      }
    }
    // Merge coplanar rectangles before the translucent sorter sees them.
    boolean changed;
    do {
      changed = false;
      outer: for (int i = 0; i < faces.size(); ++i) for (int j = i+1; j < faces.size(); ++j) {
        double[] a = faces.get(i), b = faces.get(j);
        if (a[6] != b[6]) continue;
        int normal = a[6] < 2 ? 1 : a[6] < 4 ? 2 : 0;
        int plane = normal + (((int)a[6] & 1) == 0 ? 0 : 3);
        if (a[plane] != b[plane]) continue;
        for (int axis = 0; axis < 3; ++axis) {
          if (axis == normal) continue;
          int other = 3-normal-axis;
          if (a[other] != b[other] || a[other+3] != b[other+3]
              || !(a[axis+3] == b[axis] || b[axis+3] == a[axis])) continue;
          a[axis] = Math.min(a[axis],b[axis]); a[axis+3] = Math.max(a[axis+3],b[axis+3]);
          faces.remove(j); changed = true; break outer;
        }
      }
    } while (changed);
    int color = Blocks.water.colorMultiplier(world,position.x,position.y,position.z);
    for (double[] face : faces) {
      int side = (int)face[6];
      setFaceLighting(world,position,side,color);
      renderFace(position,face[0],face[1],face[2],face[3],face[4],face[5],side,
          Blocks.water.getIcon(side < 2 ? 1 : 2,0));
    }
    return true;
  }

  private static void renderCell(
      World world,
      ClientWaterloggedBlocks.Position position,
      int cellX,
      int cellY,
      int cellZ,
      boolean topAlreadyRendered) {
    double minX = cellX == 0 ? 0.0D : 0.5D;
    double minY = cellY == 0 ? 0.0D : 0.5D;
    double minZ = cellZ == 0 ? 0.0D : 0.5D;
    double maxX = cellX == 1 ? 1.0D : 0.5D;
    double maxY = cellMaxY(world, position, cellX, cellY, cellZ);
    double maxZ = cellZ == 1 ? 1.0D : 0.5D;
    if (world.getBlock(position.x, position.y, position.z) instanceof BlockPane) {
      double[] bounds = WaterloggedGeometry.paneWaterCellBounds(
          WaterloggedGeometry.waterCellsForRender(world, position.x, position.y, position.z),
          cellX, cellY, cellZ);
      minX = bounds[0]; maxX = bounds[1]; minZ = bounds[2]; maxZ = bounds[3];
    }
    int color = Blocks.water.colorMultiplier(world, position.x, position.y, position.z);
    IIcon still = Blocks.water.getIcon(1, 0);
    IIcon flowing = Blocks.water.getIcon(2, 0);
    if (shouldRenderFace(world, position, cellX, cellY, cellZ, 0, -1, 0)) {
      setFaceLighting(world, position, 0, color);
      renderFace(position, minX, minY, minZ, maxX, maxY, maxZ, 0, still);
    }
    if (!topAlreadyRendered
        && shouldRenderFace(world, position, cellX, cellY, cellZ, 0, 1, 0)) {
      setFaceLighting(world, position, 1, color);
      renderFace(position, minX, minY, minZ, maxX, maxY, maxZ, 1, still);
    }
    if (shouldRenderFace(world, position, cellX, cellY, cellZ, 0, 0, -1)) {
      setFaceLighting(world, position, 2, color);
      renderFace(position, minX, minY, minZ, maxX, maxY, maxZ, 2, flowing);
    }
    if (shouldRenderFace(world, position, cellX, cellY, cellZ, 0, 0, 1)) {
      setFaceLighting(world, position, 3, color);
      renderFace(position, minX, minY, minZ, maxX, maxY, maxZ, 3, flowing);
    }
    if (shouldRenderFace(world, position, cellX, cellY, cellZ, -1, 0, 0)) {
      setFaceLighting(world, position, 4, color);
      renderFace(position, minX, minY, minZ, maxX, maxY, maxZ, 4, flowing);
    }
    if (shouldRenderFace(world, position, cellX, cellY, cellZ, 1, 0, 0)) {
      setFaceLighting(world, position, 5, color);
      renderFace(position, minX, minY, minZ, maxX, maxY, maxZ, 5, flowing);
    }
  }

  private static double cellMaxY(
      World world,
      ClientWaterloggedBlocks.Position position,
      int cellX,
      int cellY,
      int cellZ) {
    if (cellY == 0) return 0.5D;
    int aboveY = position.y + 1;
    Block block = world.getBlock(position.x, position.y, position.z);
    if (block instanceof BlockDoor
        && world.getBlock(position.x, aboveY, position.z) == block
        && ClientWaterloggedBlocks.contains(
            world.provider.dimensionId, position.x, aboveY, position.z)) return 1.0D;
    if (world.getBlock(position.x, aboveY, position.z).getMaterial() == Material.water)
      return 1.0D;
    if (ClientWaterloggedBlocks.contains(
            world.provider.dimensionId, position.x, aboveY, position.z)
        && WaterloggedBlockSupport.canWaterlog(world, position.x, aboveY, position.z)) {
      boolean[] above =
          WaterloggedGeometry.waterCellsForRender(world, position.x, aboveY, position.z);
      if (above[WaterloggedGeometry.index(cellX, 0, cellZ)]) return 1.0D;
    }
    return SOURCE_SURFACE;
  }

  private static void setFaceLighting(
      World world, ClientWaterloggedBlocks.Position position, int side, int color) {
    int lightX = position.x;
    int lightY = position.y;
    int lightZ = position.z;
    float shade = 1.0F;
    if (side == 0) {
      lightY--;
      shade = 0.5F;
    } else if (side == 2) {
      lightZ--;
      shade = 0.8F;
    } else if (side == 3) {
      lightZ++;
      shade = 0.8F;
    } else if (side == 4) {
      lightX--;
      shade = 0.6F;
    } else if (side == 5) {
      lightX++;
      shade = 0.6F;
    }

    float red = (float) (color >> 16 & 255) / 255.0F;
    float green = (float) (color >> 8 & 255) / 255.0F;
    float blue = (float) (color & 255) / 255.0F;
    Tessellator tessellator = Tessellator.instance;
    tessellator.setBrightness(
        Blocks.water.getMixedBrightnessForBlock(world, lightX, lightY, lightZ));
    tessellator.setColorOpaque_F(red * shade, green * shade, blue * shade);
  }


  private static void renderFace(
      ClientWaterloggedBlocks.Position position,
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      int side,
      IIcon icon) {
    double x0 = position.x + minX, x1 = position.x + maxX;
    double y0 = position.y + minY, y1 = position.y + maxY;
    double z0 = position.z + minZ, z1 = position.z + maxZ;
    // Offset only the face's normal, never its edges: neighbouring water must
    // meet exactly at block boundaries instead of exposing the terrain below.
    if (side == 0 && minY == 0.0D) y0 += EDGE;
    if (side == 2 && minZ == 0.0D) z0 += EDGE;
    if (side == 3 && maxZ == 1.0D) z1 -= EDGE;
    if (side == 4 && minX == 0.0D) x0 += EDGE;
    if (side == 5 && maxX == 1.0D) x1 -= EDGE;
    if (side == 2 && minZ == 9D / 16D) z0 += EDGE;
    if (side == 3 && maxZ == 7D / 16D) z1 -= EDGE;
    if (side == 4 && minX == 9D / 16D) x0 += EDGE;
    if (side == 5 && maxX == 7D / 16D) x1 -= EDGE;
    Tessellator tessellator = Tessellator.instance;
    if (side == 0 || side == 1) {
      double u0 = icon.getInterpolatedU(minX * 16), u1 = icon.getInterpolatedU(maxX * 16);
      double v0 = icon.getInterpolatedV(minZ * 16), v1 = icon.getInterpolatedV(maxZ * 16);
      double y = side == 0 ? y0 : y1;
      if (side == 0) {
        addFaceVertex(tessellator, x0, y, z0, u0, v0);
        addFaceVertex(tessellator, x1, y, z0, u1, v0);
        addFaceVertex(tessellator, x1, y, z1, u1, v1);
        addFaceVertex(tessellator, x0, y, z1, u0, v1);
      } else {
        addFaceVertex(tessellator, x0, surfaceY(position,x0,z1,maxY), z1, u0, v1);
        addFaceVertex(tessellator, x1, surfaceY(position,x1,z1,maxY), z1, u1, v1);
        addFaceVertex(tessellator, x1, surfaceY(position,x1,z0,maxY), z0, u1, v0);
        addFaceVertex(tessellator, x0, surfaceY(position,x0,z0,maxY), z0, u0, v0);
      }
      return;
    }
    double v0 = icon.getInterpolatedV((1.0D - maxY) * 16);
    double v1 = icon.getInterpolatedV((1.0D - minY) * 16);
    if (side == 2 || side == 3) {
      double u0 = icon.getInterpolatedU(minX * 16), u1 = icon.getInterpolatedU(maxX * 16);
      double z = side == 2 ? z0 : z1;
      if (side == 2) {
        addFaceVertex(tessellator, x0, surfaceY(position,x0,z,maxY), z, u0, v0);
        addFaceVertex(tessellator, x1, surfaceY(position,x1,z,maxY), z, u1, v0);
        addFaceVertex(tessellator, x1, y0, z, u1, v1);
        addFaceVertex(tessellator, x0, y0, z, u0, v1);
      } else {
        addFaceVertex(tessellator, x0, y0, z, u0, v1);
        addFaceVertex(tessellator, x1, y0, z, u1, v1);
        addFaceVertex(tessellator, x1, surfaceY(position,x1,z,maxY), z, u1, v0);
        addFaceVertex(tessellator, x0, surfaceY(position,x0,z,maxY), z, u0, v0);
      }
      return;
    }
    double u0 = icon.getInterpolatedU(minZ * 16), u1 = icon.getInterpolatedU(maxZ * 16);
    double x = side == 4 ? x0 : x1;
    if (side == 4) {
      addFaceVertex(tessellator, x, y0, z0, u0, v1);
      addFaceVertex(tessellator, x, y0, z1, u1, v1);
      addFaceVertex(tessellator, x, surfaceY(position,x,z1,maxY), z1, u1, v0);
      addFaceVertex(tessellator, x, surfaceY(position,x,z0,maxY), z0, u0, v0);
    } else {
      addFaceVertex(tessellator, x, surfaceY(position,x,z0,maxY), z0, u0, v0);
      addFaceVertex(tessellator, x, surfaceY(position,x,z1,maxY), z1, u1, v0);
      addFaceVertex(tessellator, x, y0, z1, u1, v1);
      addFaceVertex(tessellator, x, y0, z0, u0, v1);
    }
  }

  private static boolean shouldRenderFace(
      World world,
      ClientWaterloggedBlocks.Position position,
      int cellX,
      int cellY,
      int cellZ,
      int directionX,
      int directionY,
      int directionZ) {
    int adjacentCellX = cellX + directionX;
    int adjacentCellY = cellY + directionY;
    int adjacentCellZ = cellZ + directionZ;
    if (insideCell(adjacentCellX, adjacentCellY, adjacentCellZ)) {
      // Glass is transparent: the wet/dry boundary needs a water side face.
      // Its plane is clipped to the glass surface and sorted in the chunk buffer.
      if (!(world.getBlock(position.x, position.y, position.z) instanceof BlockPane)) return false;
      boolean[] water = WaterloggedGeometry.waterCellsForRender(world, position.x, position.y, position.z);
      return !water[WaterloggedGeometry.index(adjacentCellX, adjacentCellY, adjacentCellZ)];
    }

    int adjacentX = position.x + directionX;
    int adjacentY = position.y + directionY;
    int adjacentZ = position.z + directionZ;
    if (world.getBlock(adjacentX, adjacentY, adjacentZ).getMaterial() == Material.water) return false;
    int side =
        directionY < 0
            ? 0
            : directionY > 0
                ? 1
                : directionZ < 0
                    ? 2
                    : directionZ > 0 ? 3 : directionX < 0 ? 4 : 5;
    if (world.isSideSolid(
            adjacentX, adjacentY, adjacentZ, ForgeDirection.getOrientation(side ^ 1), false)
        && !(directionY != 0
            && isPartialSolid(world.getBlock(position.x, position.y, position.z))))
      return false;
    if (!ClientWaterloggedBlocks.contains(
            world.provider.dimensionId, adjacentX, adjacentY, adjacentZ)
        || !WaterloggedBlockSupport.canWaterlog(world, adjacentX, adjacentY, adjacentZ)) return true;
    boolean[] adjacentWater = WaterloggedGeometry.waterCellsForRender(
        world, adjacentX, adjacentY, adjacentZ);
    int neighbourCellX = directionX < 0 ? 1 : directionX > 0 ? 0 : cellX;
    int neighbourCellY = directionY < 0 ? 1 : directionY > 0 ? 0 : cellY;
    int neighbourCellZ = directionZ < 0 ? 1 : directionZ > 0 ? 0 : cellZ;
    return !adjacentWater[WaterloggedGeometry.index(neighbourCellX, neighbourCellY, neighbourCellZ)];
  }

  private static boolean insideCell(int x, int y, int z) {
    return x >= 0 && x < 2 && y >= 0 && y < 2 && z >= 0 && z < 2;
  }

  private static boolean isPartialSolid(Block block) {
    return block instanceof BlockStairs
        || block instanceof net.minecraft.block.BlockSlab;
  }
}
