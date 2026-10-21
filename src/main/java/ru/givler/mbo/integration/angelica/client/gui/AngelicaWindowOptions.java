package ru.givler.mbo.integration.angelica.client.gui;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import net.minecraft.client.resources.I18n;
import ru.givler.mbo.client.settings.WindowSettings;

/** Native Apply/Undo controls for Minecraft focus and the optional LWJGL3ify mode. */
public final class AngelicaWindowOptions {
  private static final String ROOT="me.jellysquid.mods.sodium.client.gui.options.";
  private AngelicaWindowOptions() {}
  public static void attach(List<Object> pages)throws ReflectiveOperationException {
    for(int p=0;p<pages.size();p++) {
      Object page=pages.get(p);
      List<?> options=(List<?>)page.getClass().getMethod("getOptions").invoke(page);
      boolean found=false;
      for(Object option:options) {
        String name=(String)option.getClass().getMethod("getName").invoke(option);
        if(name.equals(I18n.format("mbo.window.pause")))return;
        if(name.equals(I18n.format("options.fullscreen")))found=true;
      }
      if(!found)continue;
      List<Object> additions=createOptions(),groups=new ArrayList<Object>();
      for(Object group:(List<?>)page.getClass().getMethod("getGroups").invoke(page)) {
        Object builder=Class.forName(ROOT+"OptionGroup").getMethod("createBuilder").invoke(null);
        Method add=builder.getClass().getMethod("add",Class.forName(ROOT+"Option"));
        for(Object option:(List<?>)group.getClass().getMethod("getOptions").invoke(group)) {
          add.invoke(builder,option);
          if(I18n.format("options.fullscreen").equals(option.getClass().getMethod("getName").invoke(option)))
            for(Object addition:additions)add.invoke(builder,addition);
        }
        groups.add(builder.getClass().getMethod("build").invoke(builder));
      }
      pages.set(p,page.getClass().getConstructor(String.class,ImmutableList.class)
          .newInstance(page.getClass().getMethod("getName").invoke(page),ImmutableList.copyOf(groups)));
      return;
    }
    throw new IllegalStateException("Angelica fullscreen option not found");
  }
  public static List<Object> createOptions()throws ReflectiveOperationException {
    Map<String,Boolean> values=new HashMap<String,Boolean>();
    values.put("fullscreen",WindowSettings.fullscreen());values.put("borderless",WindowSettings.borderless());values.put("pause",WindowSettings.pause());
    Map<String,Boolean> initial=new HashMap<String,Boolean>(values);
    Class<?> storageType=Class.forName(ROOT+"storage.OptionStorage");
    Object storage=Proxy.newProxyInstance(storageType.getClassLoader(),new Class<?>[]{storageType},(p,m,a)->{
      if(m.getName().equals("getData"))return values;
      if(m.getName().equals("save")){
        // Another Angelica page can change fullscreen in the same Apply action.
        boolean pause=values.get("pause").equals(initial.get("pause"))?WindowSettings.pause():values.get("pause");
        boolean fullscreen=values.get("fullscreen").equals(initial.get("fullscreen"))?WindowSettings.fullscreen():values.get("fullscreen");
        boolean borderless=values.get("borderless").equals(initial.get("borderless"))?WindowSettings.borderless():values.get("borderless");
        WindowSettings.apply(pause,fullscreen,borderless);initial.putAll(values);return null;
      }
      if(m.getName().equals("hashCode"))return System.identityHashCode(p);
      if(m.getName().equals("equals"))return p==a[0];return "MBO window settings";
    });
    List<Object> options=new ArrayList<Object>();
    for(String key:new String[]{"borderless","pause"}) {
      if(key.equals("borderless") && !WindowSettings.supportsBorderless())continue;
      Object builder=Class.forName(ROOT+"OptionImpl").getMethod("createBuilder",Class.class,storageType).invoke(null,Boolean.class,storage);
      builder.getClass().getMethod("setName",String.class).invoke(builder,I18n.format("mbo.window."+key));
      builder.getClass().getMethod("setTooltip",String.class).invoke(builder,I18n.format("mbo.window."+key+".hint"));
      BiConsumer<Map<String,Boolean>,Boolean> setter=(data,value)->data.put(key,value);
      Function<Map<String,Boolean>,Boolean> getter=data->data.get(key);
      builder.getClass().getMethod("setBinding",BiConsumer.class,Function.class).invoke(builder,setter,getter);
      Function<Object,Object> control=option->{try{return Class.forName(ROOT+"control.TickBoxControl").getConstructor(Class.forName(ROOT+"Option")).newInstance(option);}catch(ReflectiveOperationException error){throw new IllegalStateException(error);}};
      builder.getClass().getMethod("setControl",Function.class).invoke(builder,control);
      Object option=builder.getClass().getMethod("build").invoke(builder);
      options.add(option);
    }
    return options;
  }
}
