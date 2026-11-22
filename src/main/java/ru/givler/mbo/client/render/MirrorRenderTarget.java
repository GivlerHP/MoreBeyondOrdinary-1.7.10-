package ru.givler.mbo.client.render;

import java.nio.ByteBuffer;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/** Independent of Minecraft's fboEnable setting, which shader renderers may override. */
public final class MirrorRenderTarget {
    public final int framebuffer, texture, depth;
    public final int width,height;
    public MirrorRenderTarget() { this(96,224); }
    public MirrorRenderTarget(int width,int height) {
        if(width<1 || height<1)throw new IllegalArgumentException("Mirror target dimensions must be positive");
        this.width=width;this.height=height;
        int previous=GL11.glGetInteger(36006),renderbuffer=GL11.glGetInteger(36007);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        framebuffer=OpenGlHelper.func_153165_e();
        depth=OpenGlHelper.func_153185_f();
        texture=GL11.glGenTextures();
        try {
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA8,width,height,0,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,(ByteBuffer)null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);
            OpenGlHelper.func_153171_g(36160,framebuffer);
            OpenGlHelper.func_153188_a(36160,36064,GL11.GL_TEXTURE_2D,texture,0);
            OpenGlHelper.func_153176_h(36161,depth);
            OpenGlHelper.func_153186_a(36161,33190,width,height);
            OpenGlHelper.func_153190_b(36160,36096,36161,depth);
            GL11.glDrawBuffer(36064);GL11.glReadBuffer(36064);
            if(OpenGlHelper.func_153167_i(36160)!=36053)throw new IllegalStateException("Incomplete mirror framebuffer");
        } catch(RuntimeException failure) { delete();throw failure;
        } finally {
            OpenGlHelper.func_153171_g(36160,previous);
            OpenGlHelper.func_153176_h(36161,renderbuffer);
            GL11.glPopAttrib();
        }
    }
    public void begin() {
        OpenGlHelper.func_153171_g(36160,framebuffer);
        GL11.glDrawBuffer(36064);GL11.glReadBuffer(36064);
        GL11.glViewport(0,0,width,height);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glColorMask(true,true,true,true);GL11.glDepthMask(true);
        GL11.glClearColor(.34F,.38F,.41F,1);GL11.glClearDepth(1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
    }
    public void bindTexture() { OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture); }
    public void delete() {
        GL11.glDeleteTextures(texture);
        OpenGlHelper.func_153184_g(depth);
        OpenGlHelper.func_153174_h(framebuffer);
    }
}
