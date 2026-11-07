package ru.givler.mbo.client.render.light;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.lang.reflect.Field;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;

/** LastDawn carry poses, adapted to 1.7.10's arm geometry and MF2's independent hand passes. */
public final class PortableCarry {
  private static boolean firstPersonArm;
  private static final Map<EntityPlayer,Motion> MOTION=new WeakHashMap<>();
  private static Object world;
  private static Field equipped,previousEquipped,mainItem;
  private static final class Motion {float yaw,previous,tilt;Motion(float yaw){this.yaw=yaw;}}
  static ItemStack offhand(EntityPlayer player) {return Loader.isModLoaded("minefantasy2")?MF2PortableCarryBridge.offhand(player):null;}
  static PortableLightRenderer renderer(ItemStack stack) {
    if(stack==null)return null;
    IItemRenderer r=MinecraftForgeClient.getItemRenderer(stack,IItemRenderer.ItemRenderType.EQUIPPED);
    return r instanceof PortableLightRenderer?(PortableLightRenderer)r:null;
  }
  private static boolean allowed(EntityPlayer player) {
    if(player==null || player.inventory==null || !player.isEntityAlive() || player.ridingEntity instanceof ru.givler.mbo.entity.EntityHangingAnchor)return false;
    ItemStack use=player.getItemInUse();
    return use==null || use.getItemUseAction()!=EnumAction.bow;
  }
  @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
    if(event.phase!=TickEvent.Phase.END)return;
    Minecraft mc=Minecraft.getMinecraft();
    PortableCarryParticles.tick();
    if(world!=mc.theWorld){MOTION.clear();world=mc.theWorld;}
    if(mc.theWorld==null)return;
    for(Object object:mc.theWorld.playerEntities) {
      EntityPlayer player=(EntityPlayer)object;Motion state=MOTION.get(player);
      if(state==null){state=new Motion(player.rotationYaw);MOTION.put(player,state);}
      float turn=MathHelper.wrapAngleTo180_float(player.rotationYaw-state.yaw);
      state.yaw=player.rotationYaw;state.previous=state.tilt;
      state.tilt+=(MathHelper.clamp_float(-turn*.7F,-10,10)-state.tilt)*.22F;
    }
  }
  static float sway(EntityPlayer player,float partial) {
    Motion state=MOTION.get(player);
    float turn=state==null?0:state.previous+(state.tilt-state.previous)*partial;
    return turn+MathHelper.sin(player.limbSwing*.8F)*Math.min(1,player.limbSwingAmount)*5;
  }
  public static void pose(ModelBiped model,Entity entity) {
    if(firstPersonArm || !(entity instanceof EntityPlayer) || !allowed((EntityPlayer)entity))return;
    EntityPlayer player=(EntityPlayer)entity;
    poseArm(model.bipedRightArm,renderer(player.getCurrentEquippedItem()),1,player,model.isSneak,player.swingProgress);
    poseArm(model.bipedLeftArm,renderer(offhand(player)),-1,player,model.isSneak,
        Loader.isModLoaded("minefantasy2")?MF2PortableCarryBridge.swing(player):0);
  }
  private static void poseArm(ModelRenderer arm,PortableLightRenderer renderer,int side,EntityPlayer player,boolean crouch,float attack) {
    if(renderer==null)return;
    float walk=MathHelper.sin(player.limbSwing*.8F)*Math.min(1,player.limbSwingAmount);
    float weight=1-MathHelper.sin(MathHelper.clamp_float(attack,0,1)*(float)Math.PI);
    arm.rotateAngleX+=((renderer.isTorch()?-1.40F:-1.48F)-(crouch?.15F:0)+side*walk*.1F-arm.rotateAngleX)*weight;
    arm.rotateAngleY+=(-side*.1F+walk*.015F-arm.rotateAngleY)*weight;
    arm.rotateAngleZ+=(side*.1F+walk*.025F-arm.rotateAngleZ)*weight;
  }
  @SubscribeEvent public void beforeEquipped(RenderPlayerEvent.Specials.Pre event) {
    if(allowed(event.entityPlayer) && renderer(event.entityPlayer.getCurrentEquippedItem())!=null)event.renderItem=false;
  }
  @SubscribeEvent public void afterEquipped(RenderPlayerEvent.Specials.Post event) {
    if(allowed(event.entityPlayer))thirdPerson(event.entityPlayer,event.renderer.modelBipedMain,event.partialRenderTick,event.entityPlayer.getCurrentEquippedItem(),1);
  }
  public static boolean renderOffhandThird(EntityPlayer player,ModelBiped model,float partial,ItemStack override) {
    if(!allowed(player))return false;
    ItemStack stack=offhand(player);
    if(renderer(stack)==null)return false;
    thirdPerson(player,model,partial,stack,-1);return true;
  }
  private static void thirdPerson(EntityPlayer player,ModelBiped model,float partial,ItemStack stack,int side) {
    PortableLightRenderer render=renderer(stack);if(render==null)return;
    ModelRenderer arm=side>0?model.bipedRightArm:model.bipedLeftArm;
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
    try {
      arm.postRender(.0625F);GL11.glTranslatef(-side*.0625F,.5F,0);
      GL11.glRotatef(-arm.rotateAngleX*180F/(float)Math.PI,1,0,0);
      GL11.glRotatef(-arm.rotateAngleY*180F/(float)Math.PI,0,1,0);
      GL11.glRotatef(-arm.rotateAngleZ*180F/(float)Math.PI,0,0,1);
      GL11.glRotatef(180,1,0,0);GL11.glRotatef(sway(player,partial)*(render.isTorch()?.2F:1),0,0,1);
      float scale=render.isTorch()?.8F:.72F;GL11.glScalef(scale,scale,scale);
      GL11.glTranslatef(0,render.isTorch()?-.125F:-render.lanternGripHeight(),0);
      render.renderModel(stack);
      if(render.isTorch())PortableCarryParticles.draw(stack,player,side,partial);
    } finally {GL11.glPopMatrix();GL11.glPopAttrib();}
  }
  public static boolean renderMain(ItemRenderer itemRenderer,float partial) {
    Minecraft mc=Minecraft.getMinecraft();if(!allowed(mc.thePlayer))return false;
    ItemStack stack;
    try {
      if(mainItem==null)mainItem=cpw.mods.fml.relauncher.ReflectionHelper.findField(ItemRenderer.class,"itemToRender","field_78453_b");
      stack=(ItemStack)mainItem.get(itemRenderer);
    } catch(IllegalAccessException e){throw new IllegalStateException(e);}
    if(renderer(stack)==null)return false;
    return firstPerson(mc,stack,1,partial,mainEquip(itemRenderer,partial));
  }
  public static boolean renderOffhand(float partial,Minecraft mc,ItemRenderer itemRenderer) {
    if(!allowed(mc.thePlayer) || !Loader.isModLoaded("minefantasy2"))return false;
    ItemStack stack=MF2PortableCarryBridge.cached(itemRenderer,mc.thePlayer);if(renderer(stack)==null)return false;
    return firstPerson(mc,stack,-1,partial,MF2PortableCarryBridge.equip(itemRenderer,partial));
  }
  public static void finishMain(ItemRenderer renderer,float partial) {
    if(Loader.isModLoaded("minefantasy2"))MF2PortableCarryBridge.renderRemaining(renderer,partial);
  }
  private static float mainEquip(ItemRenderer renderer,float partial) {
    try {
      if(equipped==null){equipped=cpw.mods.fml.relauncher.ReflectionHelper.findField(ItemRenderer.class,"equippedProgress","field_78454_c");previousEquipped=cpw.mods.fml.relauncher.ReflectionHelper.findField(ItemRenderer.class,"prevEquippedProgress","field_78451_d");}
      return 1-(previousEquipped.getFloat(renderer)+(equipped.getFloat(renderer)-previousEquipped.getFloat(renderer))*partial);
    } catch(IllegalAccessException e){throw new IllegalStateException(e);}
  }
  private static boolean firstPerson(Minecraft mc,ItemStack stack,int side,float partial,float equip) {
    PortableLightRenderer render=renderer(stack);EntityPlayer player=mc.thePlayer;
    Render renderer=RenderManager.instance.getEntityRenderObject(player);if(!(renderer instanceof RenderPlayer))return false;
    float lightX=OpenGlHelper.lastBrightnessX,lightY=OpenGlHelper.lastBrightnessY;
    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);GL11.glPushMatrix();
    try {
      int light=player.getBrightnessForRender(partial);OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,light&65535,light>>>16);
      OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);GL11.glEnable(GL11.GL_TEXTURE_2D);GL11.glEnable(32826);
      GL11.glColor4f(1,1,1,1);
      RenderHelper.enableStandardItemLighting();
      float age=player.ticksExisted+partial;
      beforeArm(!render.isTorch(),side,equip,age);
      vanillaArm(side);
      if(!player.isInvisible()) {
        firstPersonArm=true;
        try {
          mc.getTextureManager().bindTexture(mc.thePlayer.getLocationSkin());
          RenderPlayer skin=(RenderPlayer)renderer;
          if(side>0)skin.renderFirstPersonArm(mc.thePlayer);
          else {
            ModelBiped model=skin.modelBipedMain;
            model.setRotationAngles(0,0,0,0,0,.0625F,player);
            ModelRenderer arm=model.bipedLeftArm;
            float x=arm.rotateAngleX,y=arm.rotateAngleY,z=arm.rotateAngleZ;
            try {arm.rotateAngleX=arm.rotateAngleY=arm.rotateAngleZ=0;arm.render(.0625F);}
            finally {arm.rotateAngleX=x;arm.rotateAngleY=y;arm.rotateAngleZ=z;}
          }
        } finally {firstPersonArm=false;}
      }
      block(!render.isTorch(),side,sway(player,partial));
      GL11.glTranslatef(.5F,render.isTorch()?0:.8125F-render.lanternGripHeight(),.5F);render.renderModel(stack);
      if(render.isTorch())PortableCarryParticles.draw(stack,player,side,partial);
    } finally {
      firstPersonArm=false;OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,lightX,lightY);GL11.glPopMatrix();GL11.glPopAttrib();
    }
    return true;
  }
  static void beforeArm(boolean lantern,int side,float equip,float age) {
    GL11.glTranslatef(0,(float)Math.sin(age*.1)*.007F*side,0);GL11.glRotatef(.15F*MathHelper.sin(age*.15F)*side,0,1,0);
    GL11.glTranslatef(0,-.1F,.1F);
    if(lantern){GL11.glTranslatef(0,.1F,0);GL11.glTranslatef(.1F*side,0,-.1F);GL11.glRotatef(10,1,0,0);}
    GL11.glTranslatef(side,-equip*.3F,.3F);GL11.glRotatef(45*side,0,1,0);GL11.glRotatef(-40*side,0,0,1);GL11.glRotatef(30,1,0,0);GL11.glScalef(.9F,.9F,.9F);
  }
  static void vanillaArm(int side) {
    GL11.glTranslatef(side*.64000005F,-.6F,-.71999997F);GL11.glRotatef(side*45,0,1,0);
    GL11.glTranslatef(-side,3.6F,3.5F);GL11.glRotatef(side*120,0,0,1);GL11.glRotatef(200,1,0,0);GL11.glRotatef(side*-135,0,1,0);GL11.glTranslatef(side*5.6F,0,0);
  }
  static void block(boolean lantern,int side,float sway) {
    GL11.glTranslatef(-.3F*side,.65F,-.1F);GL11.glRotatef(-65*side,0,1,0);GL11.glRotatef(10,1,0,0);
    if(lantern){GL11.glTranslatef(0,0,-.1F);GL11.glRotatef(-35*side,0,1,0);GL11.glRotatef(15,1,0,0);GL11.glRotatef(75*side+sway,0,0,1);GL11.glTranslatef(.3F*side,-.35F,0);GL11.glTranslatef(0,0,.1F);GL11.glScalef(1.5F,1.5F,1.5F);}
    else {GL11.glScalef(1.5F,1.5F,1.5F);GL11.glRotatef(-25*side,0,1,0);GL11.glRotatef(5,1,0,0);GL11.glRotatef(75*side,0,0,1);GL11.glTranslatef(.2F*side,.2F,.05F);}
    if(side<0)GL11.glTranslatef(-.4F,0,0);
    GL11.glScalef(.4F,.4F,.4F);GL11.glTranslatef(-.9F*side,-.45F,-.5F);
  }
}
