package ru.givler.mbo.client.render.decormodels;

import net.minecraft.client.renderer.Tessellator;

/** Records model-space attributes without invoking shader or terrain Tessellator internals. */
final class DecorGeometryRecorder extends Tessellator {
  private int[] vertices=new int[1024];
  private int length,color=-1,normal;
  @Override public void setColorRGBA_F(float r,float g,float b,float a) {
    setColorRGBA((int)(r*255),(int)(g*255),(int)(b*255),(int)(a*255));
  }
  @Override public void setColorRGBA(int r,int g,int b,int a) {
    r=Math.max(0,Math.min(255,r));g=Math.max(0,Math.min(255,g));b=Math.max(0,Math.min(255,b));a=Math.max(0,Math.min(255,a));
    color=java.nio.ByteOrder.nativeOrder()==java.nio.ByteOrder.LITTLE_ENDIAN?r|g<<8|b<<16|a<<24:a|b<<8|g<<16|r<<24;
  }
  @Override public void setNormal(float x,float y,float z) {
    normal=((byte)(x*127)&255)|((byte)(y*127)&255)<<8|((byte)(z*127)&255)<<16;
  }
  @Override public void addVertexWithUV(double x,double y,double z,double u,double v) {
    if(length+8>vertices.length)vertices=java.util.Arrays.copyOf(vertices,vertices.length*2);
    vertices[length++]=Float.floatToRawIntBits((float)x);vertices[length++]=Float.floatToRawIntBits((float)y);
    vertices[length++]=Float.floatToRawIntBits((float)z);vertices[length++]=Float.floatToRawIntBits((float)u);
    vertices[length++]=Float.floatToRawIntBits((float)v);vertices[length++]=color;vertices[length++]=normal;vertices[length++]=0;
  }
  int[] finish(){return java.util.Arrays.copyOf(vertices,length);}
}
