package ru.givler.mbo.lighting;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import ru.givler.mbo.integration.angelica.client.render.AngelicaTileEntityHooks;

/** Uses actual world/chunk/tile types without a GL context. */
public final class AngelicaLiveTileSmoke {
  static class TestWorld extends WorldClient {
    Map<String,Chunk> chunks;IChunkProvider provider;int missingRequests;
    TestWorld(){super(null,null,0,net.minecraft.world.EnumDifficulty.NORMAL,null);}
    @Override public IChunkProvider getChunkProvider(){return provider;}
    @Override public Chunk getChunkFromChunkCoords(int x,int z){
      Chunk c=chunks.get(x+","+z);if(c==null){missingRequests++;throw new AssertionError("Loaded missing chunk");}return c;
    }
  }
  static class Model extends TileEntity {
    double range=4096;
    @Override public boolean canUpdate(){return false;}
    @Override public double getMaxRenderDistanceSquared(){return range;}
    @Override public AxisAlignedBB getRenderBoundingBox(){return AxisAlignedBB.getBoundingBox(xCoord,yCoord,zCoord,xCoord+1,yCoord+1,zCoord+1);}
  }
  public static void run()throws Exception {
    Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);
    sun.misc.Unsafe unsafe=(sun.misc.Unsafe)f.get(null);
    TestWorld world=(TestWorld)unsafe.allocateInstance(TestWorld.class);world.chunks=new HashMap<String,Chunk>();
    world.provider=(IChunkProvider)Proxy.newProxyInstance(IChunkProvider.class.getClassLoader(),new Class<?>[]{IChunkProvider.class},(p,m,a)->{
      if(m.getName().equals("chunkExists"))return world.chunks.containsKey(a[0]+","+a[1]);
      throw new AssertionError("Unexpected provider call: "+m.getName());
    });
    Chunk chunk=(Chunk)unsafe.allocateInstance(Chunk.class);
    Field map=Chunk.class.getDeclaredField("chunkTileEntityMap");map.setAccessible(true);map.set(chunk,new HashMap<Object,Object>());
    world.chunks.put("-1,-1",chunk);
    Model tile=new Model();tile.setWorldObj(world);tile.xCoord=-1;tile.yCoord=-20;tile.zCoord=-1;
    chunk.chunkTileEntityMap.put("model",tile);
    ICamera camera=new ICamera(){public void setPosition(double x,double y,double z){}public boolean isBoundingBoxInFrustum(AxisAlignedBB box){return box.minX<0;}};
    List<TileEntity> frame=new ArrayList<TileEntity>();
    for(int i=0;i<4;i++) {
      frame.clear();
      AngelicaTileEntityHooks.collect(world,camera,-.5,-19.5,-.5,2,frame,t->true);
      if(frame.size()!=1 || frame.get(0)!=tile)throw new AssertionError("Non-ticking/negative-height live model omitted");
    }
    tile.range=0;AngelicaTileEntityHooks.collect(world,camera,-.5,-19.5,-.5,2,frame,t->true);
    if(!frame.isEmpty())throw new AssertionError("Distance ignored");tile.range=4096;
    ICamera outside=new ICamera(){public void setPosition(double x,double y,double z){}public boolean isBoundingBoxInFrustum(AxisAlignedBB box){return false;}};
    AngelicaTileEntityHooks.collect(world,outside,-.5,-19.5,-.5,2,frame,t->true);
    if(!frame.isEmpty())throw new AssertionError("Frustum ignored");
    AngelicaTileEntityHooks.collect(world,camera,-.5,-19.5,-.5,2,frame,t->false);
    if(!frame.isEmpty())throw new AssertionError("Renderer eligibility ignored");
    tile.invalidate();AngelicaTileEntityHooks.collect(world,camera,-.5,-19.5,-.5,2,frame,t->true);
    if(!frame.isEmpty())throw new AssertionError("Invalid model retained");tile.validate();
    chunk.chunkTileEntityMap.clear();AngelicaTileEntityHooks.collect(world,camera,-.5,-19.5,-.5,2,frame,t->true);
    if(!frame.isEmpty())throw new AssertionError("Removed model retained");
    frame.add(tile);AngelicaTileEntityHooks.collect(null,camera,0,0,0,2,frame,t->true);
    if(!frame.isEmpty() || world.missingRequests!=0)throw new AssertionError("Unload or missing chunk handling");
    System.out.println("Live TESRs: no terrain results needed, non-ticking/negative-height models, frustum/distance/renderer culling, removal and no missing chunk loads PASS");
  }
}
