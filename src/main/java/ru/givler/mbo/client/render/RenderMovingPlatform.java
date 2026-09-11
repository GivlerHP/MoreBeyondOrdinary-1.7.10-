package ru.givler.mbo.client.render;

import java.util.HashMap;
import java.util.Map;
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
    bindTexture(TextureMap.locationBlocksTexture);
    Cache compiled = getCache(p);
    GL11.glPushMatrix();
    GL11.glTranslated(x, y, z);
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glCallList(compiled.list);
    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glPopMatrix();
  }

  private Cache getCache(EntityMovingPlatform platform) {
    int ox = (int) Math.floor(platform.posX),
        oy = (int) Math.floor(platform.posY),
        oz = (int) Math.floor(platform.posZ);
    int signature = 1;
    for (PlatformBlock b : platform.getBlocks()) {
      signature = 31 * signature + net.minecraft.block.Block.getIdFromBlock(b.block);
      signature = 31 * signature + b.meta;
      signature = 31 * signature + b.x;
      signature = 31 * signature + b.y;
      signature = 31 * signature + b.z;
    }
    int cx = ox + platform.getSizeX() / 2,
        cy = oy + platform.getSizeY() / 2,
        cz = oz + platform.getSizeZ() / 2;
    signature = 31 * signature + platform.worldObj.getLightBrightnessForSkyBlocks(cx, cy, cz, 0);
    signature = 31 * signature + platform.worldObj.getBiomeGenForCoords(cx, cz).biomeID;
    Cache old = cache.get(platform.getPlatformId());
    if (old != null && old.signature == signature) return old;
    if (old != null) GL11.glDeleteLists(old.list, 1);
    int list = GL11.glGenLists(1);
    GL11.glNewList(list, GL11.GL_COMPILE);
    RenderBlocks renderer =
        new RenderBlocks(
            new PlatformBlockAccess(platform.getBlocks(), platform.worldObj, ox, oy, oz));
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
