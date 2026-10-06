package ru.givler.mbo.integration.thaumcraft.client.render;

import java.nio.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

/** Projects the actual scanner model's inner hexagon, including hand/equip transforms. */
public final class ThaumometerLens {
  private static final FloatBuffer MODEL = BufferUtils.createFloatBuffer(16);
  private static final FloatBuffer PROJECTION = BufferUtils.createFloatBuffer(16);
  private static final FloatBuffer POINT = BufferUtils.createFloatBuffer(3);
  private static final IntBuffer VIEW = BufferUtils.createIntBuffer(16);
  private static final float[][] OUTLINE = {{0,-1},{.866F,-.5F},{.866F,.5F},{0,1},{-.866F,.5F},{-.866F,-.5F}};
  private static final float[][] screen = new float[6][2];
  private static long captured;
  private static int width, height;
  private static int stencilBuffer = -1, stencilWidth, stencilHeight;
  private ThaumometerLens() {}

  public static void prepare() {
    Minecraft mc = Minecraft.getMinecraft();
    if (!OpenGlHelper.isFramebufferEnabled()) return;
    Framebuffer f = mc.getFramebuffer();
    if (f.depthBuffer < 0 || (f.depthBuffer == stencilBuffer
        && f.framebufferWidth == stencilWidth && f.framebufferHeight == stencilHeight)) return;
    int previousFramebuffer = GL11.glGetInteger(36006);
    int previousRenderbuffer = GL11.glGetInteger(36007);
    f.bindFramebuffer(false);
    OpenGlHelper.func_153176_h(36161, f.depthBuffer); // GL_RENDERBUFFER
    OpenGlHelper.func_153186_a(36161, 35056, f.framebufferWidth, f.framebufferHeight); // DEPTH24_STENCIL8
    OpenGlHelper.func_153190_b(36160, 36128, 36161, f.depthBuffer); // GL_STENCIL_ATTACHMENT
    OpenGlHelper.func_153176_h(36161, previousRenderbuffer);
    f.checkFramebufferComplete();
    OpenGlHelper.func_153171_g(36160, previousFramebuffer);
    stencilBuffer = f.depthBuffer; stencilWidth = f.framebufferWidth; stencilHeight = f.framebufferHeight;
  }

  public static void capture(IItemRenderer.ItemRenderType type) {
    Minecraft mc = Minecraft.getMinecraft();
    if (type != IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON || mc.gameSettings.thirdPersonView != 0) return;
    GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, MODEL);
    GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, PROJECTION);
    GL11.glGetInteger(GL11.GL_VIEWPORT, VIEW);
    for (int i=0; i<6; i++) {
      if (!GLU.gluProject(OUTLINE[i][0], .11F, OUTLINE[i][1], MODEL, PROJECTION, VIEW, POINT)) return;
      screen[i][0]=POINT.get(0); screen[i][1]=POINT.get(1);
    }
    width=mc.displayWidth; height=mc.displayHeight; captured=System.nanoTime();
  }

  public static boolean begin() {
    Minecraft mc=Minecraft.getMinecraft();
    if (mc.gameSettings.thirdPersonView != 0 || captured == 0 || System.nanoTime()-captured > 250000000L
        || width != mc.displayWidth || height != mc.displayHeight) return false;
    if (!OpenGlHelper.isFramebufferEnabled() || mc.getFramebuffer().depthBuffer != stencilBuffer
        || GL11.glGetInteger(36006) != mc.getFramebuffer().framebufferObject) return false;
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    int mode=GL11.glGetInteger(GL11.GL_MATRIX_MODE);
    GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPushMatrix(); GL11.glLoadIdentity();
    GL11.glOrtho(0,width,0,height,-1,1);
    GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glPushMatrix(); GL11.glLoadIdentity();
    GL11.glDisable(GL11.GL_DEPTH_TEST); GL11.glDisable(GL11.GL_CULL_FACE);
    GL11.glDisable(GL11.GL_ALPHA_TEST); GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glDisable(GL11.GL_SCISSOR_TEST);
    GL11.glColorMask(false,false,false,false); GL11.glDepthMask(false);
    GL11.glEnable(GL11.GL_STENCIL_TEST); GL11.glStencilMask(1);
    GL11.glClearStencil(0); GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
    GL11.glStencilFunc(GL11.GL_ALWAYS,1,1);
    GL11.glStencilOp(GL11.GL_KEEP,GL11.GL_KEEP,GL11.GL_REPLACE);
    GL11.glBegin(GL11.GL_TRIANGLE_FAN);
    for(float[] point:screen) GL11.glVertex2f(point[0],point[1]);
    GL11.glEnd();
    GL11.glPopMatrix(); GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glPopMatrix();
    GL11.glMatrixMode(mode); GL11.glPopAttrib();
    GL11.glEnable(GL11.GL_STENCIL_TEST); GL11.glStencilMask(0);
    GL11.glStencilOp(GL11.GL_KEEP,GL11.GL_KEEP,GL11.GL_KEEP);
    return true;
  }

  public static void select(boolean inside) {
    GL11.glStencilFunc(GL11.GL_EQUAL,inside?1:0,1);
  }
}
