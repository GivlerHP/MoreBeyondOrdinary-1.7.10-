package ru.givler.mbo.integration.angelica.client.render;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

/** Live TESRs have a lifetime independent of asynchronous terrain mesh results. */
public final class AngelicaTileEntityHooks {
  private static boolean reported;
  private AngelicaTileEntityHooks() {}
  public static void reset() { reported=false; }
  public static void complete(Object renderer, List<TileEntity> frame) {
    Minecraft mc=Minecraft.getMinecraft();
    if(!mc.func_152345_ab())throw new IllegalStateException("Live TESR collection is not on client thread");
    if(mc.theWorld==null || mc.renderViewEntity==null){frame.clear();return;}
    TileEntityRendererDispatcher dispatcher=TileEntityRendererDispatcher.instance;
    double x=TileEntityRendererDispatcher.staticPlayerX,y=TileEntityRendererDispatcher.staticPlayerY,z=TileEntityRendererDispatcher.staticPlayerZ;
    Frustrum camera=new Frustrum();camera.setPosition(x,y,z);
    List<TileEntity> before=ru.givler.mbo.client.diagnostics.TileEntityRenderTrace.enabled()?new java.util.ArrayList<TileEntity>(frame):null;
    collect(mc.theWorld,camera,x,y,z,mc.gameSettings.renderDistanceChunks,frame,dispatcher::hasSpecialRenderer);
    if(before!=null)ru.givler.mbo.client.diagnostics.TileEntityRenderTrace.visible(before,frame);
    if(!reported){reported=true;System.out.println("[MBO] Angelica live TESR collection: loaded chunk maps, independent of terrain rebuilds");}
  }
  /** No section-cache or history access; never request a missing chunk. Includes non-ticking TESRs. */
  public static void collect(World world,ICamera camera,double x,double y,double z,int range,List<TileEntity> frame,Predicate<TileEntity> hasRenderer) {
    frame.clear();if(world==null)return;
    IChunkProvider provider=world.getChunkProvider();
    int centerX=((int)Math.floor(x))>>4,centerZ=((int)Math.floor(z))>>4;
    range=Math.max(0,range);
    for(int cx=centerX-range;cx<=centerX+range;cx++)for(int cz=centerZ-range;cz<=centerZ+range;cz++) {
      if(!provider.chunkExists(cx,cz))continue;
      Chunk chunk=world.getChunkFromChunkCoords(cx,cz);
      for(Object value:chunk.chunkTileEntityMap.values()) {
        if(!(value instanceof TileEntity))continue;
        TileEntity tile=(TileEntity)value;
        if(tile.isInvalid() || tile.getWorldObj()!=world || (tile.xCoord>>4)!=cx || (tile.zCoord>>4)!=cz
            || !(tile.getDistanceFrom(x,y,z)<tile.getMaxRenderDistanceSquared()) || !hasRenderer.test(tile))continue;
        AxisAlignedBB box=tile.getRenderBoundingBox();
        if(box==null || box==TileEntity.INFINITE_EXTENT_AABB || camera.isBoundingBoxInFrustum(box))frame.add(tile);
      }
    }
  }
}
