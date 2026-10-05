package ru.givler.mbo.client.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.nio.Buffer;
import java.nio.DoubleBuffer;
import org.lwjgl.BufferUtils;
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
  private final DoubleBuffer clipPlane = BufferUtils.createDoubleBuffer(4);
  private static long lightEpoch;

  public RenderMovingPlatform() {
    net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(this);
  }

  @cpw.mods.fml.common.eventhandler.SubscribeEvent
  public void unload(net.minecraftforge.event.world.WorldEvent.Unload event) {
    if (!event.world.isRemote) return;
    for (Cache entry : cache.values()) GL11.glDeleteLists(entry.list,1);
    cache.clear();
  }

  @cpw.mods.fml.common.eventhandler.SubscribeEvent
  public void tick(cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent event) {
    if (event.phase != cpw.mods.fml.common.gameevent.TickEvent.Phase.END) return;
    ++lightEpoch;
    for (java.util.Iterator<Cache> it = cache.values().iterator(); it.hasNext();) {
      Cache entry = it.next(); EntityMovingPlatform owner = entry.owner.get();
      if (owner == null || owner.isDead || owner.worldObj != net.minecraft.client.Minecraft.getMinecraft().theWorld
          || owner.worldObj.getEntityByID(owner.getEntityId()) != owner) {
        GL11.glDeleteLists(entry.list,1); it.remove();
      }
    }
  }

  @Override
  public void doRender(Entity entity, double x, double y, double z, float yaw, float partial) {
    EntityMovingPlatform p = (EntityMovingPlatform) entity;
    if (!p.shouldRenderMovingBlocks()) return;
    double worldX = p.lastTickPosX + (p.posX - p.lastTickPosX) * partial;
    double worldY = p.lastTickPosY + (p.posY - p.lastTickPosY) * partial;
    double worldZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partial;
    boolean handoff = p.isAwaitingMaterialization();
    if (handoff) {
      x += p.posX - worldX;
      y += p.posY - worldY;
      z += p.posZ - worldZ;
      worldX = p.posX;
      worldY = p.posY;
      worldZ = p.posZ;
    }
    bindTexture(TextureMap.locationBlocksTexture);
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL11.glPushMatrix();
    try {
    GL11.glTranslated(x, y, z);
    if (p.isClipAboveSelection() && !p.isRebuildPending()) {
      ((Buffer) clipPlane).clear();
      clipPlane.put(0D).put(-1D).put(0D).put(p.getRenderCeilingY() - worldY);
      ((Buffer) clipPlane).flip();
      // Defined after the platform translation: the constant is local to its current position.
      GL11.glClipPlane(GL11.GL_CLIP_PLANE0, clipPlane);
      GL11.glEnable(GL11.GL_CLIP_PLANE0);
    }
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_LIGHTING);
    if (handoff) {
      GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
      GL11.glPolygonOffset(1F, 1F);
    }
    GL11.glCallList(getCache(p, worldX, worldY, worldZ).list);
    } finally {
      GL11.glPopMatrix();
      GL11.glPopAttrib();
    }
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
    Cache old = cache.get(platform.getPlatformId());
    LightSamples samples=old!=null && old.samples.matches(platform,worldX,worldY,worldZ)
        ? old.samples : new LightSamples(platform,worldX,worldY,worldZ);
    int[] light = interpolatedLightField(platform, worldX, worldY, worldZ,samples);
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
    if (old != null && old.signature == signature) { old.samples=samples; return old; }
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
    for (PlatformBlock b : platform.getBlocks()) {
      // Cull interior faces only for vanilla-style full cubes; custom models
      // retain their original rendering behaviour.
      renderer.renderAllFaces=platform.isClipAboveSelection()
          || !(b.block.getRenderType()==0 && b.block.isNormalCube());
      renderer.renderBlockByRenderType(b.block, b.x, b.y, b.z);
    }
    tessellator.draw();
    GL11.glEndList();
    Cache made = new Cache(list, signature,platform,samples);
    cache.put(platform.getPlatformId(), made);
    return made;
  }

  private static int[] interpolatedLightField(
      EntityMovingPlatform platform, double worldX, double worldY, double worldZ) {
    return interpolatedLightField(platform,worldX,worldY,worldZ,new LightSamples(platform,worldX,worldY,worldZ));
  }

  private static int[] interpolatedLightField(
      EntityMovingPlatform platform, double worldX, double worldY, double worldZ,LightSamples samples) {
    int sizeX = platform.getSizeX(), sizeY = platform.getSizeY(), sizeZ = platform.getSizeZ();
    int sy = sizeY + 2, sz = sizeZ + 2;
    int[] result = new int[(sizeX + 2) * sy * sz];
    int index = 0;
    for (int x = -1; x <= sizeX; x++)
      for (int y = -1; y <= sizeY; y++)
        for (int z = -1; z <= sizeZ; z++)
          result[index++] = interpolateLight(samples, worldX + x, worldY + y, worldZ + z);
    return result;
  }

  private static int interpolateLight(
      LightSamples samples, double x, double y, double z) {
    int x0 = (int) Math.floor(x), y0 = (int) Math.floor(y), z0 = (int) Math.floor(z);
    double fx = x - x0, fy = y - y0, fz = z - z0;
    double block = 0D, sky = 0D;
    for (int dx = 0; dx <= 1; dx++)
      for (int dy = 0; dy <= 1; dy++)
        for (int dz = 0; dz <= 1; dz++) {
          double weight = (dx == 0 ? 1D - fx : fx)
              * (dy == 0 ? 1D - fy : fy)
              * (dz == 0 ? 1D - fz : fz);
          if (weight == 0D) continue;
          int packed = samples.get(x0+dx,y0+dy,z0+dz);
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
    final java.lang.ref.WeakReference<EntityMovingPlatform> owner;
    LightSamples samples;

    Cache(int list, int signature,EntityMovingPlatform platform,LightSamples samples) {
      this.list = list;
      this.signature = signature;
      owner = new java.lang.ref.WeakReference<EntityMovingPlatform>(platform);
      this.samples=samples;
    }
  }

  private static final class LightSamples {
    final net.minecraft.world.World world;
    final int x,y,z,sy,sz;
    final int[] values;
    final long epoch=lightEpoch;
    LightSamples(EntityMovingPlatform platform,double worldX,double worldY,double worldZ) {
      this.world = platform.worldObj;
      x = (int)Math.floor(worldX)-1; y = (int)Math.floor(worldY)-1; z = (int)Math.floor(worldZ)-1;
      sy = platform.getSizeY()+3; sz = platform.getSizeZ()+3;
      values = new int[(platform.getSizeX()+3)*sy*sz];
      Arrays.fill(values,Integer.MIN_VALUE);
    }
    boolean matches(EntityMovingPlatform owner,double worldX,double worldY,double worldZ) {
      return owner.worldObj==world && epoch==lightEpoch
          && x==(int)Math.floor(worldX)-1 && y==(int)Math.floor(worldY)-1 && z==(int)Math.floor(worldZ)-1
          && sy==owner.getSizeY()+3 && sz==owner.getSizeZ()+3
          && values.length==(owner.getSizeX()+3)*sy*sz;
    }
    int get(int worldX,int worldY,int worldZ) {
      int index = ((worldX-x)*sy+(worldY-y))*sz+(worldZ-z);
      if (values[index] == Integer.MIN_VALUE)
        values[index] = world.getLightBrightnessForSkyBlocks(worldX,worldY,worldZ,0);
      return values[index];
    }
  }

  @Override
  protected ResourceLocation getEntityTexture(Entity entity) {
    return null;
  }
}
