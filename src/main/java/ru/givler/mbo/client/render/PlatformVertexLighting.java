package ru.givler.mbo.client.render;

import java.lang.reflect.Field;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.renderer.Tessellator;
import ru.givler.mbo.client.lighting.ColoredLightRenderHooks;
import ru.givler.mbo.client.lighting.ColoredLightShaderBridge;
import ru.givler.mbo.config.ColoredLightConfig;

/** Lights the platform's completed local-space mesh before it enters the display list. */
public final class PlatformVertexLighting {
  private static final Field RAW=ReflectionHelper.findField(Tessellator.class,"rawBuffer","field_78405_h");
  private static final Field INDEX=ReflectionHelper.findField(Tessellator.class,"rawBufferIndex","field_78411_s");
  private static final Field COUNT=ReflectionHelper.findField(Tessellator.class,"vertexCount","field_78406_i");
  private PlatformVertexLighting() {}
  private static boolean resolveManager=true;
  private static java.lang.reflect.Method managerGet;
  /** GTNHLib's main-thread DirectTessellator has no vanilla rawBuffer. */
  static boolean begin() {
    if(resolveManager) {
      resolveManager=false;
      try {managerGet=Class.forName("com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager").getMethod("get");}
      catch(ClassNotFoundException absent) {return false;}
      catch(ReflectiveOperationException error) {throw new IllegalStateException("Cannot resolve platform tessellator",error);}
    }
    return managerGet!=null && ru.givler.mbo.integration.angelica.client.render.AngelicaDeferredTessellator.begin();
  }
  static Tessellator tessellator() {
    if(managerGet==null)return Tessellator.instance;
    try {return (Tessellator)managerGet.invoke(null);}
    catch(ReflectiveOperationException error) {throw new IllegalStateException("Cannot obtain platform tessellator",error);}
  }
  static void end(boolean owned) {
    if(managerGet!=null)ru.givler.mbo.integration.angelica.client.render.AngelicaDeferredTessellator.end(owned);
  }

  static void apply(Tessellator tess,double x,double y,double z) {
    if(!ColoredLightConfig.enabled || ColoredLightShaderBridge.active)return;
    try {
      int count=COUNT.getInt(tess),used=INDEX.getInt(tess);
      if(count==0)return;
      if(used%count!=0 || used/count<8)throw new IllegalStateException("Unsupported platform vertex layout");
      apply((int[])RAW.get(tess),count,used/count,x,y,z);
    }catch(IllegalAccessException e){throw new IllegalStateException("Cannot light platform mesh",e);}
  }
  public static void apply(int[] data,int count,int stride,double x,double y,double z) {
    if(!ColoredLightConfig.enabled || ColoredLightShaderBridge.active)return;
    for(int q=0;q+3<count;q+=4) {
      int a=q*stride,b=a+stride,c=b+stride;
      double ux=value(data,b)-value(data,a),uy=value(data,b+1)-value(data,a+1),uz=value(data,b+2)-value(data,a+2);
      double vx=value(data,c)-value(data,a),vy=value(data,c+1)-value(data,a+1),vz=value(data,c+2)-value(data,a+2);
      double nx=uy*vz-uz*vy,ny=uz*vx-ux*vz,nz=ux*vy-uy*vx;
      double length=Math.sqrt(nx*nx+ny*ny+nz*nz);
      if(length!=0){nx*=.5/length;ny*=.5/length;nz*=.5/length;}
      // Sample the incident side of the face. The moving block's interior is
      // not a world light cell; sampling it dilutes RGB with unlit solid cells.
      for(int v=0;v<4;v++) {
        int at=(q+v)*stride;
        int rgb=ColoredLightRenderHooks.worldRGB(x+value(data,at)+nx,y+value(data,at+1)+ny,z+value(data,at+2)+nz);
        int brightness=data[at+7];
        data[at+5]=ColoredLightRenderHooks.worldTint(data[at+5],brightness,rgb);
        data[at+7]=ColoredLightRenderHooks.worldBrightness(brightness,rgb);
      }
    }
  }
  private static float value(int[] data,int at){return Float.intBitsToFloat(data[at]);}
}
