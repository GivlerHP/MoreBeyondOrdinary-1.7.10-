package ru.givler.mbo.integration.angelica.client.gui;
import com.google.common.collect.ImmutableList;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
/** Replace Angelica's integer scale with RightProperGUIScale's configured fractional range. */
public final class AngelicaGuiScale {
  private static final String ROOT="me.jellysquid.mods.sodium.client.gui.options.";
  private AngelicaGuiScale() {}
  public static void replace(List<Object> pages)throws ReflectiveOperationException {
    Class<?> scale;
    try{scale=Class.forName("com.github.basdxz.rightproperguiscale.GUIScale");}catch(ClassNotFoundException absent){return;}
    Object replacement=null;
    for(int p=0;p<pages.size();p++) {
      Object page=pages.get(p);List<Object> groups=new ArrayList<Object>();boolean changed=false;
      for(Object group:(List<?>)page.getClass().getMethod("getGroups").invoke(page)) {
        Object builder=Class.forName(ROOT+"OptionGroup").getMethod("createBuilder").invoke(null);
        for(Object option:(List<?>)group.getClass().getMethod("getOptions").invoke(group)) {
          if(I18n.format("options.guiScale").equals(option.getClass().getMethod("getName").invoke(option))) {
            if(replacement==null)replacement=option(scale);option=replacement;changed=true;
          }
          builder.getClass().getMethod("add",Class.forName(ROOT+"Option")).invoke(builder,option);
        }
        groups.add(builder.getClass().getMethod("build").invoke(builder));
      }
      if(changed)pages.set(p,page.getClass().getConstructor(String.class,ImmutableList.class)
          .newInstance(page.getClass().getMethod("getName").invoke(page),ImmutableList.copyOf(groups)));
    }
  }
  static Object option(final Class<?> scale)throws ReflectiveOperationException {
    Class<?> config=Class.forName("com.github.basdxz.rightproperguiscale.config.RightProperGUIScaleConfig");
    final double min=config.getField("GUI_SCALE_MIN").getDouble(null),max=config.getField("GUI_SCALE_MAX").getDouble(null),step=config.getField("GUI_SCALE_STEP").getDouble(null);
    final int ticks=Math.max(1,(int)Math.round((max-min)/step));
    final int[] value={Math.max(0,Math.min(ticks,(int)Math.round((((Number)scale.getMethod("value").invoke(null)).doubleValue()-min)/step)))};
    Class<?> storageType=Class.forName(ROOT+"storage.OptionStorage");
    Object storage=Proxy.newProxyInstance(storageType.getClassLoader(),new Class<?>[]{storageType},(p,m,a)->{
      if(m.getName().equals("getData"))return value;
      if(m.getName().equals("save")) {
        float next=(float)Math.min(max,min+value[0]*step);
        scale.getMethod("set",float.class).invoke(null,next);
        Minecraft.getMinecraft().gameSettings.saveOptions();return null;
      }
      if(m.getName().equals("hashCode"))return System.identityHashCode(p);
      if(m.getName().equals("equals"))return p==a[0];return "RightProperGUIScale storage";
    });
    Object b=Class.forName(ROOT+"OptionImpl").getMethod("createBuilder",Class.class,storageType).invoke(null,Integer.class,storage);
    b.getClass().getMethod("setName",String.class).invoke(b,I18n.format("options.guiScale"));
    b.getClass().getMethod("setTooltip",String.class).invoke(b,I18n.format("mbo.guiScale.hint"));
    BiConsumer<int[],Integer> setter=(data,v)->data[0]=v;Function<int[],Integer> getter=data->data[0];
    b.getClass().getMethod("setBinding",BiConsumer.class,Function.class).invoke(b,setter,getter);
    Function<Object,Object> control=option->{try {
      Class<?> formatter=Class.forName(ROOT+"control.ControlValueFormatter");
      Object f=Proxy.newProxyInstance(formatter.getClassLoader(),new Class<?>[]{formatter},(p,m,a)->{
        if(m.getName().equals("format"))return String.format(Locale.ROOT,"%.2fx",Math.min(max,min+((Integer)a[0])*step));
        if(m.getName().equals("hashCode"))return System.identityHashCode(p);
        if(m.getName().equals("equals"))return p==a[0];return "Fractional GUI scale";
      });
      return Class.forName(ROOT+"control.SliderControl").getConstructor(Class.forName(ROOT+"Option"),int.class,int.class,int.class,formatter).newInstance(option,0,ticks,1,f);
    }catch(ReflectiveOperationException e){throw new IllegalStateException(e);}};
    b.getClass().getMethod("setControl",Function.class).invoke(b,control);
    return b.getClass().getMethod("build").invoke(b);
  }
}
