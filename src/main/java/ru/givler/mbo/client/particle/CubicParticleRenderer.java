package ru.givler.mbo.client.particle;

import net.minecraft.block.Block;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import ru.givler.mbo.config.ParticleConfig;

/** A fixed six-quad mesh submitted into the existing particle batch. */
public final class CubicParticleRenderer {
  private static final int[][] FACES={
      {4,6,7,5},{1,3,2,0},{3,7,6,2},{0,4,5,1},{5,7,3,1},{0,2,6,4}};
  private static final float[] SHADE={.6F,.6F,1F,.5F,.8F,.8F};
  private CubicParticleRenderer() {}
  public static boolean render(EntityFX particle,Tessellator tess,float partial) {
    if(!ParticleConfig.cubicParticles || !(particle instanceof EntityDiggingFX)
        || !(particle instanceof ParticleGeometry)) return false;
    ParticleGeometry geometry=(ParticleGeometry)particle;
    Block block=geometry.mbo$particleBlock();
    if(block==null || !block.isNormalCube()) return false;
    float jitterX=geometry.mbo$particleJitterX(),jitterY=geometry.mbo$particleJitterY();
    float u0=(geometry.mbo$particleTextureX()+jitterX/4F)/16F,u1=u0+.015609375F;
    float v0=(geometry.mbo$particleTextureY()+jitterY/4F)/16F,v1=v0+.015609375F;
    IIcon icon=geometry.mbo$particleIcon();
    if(icon!=null) {
      u0=icon.getInterpolatedU(jitterX*4F);u1=icon.getInterpolatedU((jitterX+1F)*4F);
      v0=icon.getInterpolatedV(jitterY*4F);v1=icon.getInterpolatedV((jitterY+1F)*4F);
    }
    double x=particle.prevPosX+(particle.posX-particle.prevPosX)*partial-EntityFX.interpPosX;
    double y=particle.prevPosY+(particle.posY-particle.prevPosY)*partial-EntityFX.interpPosY;
    double z=particle.prevPosZ+(particle.posZ-particle.prevPosZ)*partial-EntityFX.interpPosZ;
    float scale=.1F*geometry.mbo$particleScale();
    // The flat debris starts at 60% brightness. Cube faces supply their own shading.
    float red=Math.min(1,particle.getRedColorF()*4F/3F);
    float green=Math.min(1,particle.getGreenColorF()*4F/3F);
    float blue=Math.min(1,particle.getBlueColorF()*4F/3F);
    for(int face=0;face<6;face++) {
      float shade=SHADE[face];
      tess.setColorRGBA_F(red*shade,green*shade,blue*shade,geometry.mbo$particleAlpha());
      for(int corner=0;corner<4;corner++) {
        int vertex=FACES[face][corner];
        tess.addVertexWithUV(x+((vertex&4)==0?-scale:scale),
            y+((vertex&2)==0?-scale:scale),z+((vertex&1)==0?-scale:scale),
            corner<2?u0:u1,corner==0 || corner==3?v0:v1);
      }
    }
    return true;
  }
}
