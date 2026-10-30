package ru.givler.mbo.client.handler;

import java.util.*;
import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraftforge.client.event.*;
import org.lwjgl.input.*;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.integration.thaumcraft.client.render.ThaumometerLens;
import ru.givler.mbo.magic.possession.PossessionManager;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.*;

public final class PossessionClient extends Gui {
  public static final PossessionClient INSTANCE=new PossessionClient();
  private final Map<Integer,PacketPossessionState> revealed=new HashMap<Integer,PacketPossessionState>();
  private final Map<UUID,EntityOtherPlayerMP> ghosts=new HashMap<UUID,EntityOtherPlayerMP>();
  private long token;private int target=-1,attempt,exit,flash,waiting,silence;private boolean active,mouseHeld,exitArmed;
  private double bodyX,bodyY,bodyZ;private float bodyYaw,bodyPitch,health,maxHealth;private int air;
  private float lookYaw,lookPitch;
  private net.minecraft.world.World world;
  private PotionEffect priorVision,ownVision;
  private PossessionClient(){}
  public void state(PacketPossessionState p) {
    Minecraft mc=Minecraft.getMinecraft();
    if(mc.theWorld==null||mc.thePlayer==null||mc.thePlayer.dimension!=p.dimension)return;
    if(p.kind==PacketPossessionState.REVEAL){revealed.put(p.id,p);return;}
    if(p.kind==PacketPossessionState.HIDE){PacketPossessionState old=revealed.get(p.id);if(old!=null&&old.token==p.token){revealed.remove(p.id);ghosts.remove(old.owner);}return;}
    if(!mc.thePlayer.getUniqueID().equals(p.owner))return;
    if(p.kind==PacketPossessionState.ATTEMPT) {
      clearControl();world=mc.theWorld;token=p.token;target=p.id;attempt=PossessionManager.ATTEMPT_TICKS;waiting=0;silence=0;
      bodyX=mc.thePlayer.posX;bodyY=mc.thePlayer.posY;bodyZ=mc.thePlayer.posZ;bodyYaw=mc.thePlayer.rotationYaw;bodyPitch=mc.thePlayer.rotationPitch;return;
    }
    if(p.token!=token)return;
    silence=0;
    if(p.kind==PacketPossessionState.STOP){boolean failed=!active;clearControl();flash=failed?12:6;return;}
    if(p.kind==PacketPossessionState.START){active=true;attempt=0;waiting=0;exitArmed=false;lookYaw=MathHelper.wrapAngleTo180_float(p.yaw);lookPitch=p.pitch;}
    if(active) {
      health=p.health;maxHealth=p.maxHealth;air=p.air;Entity e=mc.theWorld.getEntityByID(target);
      if(e instanceof EntityLivingBase){e.setPositionAndRotation2(p.x,p.y,p.z,lookYaw,lookPitch,1);((EntityLivingBase)e).setHealth(p.health);mc.renderViewEntity=(EntityLivingBase)e;applyLook((EntityLivingBase)e);}
    }
  }
  private void clearControl() {
    Minecraft mc=Minecraft.getMinecraft();
    if(mc.thePlayer!=null) {
      mc.renderViewEntity=mc.thePlayer;if(active){mc.thePlayer.rotationYaw=bodyYaw;mc.thePlayer.rotationPitch=bodyPitch;}
      if(ownVision!=null&&mc.thePlayer.getActivePotionEffect(Potion.nightVision)==ownVision){mc.thePlayer.removePotionEffectClient(Potion.nightVision.id);if(priorVision!=null&&priorVision.getDuration()>0)mc.thePlayer.addPotionEffect(priorVision);}
    }
    ownVision=priorVision=null;active=false;token=0;target=-1;attempt=exit=waiting=silence=0;mouseHeld=false;
  }
  private static boolean key(net.minecraft.client.settings.KeyBinding k){int c=k.getKeyCode();return c<0?Mouse.isButtonDown(c+100):c>0&&Keyboard.isKeyDown(c);}
  @SubscribeEvent public void tick(TickEvent.ClientTickEvent e) {
    if(e.phase!=TickEvent.Phase.END)return;Minecraft mc=Minecraft.getMinecraft();
    if(mc.theWorld!=world){clearControl();revealed.clear();ghosts.clear();world=mc.theWorld;}
    if(flash>0)flash--;if(token==0)return;
    if(mc.thePlayer==null||mc.theWorld==null||!mc.thePlayer.isEntityAlive()||++silence>120){clearControl();return;}
    if(attempt>0)attempt--;if(!active)return;
    Entity entity=mc.theWorld.getEntityByID(target);
    if(!(entity instanceof EntityLivingBase)){if(++waiting>100)clearControl();return;}
    waiting=0;EntityLivingBase animal=(EntityLivingBase)entity;mc.renderViewEntity=animal;
    boolean gui=mc.currentScreen!=null;
    float forward=gui?0:(key(mc.gameSettings.keyBindForward)?1:0)-(key(mc.gameSettings.keyBindBack)?1:0);
    float strafe=gui?0:(key(mc.gameSettings.keyBindLeft)?1:0)-(key(mc.gameSettings.keyBindRight)?1:0);
    boolean right=!gui&&Mouse.isButtonDown(1),left=!gui&&Mouse.isButtonDown(0);
    if(!right)exitArmed=true;exit=right&&exitArmed?Math.min(PossessionManager.EXIT_TICKS,exit+1):0;
    int flags=(!gui&&key(mc.gameSettings.keyBindJump)?1:0)|(!gui&&key(mc.gameSettings.keyBindSneak)?2:0)|(exit>0?4:0)|(left&&!mouseHeld?8:0);mouseHeld=left;
    PacketManager.INSTANCE.sendToServer(new PacketPossessionInput(token,forward,strafe,lookYaw,lookPitch,flags));
    mc.thePlayer.setPosition(bodyX,bodyY,bodyZ);mc.thePlayer.motionX=mc.thePlayer.motionY=mc.thePlayer.motionZ=0;
    if(animal instanceof EntityBat) {
      PotionEffect old=mc.thePlayer.getActivePotionEffect(Potion.nightVision);
      if(ownVision==null||old!=ownVision)priorVision=old==null?null:new PotionEffect(old);
      mc.thePlayer.removePotionEffectClient(Potion.nightVision.id);mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id,220,0,true));ownVision=mc.thePlayer.getActivePotionEffect(Potion.nightVision);
      if(priorVision!=null)priorVision=new PotionEffect(priorVision.getPotionID(),Math.max(0,priorVision.getDuration()-1),priorVision.getAmplifier(),priorVision.getIsAmbient());
    }
  }
  @SubscribeEvent public void disconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent e){ru.givler.mbo.network.EditorPacketAccess.scheduleClient(()->{clearControl();revealed.clear();ghosts.clear();world=null;});}
  @SubscribeEvent public void gui(GuiOpenEvent e){if(active&&e.gui instanceof GuiContainer)e.setCanceled(true);}
  @SubscribeEvent public void mouse(MouseEvent e){if(token!=0&&((e.button==0||e.button==1)&&e.buttonstate||e.dwheel!=0))e.setCanceled(true);}
  @SubscribeEvent public void hand(RenderHandEvent e){if(active)e.setCanceled(true);}
  @SubscribeEvent public void fog(EntityViewRenderEvent.FogDensity e) {
    if(active&&e.entity==Minecraft.getMinecraft().renderViewEntity&&e.entity.isInWater()) {e.density=.02F;e.setCanceled(true);}
  }
  @SubscribeEvent public void overlay(RenderGameOverlayEvent.Pre e){if(active)switch(e.type){case HOTBAR:case HEALTH:case ARMOR:case FOOD:case AIR:case EXPERIENCE:case HEALTHMOUNT:case JUMPBAR:e.setCanceled(true);break;default:break;}}
  @SubscribeEvent public void hud(RenderGameOverlayEvent.Post e) {
    if(e.type!=RenderGameOverlayEvent.ElementType.ALL)return;Minecraft mc=Minecraft.getMinecraft();int w=e.resolution.getScaledWidth(),h=e.resolution.getScaledHeight();
    if(active) {
      mc.getTextureManager().bindTexture(icons);int hearts=Math.min(40,(int)Math.ceil(maxHealth/2)),rows=(hearts+19)/20;
      for(int i=0;i<hearts;i++){int x=w/2-Math.min(20,hearts)*4+(i%20)*8,y=h-30-(rows-1-i/20)*10;drawTexturedModalRect(x,y,16,0,9,9);if(health>i*2)drawTexturedModalRect(x,y,health>=i*2+2?52:61,0,9,9);}
      if(air<300)drawCenteredString(mc.fontRenderer,StatCollector.translateToLocal("mbo.possession.air")+": "+Math.max(0,air/20),w/2,h-45-rows*10,0x80D8FF);
    }
    float intensity=attempt>0?(float)Math.sin((1-attempt/(float)PossessionManager.ATTEMPT_TICKS)*Math.PI):exit/(float)PossessionManager.EXIT_TICKS;
    if(flash>0)intensity=Math.max(intensity,flash/12F);if(intensity>0)drawRect(0,0,w,h,((int)(Math.min(1,intensity)*210)<<24)|0x241237);
    if(active&&exit>0)drawCenteredString(mc.fontRenderer,StatCollector.translateToLocal("mbo.possession.return"),w/2,h/2+25,0xDAC8EE);
  }
  @SubscribeEvent public void renderTick(TickEvent.RenderTickEvent e) {
    Minecraft mc=Minecraft.getMinecraft();if(e.phase!=TickEvent.Phase.START||mc.thePlayer==null)return;
    if(active&&mc.renderViewEntity!=null&&mc.renderViewEntity!=mc.thePlayer)applyLook(mc.renderViewEntity);
    if(holdingLens())ThaumometerLens.prepare();
  }
  /** Vanilla already applies mouse sensitivity before setAngles; preserve its 0.15 degree scale. */
  public boolean turn(Entity entity,float x,float y) {
    Minecraft mc=Minecraft.getMinecraft();if(!active||entity!=mc.thePlayer)return false;
    lookYaw=MathHelper.wrapAngleTo180_float(lookYaw+x*.15F);
    lookPitch=MathHelper.clamp_float(lookPitch-y*.15F,-90,90);
    if(mc.renderViewEntity!=null&&mc.renderViewEntity!=mc.thePlayer)applyLook(mc.renderViewEntity);
    return true;
  }
  private void applyLook(EntityLivingBase animal) {
    animal.rotationYaw=animal.prevRotationYaw=lookYaw;animal.rotationPitch=animal.prevRotationPitch=lookPitch;
    animal.rotationYawHead=animal.prevRotationYawHead=lookYaw;animal.renderYawOffset=animal.prevRenderYawOffset=lookYaw;
  }
  private static boolean holdingLens(){Minecraft mc=Minecraft.getMinecraft();return mc.thePlayer!=null&&mc.thePlayer.getHeldItem()!=null&&mc.thePlayer.getHeldItem().getItem()==cpw.mods.fml.common.registry.GameRegistry.findItem("Thaumcraft","ItemThaumometer");}
  @SubscribeEvent public void reveal(RenderWorldLastEvent e) {
    Minecraft mc=Minecraft.getMinecraft();if(!holdingLens()||revealed.isEmpty())return;GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    try {
      if(!ThaumometerLens.begin())return;ThaumometerLens.select(true);GL11.glEnable(GL11.GL_BLEND);GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);GL11.glDepthMask(false);
      for(PacketPossessionState s:revealed.values()) {
        Entity animal=mc.theWorld.getEntityByID(s.id);if(animal==null||animal.isDead)continue;EntityOtherPlayerMP ghost=ghosts.get(s.owner);
        if(ghost==null){ghost=new EntityOtherPlayerMP(mc.theWorld,new GameProfile(s.owner,s.name)){@Override public boolean isInvisibleToPlayer(EntityPlayer p){return false;}};ghost.setInvisible(true);ghosts.put(s.owner,ghost);}
        double x=animal.lastTickPosX+(animal.posX-animal.lastTickPosX)*e.partialTicks,y=animal.lastTickPosY+(animal.posY-animal.lastTickPosY)*e.partialTicks,z=animal.lastTickPosZ+(animal.posZ-animal.lastTickPosZ)*e.partialTicks;
        ghost.setPosition(x,y,z);ghost.rotationYawHead=animal.rotationYaw;ghost.renderYawOffset=animal.rotationYaw;
        RenderManager.instance.renderEntityWithPosYaw(ghost,x-RenderManager.renderPosX,y-RenderManager.renderPosY,z-RenderManager.renderPosZ,animal.rotationYaw,e.partialTicks);
      }
    }finally{GL11.glPopAttrib();GL11.glColor4f(1,1,1,1);}
  }
}
