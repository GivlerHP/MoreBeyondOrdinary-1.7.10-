package ru.givler.mbo.lighting;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import ru.givler.mbo.integration.angelica.client.render.AngelicaTileEntitySnapshot;
public final class AngelicaTileEntitySnapshotSmoke {
  public static final class Manager { public net.minecraft.world.World world; }
  public static final class Section { public int getOriginX(){return 0;}public int getOriginY(){return -64;}public int getOriginZ(){return 0;} }
  public static void main(String[] args)throws Exception {
    Field unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
    sun.misc.Unsafe unsafe=(sun.misc.Unsafe)unsafeField.get(null);
    Minecraft mc=(Minecraft)unsafe.allocateInstance(Minecraft.class);
    Field singleton=null,thread=null;
    for(Field f:Minecraft.class.getDeclaredFields()) {
      if(Modifier.isStatic(f.getModifiers()) && f.getType()==Minecraft.class)singleton=f;
      if(f.getType()==Thread.class)thread=f;
    }
    singleton.setAccessible(true);singleton.set(null,mc);thread.setAccessible(true);thread.set(mc,Thread.currentThread());
    Class<?> dataClass=Class.forName("com.gtnewhorizons.angelica.rendering.celeritas.AngelicaBuiltRenderSectionData");
    Object data=dataClass.newInstance();
    List<TileEntity> global=(List<TileEntity>)dataClass.getField("globalBlockEntities").get(data);
    List<TileEntity> culled=(List<TileEntity>)dataClass.getField("culledBlockEntities").get(data);
    global.add((TileEntity)unsafe.allocateInstance(TileEntity.class));culled.add((TileEntity)unsafe.allocateInstance(TileEntity.class));
    dataClass.getMethod("bake").invoke(data);
    AngelicaTileEntitySnapshot.refresh(new Manager(),new Section(),data);
    global=(List<TileEntity>)dataClass.getField("globalBlockEntities").get(data);
    culled=(List<TileEntity>)dataClass.getField("culledBlockEntities").get(data);
    if(!global.isEmpty() || !culled.isEmpty() || ((float[])dataClass.getField("culledBlockEntityBounds").get(data)).length!=0
        || Array.getLength(dataClass.getField("sectionBuilt").get(data))!=0
        || ((int[])dataClass.getField("sectionBuiltResults").get(data)).length!=0)throw new AssertionError("Unloaded world retained stale metadata");
    final Throwable[] failure={null};Thread worker=new Thread(()->{try{AngelicaTileEntitySnapshot.refresh(new Manager(),new Section(),data);}catch(Throwable e){failure[0]=e;}});
    worker.start();worker.join();if(!(failure[0] instanceof IllegalStateException))throw new AssertionError("Worker allowed to touch live metadata");
    if(!AngelicaTileEntitySnapshot.inside(AxisAlignedBB.getBoundingBox(0,-64,0,16,-48,16),0,-64,0)
        || AngelicaTileEntitySnapshot.inside(AxisAlignedBB.getBoundingBox(-1,-64,0,16,-48,16),0,-64,0)
        || AngelicaTileEntitySnapshot.inside(null,0,-64,0))throw new AssertionError("Section/global bounds classification");
    AngelicaTileEntitySnapshot.refresh(new Manager(),new Section(),data);
    System.out.println("Actual baked Angelica metadata: immutable lists replaced, repeated upload safe, unload clears data, worker rejected and negative-height bounds passed");
  }
}
