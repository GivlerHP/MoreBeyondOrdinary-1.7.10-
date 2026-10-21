package ru.givler.mbo.lighting;

import java.lang.reflect.*;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import ru.givler.mbo.client.settings.WindowSettings;
import ru.givler.mbo.integration.angelica.client.gui.AngelicaWindowOptions;

/** Exercise real Minecraft settings and real Angelica Apply/Undo without opening a window. */
public final class WindowSettingsSmoke {
  public static class Settings extends GameSettings {
    int saves;
    @Override public void saveOptions(){saves++;}
  }
  public static void main(String[] args)throws Exception {
    Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);
    sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);
    Field singleton=Minecraft.class.getDeclaredField("theMinecraft");singleton.setAccessible(true);Object old=singleton.get(null);
    Field locale=net.minecraft.client.resources.I18n.class.getDeclaredField("i18nLocale");locale.setAccessible(true);Object oldLocale=locale.get(null);
    try {
      Minecraft mc=(Minecraft)unsafe.allocateInstance(Minecraft.class);singleton.set(null,mc);
      Settings settings=(Settings)unsafe.allocateInstance(Settings.class);settings.pauseOnLostFocus=true;mc.gameSettings=settings;
      locale.set(null,new net.minecraft.client.resources.Locale());
      List<?> options=AngelicaWindowOptions.createOptions();
      if(options.size()!=(WindowSettings.supportsBorderless()?2:1))throw new AssertionError("Window controls missing");
      Object pause=options.get(options.size()-1);Class<?> type=pause.getClass();
      type.getMethod("setValue",Object.class).invoke(pause,false);
      if(!settings.pauseOnLostFocus || settings.saves!=0)throw new AssertionError("Pending option applied early");
      type.getMethod("reset").invoke(pause);
      if(!Boolean.TRUE.equals(type.getMethod("getValue").invoke(pause)))throw new AssertionError("Undo failed");
      type.getMethod("setValue",Object.class).invoke(pause,false);
      type.getMethod("applyChanges").invoke(pause);
      Object storage=type.getMethod("getStorage").invoke(pause);storage.getClass().getMethod("save").invoke(storage);
      if(settings.pauseOnLostFocus || settings.saves!=1)throw new AssertionError("Focus setting not persisted");
      WindowSettings.tick(); // An unchanged window mode must not invoke native window functions.
      settings.pauseOnLostFocus=true;
      if(!WindowSettings.pause())throw new AssertionError("F3+P changes ignored");
      List<?> secondOptions=AngelicaWindowOptions.createOptions();
      Field full=Minecraft.class.getDeclaredField("fullscreen");full.setAccessible(true);full.setBoolean(mc,true);
      Object secondPause=secondOptions.get(secondOptions.size()-1);
      secondPause.getClass().getMethod("setValue",Object.class).invoke(secondPause,false);
      secondPause.getClass().getMethod("applyChanges").invoke(secondPause);
      Object secondStorage=secondPause.getClass().getMethod("getStorage").invoke(secondPause);
      secondStorage.getClass().getMethod("save").invoke(secondStorage);
      WindowSettings.tick();
      if(!mc.isFullScreen())throw new AssertionError("Another page's fullscreen change was overwritten");
      for(Object option:options)if(option.getClass().getMethod("getControl").invoke(option)==null)throw new AssertionError("Control missing");
      checkPlacement(options.get(0));
      System.out.println("Window settings: real Angelica controls, deferred Apply/Undo, Minecraft save, F3+P sync and no native call for unchanged mode PASS");
    } finally {singleton.set(null,old);locale.set(null,oldLocale);}
  }

  private static void checkPlacement(Object original)throws Exception {
    Field name=original.getClass().getDeclaredField("name");name.setAccessible(true);
    name.set(original,net.minecraft.client.resources.I18n.format("options.fullscreen"));
    String root="me.jellysquid.mods.sodium.client.gui.options.";
    Object builder=Class.forName(root+"OptionGroup").getMethod("createBuilder").invoke(null);
    builder.getClass().getMethod("add",Class.forName(root+"Option")).invoke(builder,original);
    Object group=builder.getClass().getMethod("build").invoke(builder);
    Object page=Class.forName(root+"OptionPage").getConstructor(String.class,com.google.common.collect.ImmutableList.class)
        .newInstance("General",com.google.common.collect.ImmutableList.of(group));
    java.util.List<Object> pages=new java.util.ArrayList<Object>();pages.add(page);
    AngelicaWindowOptions.attach(pages);
    if(pages.size()!=1)throw new AssertionError("Unexpected separate window page");
    List<?> placed=(List<?>)pages.get(0).getClass().getMethod("getOptions").invoke(pages.get(0));
    int expected=WindowSettings.supportsBorderless()?3:2;
    if(placed.size()!=expected || placed.get(0)!=original)throw new AssertionError("Fullscreen replaced or window options misplaced");
    AngelicaWindowOptions.attach(pages);
    if(((List<?>)pages.get(0).getClass().getMethod("getOptions").invoke(pages.get(0))).size()!=expected)
      throw new AssertionError("Window options duplicated on GUI rebuild");
  }
}
