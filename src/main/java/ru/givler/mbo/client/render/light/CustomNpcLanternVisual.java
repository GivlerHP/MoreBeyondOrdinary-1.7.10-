package ru.givler.mbo.client.render.light;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.model.blocks.lantern.LanternFloor;
import org.lwjgl.opengl.GL11;

/** Loaded only when CustomNPCs is available. */
final class CustomNpcLanternVisual {
  private static LanternFloor lantern;
  static float gripHeight() {
    if(lantern==null)lantern=new LanternFloor();
    float top=Float.POSITIVE_INFINITY;
    for(Object cube:lantern.Lantern.cubeList)top=Math.min(top,((net.minecraft.client.model.ModelBox)cube).posY1);
    return 1.5F-(lantern.Lantern.rotationPointY+top)/16F;
  }
  static float gripX() {
    if(lantern==null)lantern=new LanternFloor();
    float top=Float.POSITIVE_INFINITY,x=0;
    for(Object cube:lantern.Lantern.cubeList) {
      net.minecraft.client.model.ModelBox box=(net.minecraft.client.model.ModelBox)cube;
      if(box.posY1<top){top=box.posY1;x=box.posX1;}
      else if(box.posY1==top)x=Math.min(x,box.posX1);
    }
    return (lantern.Lantern.rotationPointX+x)/16F;
  }
  static void render(boolean lit) {
    if(lantern==null)lantern=new LanternFloor();
    Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("customnpcs","textures/models/lantern.png"));
    GL11.glPushMatrix();
    try {
      GL11.glTranslatef(0,1.5F,0);GL11.glScalef(1,-1,-1);
      lantern.Lantern.render(.0625F);
      float x=OpenGlHelper.lastBrightnessX,y=OpenGlHelper.lastBrightnessY;
      try {
        if(lit)OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,240,240);
        lantern.Light.render(.0625F);
      } finally { GL11.glColor4f(1,1,1,1);OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,x,y); }
    } finally { GL11.glPopMatrix(); }
  }
}
