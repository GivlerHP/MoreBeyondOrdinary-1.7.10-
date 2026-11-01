package ru.givler.mbo.client.render;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.entity.fauna.EntityMBOCamel;

/** Skin-only apparition inside the thaumometer lens; never draws player labels or gear. */
public final class PossessionGhostRenderer extends RenderPlayer {
  private boolean seated;

  public PossessionGhostRenderer() {
    modelBipedMain=new ModelBiped() {
      @Override public void setRotationAngles(float phase,float amount,float age,float yaw,float pitch,float scale,Entity entity) {
        super.setRotationAngles(0,0,age,yaw,pitch,scale,entity);
        bipedRightArm.rotateAngleZ=.35F;
        bipedLeftArm.rotateAngleZ=-.35F;
        if(seated) {
          bipedRightLeg.rotateAngleX=bipedLeftLeg.rotateAngleX=-1.2F;
          bipedRightLeg.rotateAngleY=.25F;bipedLeftLeg.rotateAngleY=-.25F;
        }
      }
    };
    mainModel=modelBipedMain;
  }

  /** Render-space placement uses one interpolated pose; the proxy itself never ticks. */
  public static void align(EntityOtherPlayerMP ghost,EntityLivingBase animal,float partial) {
    float yaw=animal.prevRenderYawOffset+MathHelper.wrapAngleTo180_float(animal.renderYawOffset-animal.prevRenderYawOffset)*partial;
    float head=animal.prevRotationYawHead+MathHelper.wrapAngleTo180_float(animal.rotationYawHead-animal.prevRotationYawHead)*partial;
    float pitch=animal.prevRotationPitch+(animal.rotationPitch-animal.prevRotationPitch)*partial;
    double x=animal.lastTickPosX+(animal.posX-animal.lastTickPosX)*partial;
    double y=animal.lastTickPosY+(animal.posY-animal.lastTickPosY)*partial;
    double z=animal.lastTickPosZ+(animal.posZ-animal.lastTickPosZ)*partial;
    ghost.yOffset=0;
    if(animal instanceof EntityMBOCamel) {
      // Reuse the actual front seat height, including sitting/standing transitions.
      ((EntityMBOCamel)animal).positionPassenger(ghost,true);
      y+=ghost.posY-animal.posY;
      x-=MathHelper.sin(yaw*(float)Math.PI/180)*.5F;
      z+=MathHelper.cos(yaw*(float)Math.PI/180)*.5F;
    } else if(animal instanceof EntityHorse) {
      y+=animal.getMountedYOffset()+ghost.getYOffset();
      x+=MathHelper.sin(yaw*(float)Math.PI/180)*.18F;
      z-=MathHelper.cos(yaw*(float)Math.PI/180)*.18F;
    } else y+=Math.max(0,animal.height*.75F-.8F);
    ghost.setPosition(x,y,z);
    ghost.prevPosX=ghost.lastTickPosX=x;ghost.prevPosY=ghost.lastTickPosY=y;ghost.prevPosZ=ghost.lastTickPosZ=z;
    ghost.rotationYaw=ghost.prevRotationYaw=yaw;
    ghost.renderYawOffset=ghost.prevRenderYawOffset=yaw;
    ghost.rotationYawHead=ghost.prevRotationYawHead=head;
    ghost.rotationPitch=ghost.prevRotationPitch=pitch;
    ghost.limbSwing=ghost.limbSwingAmount=ghost.prevLimbSwingAmount=0;
    ghost.ticksExisted=animal.ticksExisted;
    ghost.yOffset=0;
  }

  public void renderGhost(EntityOtherPlayerMP ghost,EntityLivingBase animal,float partial) {
    align(ghost,animal,partial);seated=animal instanceof EntityHorse;
    setRenderManager(RenderManager.instance);
    GL11.glPushMatrix();
    try {
      GL11.glTranslated(ghost.posX-RenderManager.renderPosX,ghost.posY-RenderManager.renderPosY,ghost.posZ-RenderManager.renderPosZ);
      doRender(ghost,0,0,0,ghost.rotationYaw,partial);
    } finally {GL11.glPopMatrix();}
  }

  @Override protected void passSpecialRender(EntityLivingBase entity,double x,double y,double z) {}
  @Override protected void renderEquippedItems(AbstractClientPlayer player,float partial) {}
  @Override protected int shouldRenderPass(AbstractClientPlayer player,int pass,float partial) {return -1;}
}
