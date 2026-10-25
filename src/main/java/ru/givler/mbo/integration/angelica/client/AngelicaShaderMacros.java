package ru.givler.mbo.integration.angelica.client;

import java.lang.reflect.*;
import java.util.*;

/** Advertise the CTM implementation already supplying the atlas UVs to shader packs. */
public final class AngelicaShaderMacros {
  private AngelicaShaderMacros() {}
  public static Iterable<?> withConnectedTextures(Iterable<?> original) {
    try {
      if(!Class.forName("com.gtnewhorizons.angelica.config.AngelicaConfig")
          .getField("enableMCPatcherForgeFeatures").getBoolean(null)
          || !Class.forName("jss.notfine.config.MCPatcherForgeConfig$ConnectedTextures")
          .getField("enabled").getBoolean(null))return original;
      Class<?> pair=Class.forName("net.coderbot.iris.shaderpack.StringPair");
      Method key=pair.getMethod("getKey");
      List<Object> result=new ArrayList<Object>();
      for(Object entry:original) {
        if("IRIS_HAS_CONNECTED_TEXTURES".equals(key.invoke(entry)))return original;
        result.add(entry);
      }
      result.add(pair.getConstructor(String.class,String.class).newInstance("IRIS_HAS_CONNECTED_TEXTURES",""));
      return result;
    } catch(ReflectiveOperationException error) {
      throw new IllegalStateException("Cannot advertise Angelica CTM to shader packs",error);
    }
  }
}
