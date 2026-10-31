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
      System.out.println("Bat vision real OpenGL: shader compilation, muted colors, cold tint, resize, state restoration and texture cleanup PASS");
    }finally{BatVisionRenderer.release();singleton.set(null,old);buffer.destroy();}
  }
}
