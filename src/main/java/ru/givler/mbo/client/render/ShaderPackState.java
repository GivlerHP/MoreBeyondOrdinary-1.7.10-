package ru.givler.mbo.client.render;

import java.lang.reflect.Method;

/** Optional Iris/OptiFine state query. A selected but disabled pack is not active. */
public final class ShaderPackState {
  private static final Access ACCESS = findAccess();
  private ShaderPackState() {}
  private static Access findAccess() {
    try {
      Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
      Object instance = api.getMethod("getInstance").invoke(null);
      Class<?> iris = Class.forName("net.coderbot.iris.Iris");
      Method revision;
      try { revision = iris.getMethod("getShaderPackLoadId"); }
      catch (NoSuchMethodException old) { revision = iris.getMethod("getCurrentPackName"); }
      return new Access(instance, api.getMethod("isShaderPackInUse"), revision);
    } catch (ReflectiveOperationException | LinkageError absent) {
      try {
        return new Access(null, null, Class.forName("shadersmod.client.Shaders").getMethod("getShaderPackName"));
      } catch (ReflectiveOperationException | LinkageError ignored) { return null; }
    }
  }
  public static boolean isActive() {
    if (ACCESS == null) return false;
    try {
      if (ACCESS.active != null) return Boolean.TRUE.equals(ACCESS.active.invoke(ACCESS.api));
      Object name = ACCESS.revision.invoke(null);
      return name != null && !"OFF".equalsIgnoreCase(name.toString())
          && !"(internal)".equalsIgnoreCase(name.toString()) && !name.toString().isEmpty();
    } catch (ReflectiveOperationException | LinkageError ignored) { return false; }
  }
  public static Object revision() {
    if (ACCESS == null) return null;
    try { return ACCESS.revision.invoke(null); }
    catch (ReflectiveOperationException | LinkageError ignored) { return null; }
  }
  private static final class Access {
    final Object api; final Method active, revision;
    Access(Object api, Method active, Method revision) { this.api=api; this.active=active; this.revision=revision; }
  }
}
