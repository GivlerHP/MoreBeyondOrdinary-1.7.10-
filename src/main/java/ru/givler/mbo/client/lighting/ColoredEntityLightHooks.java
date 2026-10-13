package ru.givler.mbo.client.lighting;

import java.nio.FloatBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import ru.givler.mbo.config.ColoredLightConfig;

/** A scoped fixed-pipeline lighting multiplier; model colors and the original lightmap stay intact. */
public final class ColoredEntityLightHooks {
  private static int texture;
  private static final ThreadLocal<State> STATE=new ThreadLocal<State>() { protected State initialValue(){return new State();} };
  private ColoredEntityLightHooks() {}
  public static int begin(Entity entity,float partial) {
    if(!ColoredLightConfig.enabled || ColoredLightShaderBridge.active || entity==null || entity.worldObj==null
        || entity.worldObj!=Minecraft.getMinecraft().theWorld || entity instanceof ru.givler.mbo.movingplatform.EntityMovingPlatform)return 0;
    double x=entity.lastTickPosX+(entity.posX-entity.lastTickPosX)*partial;
    double y=entity.lastTickPosY+(entity.posY-entity.lastTickPosY)*partial-entity.yOffset;
    double z=entity.lastTickPosZ+(entity.posZ-entity.lastTickPosZ)*partial;
    y+=entity.boundingBox==null ? entity.height*.66 : (entity.boundingBox.maxY-entity.boundingBox.minY)*.66;
    int rgb=ColoredLightRenderHooks.worldRGB(x,y,z);
    if(rgb==0)return 0;
    if(GLContext.getCapabilities().OpenGL20 && GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM)!=0)return 0;
    int brightness=(int)OpenGlHelper.lastBrightnessX | (int)OpenGlHelper.lastBrightnessY<<16;
    int tint=ColoredLightRenderHooks.worldTint(-1,brightness,rgb);
    int lit=ColoredLightRenderHooks.worldBrightness(brightness,rgb);
    if(tint==-1 && lit==brightness)return 0;
    int active=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    GL13.glActiveTexture(GL13.GL_TEXTURE1);
    boolean lightmap=GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
    GL13.glActiveTexture(GL13.GL_TEXTURE2);
    State state=STATE.get();
    boolean occupied=GL11.glIsEnabled(GL11.GL_TEXTURE_2D) && state.depth==0;
    GL13.glActiveTexture(active);
    if(!lightmap || occupied)return 0; // GUI and renderers owning the extra texture stage remain untouched.
    if(state.depth==state.frames.length)state.frames=java.util.Arrays.copyOf(state.frames,state.depth*2);
    Frame frame=state.frames[state.depth]; if(frame==null)state.frames[state.depth]=frame=new Frame();
    frame.x=OpenGlHelper.lastBrightnessX; frame.y=OpenGlHelper.lastBrightnessY;
    GL11.glPushAttrib(GL11.GL_TEXTURE_BIT|GL11.GL_CURRENT_BIT);
    try {
      GL13.glActiveTexture(GL13.GL_TEXTURE2);
      if(texture==0) {
        texture=GL11.glGenTextures(); GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
        java.nio.ByteBuffer white=BufferUtils.createByteBuffer(4); white.putInt(-1).flip();
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,1,1,0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,white);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_NEAREST);
      } else GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
      GL11.glEnable(GL11.GL_TEXTURE_2D);
      GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL11.GL_TEXTURE_ENV_MODE,GL13.GL_COMBINE);
      GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL13.GL_COMBINE_RGB,GL11.GL_MODULATE);
      GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL13.GL_SOURCE0_RGB,GL13.GL_PREVIOUS);
      GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL13.GL_SOURCE1_RGB,GL13.GL_CONSTANT);
      GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL13.GL_OPERAND0_RGB,GL11.GL_SRC_COLOR);
      GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL13.GL_OPERAND1_RGB,GL11.GL_SRC_COLOR);
      GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL13.GL_RGB_SCALE,1);
      GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL13.GL_COMBINE_ALPHA,GL11.GL_REPLACE);
      GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL13.GL_SOURCE0_ALPHA,GL13.GL_PREVIOUS);
      GL11.glTexEnvi(GL11.GL_TEXTURE_ENV,GL13.GL_OPERAND0_ALPHA,GL11.GL_SRC_ALPHA);
      GL11.glTexEnvf(GL11.GL_TEXTURE_ENV,GL11.GL_ALPHA_SCALE,1F);
      boolean little=java.nio.ByteOrder.nativeOrder()==java.nio.ByteOrder.LITTLE_ENDIAN;
      frame.color.clear(); frame.color.put((tint>>>(little?0:24)&255)/255F).put((tint>>>(little?8:16)&255)/255F).put((tint>>>(little?16:8)&255)/255F).put(1F).flip();
      GL11.glTexEnv(GL11.GL_TEXTURE_ENV,GL11.GL_TEXTURE_ENV_COLOR,frame.color);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,lit&65535,lit>>>16);
      GL13.glActiveTexture(active);
      return ++state.depth;
    } catch(RuntimeException error) { GL11.glPopAttrib(); OpenGlHelper.lastBrightnessX=frame.x; OpenGlHelper.lastBrightnessY=frame.y; throw error; }
  }
  public static void end(int token) {
    if(token==0)return;
    State state=STATE.get(); Frame frame=state.frames[--state.depth];
    GL11.glPopAttrib(); OpenGlHelper.lastBrightnessX=frame.x; OpenGlHelper.lastBrightnessY=frame.y;
  }
  public static void release() { if(texture!=0){GL11.glDeleteTextures(texture);texture=0;} STATE.remove(); }
  private static final class Frame { float x,y; final FloatBuffer color=BufferUtils.createFloatBuffer(4); }
  private static final class State { int depth; Frame[] frames=new Frame[4]; }
}
