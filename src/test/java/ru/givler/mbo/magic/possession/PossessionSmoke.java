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
    TestPlayer(){super(null,null,new GameProfile(OWNER,"mage"),null);}
    @Override public UUID getUniqueID(){return OWNER;}
    @Override public String getCommandSenderName(){return "mage";}
    @Override public void setHealth(float value){getDataWatcher().updateObject(6,value);}
    @Override public boolean isEntityAlive(){return getHealth()>0;}
    @Override public boolean isPotionActive(net.minecraft.potion.Potion potion){return false;}
  }
  static class TestWorld extends World {
    TestWorld(){super(null,"test",(net.minecraft.world.WorldSettings)null,null,null);}
    protected IChunkProvider createChunkProvider(){return null;}
    protected int func_152379_p(){return 0;}
    public Entity getEntityByID(int id){return null;}
    public long getTotalWorldTime(){return 0;}
  }
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
    TestPlayer player=(TestPlayer)unsafe.allocateInstance(TestPlayer.class);
    Field watcher=Entity.class.getDeclaredField("dataWatcher");watcher.setAccessible(true);DataWatcher data=new DataWatcher(player);data.addObject(6,20F);watcher.set(player,data);
    TestWorld world=(TestWorld)unsafe.allocateInstance(TestWorld.class);player.worldObj=world;
    Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);provider.set(world,new net.minecraft.world.WorldProviderSurface());
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
    sheep.setPosition(100,20,200);
    require(PossessionManager.viewX(player)==100&&PossessionManager.viewZ(player)==200&&player.posX==0&&player.posZ==0,"Camera streaming moved the real body");
    PossessionManager.input(player,s.token+1,1,0,0,0,4);require(s.exit==0,"Stale token accepted");
    PossessionManager.input(player,s.token,Float.NaN,0,0,0,4);require(s.exit==0,"Non-finite movement accepted");
    PossessionManager.input(player,s.token,1,0,30,20,4);require(s.exit==1&&s.forward==1,"Input lost");
    PossessionManager.input(player,s.token,1,0,30,20,4);require(s.exit==1,"Packet flood accelerates exit");
    s.age++;PossessionManager.input(player,s.token,0,0,0,0,0);require(s.exit==0,"Exit hold not reset");
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
    link(player,sheep);PossessionManager.INSTANCE.unload(new net.minecraftforge.event.world.WorldEvent.Unload(world));
    require(players().isEmpty()&&animals().isEmpty(),"World shutdown leaked possession");
    s=link(player,sheep);
    PacketPossessionState packet=new PacketPossessionState(s,PacketPossessionState.START);
    io.netty.buffer.ByteBuf buffer=Unpooled.buffer();packet.toBytes(buffer);PacketPossessionState restored=new PacketPossessionState();restored.fromBytes(buffer);buffer.release();
    require(restored.token==s.token&&restored.owner.equals(OWNER)&&restored.id==sheep.getEntityId(),"State packet roundtrip");players().clear();animals().clear();
    scrollInteraction(player,sheep);
    movementControl(world);
    lookControl(unsafe);
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
    String[] names={"net.minecraft.entity.Entity","net.minecraft.entity.EntityLivingBase","net.minecraft.entity.passive.EntitySheep","net.minecraft.entity.passive.EntityHorse",
      "net.minecraft.server.management.PlayerManager","net.minecraft.entity.EntityTrackerEntry",
      "net.minecraft.network.NetHandlerPlayServer","ru.givler.mbo.entity.fauna.EntityMBOFish","ru.givler.mbo.entity.fauna.EntityMBOCamel","ru.givler.mbo.entity.fauna.EntityMBOGoat"};
    for(String name:names) {
      ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=PossessionSmoke.class.getClassLoader().getResourceAsStream(name.replace('.','/')+".class")){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
      PossessionTransformer transformer=new PossessionTransformer();byte[] original=out.toByteArray(),patched=transformer.transform(name,name,original);
      require(!Arrays.equals(original,patched),"Missing possession hook: "+name);require(Arrays.equals(patched,transformer.transform(name,name,patched)),"Non-idempotent hook: "+name);
      StringWriter errors=new StringWriter();CheckClassAdapter.verify(new ClassReader(patched),false,new PrintWriter(errors));require(errors.toString().isEmpty(),name+": "+errors);
      if(name.endsWith("NetHandlerPlayServer")){ClassNode node=new ClassNode();new ClassReader(patched).accept(node,0);int hooks=0;for(MethodNode m:node.methods)for(AbstractInsnNode ins:m.instructions.toArray())if(ins instanceof MethodInsnNode&&((MethodInsnNode)ins).name.equals("isControlling"))hooks++;require(hooks>=7,"Incomplete inventory/action packet guards");}
    }
  }
  static void movementControl(World world) {
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
  static void lookControl(sun.misc.Unsafe unsafe)throws Exception {
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
      ru.givler.mbo.client.handler.PossessionClient.INSTANCE.turn(mc.thePlayer,20,20);
      require(Math.abs(animal.rotationYaw+178)<1e-5&&Math.abs(animal.rotationPitch+3)<1e-5,"Mouse sensitivity or angle wrapping changed");
      for(int i=0;i<1000;i++)ru.givler.mbo.client.handler.PossessionClient.INSTANCE.turn(mc.thePlayer,0,0);
      require(Math.abs(animal.rotationYaw+178)<1e-5&&animal.prevRotationYaw==animal.rotationYaw,"Idle frames accumulate rotation");
      animal.rotationYaw=90;animal.prevRotationYaw=45;
      ru.givler.mbo.client.handler.PossessionClient.INSTANCE.turn(mc.thePlayer,0,0);
      require(Math.abs(animal.rotationYaw+178)<1e-5,"Tracked animal angle fed back into mouse control");
      require(mc.thePlayer.rotationYaw==37&&mc.thePlayer.rotationPitch==12,"Mouse turned the abandoned body");
      ru.givler.mbo.client.handler.PossessionClient.INSTANCE.turn(mc.thePlayer,0,-100000);
      require(animal.rotationPitch==90,"Pitch was not clamped");
      active.setBoolean(instance,false);require(!ru.givler.mbo.client.handler.PossessionClient.INSTANCE.turn(mc.thePlayer,20,20),"Normal mouse control intercepted outside possession");
    } finally {singleton.set(null,oldMc);active.setBoolean(instance,oldActive);yaw.setFloat(instance,oldYaw);pitch.setFloat(instance,oldPitch);}
  }
}
