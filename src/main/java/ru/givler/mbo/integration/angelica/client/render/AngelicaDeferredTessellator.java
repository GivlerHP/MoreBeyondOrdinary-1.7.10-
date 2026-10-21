package ru.givler.mbo.integration.angelica.client.render;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Scope the main-thread half of a chunk job just like its worker half. Optional GTNHLib linkage. */
public final class AngelicaDeferredTessellator {
  private static Method getLocal, enter, exit;
  private static Field active;
  private static boolean reported;
  private AngelicaDeferredTessellator() {}

  private static void initialize() throws ReflectiveOperationException {
    if (getLocal != null) return;
    Class<?> manager = Class.forName("com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager");
    Method local = manager.getMethod("getLocal");
    Field enabled = local.getReturnType().getDeclaredField("active");
    enabled.setAccessible(true);
    enter = manager.getMethod("enterLocalMode");
    exit = manager.getMethod("exitLocalMode");
    active = enabled;
    getLocal = local;
  }

  public static boolean begin() {
    ru.givler.mbo.client.diagnostics.TileEntityRenderTrace.deferred("ENTER");
    try {
      initialize();
      if (active.getBoolean(getLocal.invoke(null))) return false;
      enter.invoke(null);
      if (!reported) {
        reported = true;
        System.out.println("[MBO] Angelica deferred terrain meshing: isolated GTNHLib Tessellator enabled");
      }
      return true;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Cannot isolate Angelica deferred Tessellator", e);
    }
  }

  public static void end(boolean owned) {
    try { if(owned)exit.invoke(null); }
    catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Cannot release Angelica deferred Tessellator", e);
    }
    finally {ru.givler.mbo.client.diagnostics.TileEntityRenderTrace.deferred("EXIT");}
  }
}
