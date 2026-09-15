package ru.givler.mbo.client.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;
import ru.givler.mbo.movingplatform.PlatformBlock;

public class RenderMovingPlatform extends Render {
  private final Map<java.util.UUID, Cache> cache = new HashMap<java.util.UUID, Cache>();

  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
    EntityMovingPlatform p = (EntityMovingPlatform) entity;
    if (!p.shouldRenderMovingBlocks()) return;
    double worldX = p.lastTickPosX + (p.posX - p.lastTickPosX) * partial;
    double worldY = p.lastTickPosY + (p.posY - p.lastTickPosY) * partial;
    double worldZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partial;
    bindTexture(TextureMap.locationBlocksTexture);
    GL11.glPushMatrix();
    GL11.glTranslated(x, y, z);
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_LIGHTING);
    if (p.isAwaitingMaterialization()) renderMissingBlocks(p, worldX, worldY, worldZ);
    else GL11.glCallList(getCache(p, worldX, worldY, worldZ).list);
    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glPopMatrix();
  }

  private void renderMissingBlocks(
      EntityMovingPlatform platform, double worldX, double worldY, double worldZ) {
    int[] light = interpolatedLightField(platform, worldX, worldY, worldZ);
    RenderBlocks renderer =
        new RenderBlocks(
            new PlatformBlockAccess(
                platform.getBlocks(), light, platform.getSizeX(), platform.getSizeY(),
                platform.getSizeZ(), platform.worldObj, (int) Math.floor(worldX),
                (int) Math.floor(worldY), (int) Math.floor(worldZ)));
    renderer.renderAllFaces = true;
    Tessellator tessellator = Tessellator.instance;
    tessellator.startDrawingQuads();
    for (PlatformBlock block : platform.getBlocks())
      if (platform.shouldRenderPlatformBlock(block))
        renderer.renderBlockByRenderType(block.block, block.x, block.y, block.z);
    tessellator.draw();
  }

  private Cache getCache(
      EntityMovingPlatform platform, double worldX, double worldY, double worldZ) {
    int ox = (int) Math.floor(worldX),
        oy = (int) Math.floor(worldY),
        oz = (int) Math.floor(worldZ);
    int[] light = interpolatedLightField(platform, worldX, worldY, worldZ);
    int signature = 1;
    for (PlatformBlock b : platform.getBlocks()) {
      signature = 31 * signature + net.minecraft.block.Block.getIdFromBlock(b.block);
      signature = 31 * signature + b.meta;
      signature = 31 * signature + b.x;
      signature = 31 * signature + b.y;
      signature = 31 * signature + b.z;
    }
    signature = 31 * signature + Arrays.hashCode(light);
    int cx = ox + platform.getSizeX() / 2, cz = oz + platform.getSizeZ() / 2;
    signature = 31 * signature + platform.worldObj.getBiomeGenForCoords(cx, cz).biomeID;
    Cache old = cache.get(platform.getPlatformId());
    if (old != null && old.signature == signature) return old;
    if (old != null) GL11.glDeleteLists(old.list, 1);
    int list = GL11.glGenLists(1);
    GL11.glNewList(list, GL11.GL_COMPILE);
    RenderBlocks renderer =
        new RenderBlocks(
            new PlatformBlockAccess(
                platform.getBlocks(), light, platform.getSizeX(), platform.getSizeY(),
                platform.getSizeZ(), platform.worldObj, ox, oy, oz));
    renderer.renderAllFaces = true;
    Tessellator tessellator = Tessellator.instance;
    tessellator.startDrawingQuads();
    for (PlatformBlock b : platform.getBlocks())
      renderer.renderBlockByRenderType(b.block, b.x, b.y, b.z);
    tessellator.draw();
    GL11.glEndList();
    Cache made = new Cache(list, signature);
    cache.put(platform.getPlatformId(), made);
    return made;
  }

  private static int[] interpolatedLightField(
      EntityMovingPlatform platform, double worldX, double worldY, double worldZ) {
    int sizeX = platform.getSizeX(), sizeY = platform.getSizeY(), sizeZ = platform.getSizeZ();
    int sy = sizeY + 2, sz = sizeZ + 2;
    int[] result = new int[(sizeX + 2) * sy * sz];
    int index = 0;
    for (int x = -1; x <= sizeX; x++)
      for (int y = -1; y <= sizeY; y++)
        for (int z = -1; z <= sizeZ; z++)
          result[index++] = interpolateLight(platform, worldX + x, worldY + y, worldZ + z);
    return result;
  }

  private static int interpolateLight(
      EntityMovingPlatform platform, double x, double y, double z) {
    int x0 = (int) Math.floor(x), y0 = (int) Math.floor(y), z0 = (int) Math.floor(z);
    double fx = x - x0, fy = y - y0, fz = z - z0;
    double block = 0D, sky = 0D;
    for (int dx = 0; dx <= 1; dx++)
      for (int dy = 0; dy <= 1; dy++)
        for (int dz = 0; dz <= 1; dz++) {
          double weight = (dx == 0 ? 1D - fx : fx)
              * (dy == 0 ? 1D - fy : fy)
              * (dz == 0 ? 1D - fz : fz);
          int packed = platform.worldObj.getLightBrightnessForSkyBlocks(
              x0 + dx, y0 + dy, z0 + dz, 0);
          block += (packed & 0xffff) * weight;
          sky += (packed >>> 16 & 0xffff) * weight;
        }
    return clampLight((int) Math.round(block)) | clampLight((int) Math.round(sky)) << 16;
  }

  private static int clampLight(int value) {
    return value < 0 ? 0 : value > 0xffff ? 0xffff : value;
  }

  private static final class Cache {
    final int list, signature;

    Cache(int list, int signature) {
      this.list = list;
      this.signature = signature;
    }
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return null;
  }
}
