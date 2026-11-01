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
import ru.givler.mbo.entity.boat.EntityMBOBoat;
import ru.givler.mbo.entity.boat.EntityMBOBoatSeat;
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
    public final int slot;
    final float originalStepHeight;
    final boolean floats;
    int age,lastInput,exit,actionCooldown,jumpCooldown,pose,actionTicks,dashCooldown,eatTicks,eatX,eatY,eatZ,horseCharge;
    float forward,strafe,lookYaw,lookPitch,torsoYaw;
    boolean jump,sneak,sprint,active,wasSitting,jumpHeld,steering;
    ForgeChunkManager.Ticket ticket;
    public float jumpCharge(){return PossessionActions.jumpCharge(horseCharge);}
    Session(EntityPlayerMP p,EntityLivingBase a) {
      player=p;animal=a;x=p.posX;y=p.posY;z=p.posZ;yaw=p.rotationYaw;pitch=p.rotationPitch;
      lookYaw=a.rotationYaw;lookPitch=a.rotationPitch;
      torsoYaw=a.renderYawOffset;
      wasSitting=a instanceof EntityTameable&&((EntityTameable)a).isSitting();
      slot=p.inventory==null?0:p.inventory.currentItem;originalStepHeight=a.stepHeight;
      boolean swimming=false;
      if(a instanceof EntityLiving)for(Object entry:((EntityLiving)a).tasks.taskEntries)
        if(((net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry)entry).action instanceof net.minecraft.entity.ai.EntityAISwimming){swimming=true;break;}
      floats=swimming;
    }
  }
  private PossessionManager() {}
  public static boolean isControlling(Entity e){return e instanceof EntityPlayer&&PLAYERS.containsKey(e.getUniqueID());}
  public static float torsoYaw(float torso,float head,boolean moving) {
    if(moving)torso+=MathHelper.wrapAngleTo180_float(head-torso)*.3F;
    return head-MathHelper.clamp_float(MathHelper.wrapAngleTo180_float(head-torso),-75F,75F);
  }
  public static boolean isControlled(Entity e){return e!=null&&(e.worldObj!=null&&e.worldObj.isRemote?e.getEntityData().getBoolean("MBOPossessed"):ANIMALS.containsKey(e));}
  public static boolean passenger(Entity entity) {
    if(!(entity instanceof EntityPlayer))return false;
    Entity mount=entity.ridingEntity;
    if(mount instanceof EntityMBOCamelSeat)mount=((EntityMBOCamelSeat)mount).getCamel();
    return mount instanceof EntityHorse&&isControlled(mount);
  }
  public static boolean clientHorse(EntityHorse horse){
    if(!horse.worldObj.isRemote||!isControlled(horse))return false;
    // Suppress passenger physics, but retain vanilla horse animation from server movement.
    horse.prevLimbSwingAmount=horse.limbSwingAmount;
    double dx=horse.posX-horse.prevPosX,dz=horse.posZ-horse.prevPosZ;
    float amount=Math.min(1F,MathHelper.sqrt_double(dx*dx+dz*dz)*4F);
    horse.limbSwingAmount+=(amount-horse.limbSwingAmount)*.4F;
    horse.limbSwing+=horse.limbSwingAmount;
    return true;
  }
  public static boolean blockRiderJump(EntityHorse horse){Session s=ANIMALS.get(horse);return isControlled(horse)&&(s==null||!s.steering);}
  public static boolean clientBatMovement(EntityLivingBase animal){
    if(!(animal instanceof EntityBat)||!animal.worldObj.isRemote||!isControlled(animal))return false;
    // Keep native network interpolation, but don't apply client gravity between server updates.
    ((EntityBat)animal).setIsBatHanging(false);
    return true;
  }
  private static EntityMBOBoat boatMount(Entity animal) {
    Entity mount=animal.ridingEntity;
    return mount instanceof EntityMBOBoat?(EntityMBOBoat)mount:
        mount instanceof EntityMBOBoatSeat?((EntityMBOBoatSeat)mount).getBoat():null;
  }
  public static boolean steerBoat(EntityMBOBoat boat) {
    Session s=ANIMALS.get(boat.riddenByEntity);
    if(s==null||!s.active||!s.player.capabilities.isCreativeMode||s.animal.ridingEntity!=boat)return false;
    boat.possessionInputs(s.strafe,s.forward);return true;
  }
  private static boolean permittedBoatMount(EntityLivingBase animal) {
    EntityMBOBoat boat=boatMount(animal);
    Session s=ANIMALS.get(animal);
    return boat!=null&&!boat.isDead&&!animal.ridingEntity.isDead&&boat.canCarryPossessed(animal,s!=null&&s.player.capabilities.isCreativeMode)
        &&animal.ridingEntity.riddenByEntity==animal;
  }
  private static void boardBoat(Session s) {
    if(s.animal.ridingEntity!=null)return;
    Vec3 start=Vec3.createVectorHelper(s.animal.posX,s.animal.posY+s.animal.getEyeHeight(),s.animal.posZ);
    double yaw=Math.toRadians(s.lookYaw),pitch=Math.toRadians(s.lookPitch),cos=Math.cos(pitch);
    Vec3 end=start.addVector(-Math.sin(yaw)*cos*3,-Math.sin(pitch)*3,Math.cos(yaw)*cos*3);
    // The legacy block ray tracer mutates its start vector while stepping through cells.
    MovingObjectPosition wall=s.animal.worldObj.rayTraceBlocks(Vec3.createVectorHelper(start.xCoord,start.yCoord,start.zCoord),end);
    double nearest=wall==null?3:start.distanceTo(wall.hitVec);
    EntityMBOBoat selected=null;
    for(Object entry:s.animal.worldObj.getEntitiesWithinAABB(EntityMBOBoat.class,s.animal.boundingBox.expand(3,3,3))) {
      EntityMBOBoat boat=(EntityMBOBoat)entry;
      MovingObjectPosition hit=boat.boundingBox.expand(.15,.15,.15).calculateIntercept(start,end);
      if(hit!=null&&start.distanceTo(hit.hitVec)<nearest){nearest=start.distanceTo(hit.hitVec);selected=boat;}
    }
    if(selected!=null)selected.boardPossessed(s.animal,s.player.capabilities.isCreativeMode);
  }
  public static boolean riderMovement(EntityPlayerMP player,net.minecraft.network.play.client.C03PacketPlayer packet) {
    if(!passenger(player))return false;
    if(packet.func_149463_k()&&Float.isFinite(packet.func_149462_g())&&Float.isFinite(packet.func_149470_h())) {
      player.rotationYaw=MathHelper.wrapAngleTo180_float(packet.func_149462_g());player.rotationPitch=MathHelper.clamp_float(packet.func_149470_h(),-90,90);
    }
    if(player.ridingEntity!=null)player.ridingEntity.updateRiderPosition();
    return true;
  }
  public static boolean riderAction(EntityPlayerMP player,net.minecraft.network.play.client.C0BPacketEntityAction packet){return passenger(player)&&packet.func_149513_d()==6;}
  public static EntityLivingBase view(EntityPlayer p){
    if(p.worldObj!=null&&p.worldObj.isRemote)return ru.givler.mbo.MoreBeyondOrdinary.proxy.possessionView(p);
    Session s=PLAYERS.get(p.getUniqueID());return s!=null&&s.active?s.animal:p;
  }
  public static double viewX(EntityPlayer p){return view(p).posX;}
  public static double viewZ(EntityPlayer p){return view(p).posZ;}
  public static double viewY(EntityPlayer p){return view(p).posY;}
  public static EntityPlayer listener(EntityPlayer p){return ru.givler.mbo.MoreBeyondOrdinary.proxy.possessionListener(p);}
  public static boolean turn(Entity entity,float x,float y) {
    return entity.worldObj!=null&&entity.worldObj.isRemote&&ru.givler.mbo.MoreBeyondOrdinary.proxy.possessionTurn(entity,x,y);
  }
  static double movementSpeed(EntityLivingBase animal) {
    net.minecraft.entity.ai.attributes.IAttributeInstance attribute=animal.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
    return attribute==null?0:Math.max(0,attribute.getAttributeValue());
  }
  static float walkingInput(boolean sprint) { return sprint?.98F:.49F; }
  static void pauseStaleInput(Session s) {
    if(s.age-s.lastInput>20){s.forward=s.strafe=0;s.jump=s.sneak=s.sprint=s.jumpHeld=false;s.exit=s.horseCharge=0;}
  }
  static void freeMovement(EntityLivingBase animal,float forward,float strafe,float vertical) {
    double length=Math.max(1,Math.sqrt(forward*forward+strafe*strafe+vertical*vertical));
    double speed=movementSpeed(animal)/length,angle=Math.toRadians(animal.rotationYaw);
    animal.motionX=(-Math.sin(angle)*forward+Math.cos(angle)*strafe)*speed;
    animal.motionZ=(Math.cos(angle)*forward+Math.sin(angle)*strafe)*speed;
    animal.motionY=vertical*speed;
  }
  static void batMovement(EntityLivingBase animal,float forward,float strafe,float vertical) {
    double phase=animal.ticksExisted+animal.getEntityId()*13D;
    double angle=Math.toRadians(animal.rotationYaw+Math.sin(phase*.31)*12);
    double speed=Math.min(.18,movementSpeed(animal)*.22)/Math.max(1,Math.sqrt(forward*forward+strafe*strafe+vertical*vertical));
    double moving=Math.min(1,Math.abs(forward)+Math.abs(strafe)+Math.abs(vertical));
    double response=.20+.13*(.5+.5*Math.sin(phase*.7));
    animal.motionX+=((-Math.sin(angle)*forward+Math.cos(angle)*strafe)*speed+Math.sin(phase*.53)*.018*moving-animal.motionX)*response;
    animal.motionZ+=((Math.cos(angle)*forward+Math.sin(angle)*strafe)*speed+Math.cos(phase*.47)*.018*moving-animal.motionZ)*response;
    animal.motionY+=(vertical*speed-animal.motionY)*.28;
  }
  public static boolean eligible(EntityPlayer player,EntityLivingBase animal) {
    boolean vanilla=animal.getClass().getName().startsWith("net.minecraft.entity.passive.")
        &&(animal instanceof EntityAnimal||animal instanceof EntityAmbientCreature||animal instanceof EntityWaterMob);
    boolean mbo=animal instanceof EntityRabbit||animal instanceof EntityMBOFish||animal instanceof EntityMBOPolarBear
        ||animal instanceof EntityMBOTurtle||animal instanceof EntityMBOFrog||animal instanceof EntityMBOTadpole
        ||animal instanceof EntityMBOGoat||animal instanceof EntityMBOPanda||animal instanceof EntityMBOFox
        ||animal instanceof EntityMBOAxolotl||animal instanceof EntityMBOGlowSquid
        ||(animal instanceof EntityMBOCamel&&!(animal instanceof EntityMBOCamelHusk))||animal instanceof EntityMBOUndeadHorse;
    if(!(vanilla||mbo)||!animal.isEntityAlive()||animal.ridingEntity!=null&&!(isControlled(animal)&&permittedBoatMount(animal)))return false;
    if(animal.riddenByEntity!=null&&(!(animal instanceof EntityHorse)||!(animal.riddenByEntity instanceof EntityPlayer)||animal.riddenByEntity==player))return false;
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
    boolean dismount=(flags&2)!=0&&!s.sneak;
    s.jump=(flags&1)!=0;s.sneak=(flags&2)!=0;s.sprint=(flags&16)!=0;s.lastInput=s.age;
    if(dismount&&boatMount(s.animal)!=null)s.animal.mountEntity(null);
    if((flags&32)!=0)boardBoat(s);
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
    s.player.setSneaking(false);s.player.setSprinting(false);
    if(s.animal instanceof EntityTameable)((EntityTameable)s.animal).setSitting(false);
    if(s.animal instanceof EntityLiving){((EntityLiving)s.animal).getNavigator().clearPathEntity();((EntityLiving)s.animal).setAttackTarget(null);}
    send(s,PacketPossessionState.START);broadcast(s,PacketPossessionState.REVEAL);
  }
  private static void end(Session s,boolean death) {
    if(PLAYERS.remove(s.player.getUniqueID())!=s)return;
    ANIMALS.remove(s.animal);if(s.active)PossessionActions.reset(s);
    s.animal.stepHeight=s.originalStepHeight;
    if(s.player.inventory!=null)s.player.inventory.currentItem=s.slot;
    if(s.active&&s.animal instanceof EntityTameable)((EntityTameable)s.animal).setSitting(s.wasSitting);
    if(s.ticket!=null)ForgeChunkManager.releaseTicket(s.ticket);
    if(death&&s.player.isEntityAlive())s.player.setHealth(Math.max(1F,s.player.getHealth()-6F));
    if(s.player.playerNetServerHandler==null)return;
    s.player.playerNetServerHandler.sendPacket(new net.minecraft.network.play.server.S09PacketHeldItemChange(s.slot));
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
    s.torsoYaw=torsoYaw(s.torsoYaw,s.lookYaw,Math.abs(s.forward)+Math.abs(s.strafe)>0);
    animal.rotationYaw=s.lookYaw;animal.rotationPitch=s.lookPitch;animal.rotationYawHead=s.lookYaw;animal.renderYawOffset=s.torsoYaw;
    if(animal instanceof EntityLiving)((EntityLiving)animal).setAttackTarget(null);
    if(s.jumpCooldown>0)s.jumpCooldown--;if(s.dashCooldown>0)s.dashCooldown--;
    PossessionActions.pose(s);
    if(ANIMALS.get(animal)!=s)return true;
    if(boatMount(animal)!=null){animal.motionX=animal.motionY=animal.motionZ=0;return true;}
    boolean water=animal.isInWater(),aquatic=animal instanceof EntityWaterMob||animal instanceof EntityMBOFish
        ||animal instanceof EntityMBOTadpole||animal instanceof EntityMBOAxolotl||animal instanceof EntityMBOTurtle;
    float forward=s.forward,strafe=s.strafe;
    boolean blocked=PossessionActions.movementBlocked(s);
    if(blocked){forward=strafe=0;animal.motionX=animal.motionZ=0;}
    // The normal AI/jump-helper tick is bypassed during possession. Preserve its
    // water jump impulse for native surface swimmers, without running other AI.
    if(water&&!aquatic&&!(animal instanceof EntityBat)&&!blocked
        &&(s.jump||s.floats&&!s.sneak&&animal.getRNG().nextFloat()<.8F))animal.motionY+=.04D;
    if(animal instanceof EntityHorse) {
      int charge=PossessionActions.updateCharge(s,blocked);
      PossessionActions.travelHorse(s,strafe,forward,charge);
    } else if(animal instanceof EntityBat||aquatic&&water) {
      if(animal instanceof EntityBat)batMovement(animal,forward,strafe,blocked?0:(s.jump?1:0)-(s.sneak?1:0));
      else freeMovement(animal,forward,strafe,blocked?0:(s.jump?1:0)-(s.sneak?1:0));
      animal.moveEntity(animal.motionX,animal.motionY,animal.motionZ);
    } else {
      if(!blocked&&(s.jump||animal instanceof EntityRabbit&&Math.abs(forward)+Math.abs(strafe)>0)&&animal.onGround&&s.jumpCooldown==0) {
        if(animal instanceof PossessionAnimal)((PossessionAnimal)animal).mbo$jump();else animal.motionY=.42;
        if(animal instanceof EntityRabbit)animal.worldObj.setEntityState(animal,(byte)1);
        s.jumpCooldown=10;
      }
      animal.setAIMoveSpeed((float)movementSpeed(animal));
      float input=walkingInput(s.sprint)/(float)Math.max(1,Math.sqrt(strafe*strafe+forward*forward));
      animal.moveEntityWithHeading(strafe*input,forward*input);
      if(animal instanceof EntityChicken&&animal.motionY<0)animal.motionY*=.6;
    }
    animal.limbSwingAmount+=(Math.min(1F,(float)Math.sqrt(animal.motionX*animal.motionX+animal.motionZ*animal.motionZ)*4)-animal.limbSwingAmount)*.4F;
    animal.limbSwing+=animal.limbSwingAmount;
    animal.renderYawOffset=s.torsoYaw;return true;
  }
  @SubscribeEvent public void tick(TickEvent.ServerTickEvent e) {
    if(e.phase!=TickEvent.Phase.END||PLAYERS.isEmpty())return;
    for(Session s:new ArrayList<Session>(PLAYERS.values())) {
      s.age++;
      if(!s.player.isEntityAlive()||!s.player.playerNetServerHandler.netManager.isChannelOpen()||s.player.worldObj!=s.animal.worldObj
          ||s.player.ridingEntity!=null||!s.animal.isEntityAlive()){end(s,s.active&&!s.animal.isEntityAlive());continue;}
      EntityMBOBoat mount=boatMount(s.animal);
      if(mount!=null&&!mount.canCarryPossessed(s.animal,s.player.capabilities.isCreativeMode))s.animal.mountEntity(null);
      if(mount!=null&&s.animal.ridingEntity==mount&&!s.player.capabilities.isCreativeMode) {
        s.animal.mountEntity(null);mount.boardPossessed(s.animal,false);
      }
      if(s.active&&(!eligible(s.player,s.animal)
          ||!s.animal.worldObj.loadedEntityList.contains(s.animal))){end(s,false);continue;}
      pauseStaleInput(s);
      s.player.setPosition(s.x,s.y,s.z);s.player.motionX=s.player.motionY=s.player.motionZ=0;
      s.player.setSneaking(false);if(s.player.inventory!=null)s.player.inventory.currentItem=s.slot;
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
  @SubscribeEvent public void unload(WorldEvent.Unload e){if(!e.world.isRemote)for(Session s:new ArrayList<Session>(PLAYERS.values()))if(s.player.worldObj==e.world||s.animal.worldObj==e.world){
    // Forge's HIGHEST-priority unload handler has already removed the world's tickets.
    s.ticket=null;
    end(s,false);
  }}
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
