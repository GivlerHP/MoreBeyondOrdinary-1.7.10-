package ru.givler.mbo.client.texture;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

/** Custom MBO geometry uses the installed renderer's CTM rules, atlas and reload lifecycle. */
public final class ConnectedTextureHooks {
  private static volatile Backend backend;
  private static volatile boolean initialized;
  private static boolean reportedFailure;
  private static MethodHandle enabled, mcpatcherEnabled;
  private ConnectedTextureHooks() {}

  private static synchronized void initialize() {
    if(initialized)return;
    try {
      ClassLoader loader=ConnectedTextureHooks.class.getClassLoader();
      if(cpw.mods.fml.common.Loader.isModLoaded("angelica")) {
        backend=bind(Class.forName("com.prupe.mcpatcher.ctm.CTMUtils",false,loader),false);
        enabled=MethodHandles.publicLookup().unreflectGetter(Class.forName("jss.notfine.config.MCPatcherForgeConfig$ConnectedTextures",false,loader).getField("enabled"));
        mcpatcherEnabled=MethodHandles.publicLookup().unreflectGetter(Class.forName("com.gtnewhorizons.angelica.config.AngelicaConfig",false,loader).getField("enableMCPatcherForgeFeatures"));
      } else {
        try {
          backend=bind(Class.forName("ConnectedTextures",false,loader),true);
          enabled=MethodHandles.publicLookup().unreflect(Class.forName("Config",false,loader).getMethod("isConnectedTextures"));
        }
        catch(ClassNotFoundException absent) { backend=null; }
      }
      if(backend!=null)System.out.println("[MBO] Custom model CTM delegate: "+(backend.optifine?"OptiFine":"Angelica/MCPatcher")
          +"; resource-pack rules and graphics settings remain owned by the renderer");
    } catch(ReflectiveOperationException error) {
      throw new IllegalStateException("Cannot bind installed connected-texture API",error);
    }
    initialized=true;
  }

  static Backend bind(Class<?> api,boolean optifine)throws ReflectiveOperationException {
    return new Backend(MethodHandles.publicLookup().unreflect(optifine
        ?api.getMethod("getConnectedTexture",IBlockAccess.class,Block.class,int.class,int.class,int.class,int.class,IIcon.class)
        :api.getMethod("getBlockIcon",IIcon.class,Block.class,IBlockAccess.class,int.class,int.class,int.class,int.class)),optifine);
  }

  public static IIcon resolve(IIcon original,Block block,IBlockAccess world,int x,int y,int z,int side) {
    if(original==null || block==null || world==null || side<0 || side>5)return original;
    initialize();Backend current=backend;
    if(current==null)return original;
    try {
      if(enabled!=null && !(boolean)enabled.invokeExact())return original;
      if(mcpatcherEnabled!=null && !(boolean)mcpatcherEnabled.invokeExact())return original;
      return current.resolve(original,block,world,x,y,z,side);
    }
    catch(VirtualMachineError error) { throw error; }
    catch(ThreadDeath error) { throw error; }
    catch(Throwable error) {
      synchronized(ConnectedTextureHooks.class) {
        if(!reportedFailure){reportedFailure=true;System.err.println("[MBO] Custom model CTM failed; using original icon: "+error);}
      }
      return original;
    }
  }

  static final class Backend {
    final MethodHandle method;final boolean optifine;
    Backend(MethodHandle method,boolean optifine){this.method=method;this.optifine=optifine;}
    IIcon resolve(IIcon icon,Block block,IBlockAccess world,int x,int y,int z,int side)throws Throwable {
      IIcon result=optifine?(IIcon)method.invokeExact(world,block,x,y,z,side,icon)
          :(IIcon)method.invokeExact(icon,block,world,x,y,z,side);
      return result==null?icon:result;
    }
  }

  /** World-facing normal; avoids treating every rotated model face as north. */
  public static int face(double nx,double ny,double nz) {
    double ax=Math.abs(nx),ay=Math.abs(ny),az=Math.abs(nz);
    if(ax+ay+az<1e-12)return -1;
    if(ay>=ax && ay>=az)return ny<0?0:1;
    if(az>=ax)return nz<0?2:3;
    return nx<0?4:5;
  }
  public static int rotatedFace(int normal,int rotation) {
    double x=(byte)normal,y=(byte)(normal>>8),z=(byte)(normal>>16);
    switch(rotation&3) {
      case 0:return face(-x,y,-z);
      case 1:return face(z,y,-x);
      case 3:return face(-z,y,x);
      default:return face(x,y,z);
    }
  }
  public static int face(double[] a,double[] b,double[] c) {
    double ux=b[0]-a[0],uy=b[1]-a[1],uz=b[2]-a[2];
    double vx=c[0]-a[0],vy=c[1]-a[1],vz=c[2]-a[2];
    return face(uy*vz-uz*vy,uz*vx-ux*vz,ux*vy-uy*vx);
  }
}
