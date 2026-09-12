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
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.world.WorldEvent;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.waterlogging.ClientWaterloggedBlocks;
import ru.givler.mbo.waterlogging.WaterloggedBlockSupport;
import ru.givler.mbo.waterlogging.WaterloggedGeometry;

public final class WaterloggedBlockRenderer {
  private static final double EDGE = 0.001D;
  private static final double EDGE_MAX = 0.999D;
  private static final double SOURCE_SURFACE =
      1.0D - BlockLiquid.getLiquidHeightPercent(0) - 0.001D;

  @SubscribeEvent
  public void unload(WorldEvent.Unload event) {
    if (event.world.isRemote) ClientWaterloggedBlocks.clear();
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void render(RenderWorldLastEvent event) {
    Minecraft minecraft = Minecraft.getMinecraft();
    World world = minecraft.theWorld;
    if (world == null || minecraft.renderViewEntity == null) return;
    int dimension = world.provider.dimensionId;
    double cameraX =
        minecraft.renderViewEntity.lastTickPosX
            + (minecraft.renderViewEntity.posX - minecraft.renderViewEntity.lastTickPosX)
                * event.partialTicks;
    double cameraY =
        minecraft.renderViewEntity.lastTickPosY
            + (minecraft.renderViewEntity.posY - minecraft.renderViewEntity.lastTickPosY)
                * event.partialTicks;
    double cameraZ =
        minecraft.renderViewEntity.lastTickPosZ
            + (minecraft.renderViewEntity.posZ - minecraft.renderViewEntity.lastTickPosZ)
                * event.partialTicks;

    minecraft.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
    EntityRenderer entityRenderer = minecraft.entityRenderer;
    entityRenderer.enableLightmap(event.partialTicks);
    GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
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
      if (WaterloggedBlockSupport.canWaterlog(world, position.x, position.y, position.z))
        renderWater(world, position);
    }
    tessellator.setTranslation(0, 0, 0);
    tessellator.draw();
    GL11.glDepthMask(true);
    GL11.glPopAttrib();
    entityRenderer.disableLightmap(event.partialTicks);
  }

  private static void renderWater(World world, ClientWaterloggedBlocks.Position position) {
    boolean[] free =
        WaterloggedGeometry.waterCellsForRender(world, position.x, position.y, position.z);
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
        double minY = cellY == 0 ? EDGE : 0.5D;
        double maxY = mergedHeight;
        setFaceLighting(world, position, 1, Blocks.water.colorMultiplier(world, position.x, position.y, position.z));
        renderFace(position, EDGE, minY, EDGE, EDGE_MAX, maxY, EDGE_MAX, 1, still);
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

  private static void renderCell(
      World world,
      ClientWaterloggedBlocks.Position position,
      int cellX,
      int cellY,
      int cellZ,
      boolean topAlreadyRendered) {
    double minX = cellX == 0 ? EDGE : 0.5D;
    double minY = cellY == 0 ? EDGE : 0.5D;
    double minZ = cellZ == 0 ? EDGE : 0.5D;
    double maxX = cellX == 1 ? EDGE_MAX : 0.5D;
    double maxY = cellMaxY(world, position, cellX, cellY, cellZ);
    double maxZ = cellZ == 1 ? EDGE_MAX : 0.5D;
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
    Tessellator tessellator = Tessellator.instance;
    if (side == 0 || side == 1) {
      double u0 = icon.getInterpolatedU(minX * 16), u1 = icon.getInterpolatedU(maxX * 16);
      double v0 = icon.getInterpolatedV(minZ * 16), v1 = icon.getInterpolatedV(maxZ * 16);
      double y = side == 0 ? y0 : y1;
      if (side == 0) {
        tessellator.addVertexWithUV(x0, y, z0, u0, v0);
        tessellator.addVertexWithUV(x1, y, z0, u1, v0);
        tessellator.addVertexWithUV(x1, y, z1, u1, v1);
        tessellator.addVertexWithUV(x0, y, z1, u0, v1);
      } else {
        tessellator.addVertexWithUV(x0, y, z1, u0, v1);
        tessellator.addVertexWithUV(x1, y, z1, u1, v1);
        tessellator.addVertexWithUV(x1, y, z0, u1, v0);
        tessellator.addVertexWithUV(x0, y, z0, u0, v0);
      }
      return;
    }
    double v0 = icon.getInterpolatedV((1.0D - maxY) * 16);
    double v1 = icon.getInterpolatedV((1.0D - minY) * 16);
    if (side == 2 || side == 3) {
      double u0 = icon.getInterpolatedU(minX * 16), u1 = icon.getInterpolatedU(maxX * 16);
      double z = side == 2 ? z0 : z1;
      if (side == 2) {
        tessellator.addVertexWithUV(x0, y1, z, u0, v0);
        tessellator.addVertexWithUV(x1, y1, z, u1, v0);
        tessellator.addVertexWithUV(x1, y0, z, u1, v1);
        tessellator.addVertexWithUV(x0, y0, z, u0, v1);
      } else {
        tessellator.addVertexWithUV(x0, y0, z, u0, v1);
        tessellator.addVertexWithUV(x1, y0, z, u1, v1);
        tessellator.addVertexWithUV(x1, y1, z, u1, v0);
        tessellator.addVertexWithUV(x0, y1, z, u0, v0);
      }
      return;
    }
    double u0 = icon.getInterpolatedU(minZ * 16), u1 = icon.getInterpolatedU(maxZ * 16);
    double x = side == 4 ? x0 : x1;
    if (side == 4) {
      tessellator.addVertexWithUV(x, y0, z0, u0, v1);
      tessellator.addVertexWithUV(x, y0, z1, u1, v1);
      tessellator.addVertexWithUV(x, y1, z1, u1, v0);
      tessellator.addVertexWithUV(x, y1, z0, u0, v0);
    } else {
      tessellator.addVertexWithUV(x, y1, z0, u0, v0);
      tessellator.addVertexWithUV(x, y1, z1, u1, v0);
      tessellator.addVertexWithUV(x, y0, z1, u1, v1);
      tessellator.addVertexWithUV(x, y0, z0, u0, v1);
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
    // Faces between two half-block cells are internal. This includes the boundary between
    // water and the solid part of the stair/slab; the block geometry already closes it.
    if (insideCell(adjacentCellX, adjacentCellY, adjacentCellZ)) {
      if (!(world.getBlock(position.x, position.y, position.z) instanceof BlockPane)) return false;
      boolean[] water =
          WaterloggedGeometry.waterCellsForRender(world, position.x, position.y, position.z);
      return !water[
          WaterloggedGeometry.index(adjacentCellX, adjacentCellY, adjacentCellZ)];
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
    return false;
  }

  private static boolean insideCell(int x, int y, int z) {
    return x >= 0 && x < 2 && y >= 0 && y < 2 && z >= 0 && z < 2;
  }

  private static boolean isPartialSolid(Block block) {
    return block instanceof BlockStairs
        || block instanceof net.minecraft.block.BlockSlab;
  }
}
