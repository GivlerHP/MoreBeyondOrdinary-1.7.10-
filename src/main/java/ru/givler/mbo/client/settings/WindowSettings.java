package ru.givler.mbo.client.settings;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

/** Uses the existing Minecraft/LWJGL3ify settings, including changes made with F3+P. */
public final class WindowSettings {
  private static boolean lookedUp, pending, targetFullscreen, refreshFullscreen;
  private static Field borderless, configuration;
  private WindowSettings() {}

  private static void lookup() {
    if(lookedUp)return;
    lookedUp=true;
    try {
      Class<?> config=Class.forName("me.eigenraven.lwjgl3ify.core.Config");
      borderless=config.getField("WINDOW_BORDERLESS_REPLACES_FULLSCREEN");
      configuration=config.getField("config");
    }catch(ClassNotFoundException | NoSuchFieldException absent){borderless=null;configuration=null;}
  }
  public static boolean supportsBorderless(){lookup();return borderless!=null;}
  public static boolean borderless() {
    lookup();if(borderless==null)return false;
    try{return borderless.getBoolean(null);}catch(IllegalAccessException error){throw new IllegalStateException(error);}
  }
  public static boolean pause(){return Minecraft.getMinecraft().gameSettings.pauseOnLostFocus;}
  public static boolean fullscreen(){return Minecraft.getMinecraft().isFullScreen();}

  public static void apply(boolean pause,boolean fullscreen,boolean withoutBorders) {
    Minecraft mc=Minecraft.getMinecraft();
    boolean changed=false;
    if(supportsBorderless() && borderless()!=withoutBorders)try {
      Configuration config=(Configuration)configuration.get(null);
      config.get("window","borderless",false).set(withoutBorders);config.save();
      borderless.setBoolean(null,withoutBorders);changed=true;
    }catch(IllegalAccessException error){throw new IllegalStateException("Cannot save LWJGL3ify window mode",error);}
    mc.gameSettings.pauseOnLostFocus=pause;
    mc.gameSettings.saveOptions();
    targetFullscreen=fullscreen;
    refreshFullscreen|=changed;
    pending=refreshFullscreen || fullscreen!=mc.isFullScreen();
  }

  /** Apply after the GUI's Apply iteration; resizing may recreate that GUI. */
  public static void tick() {
    if(!pending)return;
    pending=false;
    Minecraft mc=Minecraft.getMinecraft();
    boolean refresh=refreshFullscreen;refreshFullscreen=false;
    if(refresh && mc.isFullScreen())mc.toggleFullscreen();
    if(mc.isFullScreen()!=targetFullscreen)mc.toggleFullscreen();
    mc.gameSettings.saveOptions();
  }
}
