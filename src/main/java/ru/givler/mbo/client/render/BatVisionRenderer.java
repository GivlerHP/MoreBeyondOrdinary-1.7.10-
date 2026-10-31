package ru.givler.mbo.client.render;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.*;

/** One screen copy and one color pass; night vision supplies the underlying dark visibility. */
public final class BatVisionRenderer {
  private static int program,texture,width,height,scene;
  private static boolean unavailable;
  private BatVisionRenderer(){}
  private static int shader(int type,String source) {
    int shader=GL20.glCreateShader(type);GL20.glShaderSource(shader,source);GL20.glCompileShader(shader);
    if(GL20.glGetShaderi(shader,GL20.GL_COMPILE_STATUS)==0){String error=GL20.glGetShaderInfoLog(shader,4096);GL20.glDeleteShader(shader);throw new IllegalStateException(error);}
    return shader;
  }
  private static boolean initialize() {
    if(program!=0)return true;if(unavailable)return false;
    if(!GLContext.getCapabilities().OpenGL20){unavailable=true;return false;}
    int vertex=0,fragment=0;
    try {
      vertex=shader(GL20.GL_VERTEX_SHADER,"#version 120\nvarying vec2 uv;void main(){gl_Position=ftransform();uv=gl_MultiTexCoord0.xy;}");
      fragment=shader(GL20.GL_FRAGMENT_SHADER,"#version 120\nuniform sampler2D scene;varying vec2 uv;void main(){vec4 c=texture2D(scene,uv);float l=dot(c.rgb,vec3(.2126,.7152,.0722));vec3 muted=mix(vec3(l),c.rgb,.14);gl_FragColor=vec4(muted*vec3(.90,.97,1.04),c.a);}");
      program=GL20.glCreateProgram();GL20.glAttachShader(program,vertex);GL20.glAttachShader(program,fragment);GL20.glLinkProgram(program);
      if(GL20.glGetProgrami(program,GL20.GL_LINK_STATUS)==0)throw new IllegalStateException(GL20.glGetProgramInfoLog(program,4096));
      scene=GL20.glGetUniformLocation(program,"scene");
      return true;
    }catch(RuntimeException error){if(program!=0)GL20.glDeleteProgram(program);program=0;unavailable=true;System.err.println("[MBO] Bat vision color pass unavailable: "+error);return false;}
    finally{if(vertex!=0)GL20.glDeleteShader(vertex);if(fragment!=0)GL20.glDeleteShader(fragment);}
  }
  public static void render() {
    Minecraft mc=Minecraft.getMinecraft();if(!initialize()||mc.displayWidth<=0||mc.displayHeight<=0)return;
    int oldProgram=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM),oldTextureUnit=GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE),oldMatrix=GL11.glGetInteger(GL11.GL_MATRIX_MODE);
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    GL13.glActiveTexture(GL13.GL_TEXTURE0);
    GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPushMatrix();GL11.glLoadIdentity();GL11.glOrtho(0,1,0,1,-1,1);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPushMatrix();GL11.glLoadIdentity();
    try {
      if(texture==0)texture=GL11.glGenTextures();GL11.glBindTexture(GL11.GL_TEXTURE_2D,texture);
      if(width!=mc.displayWidth||height!=mc.displayHeight) {
        width=mc.displayWidth;height=mc.displayHeight;
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_S,GL12.GL_CLAMP_TO_EDGE);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_WRAP_T,GL12.GL_CLAMP_TO_EDGE);
        GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D,0,GL11.GL_RGBA,0,0,width,height,0);
      } else GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D,0,0,0,0,0,width,height);
      GL11.glViewport(0,0,width,height);GL11.glDisable(GL11.GL_DEPTH_TEST);GL11.glDisable(GL11.GL_BLEND);GL11.glDisable(GL11.GL_ALPHA_TEST);GL11.glDisable(GL11.GL_CULL_FACE);GL11.glDepthMask(false);
      GL20.glUseProgram(program);GL20.glUniform1i(scene,0);
      GL11.glBegin(GL11.GL_QUADS);
      GL11.glTexCoord2f(0,0);GL11.glVertex2f(0,0);GL11.glTexCoord2f(1,0);GL11.glVertex2f(1,0);
      GL11.glTexCoord2f(1,1);GL11.glVertex2f(1,1);GL11.glTexCoord2f(0,1);GL11.glVertex2f(0,1);GL11.glEnd();
    }finally {
      GL20.glUseProgram(oldProgram);GL11.glMatrixMode(GL11.GL_MODELVIEW);GL11.glPopMatrix();GL11.glMatrixMode(GL11.GL_PROJECTION);GL11.glPopMatrix();GL11.glMatrixMode(oldMatrix);
      GL13.glActiveTexture(oldTextureUnit);GL11.glPopAttrib();
    }
  }
  public static void release(){if(texture!=0){GL11.glDeleteTextures(texture);texture=0;}width=height=0;}
}
