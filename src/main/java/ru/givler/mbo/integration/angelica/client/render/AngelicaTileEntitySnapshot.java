package ru.givler.mbo.integration.angelica.client.render;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/** Resolve live TESR metadata on upload, never on terrain worker threads. */
public final class AngelicaTileEntitySnapshot {
  private static final String ROOT="com.gtnewhorizons.angelica.";
  private static Method classify,prepare;
  private static Class<?> builtType,dataType;
  private static boolean logged;
  private AngelicaTileEntitySnapshot() {}
  public static void refresh(Object manager,Object section,Object data) {
    if(data==null)return;
    try {
      if(dataType==null) {
        dataType=Class.forName(ROOT+"rendering.celeritas.AngelicaBuiltRenderSectionData");
        classify=Class.forName(ROOT+"rendering.TileEntityRenderBoundsRegistry").getMethod("classify",TileEntity.class);
        builtType=Class.forName(ROOT+"rendering.tesr.SectionBuiltBlockEntity");
        prepare=builtType.getMethod("angelica$prepareFromSection",net.minecraft.world.IBlockAccess.class);
      }
      if(!dataType.isInstance(data))return; // Empty/disposed section metadata is owned by Angelica.
      Minecraft mc=Minecraft.getMinecraft();
      if(!mc.func_152345_ab())throw new IllegalStateException("Live TESR metadata upload is not on client thread");
      World world=(World)field(manager,"world");
      int x=((Number)section.getClass().getMethod("getOriginX").invoke(section)).intValue();
      int y=((Number)section.getClass().getMethod("getOriginY").invoke(section)).intValue();
      int z=((Number)section.getClass().getMethod("getOriginZ").invoke(section)).intValue();
      List<TileEntity> nextGlobal=new ArrayList<TileEntity>(),nextCulled=new ArrayList<TileEntity>();
      List<AxisAlignedBB> boxes=new ArrayList<AxisAlignedBB>();
      List<Object> built=new ArrayList<Object>();List<Integer> results=new ArrayList<Integer>();
      if(world!=null && world==mc.theWorld && world.getChunkProvider().chunkExists(x>>4,z>>4)) {
        Chunk chunk=world.getChunkFromChunkCoords(x>>4,z>>4);
        // Snapshot only this loaded column, not the world's whole tile list.
        Object[] tiles=chunk.chunkTileEntityMap.values().toArray();
        for(Object value:tiles) {
          if(!(value instanceof TileEntity))continue;TileEntity tile=(TileEntity)value;
          if(tile.isInvalid() || tile.getWorldObj()!=world || tile.xCoord<x || tile.xCoord>=x+16
              || tile.yCoord<y || tile.yCoord>=y+16 || tile.zCoord<z || tile.zCoord>=z+16
              || !TileEntityRendererDispatcher.instance.hasSpecialRenderer(tile))continue;
          if(builtType.isInstance(tile)){built.add(tile);results.add(((Number)prepare.invoke(tile,world)).intValue());}
          byte kind=((Number)classify.invoke(null,tile)).byteValue();
          AxisAlignedBB box=null;
          if(kind==1)try{box=tile.getRenderBoundingBox();}catch(Throwable ignored){}
          if(kind!=1 || !inside(box,x,y,z))nextGlobal.add(tile);
          else {nextCulled.add(tile);boxes.add(box);}
        }
      }
      float[] bounds=new float[boxes.size()*6];
      for(int i=0;i<boxes.size();i++) {
        AxisAlignedBB b=boxes.get(i);int n=i*6;
        bounds[n]=(float)(b.minX-x);bounds[n+1]=(float)(b.minY-y);bounds[n+2]=(float)(b.minZ-z);
        bounds[n+3]=(float)(b.maxX-x);bounds[n+4]=(float)(b.maxY-y);bounds[n+5]=(float)(b.maxZ-z);
      }
      Object builtArray=Array.newInstance(builtType,built.size());int[] prepared=new int[built.size()];
      for(int i=0;i<prepared.length;i++){Array.set(builtArray,i,built.get(i));prepared[i]=results.get(i);}
      // Angelica bakes worker output into immutable lists before upload.
      // Publish new snapshots instead of mutating the baked collections.
      dataType.getField("globalBlockEntities").set(data,Collections.unmodifiableList(nextGlobal));
      dataType.getField("culledBlockEntities").set(data,Collections.unmodifiableList(nextCulled));
      dataType.getField("culledBlockEntityBounds").set(data,bounds);
      // Distance is still checked per TESR by the dispatcher. Avoid calling arbitrary mod distance overrides on workers.
      dataType.getField("maxTeRenderDistSq").setDouble(data,Double.POSITIVE_INFINITY);
      dataType.getField("sectionBuilt").set(data,builtArray);dataType.getField("sectionBuiltResults").set(data,prepared);
      ru.givler.mbo.client.diagnostics.TileEntityRenderTrace.section(x,y,z,nextGlobal.size(),nextCulled.size(),prepared.length);
      if(!logged){logged=true;System.out.println("[MBO] Angelica live TESR metadata moved to client-thread upload; terrain meshing stays threaded");}
    }catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot publish Angelica TESR snapshot",e);}
  }
  public static boolean inside(AxisAlignedBB b,int x,int y,int z) {
    return b!=null && b.minX>=x && b.minY>=y && b.minZ>=z && b.maxX<=x+16 && b.maxY<=y+16 && b.maxZ<=z+16;
  }
  private static Object field(Object o,String name)throws ReflectiveOperationException {
    for(Class<?> c=o.getClass();c!=null;c=c.getSuperclass())try{Field f=c.getDeclaredField(name);f.setAccessible(true);return f.get(o);}catch(NoSuchFieldException ignored){}
    throw new NoSuchFieldException(name);
  }
}
