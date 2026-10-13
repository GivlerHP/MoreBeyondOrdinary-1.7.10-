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
  private final ru.givler.mbo.client.render.decormodels.TemplateModelRenderer decorRenderer=
      new ru.givler.mbo.client.render.decormodels.TemplateModelRenderer() {
        protected boolean useSharedGeometry(){return true;}
      };
  private final java.util.WeakHashMap<PlatformBlock,ru.givler.mbo.tileentity.ModelTileBase> decorTiles=new java.util.WeakHashMap<>();
  private java.util.WeakHashMap<EntityMovingPlatform,LightSamples> decorLights=new java.util.WeakHashMap<>();

  public RenderMovingPlatform() {
    net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(this);
  }

  @cpw.mods.fml.common.eventhandler.SubscribeEvent
  public void unload(net.minecraftforge.event.world.WorldEvent.Unload event) {
    if (!event.world.isRemote) return;
    for (Cache entry : cache.values()) GL11.glDeleteLists(entry.list,1);
    cache.clear();
    decorTiles.clear();decorLights.clear();
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
    boolean normalBlocks=false;
    for(PlatformBlock b:p.getBlocks())if(!(b.block instanceof ru.givler.mbo.block.BlockModels)&&b.block.getRenderType()>=0){normalBlocks=true;break;}
    if(normalBlocks)GL11.glCallList(getCache(p, worldX, worldY, worldZ).list);
    renderDecor(p,partial);
    } finally {
      GL11.glPopMatrix();
      GL11.glPopAttrib();
    }
  }

  private void renderDecor(EntityMovingPlatform platform,float partial) {
    boolean hasModels=false;for(PlatformBlock b:platform.getBlocks())if(b.block instanceof ru.givler.mbo.block.BlockModels){hasModels=true;break;}
    if(!hasModels)return;
    double wx=platform.lastTickPosX+(platform.posX-platform.lastTickPosX)*partial;
    double wy=platform.lastTickPosY+(platform.posY-platform.lastTickPosY)*partial;
    double wz=platform.lastTickPosZ+(platform.posZ-platform.lastTickPosZ)*partial;
    if(decorLights==null)decorLights=new java.util.WeakHashMap<>();
    LightSamples samples=decorLights.get(platform);
    if(samples==null||!samples.matches(platform,wx,wy,wz)){samples=new LightSamples(platform,wx,wy,wz);decorLights.put(platform,samples);}
    net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.getMinecraft();
    boolean lightmap=mc.entityRenderer!=null;
    if(lightmap)mc.entityRenderer.enableLightmap(partial);
    net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
    try {
    for(PlatformBlock b:platform.getBlocks()) {
      if(!(b.block instanceof ru.givler.mbo.block.BlockModels)||b.tileData==null||!platform.shouldRenderPlatformBlock(b))continue;
      ru.givler.mbo.tileentity.ModelTileBase tile=decorTiles.get(b);
      if(tile==null) {
        tile=new ru.givler.mbo.tileentity.ModelTileBase();tile.readFromNBT(b.tileData);
        tile.blockType=b.block;tile.blockMetadata=b.meta;tile.setWorldObj(platform.worldObj);decorTiles.put(b,tile);
        if(b.tileData.hasKey("PlatformRopeMask")) {
          tile.movingPlatformRopeMask=b.tileData.getInteger("PlatformRopeMask");
          tile.movingPlatformRopeConnector=b.tileData.getBoolean("PlatformRopeConnector");
        }
      }
      tile.xCoord=(int)Math.floor(platform.posX)+b.x;tile.yCoord=(int)Math.floor(platform.posY)+b.y;tile.zCoord=(int)Math.floor(platform.posZ)+b.z;
      tile.cageOpen=b.tileData.getBoolean("cageOpen");
      tile.movingPlatformLight=interpolateLight(samples,wx+b.x,wy+b.y,wz+b.z);
      tile.movingPlatformLightX=wx+b.x+.5; tile.movingPlatformLightY=wy+b.y+.5; tile.movingPlatformLightZ=wz+b.z+.5;
      decorRenderer.renderTileEntityAt(tile,b.x,b.y,b.z,partial);
    }
    } finally {
      net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
      if(lightmap)mc.entityRenderer.disableLightmap(partial);
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
    ((PlatformBlockAccess)renderer.blockAccess).setLightOrigin(worldX,worldY,worldZ);
    Tessellator tessellator = Tessellator.instance;
    tessellator.startDrawingQuads();
    for (PlatformBlock block : platform.getBlocks())
      if (platform.shouldRenderPlatformBlock(block)&&!(block.block instanceof ru.givler.mbo.block.BlockModels))
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
    signature=31*signature+rgbSignature(worldX,worldY,worldZ,platform.getSizeX(),platform.getSizeY(),platform.getSizeZ());
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
    ((PlatformBlockAccess)renderer.blockAccess).setLightOrigin(worldX,worldY,worldZ);
    Tessellator tessellator = Tessellator.instance;
    tessellator.startDrawingQuads();
    for (PlatformBlock b : platform.getBlocks()) {
      if(b.block instanceof ru.givler.mbo.block.BlockModels)continue;
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
  static int rgbSignature(double x,double y,double z) {
    return rgbSignature(x,y,z,0,0,0);
  }
  static int rgbSignature(double x,double y,double z,int sizeX,int sizeY,int sizeZ) {
    int signature=ru.givler.mbo.config.ColoredLightConfig.enabled ? 1 : 0;
    if(signature==0)return 0;
    int minX=(int)Math.floor(x)-2,minY=(int)Math.floor(y)-2,minZ=(int)Math.floor(z)-2;
    int maxX=(int)Math.floor(x)+sizeX+2,maxY=(int)Math.floor(y)+sizeY+2,maxZ=(int)Math.floor(z)+sizeZ+2;
    signature=31*signature+ru.givler.mbo.client.lighting.ColoredLightClient.CACHE.regionSignature(minX,minY,minZ,maxX,maxY,maxZ);
    // Sources outside the platform's sampling box cannot affect its baked vertices.
    for(ru.givler.mbo.client.lighting.ColoredDynamicLights.Source source:ru.givler.mbo.client.lighting.ColoredDynamicLights.INSTANCE.sources()) {
      if(source.x+source.radius<minX || source.x-source.radius>maxX
          || source.y+source.radius<minY || source.y-source.radius>maxY
          || source.z+source.radius<minZ || source.z-source.radius>maxZ)continue;
      signature=31*signature+source.entity.getEntityId();signature=31*signature+source.packed;
      signature=31*signature+Double.hashCode(source.x);
      signature=31*signature+Double.hashCode(source.y);
      signature=31*signature+Double.hashCode(source.z);
      // Moving-light visibility also depends on obstacles along source-to-vertex rays.
      long generation=ru.givler.mbo.client.lighting.ColoredDynamicLights.INSTANCE.generation();
      signature=31*signature+(int)(generation^(generation>>>32));
    }
    signature=31*signature+Float.floatToIntBits(ru.givler.mbo.config.ColoredLightConfig.strength);
    signature=31*signature+Float.floatToIntBits(ru.givler.mbo.config.ColoredLightConfig.brightnessGain);
    signature=31*signature+Float.floatToIntBits(ru.givler.mbo.config.ColoredLightConfig.colorFalloff);
    signature=31*signature+ru.givler.mbo.client.lighting.ColoredLightClient.skyDarkness;
    signature=31*signature+(ru.givler.mbo.client.lighting.ColoredLightShaderBridge.active?1:0);
    if(ru.givler.mbo.client.lighting.ColoredLightClient.cacheActive || ru.givler.mbo.client.lighting.ColoredLightClient.volume!=null) {
      signature=31*signature+(int)Math.floor(x*16); signature=31*signature+(int)Math.floor(y*16); signature=31*signature+(int)Math.floor(z*16);
    }
    return signature;
  }

  private static int[] interpolatedLightField(
      EntityMovingPlatform platform, double worldX, double worldY, double worldZ,LightSamples samples) {
    long allocationStart=ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.start();
    int sizeX = platform.getSizeX(), sizeY = platform.getSizeY(), sizeZ = platform.getSizeZ();
    int sy = sizeY + 2, sz = sizeZ + 2;
    int[] result = new int[(sizeX + 2) * sy * sz];
    int index = 0;
    for (int x = -1; x <= sizeX; x++)
      for (int y = -1; y <= sizeY; y++)
        for (int z = -1; z <= sizeZ; z++)
          result[index++] = interpolateLight(samples, worldX + x, worldY + y, worldZ + z);
    ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.end(ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.PLATFORM,allocationStart);
    ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.work(ru.givler.mbo.client.lighting.ColoredLightAllocationDiagnostics.PLATFORM,result.length);
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
