package ru.givler.mbo.integration.angelica.client.gui;

import com.google.common.collect.ImmutableList;
import java.lang.reflect.*;
import java.util.function.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/** Expose Angelica's existing pane CTM flag through its normal Apply/Undo menu. */
public final class AngelicaCtmOptions {
  private static final String ROOT="me.jellysquid.mods.sodium.client.gui.options.";
  private AngelicaCtmOptions() {}
  public static Object createPage()throws ReflectiveOperationException {
    final Field flag=Class.forName("jss.notfine.config.MCPatcherForgeConfig$ConnectedTextures").getField("glassPane");
    final boolean[] data={flag.getBoolean(null)};
    Class<?> storageType=Class.forName(ROOT+"storage.OptionStorage");
    Object storage=Proxy.newProxyInstance(storageType.getClassLoader(),new Class<?>[]{storageType},(p,m,a)->{
      if(m.getName().equals("getData"))return data;
      if(m.getName().equals("save")) {
        flag.setBoolean(null,data[0]);
        Class<?> config=configClass();
        Class.forName("com.gtnewhorizon.gtnhlib.config.ConfigurationManager").getMethod("save",Class[].class)
            .invoke(null,(Object)new Class<?>[]{config});
        Minecraft mc=Minecraft.getMinecraft();
        if(mc.theWorld!=null && mc.renderGlobal!=null)mc.renderGlobal.loadRenderers();
        return null;
      }
      if(m.getName().equals("hashCode"))return System.identityHashCode(p);
      if(m.getName().equals("equals"))return p==a[0];
      return "Angelica CTM storage";
    });
    Object builder=Class.forName(ROOT+"OptionImpl").getMethod("createBuilder",Class.class,storageType).invoke(null,Boolean.class,storage);
    builder.getClass().getMethod("setName",String.class).invoke(builder,I18n.format("mbo.ctm.panes"));
    builder.getClass().getMethod("setTooltip",String.class).invoke(builder,I18n.format("mbo.ctm.panes.hint"));
    BiConsumer<boolean[],Boolean> setter=(values,value)->values[0]=value;
    Function<boolean[],Boolean> getter=values->values[0];
    builder.getClass().getMethod("setBinding",BiConsumer.class,Function.class).invoke(builder,setter,getter);
    Function<Object,Object> control=option->{try {
      return Class.forName(ROOT+"control.TickBoxControl").getConstructor(Class.forName(ROOT+"Option")).newInstance(option);
    }catch(ReflectiveOperationException error){throw new IllegalStateException(error);}};
    builder.getClass().getMethod("setControl",Function.class).invoke(builder,control);
    Object option=builder.getClass().getMethod("build").invoke(builder);
    Object group=Class.forName(ROOT+"OptionGroup").getMethod("createBuilder").invoke(null);
    group.getClass().getMethod("add",Class.forName(ROOT+"Option")).invoke(group,option);
    return Class.forName(ROOT+"OptionPage").getConstructor(String.class,ImmutableList.class)
        .newInstance("CTM",ImmutableList.of(group.getClass().getMethod("build").invoke(group)));
  }
  public static Class<?> configClass()throws ClassNotFoundException {
    return Class.forName("jss.notfine.config.MCPatcherForgeConfig$ConnectedTextures");
  }
}
