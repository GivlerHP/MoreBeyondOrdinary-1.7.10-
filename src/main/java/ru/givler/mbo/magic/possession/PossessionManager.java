package ru.givler.mbo.magic.possession;

import java.util.*;
import cpw.mods.fml.common.eventhandler.*;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.*;
import net.minecraft.world.*;
import net.minecraft.util.*;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.WorldEvent;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.entity.fauna.*;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketPossessionState;

/** Server-owned sessions are deliberately not persisted. */
public final class PossessionManager {
  public static final PossessionManager INSTANCE=new PossessionManager();
  private static final Map<UUID,Session> PLAYERS=new HashMap<UUID,Session>();
  private static final Map<EntityLivingBase,Session> ANIMALS=new IdentityHashMap<EntityLivingBase,Session>();
  private static long nextToken=System.nanoTime();
  public static final int ATTEMPT_TICKS=24, EXIT_TICKS=30;
  public static final class Session {
    public final EntityPlayerMP player;
    public final EntityLivingBase animal;
    public final long token=++nextToken;
    public final double x,y,z;
    public final float yaw,pitch;
    int age,lastInput,exit,actionCooldown,jumpCooldown,pose,actionTicks,dashCooldown;
    float forward,strafe,lookYaw,lookPitch;
    boolean jump,sneak,active,wasSitting;
    ForgeChunkManager.Ticket ticket;
    Session(EntityPlayerMP p,EntityLivingBase a) {
      player=p;animal=a;x=p.posX;y=p.posY;z=p.posZ;yaw=p.rotationYaw;pitch=p.rotationPitch;
      lookYaw=a.rotationYaw;lookPitch=a.rotationPitch;
      wasSitting=a instanceof EntityTameable&&((EntityTameable)a).isSitting();
    }
  }
  private PossessionManager() {}
  public static boolean isControlling(Entity e){return e instanceof EntityPlayer&&PLAYERS.containsKey(e.getUniqueID());}
  public static boolean isControlled(Entity e){return ANIMALS.containsKey(e);}
  public static EntityLivingBase view(EntityPlayer p){Session s=PLAYERS.get(p.getUniqueID());return s!=null&&s.active?s.animal:p;}
  public static double viewX(EntityPlayer p){return view(p).posX;}
  public static double viewZ(EntityPlayer p){return view(p).posZ;}
  public static boolean turn(Entity entity,float x,float y) {
    return entity.worldObj!=null&&entity.worldObj.isRemote&&ru.givler.mbo.MoreBeyondOrdinary.proxy.possessionTurn(entity,x,y);
  }
  static double movementSpeed(EntityLivingBase animal) {
    net.minecraft.entity.ai.attributes.IAttributeInstance attribute=animal.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
    return attribute==null?0:Math.max(0,attribute.getAttributeValue());
  }
  static void freeMovement(EntityLivingBase animal,float forward,float strafe,float vertical) {
    double length=Math.max(1,Math.sqrt(forward*forward+strafe*strafe+vertical*vertical));
    double speed=movementSpeed(animal)/length,angle=Math.toRadians(animal.rotationYaw);
    animal.motionX=(-Math.sin(angle)*forward+Math.cos(angle)*strafe)*speed;
    animal.motionZ=(Math.cos(angle)*forward+Math.sin(angle)*strafe)*speed;
    animal.motionY=vertical*speed;
  }
  public static boolean eligible(EntityPlayer player,EntityLivingBase animal) {
    boolean vanilla=animal.getClass().getName().startsWith("net.minecraft.entity.passive.")
        &&(animal instanceof EntityAnimal||animal instanceof EntityAmbientCreature||animal instanceof EntityWaterMob);
    boolean mbo=animal instanceof EntityRabbit||animal instanceof EntityMBOFish||animal instanceof EntityMBOPolarBear
        ||animal instanceof EntityMBOTurtle||animal instanceof EntityMBOFrog||animal instanceof EntityMBOTadpole
        ||animal instanceof EntityMBOGoat||animal instanceof EntityMBOPanda||animal instanceof EntityMBOFox
        ||animal instanceof EntityMBOAxolotl||animal instanceof EntityMBOGlowSquid
        ||(animal instanceof EntityMBOCamel&&!(animal instanceof EntityMBOCamelHusk))||animal instanceof EntityMBOUndeadHorse;
    if(!(vanilla||mbo)||!animal.isEntityAlive()||animal.ridingEntity!=null||animal.riddenByEntity!=null)return false;
    if(animal instanceof EntityTameable&&((EntityTameable)animal).isTamed()
        &&!player.getUniqueID().toString().equals(((EntityTameable)animal).func_152113_b()))return false;
    if(animal instanceof EntityHorse&&((EntityHorse)animal).isTame()
        &&!player.getUniqueID().toString().equals(((EntityHorse)animal).func_152119_ch()))return false;
    if(animal instanceof EntityMBOFox) {
      if(((EntityMBOFox)animal).hasTrustedPlayers()&&!((EntityMBOFox)animal).trusts(player.getUniqueID()))return false;
    }
    return true;
  }
  public static boolean attempt(EntityPlayerMP player,EntityLivingBase animal) {
    if(isControlling(player)||player.ridingEntity!=null||player.isPlayerSleeping()||!player.isEntityAlive())return false;
    Session s=new Session(player,animal);PLAYERS.put(player.getUniqueID(),s);send(s,PacketPossessionState.ATTEMPT);return true;
  }
  public static void input(EntityPlayerMP player,long token,float forward,float strafe,float yaw,float pitch,int flags) {
    Session s=PLAYERS.get(player.getUniqueID());
    if(s==null||!s.active||s.token!=token||!Float.isFinite(forward)||!Float.isFinite(strafe)||!Float.isFinite(yaw)||!Float.isFinite(pitch))return;
    // At most one input/action per server tick; clients cannot accelerate exit or cooldowns.
    if(s.lastInput==s.age)return;
    s.forward=MathHelper.clamp_float(forward,-1,1);s.strafe=MathHelper.clamp_float(strafe,-1,1);
    s.lookYaw=MathHelper.wrapAngleTo180_float(yaw);s.lookPitch=MathHelper.clamp_float(pitch,-90,90);
    s.jump=(flags&1)!=0;s.sneak=(flags&2)!=0;s.lastInput=s.age;
    s.exit=(flags&4)!=0?s.exit+1:0;
    if(s.exit>=EXIT_TICKS){end(s,false);return;}
    if((flags&8)!=0&&s.actionCooldown==0){s.actionCooldown=20;PossessionActions.action(s);}
  }
  private static void send(Session s,int kind){PacketManager.INSTANCE.sendTo(new PacketPossessionState(s,kind),s.player);}
  private static void broadcast(Session s,int kind) {
    PacketManager.INSTANCE.sendToAllAround(new PacketPossessionState(s,kind),new NetworkRegistry.TargetPoint(s.player.dimension,s.animal.posX,s.animal.posY,s.animal.posZ,192));
  }
  private static void begin(Session s) {
    s.ticket=ForgeChunkManager.requestTicket(MoreBeyondOrdinary.instance,s.player.worldObj,ForgeChunkManager.Type.NORMAL);
    if(s.ticket==null){end(s,false);return;}
    int cx=MathHelper.floor_double(s.x)>>4,cz=MathHelper.floor_double(s.z)>>4;
    for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)ForgeChunkManager.forceChunk(s.ticket,new ChunkCoordIntPair(cx+dx,cz+dz));
    s.active=true;s.lastInput=s.age;ANIMALS.put(s.animal,s);
    if(s.animal instanceof EntityTameable)((EntityTameable)s.animal).setSitting(false);
    if(s.animal instanceof EntityLiving){((EntityLiving)s.animal).getNavigator().clearPathEntity();((EntityLiving)s.animal).setAttackTarget(null);}
    send(s,PacketPossessionState.START);broadcast(s,PacketPossessionState.REVEAL);
  }
  private static void end(Session s,boolean death) {
    if(PLAYERS.remove(s.player.getUniqueID())!=s)return;
    ANIMALS.remove(s.animal);if(s.active)PossessionActions.reset(s);
    if(s.active&&s.animal instanceof EntityTameable)((EntityTameable)s.animal).setSitting(s.wasSitting);
    if(s.ticket!=null)ForgeChunkManager.releaseTicket(s.ticket);
    if(death&&s.player.isEntityAlive())s.player.setHealth(Math.max(1F,s.player.getHealth()-6F));
    if(s.player.playerNetServerHandler==null)return;
    if(!s.active&&s.player.playerNetServerHandler.netManager.isChannelOpen())
      s.player.addChatMessage(new ChatComponentTranslation("mbo.possession.failed"));
    if(s.player.worldObj==s.animal.worldObj&&s.player.isEntityAlive())
      s.player.playerNetServerHandler.setPlayerLocation(s.x,s.y,s.z,s.yaw,s.pitch);
    send(s,PacketPossessionState.STOP);broadcast(s,PacketPossessionState.HIDE);
    if(s.player.worldObj instanceof WorldServer)((WorldServer)s.player.worldObj).getPlayerManager().updatePlayerPertinentChunks(s.player);
  }
  /** Replaces only living movement/AI; environmental damage and entity death keep their normal ticks. */
  public static boolean living(EntityLivingBase animal) {
    if(animal.worldObj.isRemote)return false;
    Session s=ANIMALS.get(animal);if(s==null)return false;
    if(net.minecraftforge.common.ForgeHooks.onLivingUpdate(animal)||ANIMALS.get(animal)!=s)return true;
    animal.rotationYaw=s.lookYaw;animal.rotationPitch=s.lookPitch;animal.rotationYawHead=s.lookYaw;animal.renderYawOffset=s.lookYaw;
    if(animal instanceof EntityLiving)((EntityLiving)animal).setAttackTarget(null);
    if(s.jumpCooldown>0)s.jumpCooldown--;if(s.dashCooldown>0)s.dashCooldown--;
    PossessionActions.pose(s);
    boolean water=animal.isInWater(),aquatic=animal instanceof EntityWaterMob||animal instanceof EntityMBOFish
        ||animal instanceof EntityMBOTadpole||animal instanceof EntityMBOAxolotl||animal instanceof EntityMBOTurtle;
    float forward=s.forward,strafe=s.strafe;
    if(s.pose>0&&(animal instanceof EntityMBOPanda||animal instanceof EntityMBOFox||animal instanceof EntityMBOAxolotl))forward=strafe=0;
    if(animal instanceof EntityMBOCamel&&((EntityMBOCamel)animal).sitting())forward=strafe=0;
    if(animal instanceof EntityBat||aquatic&&water) {
      freeMovement(animal,forward,strafe,(s.jump?1:0)-(s.sneak?1:0));
      animal.moveEntity(animal.motionX,animal.motionY,animal.motionZ);
    } else {
      if((s.jump||animal instanceof EntityRabbit&&Math.abs(forward)+Math.abs(strafe)>0)&&animal.onGround&&s.jumpCooldown==0) {
        if(animal instanceof EntityMBOCamel)PossessionActions.dash(s);
        else {animal.motionY=animal instanceof EntityMBOGoat?.7:.42;if(animal instanceof EntityRabbit)animal.worldObj.setEntityState(animal,(byte)1);}
        s.jumpCooldown=10;
      }
      animal.setAIMoveSpeed((float)movementSpeed(animal));
      animal.moveEntityWithHeading(strafe,forward);
      if(animal instanceof EntityChicken&&animal.motionY<0)animal.motionY*=.6;
    }
    animal.limbSwingAmount+=(Math.min(1F,(float)Math.sqrt(animal.motionX*animal.motionX+animal.motionZ*animal.motionZ)*4)-animal.limbSwingAmount)*.4F;
    animal.limbSwing+=animal.limbSwingAmount;return true;
  }
  @SubscribeEvent public void tick(TickEvent.ServerTickEvent e) {
    if(e.phase!=TickEvent.Phase.END||PLAYERS.isEmpty())return;
    for(Session s:new ArrayList<Session>(PLAYERS.values())) {
      s.age++;
      if(!s.player.isEntityAlive()||!s.player.playerNetServerHandler.netManager.isChannelOpen()||s.player.worldObj!=s.animal.worldObj
          ||s.player.ridingEntity!=null||!s.animal.isEntityAlive()){end(s,s.active&&!s.animal.isEntityAlive());continue;}
      if(s.active&&(s.age-s.lastInput>100||!eligible(s.player,s.animal)||s.animal.riddenByEntity!=null||s.animal.ridingEntity!=null
          ||!s.animal.worldObj.loadedEntityList.contains(s.animal))){end(s,false);continue;}
      s.player.setPosition(s.x,s.y,s.z);s.player.motionX=s.player.motionY=s.player.motionZ=0;
      s.player.rotationYaw=s.yaw;s.player.rotationPitch=s.pitch;
      if(!s.active&&s.age>=ATTEMPT_TICKS) {
        if(!eligible(s.player,s.animal)||ANIMALS.containsKey(s.animal)||s.player.getDistanceSqToEntity(s.animal)>64||!s.player.canEntityBeSeen(s.animal)){end(s,false);continue;}
        begin(s);
      }
      if(s.active){if(s.actionCooldown>0)s.actionCooldown--;((WorldServer)s.player.worldObj).getPlayerManager().updatePlayerPertinentChunks(s.player);send(s,PacketPossessionState.UPDATE);}
    }
  }
  @SubscribeEvent(priority=EventPriority.LOWEST) public void hurt(LivingHurtEvent e){if(e.entityLiving.worldObj.isRemote)return;Session s=PLAYERS.get(e.entityLiving.getUniqueID());if(s!=null&&e.ammount>0)end(s,false);}
  @SubscribeEvent public void death(LivingDeathEvent e){if(e.entityLiving.worldObj.isRemote)return;Session s=ANIMALS.get(e.entityLiving);if(s!=null)end(s,true);s=PLAYERS.get(e.entityLiving.getUniqueID());if(s!=null)end(s,false);}
  @SubscribeEvent public void attack(LivingAttackEvent e){if(isControlled(e.source.getEntity())||isControlling(e.source.getEntity()))e.setCanceled(true);}
  @SubscribeEvent public void interact(PlayerInteractEvent e){if(isControlling(e.entityPlayer))e.setCanceled(true);}
  @SubscribeEvent public void attackEntity(AttackEntityEvent e){if(isControlling(e.entityPlayer))e.setCanceled(true);}
  @SubscribeEvent public void interactEntity(EntityInteractEvent e) {
    if(isControlling(e.entityPlayer)){e.setCanceled(true);return;}
    net.minecraft.item.ItemStack held=e.entityPlayer.getCurrentEquippedItem();
    if(held!=null&&held.getItem() instanceof ru.givler.mbo.item.magic.ItemSpellScroll
        &&e.target instanceof EntityLivingBase
        &&((ru.givler.mbo.item.magic.ItemSpellScroll)held.getItem()).possessEntity(held,e.entityPlayer,(EntityLivingBase)e.target))
      e.setCanceled(true);
  }
  @SubscribeEvent public void toss(ItemTossEvent e){if(isControlling(e.player))e.setCanceled(true);}
  @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent e){release(e.player);}
  @SubscribeEvent public void dimension(PlayerEvent.PlayerChangedDimensionEvent e){release(e.player);}
  @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent e){release(e.player);}
  private static void release(EntityPlayer p){Session s=PLAYERS.get(p.getUniqueID());if(s!=null)end(s,false);}
  @SubscribeEvent public void unload(WorldEvent.Unload e){if(!e.world.isRemote)for(Session s:new ArrayList<Session>(PLAYERS.values()))if(s.player.worldObj==e.world||s.animal.worldObj==e.world)end(s,false);}
  @SubscribeEvent public void joined(EntityJoinWorldEvent e){if(!e.world.isRemote&&e.entity instanceof EntityPlayerMP)for(Session s:PLAYERS.values())if(s.active)PacketManager.INSTANCE.sendTo(new PacketPossessionState(s,PacketPossessionState.REVEAL),(EntityPlayerMP)e.entity);}
  @SubscribeEvent public void tracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking e) {
    Session s=ANIMALS.get(e.target);if(s!=null&&e.entityPlayer instanceof EntityPlayerMP)
      PacketManager.INSTANCE.sendTo(new PacketPossessionState(s,PacketPossessionState.REVEAL),(EntityPlayerMP)e.entityPlayer);
  }
  @SubscribeEvent public void stopTracking(net.minecraftforge.event.entity.player.PlayerEvent.StopTracking e) {
    Session s=ANIMALS.get(e.target);if(s!=null&&e.entityPlayer instanceof EntityPlayerMP)
      PacketManager.INSTANCE.sendTo(new PacketPossessionState(s,PacketPossessionState.HIDE),(EntityPlayerMP)e.entityPlayer);
  }
}
