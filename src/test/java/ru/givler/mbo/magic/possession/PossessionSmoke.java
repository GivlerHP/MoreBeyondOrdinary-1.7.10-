package ru.givler.mbo.magic.possession;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.monster.*;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.event.entity.living.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;
import ru.givler.mbo.core.PossessionTransformer;
import ru.givler.mbo.entity.fauna.*;
import ru.givler.mbo.network.packet.PacketPossessionState;

public final class PossessionSmoke {
  static final UUID OWNER=UUID.fromString("00000000-0000-0000-0000-000000000001");
  static class TestPlayer extends EntityPlayerMP {
    UUID identity;
    TestPlayer(){super(null,null,new GameProfile(OWNER,"mage"),null);}
    @Override public UUID getUniqueID(){return identity==null?OWNER:identity;}
    @Override public String getCommandSenderName(){return "mage";}
    @Override public void setHealth(float value){getDataWatcher().updateObject(6,value);}
    @Override public boolean isEntityAlive(){return getHealth()>0;}
    @Override public boolean isPotionActive(net.minecraft.potion.Potion potion){return false;}
  }
  public static class TestWorld extends World {
    long time;net.minecraft.block.Block ground;int sounds,states;boolean obstacle;
    java.util.List nearbyBoats;
    java.util.List nearbyAnimals;
    final net.minecraft.world.GameRules rules=new net.minecraft.world.GameRules();
    TestWorld(){super(null,"test",(net.minecraft.world.WorldSettings)null,null,null);}
    protected IChunkProvider createChunkProvider(){return null;}
    protected int func_152379_p(){return 0;}
    public Entity getEntityByID(int id){return null;}
    public java.util.List getEntitiesWithinAABB(Class type,net.minecraft.util.AxisAlignedBB box){if(nearbyAnimals!=null&&type==EntityAnimal.class)return nearbyAnimals;return nearbyBoats!=null&&type==ru.givler.mbo.entity.boat.EntityMBOBoat.class?nearbyBoats:super.getEntitiesWithinAABB(type,box);}
    public long getTotalWorldTime(){return time;}
    public net.minecraft.block.Block getBlock(int x,int y,int z){return y==0?ground:obstacle&&y==1&&z==2?net.minecraft.init.Blocks.stone:net.minecraft.init.Blocks.air;}
    public int getBlockMetadata(int x,int y,int z){return 0;}
    public net.minecraft.world.GameRules getGameRules(){return rules==null?RULES:rules;}
    public boolean setBlock(int x,int y,int z,net.minecraft.block.Block block,int meta,int flags){if(y==0)ground=block;return true;}
    public void playSoundAtEntity(Entity e,String sound,float volume,float pitch){sounds++;}
    public void playAuxSFX(int id,int x,int y,int z,int data){}
    public void setEntityState(Entity e,byte state){states++;}
    public boolean func_147451_t(int x,int y,int z){return true;}
    public boolean handleMaterialAcceleration(net.minecraft.util.AxisAlignedBB box,net.minecraft.block.material.Material material,Entity entity){return false;}
    public boolean isMaterialInBB(net.minecraft.util.AxisAlignedBB box,net.minecraft.block.material.Material material){return false;}
    public boolean func_147470_e(net.minecraft.util.AxisAlignedBB box){return false;}
    public boolean checkChunksExist(int x,int y,int z,int x2,int y2,int z2){return false;}
    public java.util.List getCollidingBoundingBoxes(Entity entity,net.minecraft.util.AxisAlignedBB box) {
      java.util.List<net.minecraft.util.AxisAlignedBB> result=new ArrayList<net.minecraft.util.AxisAlignedBB>();
      net.minecraft.util.AxisAlignedBB floor=net.minecraft.util.AxisAlignedBB.getBoundingBox(-1000,0,-1000,1000,1,1000);
      if(floor.intersectsWith(box))result.add(floor);
      net.minecraft.util.AxisAlignedBB step=net.minecraft.util.AxisAlignedBB.getBoundingBox(-2,1,2,2,2,3);
      if(obstacle&&step.intersectsWith(box))result.add(step);return result;
    }
  }
  static final net.minecraft.world.GameRules RULES=new net.minecraft.world.GameRules();
  static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
  @SuppressWarnings("unchecked") static Map<UUID,PossessionManager.Session> players()throws Exception {
    Field f=PossessionManager.class.getDeclaredField("PLAYERS");f.setAccessible(true);return (Map<UUID,PossessionManager.Session>)f.get(null);
  }
  @SuppressWarnings("unchecked") static Map<EntityLivingBase,PossessionManager.Session> animals()throws Exception {
    Field f=PossessionManager.class.getDeclaredField("ANIMALS");f.setAccessible(true);return (Map<EntityLivingBase,PossessionManager.Session>)f.get(null);
  }
  static PossessionManager.Session link(TestPlayer player,EntityLivingBase animal)throws Exception {
    PossessionManager.Session s=new PossessionManager.Session(player,animal);s.active=true;s.age=10;
    players().put(OWNER,s);animals().put(animal,s);return s;
  }
  public static void main(String[] args)throws Exception {
    Field home=cpw.mods.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");home.setAccessible(true);home.set(null,new File("build/possession-smoke"));
    Field side=cpw.mods.fml.relauncher.FMLRelaunchLog.class.getDeclaredField("side");side.setAccessible(true);side.set(null,cpw.mods.fml.relauncher.Side.SERVER);
    ru.givler.mbo.config.MobSpawnConfig.load(new File("build/possession-smoke"));
    Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)uf.get(null);
    Field loader=cpw.mods.fml.common.Loader.class.getDeclaredField("instance");loader.setAccessible(true);loader.set(null,unsafe.allocateInstance(cpw.mods.fml.common.Loader.class));
    Field mods=cpw.mods.fml.common.Loader.class.getDeclaredField("namedMods");mods.setAccessible(true);mods.set(loader.get(null),new HashMap<String,cpw.mods.fml.common.ModContainer>());
    net.minecraft.init.Bootstrap.func_151354_b();
    TestPlayer player=(TestPlayer)unsafe.allocateInstance(TestPlayer.class);
    Field watcher=Entity.class.getDeclaredField("dataWatcher");watcher.setAccessible(true);DataWatcher data=new DataWatcher(player);data.addObject(0,(byte)0);data.addObject(6,20F);watcher.set(player,data);
    player.getAttributeMap().registerAttribute(SharedMonsterAttributes.movementSpeed);
    TestWorld world=(TestWorld)unsafe.allocateInstance(TestWorld.class);player.worldObj=world;
    Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(world,new net.minecraft.world.WorldProviderSurface());
    Field profiler=World.class.getDeclaredField("theProfiler");profiler.setAccessible(true);profiler.set(world,new net.minecraft.profiler.Profiler());
    EntityLivingBase[] allowed={new EntitySheep(world),new EntityWolf(world),new EntityBat(world),new EntityHorse(world),new EntitySquid(world),
      new EntityRabbit(world),new EntityMBOCod(world),new EntityMBOSalmon(world),new EntityMBOTropicalFish(world),new EntityMBOPufferfish(world),
      new EntityMBOPolarBear(world),new EntityMBOTurtle(world),new EntityMBOFrog(world),new EntityMBOTadpole(world),new EntityMBOGoat(world),
      new EntityMBOPanda(world),new EntityMBOFox(world),new EntityMBOAxolotl(world),new EntityMBOGlowSquid(world),new EntityMBOCamel(world),
      new EntityMBOZombieHorse(world),new EntityMBOSkeletonHorse(world)};
    for(EntityLivingBase a:allowed)require(PossessionManager.eligible(player,a),"Allowed animal rejected: "+a.getClass());
    for(EntityLivingBase a:new EntityLivingBase[]{new EntityVillager(world),new EntityIronGolem(world),new EntitySnowman(world),new EntityZombie(world),new EntityMBOGuardian(world),new EntityMBOCamelHusk(world)})
      require(!PossessionManager.eligible(player,a),"Forbidden creature accepted: "+a.getClass());
    EntityWolf wolf=new EntityWolf(world);wolf.setTamed(true);wolf.func_152115_b(UUID.randomUUID().toString());
    require(!PossessionManager.eligible(player,wolf),"Foreign pet accepted");wolf.func_152115_b(OWNER.toString());require(PossessionManager.eligible(player,wolf),"Own pet rejected");
    EntityHorse horse=new EntityHorse(world);horse.setHorseTamed(true);horse.func_152120_b(UUID.randomUUID().toString());
    require(!PossessionManager.eligible(player,horse),"Foreign horse accepted");horse.func_152120_b(OWNER.toString());require(PossessionManager.eligible(player,horse),"Own horse rejected");
    EntityMBOFox fox=new EntityMBOFox(world);fox.trust(UUID.randomUUID());require(!PossessionManager.eligible(player,fox),"Foreign fox accepted");fox.trust(OWNER);require(PossessionManager.eligible(player,fox),"Trusted fox rejected");
    EntitySheep sheep=new EntitySheep(world);sheep.worldObj=world;
    PossessionManager.Session s=link(player,sheep);
    s.age=1000000;s.lastInput=0;s.forward=1;s.exit=12;
    PossessionManager.pauseStaleInput(s);
    require(PossessionManager.isControlling(player)&&PossessionManager.isControlled(sheep)&&s.forward==0&&s.exit==0,"Stale input must stop movement without expiring possession");
    s.age=10;s.lastInput=0;
    sheep.setPosition(100,20,200);
    require(PossessionManager.viewX(player)==100&&PossessionManager.viewZ(player)==200&&player.posX==0&&player.posZ==0,"Camera streaming moved the real body");
    PossessionManager.input(player,s.token+1,1,0,0,0,4);require(s.exit==0,"Stale token accepted");
    PossessionManager.input(player,s.token,Float.NaN,0,0,0,4);require(s.exit==0,"Non-finite movement accepted");
    PossessionManager.input(player,s.token,1,0,30,20,4);require(s.exit==1&&s.forward==1,"Input lost");
    PossessionManager.input(player,s.token,1,0,30,20,4);require(s.exit==1,"Packet flood accelerates exit");
    s.age++;PossessionManager.input(player,s.token,0,0,0,0,0);require(s.exit==0,"Exit hold not reset");
    s.age++;PossessionManager.input(player,s.token,1,0,0,0,16);require(s.sprint,"Sprint input was not transmitted");
    for(int i=0;i<PossessionManager.EXIT_TICKS;i++){s.age++;PossessionManager.input(player,s.token,0,0,0,0,4);}
    require(!PossessionManager.isControlling(player)&&!PossessionManager.isControlled(sheep),"Voluntary exit leaked session");
    for(float hp:new float[]{20,6,2,1}) {
      player.setHealth(hp);link(player,sheep);PossessionManager.INSTANCE.death(new LivingDeathEvent(sheep,DamageSource.generic));
      require(player.getHealth()==Math.max(1,hp-6),"Death backlash killed or healed caster");
    }
    player.setHealth(20);link(player,sheep);PossessionManager.INSTANCE.hurt(new LivingHurtEvent(player,DamageSource.generic,2));
    require(!PossessionManager.isControlling(player)&&player.getHealth()==20,"Body damage must release without backlash");
    link(player,sheep);PossessionManager.INSTANCE.logout(new cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent(player));
    require(!PossessionManager.isControlling(player)&&!PossessionManager.isControlled(sheep),"Logout leaked possession");
    link(player,sheep);PossessionManager.INSTANCE.dimension(new cpw.mods.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent(player,0,1));
    require(!PossessionManager.isControlling(player)&&!PossessionManager.isControlled(sheep),"Dimension change leaked possession");
    s=link(player,sheep);
    // A shutdown ticket must not be released again after Forge clears its world registry.
    s.ticket=(net.minecraftforge.common.ForgeChunkManager.Ticket)unsafe.allocateInstance(net.minecraftforge.common.ForgeChunkManager.Ticket.class);
    PossessionManager.INSTANCE.unload(new net.minecraftforge.event.world.WorldEvent.Unload(world));
    require(s.ticket==null,"World shutdown retained its chunk ticket");
    require(players().isEmpty()&&animals().isEmpty(),"World shutdown leaked possession");
    s=link(player,sheep);
    PacketPossessionState packet=new PacketPossessionState(s,PacketPossessionState.START);
    io.netty.buffer.ByteBuf buffer=Unpooled.buffer();packet.toBytes(buffer);PacketPossessionState restored=new PacketPossessionState();restored.fromBytes(buffer);buffer.release();
    require(restored.token==s.token&&restored.owner.equals(OWNER)&&restored.id==sheep.getEntityId(),"State packet roundtrip");players().clear();animals().clear();
    scrollInteraction(player,sheep);
    movementControl(world);
    surfaceSwimming(player,world);
    grazing(player,world);
    horseControls(player,world);
    equineVariants(player,world);
    passengers(unsafe,player,world);
    possessedBoats(player,world);
    boatMechanics(player,world);
    clientHorseAnimation(unsafe);
    ghostPose(unsafe,world);
    bearNeck(world);
    activeChunks(unsafe,player,world);
    lookControl(unsafe,player);
    verifyAsm();System.out.println("Possession: direct scroll/entity interaction, species whitelist, pet ownership, packet validation, held exit, nonlethal backlash, damage cleanup and ASM PASS");
  }
  static void scrollInteraction(TestPlayer player,EntitySheep target)throws Exception {
    final int[] calls={0},syncs={0};
    ru.givler.mbo.magic.registry.SpellRegistry.register(new ru.givler.mbo.magic.api.Spell(
        ru.givler.mbo.magic.registry.SpellRegistry.id("mind_control"),ru.givler.mbo.magic.api.MagicElement.NECROMANCY,
        ru.givler.mbo.magic.api.SpellTier.ADVANCED,ru.givler.mbo.magic.api.CastType.INSTANT,0,0,context->{
          require(context.target()==target,"Clicked entity was discarded and ray-targeted again");calls[0]++;return ru.givler.mbo.magic.api.SpellResult.SUCCESS;
        }));
    player.inventory=new net.minecraft.entity.player.InventoryPlayer(player);
    player.capabilities=new net.minecraft.entity.player.PlayerCapabilities();
    player.inventoryContainer=new net.minecraft.inventory.Container(){public boolean canInteractWith(EntityPlayer p){return true;}public void detectAndSendChanges(){syncs[0]++;}};
    ru.givler.mbo.item.magic.ItemSpellScroll scroll=new ru.givler.mbo.item.magic.ItemSpellScroll();
    net.minecraft.item.ItemStack stack=scroll.create(ru.givler.mbo.magic.registry.SpellRegistry.find(ru.givler.mbo.magic.registry.SpellRegistry.id("mind_control")));
    stack.stackSize=2;player.inventory.setInventorySlotContents(0,stack);
    net.minecraftforge.event.entity.player.EntityInteractEvent event=new net.minecraftforge.event.entity.player.EntityInteractEvent(player,target){@Override public boolean isCancelable(){return true;}};
    PossessionManager.INSTANCE.interactEntity(event);
    require(event.isCanceled()&&calls[0]==1&&stack.stackSize==1&&syncs[0]==1,"Direct entity click must cast and consume exactly one scroll");
    event=new net.minecraftforge.event.entity.player.EntityInteractEvent(player,target){@Override public boolean isCancelable(){return true;}};PossessionManager.INSTANCE.interactEntity(event);
    require(calls[0]==2&&player.getCurrentEquippedItem()==null,"Last scroll not removed from held slot");
    player.capabilities.isCreativeMode=true;stack.stackSize=1;player.inventory.setInventorySlotContents(0,stack);
    event=new net.minecraftforge.event.entity.player.EntityInteractEvent(player,target){@Override public boolean isCancelable(){return true;}};PossessionManager.INSTANCE.interactEntity(event);
    require(calls[0]==3&&stack.stackSize==1,"Creative scroll consumed");
  }
  static void verifyAsm()throws Exception {
    String[] names={"net.minecraft.util.MovementInputFromOptions","net.minecraft.world.World","net.minecraftforge.common.util.ChunkCoordComparator","net.minecraft.entity.Entity","net.minecraft.entity.EntityLivingBase","net.minecraft.entity.passive.EntitySheep","net.minecraft.entity.passive.EntityHorse",
      "net.minecraft.server.management.PlayerManager","net.minecraft.entity.EntityTrackerEntry",
      "net.minecraft.network.NetHandlerPlayServer","minefantasy.mf2.mechanics.HorseMovement","minefantasy.mf2.network.packet.PacketHorseMove$Pending","net.minecraft.client.audio.SoundManager","net.minecraft.server.management.ServerConfigurationManager","ru.givler.mbo.entity.fauna.EntityMBOFish","ru.givler.mbo.entity.fauna.EntityMBOCamel","ru.givler.mbo.entity.fauna.EntityMBOGoat"};
    for(String name:names) {
      ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=PossessionSmoke.class.getClassLoader().getResourceAsStream(name.replace('.','/')+".class")){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
      PossessionTransformer transformer=new PossessionTransformer();byte[] original=out.toByteArray(),patched=transformer.transform(name,name,original);
      require(!Arrays.equals(original,patched),"Missing possession hook: "+name);require(Arrays.equals(patched,transformer.transform(name,name,patched)),"Non-idempotent hook: "+name);
      StringWriter errors=new StringWriter();CheckClassAdapter.verify(new ClassReader(patched),false,new PrintWriter(errors));require(errors.toString().isEmpty(),name+": "+errors);
      if(name.endsWith("NetHandlerPlayServer")){ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int hooks=0;for(MethodNode m:node.methods)for(AbstractInsnNode ins:m.instructions.toArray())if(ins instanceof MethodInsnNode&&((MethodInsnNode)ins).name.equals("isControlling"))hooks++;require(hooks>=7,"Incomplete inventory/action packet guards");}
    }
  }
  static void movementControl(World world) {
    EntitySheep sheep=new EntitySheep(world);
    require(PossessionManager.walkingInput(false)>.23F,"Walking was scaled by the sheep attribute twice");
    require(PossessionManager.walkingInput(true)==2*PossessionManager.walkingInput(false),"Sprint does not differ from walking");
    for(EntityLivingBase animal:new EntityLivingBase[]{new EntityMBOCod(world),new EntityBat(world),new EntitySheep(world)}) {
      animal.rotationYaw=90;
      net.minecraft.entity.ai.attributes.IAttributeInstance speed=animal.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
      speed.setBaseValue(.42);
      PossessionManager.freeMovement(animal,1,0,0);
      require(Math.abs(animal.motionX+.42)<1e-6&&Math.abs(animal.motionZ)<1e-6,"Original movement attribute ignored");
      speed.applyModifier(new net.minecraft.entity.ai.attributes.AttributeModifier(UUID.randomUUID(),"test speed",.5,2));
      PossessionManager.freeMovement(animal,1,1,1);
      double length=Math.sqrt(animal.motionX*animal.motionX+animal.motionY*animal.motionY+animal.motionZ*animal.motionZ);
      require(Math.abs(length-speed.getAttributeValue())<1e-6,"Diagonal speed or live attribute modifier ignored");
      PossessionManager.freeMovement(animal,0,0,0);require(animal.motionX==0&&animal.motionY==0&&animal.motionZ==0,"Idle animal drifts");
    }
  }
  static void surfaceSwimming(TestPlayer player,TestWorld world)throws Exception {
    Field water=Entity.class.getDeclaredField("inWater");water.setAccessible(true);
    for(EntityLivingBase animal:new EntityLivingBase[]{new EntityMBOPolarBear(world),new EntitySheep(world),new EntityCow(world),new EntityWolf(world),new EntityHorse(world),new EntityMBOCamel(world)}) {
      animal.setPosition(.5,10,.5);water.setBoolean(animal,true);animal.getRNG().setSeed(1);
      PossessionManager.Session s=link(player,animal);
      require(s.floats,"Native swimming task ignored for "+animal.getClass());
      for(int i=0;i<40;i++)require(PossessionManager.living(animal),"Swimming session not controlled");
      require(animal.posY>10,"Possessed surface swimmer sank: "+animal.getClass());
      s.sneak=true;animal.motionY=0;
      double y=animal.posY;
      for(int i=0;i<15;i++)PossessionManager.living(animal);
      require(animal.posY<y,"Deliberate diving blocked");
      players().clear();animals().clear();
    }
    require(!new PossessionManager.Session(player,new EntityMBOCod(world)).floats,"Fish acquired surface swimming");
    require(!new PossessionManager.Session(player,new EntityBat(world)).floats,"Bat acquired surface swimming");
  }
  static void grazing(TestPlayer player,TestWorld world) {
    EntitySheep sheep=new EntitySheep(world);sheep.setPosition(.5,1,.5);sheep.onGround=true;sheep.setSheared(true);world.ground=net.minecraft.init.Blocks.grass;
    PossessionManager.Session s=new PossessionManager.Session(player,sheep);
    PossessionActions.startGrazing(s);require(s.eatTicks==40,"Grass action did not start");
    for(int i=0;i<36;i++)PossessionActions.pose(s);
    require(!sheep.getSheared()&&world.ground==net.minecraft.init.Blocks.dirt,"Grazing must consume grass and regrow wool");
    require(PossessionActions.sheepMeals(sheep)==1,"Grazing did not increase persistent meal counter");
    for(int i=2;i<6;i++)require(!PossessionActions.recordMeal(sheep),"Sheep burst too early");
    require(PossessionActions.recordMeal(sheep),"Sixth successive meal did not trigger overfeeding");
    new PossessionManager.Session(player,sheep);require(PossessionActions.sheepMeals(sheep)==6,"Repossessing reset overfeeding");
    world.time+=1200;require(PossessionActions.sheepMeals(sheep)==0,"Rest did not reduce overfeeding");
    EntityBat bat=new EntityBat(world);bat.rotationYaw=0;
    double min=1,max=-1;
    for(int i=0;i<200;i++){bat.ticksExisted=i;PossessionManager.batMovement(bat,1,0,0);min=Math.min(min,bat.motionX);max=Math.max(max,bat.motionX);require(Math.sqrt(bat.motionX*bat.motionX+bat.motionY*bat.motionY+bat.motionZ*bat.motionZ)<.25,"Bat flight too fast");require(bat.motionY==0,"Level bat flight bobs vertically");}
    require(max-min>.01,"Bat flight has no lateral flutter");
    for(int i=0;i<40;i++)PossessionManager.batMovement(bat,1,0,1);
    require(bat.motionY>0,"Bat cannot climb");
    for(int i=0;i<40;i++)PossessionManager.batMovement(bat,1,0,0);
    require(Math.abs(bat.motionY)<.00001,"Bat keeps climbing after release");
  }
  static void horseControls(TestPlayer player,TestWorld world)throws Exception {
    int originalSlot=player.inventory.currentItem;player.inventory.currentItem=2;
    world.ground=net.minecraft.init.Blocks.dirt;
    EntityHorse horse=new EntityHorse(world);horse.setPosition(.5,1,.5);horse.onGround=true;
    PossessionManager.Session s=new PossessionManager.Session(player,horse);
    s.jump=true;for(int i=0;i<10;i++)require(PossessionActions.updateCharge(s,false)==-1,"Horse jumped before release");
    require(s.jumpCharge()==1,"Native full jump charge missing");s.jump=false;int charge=PossessionActions.updateCharge(s,false);
    require(charge==100&&s.horseCharge==0,"Releasing jump did not consume full charge");
    horse.getAttributeMap().getAttributeInstanceByName("horse.jumpStrength").setBaseValue(.9);
    horse.addPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.potion.Potion.jump.id,100,1));
    float yaw=player.rotationYaw,forward=player.moveForward;
    PossessionActions.travelHorse(s,0,0,charge);
    require(horse.motionY>.95&&horse.motionY<1.05,"Horse did not use original jump strength and jump boost: "+horse.motionY);
    require(horse.riddenByEntity==null&&!horse.isHorseSaddled()&&!horse.isTame(),"Temporary horse steering leaked rider or saddle");
    require(player.rotationYaw==yaw&&player.moveForward==forward&&player.ridingEntity==null,"Horse movement altered real player controls");
    horse=new EntityHorse(world);horse.setPosition(.5,1,.5);horse.onGround=true;world.obstacle=true;s=new PossessionManager.Session(player,horse);s.lookYaw=0;s.sprint=true;
    boolean climbed=false;for(int i=0;i<40;i++){PossessionActions.travelHorse(s,0,1,-1);if(horse.boundingBox.minY>=1.99)climbed=true;}
    require(climbed&&horse.posZ>2,"Native horse auto-step did not climb a full block");world.obstacle=false;
    s.active=true;players().put(OWNER,s);animals().put(horse,s);player.inventory.currentItem=5;
    PossessionManager.INSTANCE.logout(new cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent(player));
    require(player.inventory.currentItem==2&&horse.stepHeight==s.originalStepHeight,"Exit did not restore slot and step height");player.inventory.currentItem=originalSlot;
    s.pose=1;EntityMBOFox fox=new EntityMBOFox(world);PossessionManager.Session sleep=new PossessionManager.Session(player,fox);sleep.pose=1;
    require(PossessionActions.movementBlocked(sleep),"Sleeping fox can move");
    s.horseCharge=5;s.jump=true;PossessionActions.updateCharge(s,true);require(s.horseCharge==0,"Blocked animation retained a jump charge");
  }
  static void equineVariants(TestPlayer player,TestWorld world) {
    EntityHorse donkey=new EntityHorse(world);donkey.setHorseType(1);
    EntityHorse mule=new EntityHorse(world);mule.setHorseType(2);
    for(EntityHorse horse:new EntityHorse[]{new EntityHorse(world),donkey,mule,new EntityMBOZombieHorse(world),new EntityMBOSkeletonHorse(world)}) {
      horse.setPosition(.5,1,.5);horse.onGround=true;
      PossessionManager.Session s=new PossessionManager.Session(player,horse);
      double expected=(horse.getHorseJumpStrength()-.08)*.98;
      PossessionActions.travelHorse(s,0,0,100);
      require(Math.abs(horse.motionY-expected)<1e-5,"Equine variant lost native jump strength: "+horse.getClass()+" type="+horse.getHorseType());
      require(horse.stepHeight>=1&&horse.riddenByEntity==null,"Equine auto-step or rider cleanup missing");
    }
    EntityMBOCamel camel=new EntityMBOCamel(world);camel.setPosition(.5,1,.5);camel.onGround=true;camel.getDataWatcher().updateObject(27,52);
    PossessionManager.Session s=new PossessionManager.Session(player,camel);PossessionActions.travelHorse(s,0,1,100);
    require(camel.motionZ>.2&&s.dashCooldown>0,"Camel charged dash did not activate");
    require(camel.stepHeight==1.5F&&camel.riddenByEntity==null,"Camel lost native auto-step or rider cleanup");
    camel.onGround=true;camel.getDataWatcher().updateObject(27,52);PossessionActions.action(s);
    require(camel.sitting(),"Camel's special action must sit");
    require(PossessionActions.movementBlocked(s),"Sitting camel can move");
  }
  static TestPlayer passenger(sun.misc.Unsafe unsafe,TestWorld world,int id)throws Exception {
    TestPlayer player=(TestPlayer)unsafe.allocateInstance(TestPlayer.class);player.identity=new UUID(0,id);player.worldObj=world;
    DataWatcher data=new DataWatcher(player);data.addObject(0,(byte)0);data.addObject(6,20F);
    Field watcher=Entity.class.getDeclaredField("dataWatcher");watcher.setAccessible(true);watcher.set(player,data);
    Field bounds=Entity.class.getDeclaredField("boundingBox");bounds.setAccessible(true);bounds.set(player,net.minecraft.util.AxisAlignedBB.getBoundingBox(-.3,0,-.3,.3,1.8,.3));
    return player;
  }
  static void passengers(sun.misc.Unsafe unsafe,TestPlayer mage,TestWorld world)throws Exception {
    TestPlayer rider=passenger(unsafe,world,2);rider.moveForward=-1;rider.rotationYaw=180;
    EntityHorse horse=new EntityHorse(world);horse.setPosition(.5,1,.5);horse.onGround=true;horse.riddenByEntity=rider;rider.ridingEntity=horse;
    require(PossessionManager.eligible(mage,horse),"Mounted horse rejected possession");
    PossessionManager.Session s=link(mage,horse);s.lookYaw=0;
    require(PossessionManager.passenger(rider)&&PossessionManager.blockRiderJump(horse),"Passenger can control possessed horse");
    require(!PossessionActions.movementBlocked(s),"Passenger blocked possession movement");
    for(int i=0;i<10;i++)PossessionActions.travelHorse(s,0,1,-1);
    require(horse.posZ>.5&&horse.riddenByEntity==rider&&rider.ridingEntity==horse,"Passenger replaced controller or was dismounted");
    require(rider.moveForward==-1&&rider.rotationYaw==180,"Possession changed passenger input");
    double x=horse.posX,z=horse.posZ;
    net.minecraft.network.play.client.C03PacketPlayer packet=new net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook(999,999,1000,999,90,20,true);
    require(PossessionManager.riderMovement(rider,packet)&&horse.posX==x&&horse.posZ==z,"Passenger movement packet moved mount");
    require(rider.rotationYaw==90&&rider.rotationPitch==20,"Passenger cannot look around");
    require(PossessionManager.riderAction(rider,new net.minecraft.network.play.client.C0BPacketEntityAction(rider,6,100)),"Passenger jump command accepted");
    require(!PossessionManager.riderAction(rider,new net.minecraft.network.play.client.C0BPacketEntityAction(rider,1)),"Passenger crouch/dismount command blocked");
    s.steering=true;require(!PossessionManager.blockRiderJump(horse),"Controller native jump blocked");s.steering=false;
    PossessionManager.INSTANCE.logout(new cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent(mage));
    require(rider.ridingEntity==horse&&horse.riddenByEntity==rider&&!PossessionManager.passenger(rider)&&!PossessionManager.blockRiderJump(horse),"Exit failed to restore rider control");
    EntityMBOCamel camel=new EntityMBOCamel(world);camel.setPosition(.5,1,.5);camel.onGround=true;camel.getDataWatcher().updateObject(27,52);
    camel.riddenByEntity=rider;rider.ridingEntity=camel;s=link(mage,camel);
    TestPlayer rear=passenger(unsafe,world,3);EntityMBOCamelSeat seat=new EntityMBOCamelSeat(world,camel);seat.riddenByEntity=rear;rear.ridingEntity=seat;
    require(PossessionManager.passenger(rider)&&PossessionManager.passenger(rear),"Camel rear passenger can bypass possession control");
    PossessionActions.travelHorse(s,0,1,100);require(camel.riddenByEntity==rider&&seat.riddenByEntity==rear&&s.dashCooldown>0,"Charged camel dash lost passengers");
    players().clear();animals().clear();
  }
  static void possessedBoats(TestPlayer mage,TestWorld world)throws Exception {
    ru.givler.mbo.entity.boat.EntityMBOBoat boat=new ru.givler.mbo.entity.boat.EntityMBOBoat(world);
    ru.givler.mbo.entity.boat.EntityMBOBoatSeat seat=new ru.givler.mbo.entity.boat.EntityMBOBoatSeat(world,boat);
    for(EntityLivingBase animal:new EntityLivingBase[]{new EntityChicken(world),new EntitySheep(world),new EntityWolf(world),new EntityMBOPanda(world),new EntityMBOFrog(world),new EntityMBOTurtle(world),new EntityMBOAxolotl(world)})
      require(boat.canCarryPossessed(animal),"Boat rejected fitting animal: "+animal.getClass());
    for(EntityLivingBase animal:new EntityLivingBase[]{new EntityHorse(world),new EntityMBOCamel(world),new EntityMBOZombieHorse(world),new EntityMBOSkeletonHorse(world),new EntityMBOPolarBear(world),new EntityBat(world),new EntitySquid(world),new EntityMBOCod(world),new EntityMBOTadpole(world)})
      require(!boat.canCarryPossessed(animal),"Boat accepted excluded animal: "+animal.getClass());
    EntitySheep animal=new EntitySheep(world);PossessionManager.Session s=link(mage,animal);
    boolean creative=mage.capabilities.isCreativeMode;
    try {
      EntityMBOPolarBear bear=new EntityMBOPolarBear(world);
      require(!boat.boardPossessed(bear,false),"Adult bear boarded in survival");
      mage.capabilities.isCreativeMode=true;
      require(boat.boardPossessed(bear,true)&&bear.ridingEntity==boat,"Creative bear could not occupy driver's seat");
      PossessionManager.Session bearSession=link(mage,bear);bearSession.forward=1;
      require(PossessionManager.eligible(mage,bear)&&PossessionManager.steerBoat(boat),"Creative bear lost possession or steering");
      bear.mountEntity(null);players().clear();animals().clear();s=link(mage,animal);
      mage.capabilities.isCreativeMode=false;
      require(boat.boardPossessed(animal,false)&&animal.ridingEntity==seat&&boat.riddenByEntity==null,"Survival animal occupied driver's seat");
      require(PossessionManager.eligible(mage,animal),"Boarding broke possession eligibility");
      require(PossessionManager.living(animal)&&PossessionManager.isControlled(animal),"Seated animal lost possession");
      require(!PossessionManager.steerBoat(boat),"Survival passenger steered boat");
      s.age++;s.lastInput=0;PossessionManager.input(mage,s.token,0,0,0,0,2);
      require(animal.ridingEntity==null&&PossessionManager.isControlled(animal),"Sneak dismount broke possession");
      mage.capabilities.isCreativeMode=true;
      require(boat.boardPossessed(animal,true)&&animal.ridingEntity==boat,"Creative animal could not take driver's seat");
      s.forward=1;s.strafe=1;
      require(PossessionManager.steerBoat(boat),"Creative animal could not steer");
      Method control=boat.getClass().getDeclaredMethod("controlBoat");control.setAccessible(true);control.invoke(boat);
      require(boat.motionX*boat.motionX+boat.motionZ*boat.motionZ>0&&boat.rotationYaw!=0,"Animal controls did not reach native boat physics");
      mage.capabilities.isCreativeMode=false;require(!PossessionManager.steerBoat(boat),"Creative permission remained after changing game mode");
      animal.mountEntity(null);
      EntitySheep other=new EntitySheep(world);require(boat.boardPossessed(other,false),"Additional passenger rejected");
      require(!boat.boardPossessed(animal,false)&&animal.ridingEntity==null,"Occupied passenger seat replaced its rider");
      other.mountEntity(null);
      ru.givler.mbo.entity.boat.EntityMBOChestBoat chest=new ru.givler.mbo.entity.boat.EntityMBOChestBoat(world);
      require(!chest.boardPossessed(animal,false),"Survival animal occupied chest boat's single seat");
      require(chest.boardPossessed(animal,true),"Creative animal could not drive chest boat");animal.mountEntity(null);
      animal.setPosition(0,1,0);boat.setPosition(0,1,2);
      world.nearbyBoats=Collections.singletonList(boat);
      mage.capabilities.isCreativeMode=false;
      float pitch=(float)Math.toDegrees(Math.atan2(animal.posY+animal.getEyeHeight()-1.25,2));
      s.age++;s.lastInput=0;PossessionManager.input(mage,s.token,0,0,0,pitch,32);
      require(animal.ridingEntity==seat,"Right-click input did not board the aimed boat");animal.mountEntity(null);
      boat.setPosition(0,1,3.2);world.obstacle=true;
      pitch=(float)Math.toDegrees(Math.atan2(animal.posY+animal.getEyeHeight()-1.25,3.2));
      s.age++;PossessionManager.input(mage,s.token,0,0,0,pitch,32);
      require(animal.ridingEntity==null,"Animal boarded through a wall");world.obstacle=false;
      boat.setPosition(0,1,8);s.age++;PossessionManager.input(mage,s.token,0,0,0,0,32);
      require(animal.ridingEntity==null,"Animal boarded a distant boat");
    } finally {world.nearbyBoats=null;world.obstacle=false;mage.capabilities.isCreativeMode=creative;players().clear();animals().clear();}
  }
  static void boatMechanics(TestPlayer player,TestWorld world)throws Exception {
    Class<ru.givler.mbo.entity.boat.EntityMBOBoat> type=ru.givler.mbo.entity.boat.EntityMBOBoat.class;
    Method status=type.getDeclaredMethod("getBoatStatus"),motion=type.getDeclaredMethod("updateMotion"),control=type.getDeclaredMethod("controlBoat"),pickup=type.getDeclaredMethod("pickUpNearbyAnimals");
    status.setAccessible(true);motion.setAccessible(true);control.setAccessible(true);pickup.setAccessible(true);
    Field current=type.getDeclaredField("status"),previous=type.getDeclaredField("previousStatus");current.setAccessible(true);previous.setAccessible(true);
    net.minecraft.block.Block original=world.ground;
    try {
      double normal=0,ice=0;
      for(net.minecraft.block.Block ground:new net.minecraft.block.Block[]{net.minecraft.init.Blocks.dirt,net.minecraft.init.Blocks.ice,net.minecraft.init.Blocks.packed_ice}) {
        world.ground=ground;
        ru.givler.mbo.entity.boat.EntityMBOBoat boat=new ru.givler.mbo.entity.boat.EntityMBOBoat(world);boat.setPosition(.5,1,.5);
        boat.riddenByEntity=new EntitySheep(world);
        Object land=status.invoke(boat);require(land.toString().equals("ON_LAND"),"Boat lost ground contact");current.set(boat,land);previous.set(boat,land);
        boat.possessionInputs(0,1);
        for(int i=0;i<200;i++){motion.invoke(boat);control.invoke(boat);}
        double speed=Math.sqrt(boat.motionX*boat.motionX+boat.motionZ*boat.motionZ);
        if(ground==net.minecraft.init.Blocks.dirt)normal=speed;
        else {ice=speed;require(speed>1.8,"Ice boat failed to accelerate to modern ice speed");
          boat.possessionInputs(0,0);for(int i=0;i<20;i++){motion.invoke(boat);control.invoke(boat);}
          require(Math.sqrt(boat.motionX*boat.motionX+boat.motionZ*boat.motionZ)>speed*.6,"Ice boat stopped coasting too quickly");}
      }
      require(ice>normal*10,"Ice and ordinary ground have the same drag");
      ru.givler.mbo.entity.boat.EntityMBOBoat boat=new ru.givler.mbo.entity.boat.EntityMBOBoat(world);
      ru.givler.mbo.entity.boat.EntityMBOBoatSeat seat=new ru.givler.mbo.entity.boat.EntityMBOBoatSeat(world,boat);
      EntityMBOPolarBear adult=new EntityMBOPolarBear(world),cub=new EntityMBOPolarBear(world);cub.setGrowingAge(-24000);
      require(cub.width<boat.width,"Bear cub has adult collision width");
      EntityWolf wolf=new EntityWolf(world);
      world.nearbyAnimals=Arrays.asList(adult,cub,wolf);pickup.invoke(boat);
      require(cub.ridingEntity==seat&&adult.ridingEntity==null&&boat.riddenByEntity==null,"Boat failed to pick up cub while reserving driver seat");
      pickup.invoke(boat);require(seat.riddenByEntity==cub&&wolf.ridingEntity==null,"Auto-pickup replaced passenger");
      cub.mountEntity(null);boat.riddenByEntity=player;
      world.nearbyAnimals=Collections.singletonList(wolf);pickup.invoke(boat);
      require(wolf.ridingEntity==seat&&boat.riddenByEntity==player,"Boat could not collect animal with player driver");wolf.mountEntity(null);
      for(EntityLivingBase passenger:new EntityLivingBase[]{new EntitySheep(world),new EntityMBOPolarBear(world),new EntityChicken(world),new EntityRabbit(world)}) {
        passenger.limbSwing=4;passenger.limbSwingAmount=passenger.prevLimbSwingAmount=1;
        if(passenger instanceof EntityChicken) {
          EntityChicken chicken=(EntityChicken)passenger;
          chicken.destPos=chicken.field_70884_g=chicken.field_70886_e=chicken.field_70888_h=chicken.field_70889_i=1;
        }
        passenger.mountEntity(seat);boat.setPosition(10,1,20);boat.updateSecondPassenger(passenger);
        require(passenger.limbSwing==0&&passenger.limbSwingAmount==0&&passenger.prevLimbSwingAmount==0,"Boat passenger kept running animation");
        if(passenger instanceof EntityChicken) {
          EntityChicken chicken=(EntityChicken)passenger;
          require(chicken.destPos==0&&chicken.field_70884_g==0&&chicken.field_70889_i==0,"Chicken kept airborne wing animation in boat");
        }
        if(passenger instanceof EntityRabbit)require(((EntityRabbit)passenger).getJumpCompletion(.5F)==0,"Rabbit hops while seated");
        passenger.mountEntity(null);
      }
      PossessionManager.Session s=link(player,cub);world.nearbyAnimals=Collections.singletonList(cub);pickup.invoke(boat);
      require(cub.ridingEntity==null,"Auto-pickup overrode possessed animal's controls");players().clear();animals().clear();
    } finally {world.ground=original;world.nearbyAnimals=null;}
  }
  static void bearNeck(TestWorld world)throws Exception {
    EntityMBOPolarBear bear=new EntityMBOPolarBear(world);
    Field stand=EntityMBOPolarBear.class.getDeclaredField("stand"),previous=EntityMBOPolarBear.class.getDeclaredField("previousStand");stand.setAccessible(true);previous.setAccessible(true);
    Class<?> type=ru.givler.mbo.client.model.fauna.ModelMBOPolarBear.class;
    Field headField=type.getDeclaredField("head"),bodyField=type.getDeclaredField("body");headField.setAccessible(true);bodyField.setAccessible(true);
    for(boolean cub:new boolean[]{false,true}) {
      ru.givler.mbo.client.model.fauna.ModelMBOPolarBear model=new ru.givler.mbo.client.model.fauna.ModelMBOPolarBear(cub);
      double radius=-1;
      for(int i=0;i<=24;i++) {
        stand.setFloat(bear,i/4F);previous.setFloat(bear,i/4F);model.setRotationAngles(0,0,bear.ticksExisted,0,0,.0625F,bear);
        net.minecraft.client.model.ModelRenderer head=(net.minecraft.client.model.ModelRenderer)headField.get(model),body=(net.minecraft.client.model.ModelRenderer)bodyField.get(model);
        double dy=head.rotationPointY-body.rotationPointY,dz=head.rotationPointZ-body.rotationPointZ,r=dy*dy+dz*dz;
        if(radius<0)radius=r;
        require(Math.abs(radius-r)<.15,"Roaring bear head detached from torso pivot");
        if(cub)require(head.rotationPointY>10,"Cub head lifted to adult standing height");
      }
    }
  }
  static void ghostPose(sun.misc.Unsafe unsafe,TestWorld world)throws Exception {
    net.minecraft.client.entity.EntityOtherPlayerMP ghost=(net.minecraft.client.entity.EntityOtherPlayerMP)unsafe.allocateInstance(net.minecraft.client.entity.EntityOtherPlayerMP.class);
    Field bounds=Entity.class.getDeclaredField("boundingBox");bounds.setAccessible(true);bounds.set(ghost,net.minecraft.util.AxisAlignedBB.getBoundingBox(0,0,0,1,2,1));
    for(EntityLivingBase animal:new EntityLivingBase[]{new EntityMBOPolarBear(world),new EntityHorse(world),new EntityMBOCamel(world),new EntityMBOCod(world),new EntityChicken(world)}) {
      animal.lastTickPosX=10;animal.lastTickPosY=20;animal.lastTickPosZ=30;animal.setPosition(12,20,34);
      animal.prevRenderYawOffset=179;animal.renderYawOffset=-179;
      animal.prevRotationYawHead=170;animal.rotationYawHead=-170;
      animal.prevRotationPitch=10;animal.rotationPitch=20;
      ru.givler.mbo.client.render.PossessionGhostRenderer.align(ghost,animal,.5F);
      require(ghost.renderYawOffset==180&&ghost.prevRenderYawOffset==180&&ghost.rotationYaw==ghost.prevRotationYaw,"Ghost body snaps at yaw wrap");
      require(ghost.rotationYawHead==180&&ghost.prevRotationYawHead==180&&ghost.rotationPitch==15&&ghost.prevRotationPitch==15,"Ghost head interpolates a stale pose");
      require(ghost.posX==ghost.prevPosX&&ghost.posY==ghost.lastTickPosY&&ghost.posZ==ghost.prevPosZ,"Ghost position interpolates twice");
      require(ghost.posY+1.8>20+animal.height,"Ghost head remains hidden inside animal");
      if(animal instanceof EntityChicken)require(ghost.posY==20,"Chicken ghost must stand at full height");
      if(animal instanceof EntityHorse&&!(animal instanceof EntityMBOCamel))require(Math.abs(ghost.posY-20-animal.getMountedYOffset()-ghost.getYOffset())<1e-6,"Horse ghost is inside its mount instead of seated");
      double x=ghost.posX,y=ghost.posY,z=ghost.posZ;
      ru.givler.mbo.client.render.PossessionGhostRenderer.align(ghost,animal,.5F);
      require(ghost.posX==x&&ghost.posY==y&&ghost.posZ==z,"Ghost pose drifts between frames");
    }
  }
  static void clientHorseAnimation(sun.misc.Unsafe unsafe)throws Exception {
    TestWorld world=(TestWorld)unsafe.allocateInstance(TestWorld.class);
    Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(world,new net.minecraft.world.WorldProviderSurface());
    Field profiler=World.class.getDeclaredField("theProfiler");profiler.setAccessible(true);profiler.set(world,new net.minecraft.profiler.Profiler());
    Field remote=World.class.getDeclaredField("isRemote");remote.setAccessible(true);remote.setBoolean(world,true);
    for(EntityLivingBase animal:new EntityLivingBase[]{new EntitySheep(world),new EntityChicken(world),new EntityMBOPolarBear(world),new EntityHorse(world)}) {
      animal.getEntityData().setBoolean("MBOPossessed",true);animal.rotationYawHead=150;animal.rotationPitch=70;
      require(ru.givler.mbo.client.render.LivingEntityRenderHooks.modelHeadYaw(animal,150,30,.5F)==105,"Possessed head exceeded vanilla horizontal limit");
      require(ru.givler.mbo.client.render.LivingEntityRenderHooks.modelHeadYaw(animal,-150,0,.5F)==-75,"Negative head turn exceeded vanilla limit");
      require(ru.givler.mbo.client.render.LivingEntityRenderHooks.modelHeadYaw(animal,-175,175,.5F)==185,"Head turn jumped at angle wrap");
      require(ru.givler.mbo.client.render.LivingEntityRenderHooks.modelHeadYaw(animal,45,30,.5F)==45,"Ordinary head turning was suppressed");
      require(ru.givler.mbo.client.render.LivingEntityRenderHooks.modelHeadPitch(animal,70)==70&&ru.givler.mbo.client.render.LivingEntityRenderHooks.modelHeadPitch(animal,120)==90&&ru.givler.mbo.client.render.LivingEntityRenderHooks.modelHeadPitch(animal,-120)==-90,"Vanilla vertical head turning lost");
      require(animal.rotationYawHead==150&&animal.rotationPitch==70,"Head rendering changed camera controls");
      animal.getEntityData().setBoolean("MBOPossessed",false);
      require(ru.givler.mbo.client.render.LivingEntityRenderHooks.modelHeadYaw(animal,150,30,.5F)==150&&ru.givler.mbo.client.render.LivingEntityRenderHooks.modelHeadPitch(animal,70)==70,"Wild animal head tracking changed");
    }
    EntityBat bat=new EntityBat(world);bat.getEntityData().setBoolean("MBOPossessed",true);bat.setIsBatHanging(true);
    require(PossessionManager.clientBatMovement(bat)&&!bat.getIsBatHanging(),"Controlled bat still uses client gravity/hanging");
    bat.getEntityData().setBoolean("MBOPossessed",false);
    require(!PossessionManager.clientBatMovement(bat),"Wild bat movement suppressed");
    for(EntityHorse horse:new EntityHorse[]{new EntityHorse(world),new EntityMBOCamel(world),new EntityMBOZombieHorse(world),new EntityMBOSkeletonHorse(world)}) {
      horse.getEntityData().setBoolean("MBOPossessed",true);
      horse.prevPosX=horse.prevPosZ=0;horse.setPosition(.1,1,.2);
      double x=horse.posX,z=horse.posZ;float yaw=horse.rotationYaw;
      require(PossessionManager.clientHorse(horse)&&horse.limbSwingAmount>0&&horse.limbSwing>0,"Possessed equine movement has no client animation");
      require(horse.posX==x&&horse.posZ==z&&horse.rotationYaw==yaw,"Animation hook changed mount movement");
      float moving=horse.limbSwingAmount;
      horse.prevPosX=horse.posX;horse.prevPosZ=horse.posZ;
      PossessionManager.clientHorse(horse);
      require(horse.prevLimbSwingAmount==moving&&horse.limbSwingAmount<moving,"Stopped equine animation does not ease out");
      horse.getEntityData().setBoolean("MBOPossessed",false);
      float phase=horse.limbSwing;
      require(!PossessionManager.clientHorse(horse)&&horse.limbSwing==phase,"Unpossessed horse animation overridden");
    }
  }
  static void activeChunks(sun.misc.Unsafe unsafe,TestPlayer player,TestWorld originalWorld)throws Exception {
    // Execute the transformed vanilla implementation, rather than a copy of its chunk loop.
    byte[] original;
    try(InputStream in=PossessionSmoke.class.getClassLoader().getResourceAsStream("net/minecraft/world/World.class")) {
      ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);original=out.toByteArray();
    }
    byte[] patched=new PossessionTransformer().transform("net.minecraft.world.World","net.minecraft.world.World",original);
    ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);
    MethodNode method=null;for(MethodNode candidate:node.methods)if(candidate.name.equals("setActivePlayerChunksAndCheckLight"))method=candidate;
    require(method!=null,"Native active chunk method missing");method.name="stepChunks";method.access=Opcodes.ACC_PUBLIC;
    ClassWriter writer=new ClassWriter(ClassWriter.COMPUTE_MAXS|ClassWriter.COMPUTE_FRAMES);
    String generated="ru/givler/mbo/magic/possession/StreamingTestWorld";
    writer.visit(Opcodes.V1_7,Opcodes.ACC_PUBLIC,generated,null,org.objectweb.asm.Type.getInternalName(TestWorld.class),null);
    // Alias inaccessible World bookkeeping fields in the harness; preserve the chunk algorithm.
    Set<String> aliases=new HashSet<String>();
    for(AbstractInsnNode ins:method.instructions.toArray())if(ins instanceof FieldInsnNode) {
      FieldInsnNode field=(FieldInsnNode)ins;if(!field.owner.equals(node.name))continue;
      for(FieldNode definition:node.fields)if(definition.name.equals(field.name)&&(definition.access&(Opcodes.ACC_PUBLIC|Opcodes.ACC_PROTECTED))==0) {
        if(aliases.add(field.name))writer.visitField(Opcodes.ACC_PUBLIC,field.name,field.desc,null,null).visitEnd();field.owner=generated;
      }
    }
    method.accept(writer);writer.visitEnd();
    class Definer extends ClassLoader {Definer(){super(PossessionSmoke.class.getClassLoader());}Class<?> define(byte[] b){return defineClass(null,b,0,b.length);}}
    Class<?> type=new Definer().define(writer.toByteArray());TestWorld world=(TestWorld)unsafe.allocateInstance(type);
    Map<String,Object> values=new HashMap<String,Object>();values.put("theProfiler",new net.minecraft.profiler.Profiler());values.put("activeChunkSet",new HashSet<net.minecraft.world.ChunkCoordIntPair>());values.put("playerEntities",Collections.singletonList(player));values.put("rand",new Random(1));
    for(Map.Entry<String,Object> entry:values.entrySet()){Field field=World.class.getDeclaredField(entry.getKey());field.setAccessible(true);field.set(world,entry.getValue());}
    Field active=World.class.getDeclaredField("activeChunkSet");active.setAccessible(true);Method step=type.getMethod("stepChunks");
    player.worldObj=world;
    try {
      step.invoke(world);require(((Set<?>)active.get(world)).contains(new net.minecraft.world.ChunkCoordIntPair(0,0)),"Normal chunk center changed");
      EntitySheep animal=new EntitySheep(originalWorld);animal.setPosition(-128.25,20,512.75);link(player,animal);
      step.invoke(world);Set<?> chunks=(Set<?>)active.get(world);
      require(chunks.contains(new net.minecraft.world.ChunkCoordIntPair(-9,32))&&!chunks.contains(new net.minecraft.world.ChunkCoordIntPair(0,0)),"Active chunks did not follow possessed animal across negative chunk borders");
      require(player.posX==0&&player.posZ==0,"Chunk loading teleported the body");
      players().clear();animals().clear();step.invoke(world);
      require(((Set<?>)active.get(world)).contains(new net.minecraft.world.ChunkCoordIntPair(0,0)),"Exit did not restore body chunk center");
    } finally {players().clear();animals().clear();player.worldObj=originalWorld;}
  }
  static void lookControl(sun.misc.Unsafe unsafe,TestPlayer body)throws Exception {
    Class<?> client=ru.givler.mbo.client.handler.PossessionClient.class;
    Object instance=ru.givler.mbo.client.handler.PossessionClient.INSTANCE;
    Field singleton=net.minecraft.client.Minecraft.class.getDeclaredField("theMinecraft");singleton.setAccessible(true);Object oldMc=singleton.get(null);
    Field active=client.getDeclaredField("active"),yaw=client.getDeclaredField("lookYaw"),pitch=client.getDeclaredField("lookPitch");active.setAccessible(true);yaw.setAccessible(true);pitch.setAccessible(true);
    boolean oldActive=active.getBoolean(instance);float oldYaw=yaw.getFloat(instance),oldPitch=pitch.getFloat(instance);
    try {
      net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);singleton.set(null,mc);
      mc.thePlayer=(net.minecraft.client.entity.EntityClientPlayerMP)unsafe.allocateInstance(net.minecraft.client.entity.EntityClientPlayerMP.class);
      mc.thePlayer.rotationYaw=37;mc.thePlayer.rotationPitch=12;EntitySheep animal=new EntitySheep(null);mc.renderViewEntity=animal;
      active.setBoolean(instance,true);yaw.setFloat(instance,179);pitch.setFloat(instance,0);
      net.minecraft.util.MovementInput input=new net.minecraft.util.MovementInput();input.moveForward=input.moveStrafe=1;input.jump=input.sneak=true;
      ru.givler.mbo.client.handler.PossessionClient.suppressBodyInput(input);
      require(input.moveForward==0&&input.moveStrafe==0&&!input.jump&&!input.sneak,"Animal input leaked into body movement or crouching");
      Field slot=client.getDeclaredField("bodySlot");slot.setAccessible(true);int previous=slot.getInt(instance);slot.setInt(instance,2);
      body.inventory.currentItem=5;body.setSneaking(true);
      Method lock=client.getDeclaredMethod("lockBody",EntityPlayer.class);lock.setAccessible(true);lock.invoke(instance,body);
      require(body.inventory.currentItem==2&&!body.isSneaking(),"Body slot or sneak state was not locked");slot.setInt(instance,previous);
      ru.givler.mbo.client.handler.PossessionClient.INSTANCE.turn(mc.thePlayer,20,20);
      require(Math.abs(animal.rotationYaw+178)<1e-5&&Math.abs(animal.rotationPitch+3)<1e-5,"Mouse sensitivity or angle wrapping changed");
      float headTurn=net.minecraft.util.MathHelper.wrapAngleTo180_float(animal.rotationYawHead-animal.renderYawOffset);
      require(Math.abs(headTurn)>.5F&&Math.abs(headTurn)<=75F,"Actual mouse look still locks head to torso");
      float torso=animal.renderYawOffset;
      for(int i=0;i<1000;i++)ru.givler.mbo.client.handler.PossessionClient.INSTANCE.turn(mc.thePlayer,0,0);
      require(Math.abs(animal.rotationYaw+178)<1e-5&&animal.prevRotationYaw==animal.rotationYaw,"Idle frames accumulate rotation");
      require(animal.renderYawOffset==torso,"Torso rotation depends on render frame count");
      require(PossessionManager.torsoYaw(0,30,false)==0,"Looking sideways immediately turned torso");
      require(PossessionManager.torsoYaw(0,120,false)==45,"Torso failed to follow at vanilla head limit");
      require(Math.abs(PossessionManager.torsoYaw(0,30,true)-9)<.001F,"Moving torso did not follow look direction");
      animal.rotationYaw=90;animal.prevRotationYaw=45;
      ru.givler.mbo.client.handler.PossessionClient.INSTANCE.turn(mc.thePlayer,0,0);
      require(Math.abs(animal.rotationYaw+178)<1e-5,"Tracked animal angle fed back into mouse control");
      require(mc.thePlayer.rotationYaw==37&&mc.thePlayer.rotationPitch==12,"Mouse turned the abandoned body");
      Field listener=client.getDeclaredField("audioListener");listener.setAccessible(true);
      listener.set(instance,unsafe.allocateInstance(net.minecraft.client.entity.EntityOtherPlayerMP.class));
      animal.setPosition(100,20,200);
      EntityPlayer ear=ru.givler.mbo.client.handler.PossessionClient.INSTANCE.listener(mc.thePlayer);
      require(ear!=mc.thePlayer&&ear.posX==100&&ear.posZ==200&&Math.abs(ear.posY-(20+animal.getEyeHeight()))<1e-6,"Sound listener stayed at the caster's body");
      require(mc.thePlayer.posX==0&&mc.thePlayer.posZ==0,"Audio listener moved the real body");listener.set(instance,null);
      ru.givler.mbo.client.handler.PossessionClient.INSTANCE.turn(mc.thePlayer,0,-100000);
      require(animal.rotationPitch==90,"Pitch was not clamped");
      active.setBoolean(instance,false);require(!ru.givler.mbo.client.handler.PossessionClient.INSTANCE.turn(mc.thePlayer,20,20),"Normal mouse control intercepted outside possession");
    } finally {singleton.set(null,oldMc);active.setBoolean(instance,oldActive);yaw.setFloat(instance,oldYaw);pitch.setFloat(instance,oldPitch);}
  }
}
