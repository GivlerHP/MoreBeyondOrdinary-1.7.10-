package ru.givler.mbo.integration.angelica.client.gui;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import ru.givler.mbo.config.ColoredLightConfig;
import ru.givler.mbo.client.lighting.ColoredLightClient;
import ru.givler.mbo.client.lighting.ColoredEntityLightHooks;

/** Native Angelica controls through its optional API; vanilla keeps MBO's existing menu. */
public final class AngelicaColoredLightOptions {
  private static final String ROOT="me.jellysquid.mods.sodium.client.gui.options.";
  private AngelicaColoredLightOptions() {}
  public static boolean attach(GuiScreen screen) {
    if(!screen.getClass().getName().equals("me.flashyreese.mods.reeses_sodium_options.client.gui.ReeseSodiumVideoOptionsScreen"))return false;
    try {
      Field field=screen.getClass().getDeclaredField("pages");field.setAccessible(true);
      List<Object> pages=(List<Object>)field.get(screen);
      for(Object page:pages)if("RGB-MBO".equals(page.getClass().getMethod("getName").invoke(page)))return true;
      AngelicaGuiScale.replace(pages);
      AngelicaWindowOptions.attach(pages);
      pages.add(createPage());
      pages.add(AngelicaCtmOptions.createPage());
      screen.getClass().getMethod("rebuildGUI").invoke(screen);
      return true;
    }catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot add RGB-MBO page to Angelica",e);}
  }
  public static Object createPage() throws ReflectiveOperationException {
    final Map<String,Object> values=new HashMap<String,Object>();
    values.put("enabled",ColoredLightConfig.enabled);values.put("entities",ColoredLightConfig.entityLighting);
    values.put("dynamic",ColoredLightConfig.dynamicLights);values.put("saturation",Math.round(ColoredLightConfig.strength*100));
    values.put("brightness",Math.round(ColoredLightConfig.brightnessGain*100));values.put("speed",ColoredLightConfig.updateSpeed);
    Class<?> storageType=Class.forName(ROOT+"storage.OptionStorage");
    Object storage=Proxy.newProxyInstance(storageType.getClassLoader(),new Class<?>[]{storageType},(proxy,method,args)->{
      if(method.getName().equals("getData"))return values;
      if(method.getName().equals("save")){save(values);return null;}
      if(method.getName().equals("hashCode"))return System.identityHashCode(proxy);
      if(method.getName().equals("equals"))return proxy==args[0];
      return "MBO RGB options";
    });
    Object group=Class.forName(ROOT+"OptionGroup").getMethod("createBuilder").invoke(null);
    add(group,storage,storageType,"enabled",Boolean.class,1,"offHint");
    add(group,storage,storageType,"saturation",Integer.class,100,"offHint");
    add(group,storage,storageType,"brightness",Integer.class,200,"offHint");
    add(group,storage,storageType,"entities",Boolean.class,1,"offHint");
    add(group,storage,storageType,"dynamic",Boolean.class,1,"dynamicHint");
    add(group,storage,storageType,"speed",Integer.class,2,"speedHint");
    Object built=group.getClass().getMethod("build").invoke(group);
    return Class.forName(ROOT+"OptionPage").getConstructor(String.class,ImmutableList.class)
        .newInstance("RGB-MBO",ImmutableList.of(built));
  }
  private static void add(Object group,Object storage,Class<?> storageType,String key,Class<?> valueType,int max,String hint)throws ReflectiveOperationException {
    Object builder=Class.forName(ROOT+"OptionImpl").getMethod("createBuilder",Class.class,storageType).invoke(null,valueType,storage);
    builder.getClass().getMethod("setName",String.class).invoke(builder,I18n.format("mbo.light."+key));
    builder.getClass().getMethod("setTooltip",String.class).invoke(builder,I18n.format("mbo.light."+hint));
    BiConsumer<Map<String,Object>,Object> setter=(data,value)->data.put(key,value);
    Function<Map<String,Object>,Object> getter=data->data.get(key);
    builder.getClass().getMethod("setBinding",BiConsumer.class,Function.class).invoke(builder,setter,getter);
    Function<Object,Object> control=option->{try {
      Class<?> optionType=Class.forName(ROOT+"Option");
      if(valueType==Boolean.class)return Class.forName(ROOT+"control.TickBoxControl").getConstructor(optionType).newInstance(option);
      Class<?> formatterType=Class.forName(ROOT+"control.ControlValueFormatter");
      Object formatter=Proxy.newProxyInstance(formatterType.getClassLoader(),new Class<?>[]{formatterType},(p,m,a)->{
        if(m.getName().equals("format"))return key.equals("speed")?I18n.format("mbo.light.speed."+a[0]):a[0]+"%";
        if(m.getName().equals("hashCode"))return System.identityHashCode(p);
        if(m.getName().equals("equals"))return p==a[0];return "MBO formatter";
      });
      return Class.forName(ROOT+"control.SliderControl").getConstructor(optionType,int.class,int.class,int.class,formatterType)
          .newInstance(option,0,max,1,formatter);
    }catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot create native RGB control",e);}};
    builder.getClass().getMethod("setControl",Function.class).invoke(builder,control);
    Object option=builder.getClass().getMethod("build").invoke(builder);
    group.getClass().getMethod("add",Class.forName(ROOT+"Option")).invoke(group,option);
  }
  private static void save(Map<String,Object> values) {
    boolean enabled=(Boolean)values.get("enabled"), entities=(Boolean)values.get("entities"), dynamic=(Boolean)values.get("dynamic");
    float saturation=((Integer)values.get("saturation"))/100F, brightness=((Integer)values.get("brightness"))/100F;
    boolean rebuild=enabled!=ColoredLightConfig.enabled || saturation!=ColoredLightConfig.strength || brightness!=ColoredLightConfig.brightnessGain;
    boolean dynamicChanged=dynamic!=ColoredLightConfig.dynamicLights;
    ColoredLightConfig.enabled=enabled;ColoredLightConfig.entityLighting=entities;ColoredLightConfig.dynamicLights=dynamic;
    ColoredLightConfig.strength=saturation;ColoredLightConfig.brightnessGain=brightness;ColoredLightConfig.updateSpeed=(Integer)values.get("speed");
    if(!entities)ColoredEntityLightHooks.release();
    ColoredLightConfig.saveUserOptions();
    if(rebuild)ColoredLightClient.INSTANCE.applyOptions();
    else if(dynamicChanged)ColoredLightClient.INSTANCE.applyDynamicOptions();
  }
}
