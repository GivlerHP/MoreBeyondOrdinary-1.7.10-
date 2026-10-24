package ru.givler.mbo.integration.angelica.client.gui;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/** Resource-pack controls retain the renderer's configuration and Apply/Undo. */
public final class AngelicaCtmOptions {
  private static final String ROOT="me.jellysquid.mods.sodium.client.gui.options.";
  private static final String CONFIG="jss.notfine.config.MCPatcherForgeConfig$";
  private static final String ANGELICA="com.gtnewhorizons.angelica.config.AngelicaConfig";
  private static final String[][] OPTIONS={
    {"features",ANGELICA,"enableMCPatcherForgeFeatures"},
    {"connected",CONFIG+"ConnectedTextures","enabled"},
    {"panes",CONFIG+"ConnectedTextures","glassPane"},
    {"natural",ANGELICA,"enableNaturalTextures"},
    {"sky",CONFIG+"BetterSkies","enabled"},
    {"skybox",CONFIG+"BetterSkies","skybox"},
    {"mobs",CONFIG+"RandomMobs","enabled"},
    {"colors",CONFIG+"CustomColors","enabled"}
  };
  private AngelicaCtmOptions() {}
  public static Object createPage()throws ReflectiveOperationException {
    final Map<String,Boolean> data=new LinkedHashMap<>();final Map<String,Field> fields=new LinkedHashMap<>();
    for(String[] spec:OPTIONS){Field f=Class.forName(spec[1]).getField(spec[2]);fields.put(spec[0],f);data.put(spec[0],f.getBoolean(null));}
    Class<?> storageType=Class.forName(ROOT+"storage.OptionStorage");
    Object storage=Proxy.newProxyInstance(storageType.getClassLoader(),new Class<?>[]{storageType},(p,m,a)->{
      if(m.getName().equals("getData"))return data;
      if(m.getName().equals("save")) {
        Set<Class<?>> owners=new LinkedHashSet<>();
        for(Map.Entry<String,Field> entry:fields.entrySet()) {
          entry.getValue().setBoolean(null,data.get(entry.getKey()));owners.add(entry.getValue().getDeclaringClass());
        }
        Class.forName("com.gtnewhorizon.gtnhlib.config.ConfigurationManager").getMethod("save",Class[].class)
            .invoke(null,(Object)owners.toArray(new Class<?>[0]));
        // Rebuild native sky/color/CTM resource caches, not just existing chunk meshes.
        Minecraft mc=Minecraft.getMinecraft();mc.refreshResources();return null;
      }
      if(m.getName().equals("hashCode"))return System.identityHashCode(p);
      if(m.getName().equals("equals"))return p==a[0];return "MBO resource pack options";
    });
    Object group=Class.forName(ROOT+"OptionGroup").getMethod("createBuilder").invoke(null);
    for(String[] spec:OPTIONS) {
      String key=spec[0];Object builder=Class.forName(ROOT+"OptionImpl").getMethod("createBuilder",Class.class,storageType).invoke(null,Boolean.class,storage);
      builder.getClass().getMethod("setName",String.class).invoke(builder,I18n.format("mbo.pack."+key));
      builder.getClass().getMethod("setTooltip",String.class).invoke(builder,I18n.format("mbo.pack."+key+".hint"));
      BiConsumer<Map<String,Boolean>,Boolean> setter=(values,value)->values.put(key,value);
      Function<Map<String,Boolean>,Boolean> getter=values->values.get(key);
      builder.getClass().getMethod("setBinding",BiConsumer.class,Function.class).invoke(builder,setter,getter);
      Function<Object,Object> control=option->{try {
        return Class.forName(ROOT+"control.TickBoxControl").getConstructor(Class.forName(ROOT+"Option")).newInstance(option);
      }catch(ReflectiveOperationException error){throw new IllegalStateException(error);}};
      builder.getClass().getMethod("setControl",Function.class).invoke(builder,control);
      Class<?> flag=Class.forName(ROOT+"OptionFlag");Object flags=Array.newInstance(flag,1);
      Array.set(flags,0,Enum.valueOf((Class)flag,key.equals("panes")?"REQUIRES_RENDERER_RELOAD":"REQUIRES_GAME_RESTART"));
      builder.getClass().getMethod("setFlags",flags.getClass()).invoke(builder,flags);
      group.getClass().getMethod("add",Class.forName(ROOT+"Option")).invoke(group,builder.getClass().getMethod("build").invoke(builder));
    }
    return Class.forName(ROOT+"OptionPage").getConstructor(String.class,ImmutableList.class)
        .newInstance(I18n.format("mbo.pack.page"),ImmutableList.of(group.getClass().getMethod("build").invoke(group)));
  }
  public static Class<?> configClass()throws ClassNotFoundException {return Class.forName(CONFIG+"ConnectedTextures");}
}
