package ru.givler.mbo.client.render.decormodels;

import net.minecraft.client.renderer.Tessellator;

/** Raw vertex snapshots do not include FalseTweaks' triangle conversion state. */
final class DecorVertexCompatibility {
  static int stride(Tessellator builder,int length,int count) {
    if(hasTriangulator(builder))try {
      // shaderOn is reset to None after every addVertex by FalseTweaks' OptiFine mixin.
      // It is not a persisted description of an already completed buffer.
      Object shader=builder.getClass().getMethod("shaderOn").invoke(builder);
      try {
        Class<?> config=Class.forName("Config",false,builder.getClass().getClassLoader());
        if(Boolean.TRUE.equals(config.getMethod("isShaders").invoke(null)))
          shader=java.lang.Enum.valueOf((Class)shader.getClass(),"Optifine");
      }catch(ClassNotFoundException absent) { /* Vanilla renderer. */ }
      Class<?> info=Class.forName("com.falsepattern.falsetweaks.modules.vertexapi.VertexInfo",false,builder.getClass().getClassLoader());
      int size=((Number)info.getMethod("getVertexInfo",shader.getClass(),int.class).invoke(null,shader,1)).intValue();
      if(size>=8 && length%size==0)return size;
      return -1;
    }catch(ReflectiveOperationException error){return -1;}
    return count>0 && length%count==0 && length/count>=8?length/count:-1;
  }
  static void replay(Tessellator builder,int[] raw,int stride) {
    replay(builder,raw,stride,0,0);
  }
  static void replay(Tessellator builder,int[] raw,int stride,int rgb,int light) {
    boolean little=java.nio.ByteOrder.nativeOrder()==java.nio.ByteOrder.LITTLE_ENDIAN;
    for(int i=0;i<raw.length;i+=stride) {
      int color=raw[i+5],normal=raw[i+6];
      if(rgb!=0)color=ru.givler.mbo.client.lighting.ColoredLightRenderHooks.worldTint(color,light,rgb);
      builder.setColorRGBA(color>>>(little?0:24)&255,color>>>(little?8:16)&255,
          color>>>(little?16:8)&255,color>>>(little?24:0)&255);
      builder.setNormal((byte)normal/127F,(byte)(normal>>8)/127F,(byte)(normal>>16)/127F);
      builder.addVertexWithUV(Float.intBitsToFloat(raw[i]),Float.intBitsToFloat(raw[i+1]),Float.intBitsToFloat(raw[i+2]),
          Float.intBitsToFloat(raw[i+3]),Float.intBitsToFloat(raw[i+4]));
    }
  }
  static boolean beginQuads(Tessellator builder) {
    if(!hasTriangulator(builder))return false;
    invoke(builder,"disableTriangulatorLocal");return true;
  }
  static void endQuads(Tessellator builder,boolean owned) {
    if(owned)invoke(builder,"enableTriangulatorLocal");
  }
  private static void invoke(Tessellator builder,String name) {
    try {builder.getClass().getMethod(name).invoke(builder);}
    catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot scope decor quad rendering",error);}
  }
  private static Class<?> checked;
  private static boolean triangulator;
  static synchronized boolean hasTriangulator(Tessellator builder) {
    Class<?> type=builder.getClass();
    if(checked!=type) {
      checked=type;triangulator=false;
      for(java.lang.reflect.Method method:type.getMethods())
        if("hackedQuadRendering".equals(method.getName()) && method.getParameterTypes().length==0) {
          triangulator=true;break;
        }
    }
    return triangulator;
  }
}
