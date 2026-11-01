package ru.givler.mbo.client.render;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import ru.givler.mbo.client.render.boat.RenderMBOBoat;
import ru.givler.mbo.entity.boat.EntityMBOBoat;

/** Actual boat mask geometry must reject water without erasing already drawn passengers. */
public final class BoatWaterMaskSmoke {
  private static void quad(float y,float min,float max,float r,float g,float b) {
    GL11.glColor3f(r,g,b);GL11.glBegin(GL11.GL_QUADS);
    GL11.glVertex3f(min,y,min);GL11.glVertex3f(max,y,min);
    GL11.glVertex3f(max,y,max);GL11.glVertex3f(min,y,max);GL11.glEnd();
  }
  private static int[] pixel(int x,int y) {
    ByteBuffer data=BufferUtils.createByteBuffer(4);GL11.glReadPixels(x,y,1,1,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,data);
    return new int[]{data.get(0)&255,data.get(1)&255,data.get(2)&255};
  }
  public static void main(String[] args)throws Exception {
    java.lang.reflect.Field home=cpw.mods.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");home.setAccessible(true);home.set(null,new java.io.File("build/boat-mask-smoke"));
    java.lang.reflect.Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");side.setAccessible(true);side.set(null,cpw.mods.fml.relauncher.Side.CLIENT);
    java.lang.reflect.Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);
    java.lang.reflect.Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);loader.set(null,unsafe.allocateInstance(cpw.mods.fml.common.Loader.class));
    java.lang.reflect.Field mods=cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");mods.setAccessible(true);mods.set(loader.get(null),new java.util.HashMap<String,cpw.mods.fml.common.ModContainer>());
    net.minecraft.init.Bootstrap.func_151354_b();
    Pbuffer context=new Pbuffer(64,64,new PixelFormat(8,24,0),null,null);context.makeCurrent();
    try {
      GL11.glViewport(0,0,64,64);GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glLoadIdentity();GL11.glOrtho(-1.5,1.5,-1.5,1.5,-4,4);
      GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();GL11.glRotatef(90,1,0,0);
      GL11.glDisable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_CULL_FACE);
      GL11.glEnable(GL11.GL_DEPTH_TEST);GL11.glDepthFunc(GL11.GL_LEQUAL);
      RenderMBOBoat renderer=new RenderMBOBoat();EntityMBOBoat boat=new EntityMBOBoat(null);
      Method queue=RenderMBOBoat.class.getDeclaredMethod("queueWaterMask",RenderMBOBoat.class,EntityMBOBoat.class,double.class,double.class,double.class,float.class,float.class);queue.setAccessible(true);
      ForgeHooksClient.setRenderPass(0);
      for(boolean oldOrder:new boolean[]{true,false}) {
        GL11.glClearColor(0,1,0,1);GL11.glClearDepth(1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        RenderMBOBoat.beginWaterMasks();queue.invoke(null,renderer,boat,0D,0D,0D,0F,0F);
        if(oldOrder)RenderMBOBoat.finishWaterMasks();
        quad(.4F,-.25F,.25F,1,0,0);
        if(!oldOrder)RenderMBOBoat.finishWaterMasks();
        quad(.55F,-1.5F,1.5F,0,0,1);
        int[] passenger=pixel(32,32),interior=pixel(32,44),outside=pixel(58,32);
        if(oldOrder) {
          if(passenger[0]>200)throw new AssertionError("Regression fixture failed to reproduce passenger clipping");
        } else if(passenger[0]<200||passenger[1]>20||interior[1]<200||interior[2]>20||outside[2]<200)
          throw new AssertionError("Boat mask lost passenger pixels or failed to hide interior water: "+java.util.Arrays.toString(passenger)+" / "+java.util.Arrays.toString(interior)+" / "+java.util.Arrays.toString(outside));
      }
      System.out.println("Boat water mask pixels: old clipping reproduced; passenger retained, interior dry, outside water visible PASS");
    } finally {context.destroy();}
  }
}
