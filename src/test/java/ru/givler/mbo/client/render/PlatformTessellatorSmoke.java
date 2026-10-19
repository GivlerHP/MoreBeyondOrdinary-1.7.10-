package ru.givler.mbo.client.render;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.renderer.Tessellator;
import ru.givler.mbo.integration.angelica.client.render.AngelicaDeferredTessellator;

/** Exercises the actual optional adapter with a Direct/local manager contract. No GL calls. */
public final class PlatformTessellatorSmoke {
  public static final class Local extends Tessellator {public boolean active;}
  public static final class Manager {
    public static final Tessellator direct=new Tessellator();
    public static final Local local=new Local();
    public static Tessellator get(){return local.active?local:direct;}
    public static Local getLocal(){return local;}
    public static void enterLocalMode(){local.active=true;}
    public static void exitLocalMode(){local.active=false;}
  }
  public static void run() throws Exception {
    Map<Field,Object> saved=new LinkedHashMap<Field,Object>();
    try {
      set(saved,PlatformVertexLighting.class,"resolveManager",false);
      set(saved,PlatformVertexLighting.class,"managerGet",Manager.class.getMethod("get"));
      set(saved,AngelicaDeferredTessellator.class,"getLocal",Manager.class.getMethod("getLocal"));
      set(saved,AngelicaDeferredTessellator.class,"enter",Manager.class.getMethod("enterLocalMode"));
      set(saved,AngelicaDeferredTessellator.class,"exit",Manager.class.getMethod("exitLocalMode"));
      set(saved,AngelicaDeferredTessellator.class,"active",Local.class.getField("active"));
      set(saved,AngelicaDeferredTessellator.class,"reported",true);
      if(PlatformVertexLighting.tessellator()!=Manager.direct)throw new AssertionError("Expected direct renderer outside platform scope");
      boolean owned=PlatformVertexLighting.begin();
      try {
        if(!owned || PlatformVertexLighting.tessellator()!=Manager.local)throw new AssertionError("Platform selected DirectTessellator instead of raw-buffer tessellator");
        Manager.local.startDrawingQuads();Manager.local.setColorOpaque_I(0xFFFFFF);Manager.local.setBrightness(0);
        Manager.local.addVertex(0,1,1);Manager.local.addVertex(1,1,1);Manager.local.addVertex(1,1,0);Manager.local.addVertex(0,1,0);
        PlatformVertexLighting.apply(PlatformVertexLighting.tessellator(),0,8,0);
        boolean nested=PlatformVertexLighting.begin();
        PlatformVertexLighting.end(nested);
        if(nested || !Manager.local.active)throw new AssertionError("Nested scope released outer platform buffer");
      }finally {
        Field drawing=Tessellator.class.getDeclaredField("isDrawing");drawing.setAccessible(true);drawing.setBoolean(Manager.local,false);
        PlatformVertexLighting.end(owned);
      }
      if(Manager.local.active || PlatformVertexLighting.tessellator()!=Manager.direct)throw new AssertionError("Platform failed to restore direct renderer");
      System.out.println("Platform tessellator adapter: raw buffer selected over Direct, real quad processing, nested ownership and restoration passed");
    }finally {for(Map.Entry<Field,Object> e:saved.entrySet())e.getKey().set(null,e.getValue());}
  }
  private static void set(Map<Field,Object> saved,Class<?> type,String name,Object value)throws Exception {
    Field f=type.getDeclaredField(name);f.setAccessible(true);saved.put(f,f.get(null));f.set(null,value);
  }
}
