package ru.givler.mbo.client.render.light;

import java.nio.FloatBuffer;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

/** Native 1.7.10 particles held in model coordinates, following LastDawn's carry particle lifecycle. */
final class PortableCarryParticles {
  private static final ResourceLocation TEXTURE=new ResourceLocation("textures/particle/particles.png");
  private static final Map<Long,Hand> HANDS=new HashMap<>();
  private static World world;
  private static long lastTick=Long.MIN_VALUE;
  private static final class Hand {
    final List<EntityFX> particles=new ArrayList<>();
    Object item; boolean lit; long emitted=Long.MIN_VALUE,lastSeen;
  }
  static void tick() {
    World current=Minecraft.getMinecraft().theWorld;
    if(world!=current){HANDS.clear();world=current;lastTick=Long.MIN_VALUE;}
    if(world==null)return;
    long now=world.getTotalWorldTime();
    if(lastTick==now)return;
    int elapsed=lastTick==Long.MIN_VALUE?0:(int)Math.min(20,Math.max(0,now-lastTick));lastTick=now;
    Iterator<Hand> hands=HANDS.values().iterator();
    while(hands.hasNext()) {
      Hand hand=hands.next();if(now-hand.lastSeen>40){hands.remove();continue;}
      Iterator<EntityFX> particles=hand.particles.iterator();
      while(particles.hasNext()){EntityFX particle=particles.next();for(int step=0;step<elapsed && !particle.isDead;step++)particle.onUpdate();if(particle.isDead)particles.remove();}
    }
  }
  static void draw(ItemStack stack,EntityPlayer player,int side,float partial) {
    if(stack==null || player==null || player.worldObj!=Minecraft.getMinecraft().theWorld)return;
    if(world!=player.worldObj){HANDS.clear();world=player.worldObj;}
    boolean lit=PortableLights.isLit(stack);
    long key=((long)player.getEntityId()<<1)|(side<0?1:0),now=world.getTotalWorldTime();
    if(!lit){HANDS.remove(key);return;}
    Hand hand=HANDS.get(key);if(hand==null){hand=new Hand();HANDS.put(key,hand);}
    if(hand.item!=stack.getItem() || hand.lit!=lit){hand.particles.clear();hand.item=stack.getItem();hand.lit=lit;hand.emitted=Long.MIN_VALUE;}
    hand.lastSeen=now;
    if(hand.emitted!=now && (now%3==0 || hand.particles.isEmpty())) {
      if(PortableLights.isRedTorch(stack))add(hand,new EntityReddustFX(world,0,.7,0,1,0,0));
      else {add(hand,new EntityFlameFX(world,0,.7,0,0,0,0));add(hand,new EntitySmokeFX(world,0,.7,0,0,0,0));}
      hand.emitted=now;
    }
    if(hand.particles.isEmpty())return;
    FloatBuffer buffer=BufferUtils.createFloatBuffer(16);GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX,buffer);
    float[] matrix=new float[16];buffer.get(matrix);
    double scale=Math.sqrt(matrix[0]*matrix[0]+matrix[1]*matrix[1]+matrix[2]*matrix[2]);
    if(scale<.00001)return;
    double ix=EntityFX.interpPosX,iy=EntityFX.interpPosY,iz=EntityFX.interpPosZ;
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
    try {
      GL11.glLoadIdentity();GL11.glScaled(scale,scale,scale);
      OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
      Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
      GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glDisable(GL11.GL_LIGHTING);GL11.glDisable(GL11.GL_CULL_FACE);
      GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glDepthMask(false);GL11.glColor4f(1,1,1,1);
      EntityFX.interpPosX=EntityFX.interpPosY=EntityFX.interpPosZ=0;
      for(EntityFX particle:hand.particles) {
        double x=particle.posX,y=particle.posY,z=particle.posZ,px=particle.prevPosX,py=particle.prevPosY,pz=particle.prevPosZ;
        double lx=px+(x-px)*partial,ly=py+(y-py)*partial,lz=pz+(z-pz)*partial;
        try {
          particle.posX=particle.prevPosX=(matrix[0]*lx+matrix[4]*ly+matrix[8]*lz+matrix[12])/scale;
          particle.posY=particle.prevPosY=(matrix[1]*lx+matrix[5]*ly+matrix[9]*lz+matrix[13])/scale;
          particle.posZ=particle.prevPosZ=(matrix[2]*lx+matrix[6]*ly+matrix[10]*lz+matrix[14])/scale;
          Tessellator t=Tessellator.instance;t.startDrawingQuads();
          t.setBrightness(particle instanceof EntitySmokeFX?player.getBrightnessForRender(partial):0xF000F0);
          particle.renderParticle(t,partial,1,1,0,0,0);t.draw();
        } finally {particle.posX=x;particle.posY=y;particle.posZ=z;particle.prevPosX=px;particle.prevPosY=py;particle.prevPosZ=pz;}
      }
    } finally {EntityFX.interpPosX=ix;EntityFX.interpPosY=iy;EntityFX.interpPosZ=iz;GL11.glPopMatrix();GL11.glPopAttrib();}
  }
  private static void add(Hand hand,EntityFX particle) {
    particle.noClip=true;
    // New particles have no previous model-space position until their first tick.
    // Start interpolation at the emitter instead of the bottom of the torch.
    particle.prevPosX=particle.posX;
    particle.prevPosY=particle.posY;
    particle.prevPosZ=particle.posZ;
    hand.particles.add(particle);
  }
}
