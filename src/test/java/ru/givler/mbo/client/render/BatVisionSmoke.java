package ru.givler.mbo.client.render;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

/** Compile and draw the actual color pass without opening a game window. */
public final class BatVisionSmoke {
  static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
  static int[] pixel(){ByteBuffer b=BufferUtils.createByteBuffer(4);GL11.glReadPixels(8,8,1,1,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,b);return new int[]{b.get(0)&255,b.get(1)&255,b.get(2)&255};}
  public static void main(String[] args)throws Exception {
    Pbuffer buffer=new Pbuffer(32,24,new PixelFormat(8,24,0),null,null);buffer.makeCurrent();
    Field singleton=Minecraft.class.getDeclaredField("theMinecraft");singleton.setAccessible(true);Object old=singleton.get(null);
    Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);
    Minecraft mc=(Minecraft)((sun.misc.Unsafe)uf.get(null)).allocateInstance(Minecraft.class);mc.displayWidth=32;mc.displayHeight=24;singleton.set(null,mc);
    try {
      GL11.glViewport(0,0,32,24);GL11.glClearColor(1,0,0,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
      GL13.glActiveTexture(GL13.GL_TEXTURE1);GL11.glEnable(GL11.GL_BLEND);GL11.glMatrixMode(GL11.GL_MODELVIEW);
      BatVisionRenderer.render();int[] red=pixel();
      require(red[0]>red[1]&&red[0]-red[1]<60&&red[2]>red[1],"Bat vision must mute, but retain, color: "+java.util.Arrays.toString(red));
      require(GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE)==GL13.GL_TEXTURE1&&GL11.glIsEnabled(GL11.GL_BLEND),"Color pass leaked GL state");
      require(GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)==0,"Color pass leaked shader program");
      GL11.glClearColor(1,1,1,1);GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);BatVisionRenderer.render();int[] white=pixel();
      require(white[2]>=white[1]&&white[1]>white[0]&&white[0]>210,"Cold near-monochrome tint missing");
      mc.displayWidth=16;mc.displayHeight=16;BatVisionRenderer.render();
      Field texture=BatVisionRenderer.class.getDeclaredField("texture");texture.setAccessible(true);int id=texture.getInt(null);
      require(GL11.glIsTexture(id),"Screen texture was not created");BatVisionRenderer.release();require(!GL11.glIsTexture(id),"Screen texture leaked after exit");
      require(GL11.glGetError()==GL11.GL_NO_ERROR,"OpenGL error in bat vision pass");
      Object control=ru.givler.mbo.client.handler.PossessionClient.INSTANCE;
      Class<?> type=control.getClass();Field active=type.getDeclaredField("active"),roll=type.getDeclaredField("batRoll"),previous=type.getDeclaredField("previousBatRoll");
      active.setAccessible(true);roll.setAccessible(true);previous.setAccessible(true);
      boolean oldActive=active.getBoolean(control);float oldRoll=roll.getFloat(control),oldPrevious=previous.getFloat(control);
      try {
        active.setBoolean(control,true);roll.setFloat(control,1);previous.setFloat(control,0);
        mc.renderViewEntity=new net.minecraft.entity.passive.EntityBat(null);
        require(ru.givler.mbo.client.handler.PossessionClient.cameraRoll(0)==0&&ru.givler.mbo.client.handler.PossessionClient.cameraRoll(.5F)==90&&ru.givler.mbo.client.handler.PossessionClient.cameraRoll(1)==180,"Camera flip did not interpolate smoothly");
        GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glLoadIdentity();ru.givler.mbo.client.handler.PossessionClient.rollCamera(1);
        java.nio.FloatBuffer matrix=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,matrix);
        require(Math.abs(matrix.get(0)+1)<1e-5&&Math.abs(matrix.get(5)+1)<1e-5&&Math.abs(matrix.get(10)-1)<1e-5,"Hanging camera was not upside down around view axis");
        roll.setFloat(control,0);previous.setFloat(control,1);
        require(ru.givler.mbo.client.handler.PossessionClient.cameraRoll(1)==0,"Detach failed to restore upright camera");
        mc.renderViewEntity=new net.minecraft.entity.passive.EntitySheep(null);
        require(ru.givler.mbo.client.handler.PossessionClient.cameraRoll(0)==0,"Bat camera roll leaked to another creature");
      } finally {active.setBoolean(control,oldActive);roll.setFloat(control,oldRoll);previous.setFloat(control,oldPrevious);}
      System.out.println("Bat ceiling camera: smooth flip, actual upside-down GL matrix, upright return and creature isolation PASS");
      System.out.println("Bat vision real OpenGL: shader compilation, muted colors, cold tint, resize, state restoration and texture cleanup PASS");
    }finally{BatVisionRenderer.release();singleton.set(null,old);buffer.destroy();}
  }
}
