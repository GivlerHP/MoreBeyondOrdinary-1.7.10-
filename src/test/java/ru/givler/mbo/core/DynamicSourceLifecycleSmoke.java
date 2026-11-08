package ru.givler.mbo.core;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityPig;
import ru.givler.mbo.client.lighting.ColoredDynamicLights;
import sun.misc.Unsafe;
public final class DynamicSourceLifecycleSmoke {
  public static void main(String[] args)throws Exception {
    Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);Unsafe u=(Unsafe)f.get(null);
    Minecraft mc=(Minecraft)u.allocateInstance(Minecraft.class);
    Entity a=(Entity)u.allocateInstance(EntityPig.class),b=(Entity)u.allocateInstance(EntityPig.class);
    Constructor<ColoredDynamicLights.Source> c=ColoredDynamicLights.Source.class.getDeclaredConstructor(Entity.class,int.class,double.class,double.class,double.class);c.setAccessible(true);
    Method publish=ColoredDynamicLights.class.getDeclaredMethod("publish",Minecraft.class,List.class,double.class);publish.setAccessible(true);
    ColoredDynamicLights d=ColoredDynamicLights.INSTANCE;
    ColoredDynamicLights.Source first=c.newInstance(a,0xffff0000,0,64,0),second=c.newInstance(b,0xff0000ff,20,64,0);
    publish.invoke(d,mc,new ArrayList<>(Arrays.asList(first,second)),.1);long g=d.generation();
    publish.invoke(d,mc,new ArrayList<>(Arrays.asList(second,first)),.1);
    if(d.generation()!=g)throw new AssertionError("Camera reorder rebuilt stationary sources");
    publish.invoke(d,mc,new ArrayList<>(Arrays.asList(c.newInstance(a,0xffff0000,.06,64,0),second)),.1);
    if(d.generation()!=g)throw new AssertionError("Jitter rebuilt sources");
    publish.invoke(d,mc,new ArrayList<>(Arrays.asList(c.newInstance(a,0xffff0000,.12,64,0),second)),.1);
    if(d.generation()!=++g || d.sources().get(1)!=second)throw new AssertionError("Movement or stationary-source retention failed");
    publish.invoke(d,mc,new ArrayList<>(Arrays.asList(c.newInstance(a,0xff00ff00,.12,64,0),second)),.1);
    if(d.generation()!=++g)throw new AssertionError("Stationary color change lost");
    publish.invoke(d,mc,new ArrayList<>(Arrays.asList(second)),.1);
    if(d.generation()!=++g || d.sources().size()!=1)throw new AssertionError("Removal lost");
    publish.invoke(d,mc,new ArrayList<>(),.1);
    if(!d.sources().isEmpty())throw new AssertionError("Stale light");
    System.out.println("DYNAMIC_SOURCE_LIFECYCLE_OK reorder, accumulated motion, color, removal");
  }
}
