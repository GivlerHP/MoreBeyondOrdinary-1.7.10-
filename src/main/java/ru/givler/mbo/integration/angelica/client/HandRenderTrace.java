package ru.givler.mbo.integration.angelica.client;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

/** Bounded passive logging: never cancels an event or changes a draw/state. */
public final class HandRenderTrace {
  static boolean enabled;
  private static int depth,draws;
  private static final Set<String> seen=new HashSet<String>();
  private static void once(String key,String message) {
    if(seen.add(key))System.out.println("[MBO HandTrace] "+message);
  }
  static void beginEvent() {if(enabled)depth=0;}
  @cpw.mods.fml.common.eventhandler.SubscribeEvent(priority=cpw.mods.fml.common.eventhandler.EventPriority.LOWEST,receiveCanceled=true)
  public void hand(net.minecraftforge.client.event.RenderHandEvent event) {
    if(!enabled)return;
    Minecraft mc=Minecraft.getMinecraft();
    once("event"+event.isCanceled(),"RenderHandEvent canceled="+event.isCanceled()+", thirdPerson="+mc.gameSettings.thirdPersonView
        +", hideGUI="+mc.gameSettings.hideGUI+", held="+(mc.thePlayer==null?null:mc.thePlayer.getHeldItem())
        +", cameraZoom="+field(mc.entityRenderer,"cameraZoom","field_78503_V")
        +", debugView="+field(mc.entityRenderer,"debugViewDirection","field_78535_ad"));
  }
  public static void gardenOwnsHand(boolean owns) {
    if(enabled)once("garden"+owns,"GardenAddon renderHand returned "+owns);
  }
  public static void enter(Object renderer) {
    if(!enabled)return;
    depth++;
    String type=renderer.getClass().getName();
    if(seen.contains("enter"+type))return;
    java.nio.FloatBuffer matrix=org.lwjgl.BufferUtils.createFloatBuffer(16);
    GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX,matrix);
    once("enter"+type,"Entered first-person renderer="+type+", cachedItem="+field(renderer,"itemToRender","field_78453_b")
        +", projection="+matrix.get(0)+","+matrix.get(5)+","+matrix.get(10)+","+matrix.get(11)+","+matrix.get(14)+","+matrix.get(15));
  }
  public static void exit(){if(enabled)depth=Math.max(0,depth-1);}
  public static void item(Object stack) {
    if(enabled && depth>0 && draws++<4)System.out.println("[MBO HandTrace] First-person item geometry requested: "+stack);
  }
  private static Object field(Object object,String... names) {
    if(object==null)return null;
    for(Class<?> type=object.getClass();type!=null;type=type.getSuperclass())for(String name:names)
      try {Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(object);}
      catch(ReflectiveOperationException ignored){}
    return "unavailable";
  }
}
