package ru.givler.mbo.client.render;

import java.lang.invoke.*;
import net.minecraft.world.IBlockAccess;

/** Match terrain's optional dynamic-light backend in the snapshot renderer. */
final class DungeonWallLighting {
  private static boolean initialized;
  private static MethodHandle dynamic;
  private DungeonWallLighting() {}
  static int brightness(IBlockAccess world,int x,int y,int z,int minimum) {
    int packed=world.getLightBrightnessForSkyBlocks(x,y,z,minimum);
    if(!initialized) {
      initialized=true;
      try {
        Class<?> api=Class.forName("com.gtnewhorizons.angelica.dynamiclights.DynamicLights");
        dynamic=MethodHandles.publicLookup().unreflect(api.getMethod("addDynamicLight",IBlockAccess.class,int.class,int.class,int.class,int.class));
      }catch(ClassNotFoundException absent) {
        // Vanilla/OptiFine world lighting remains available without Angelica.
      }catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot bind wall dynamic lighting",error);}
    }
    if(dynamic==null)return packed;
    try{return (int)dynamic.invokeExact(world,x,y,z,packed);}
    catch(RuntimeException|Error error){throw error;}
    catch(Throwable error){throw new IllegalStateException("Cannot sample wall dynamic lighting",error);}
  }
}
