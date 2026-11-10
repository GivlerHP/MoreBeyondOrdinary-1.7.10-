package ru.givler.mbo.lootcontainer.action;

import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import ru.givler.mbo.integration.thaumcraft.item.staff.ItemStaffBasic;
import ru.givler.mbo.tileentity.TileEntityLootContainer;
import thaumcraft.api.wands.IWandable;
import sun.misc.Unsafe;

/** Behavior at shared boundaries: dispatch short-circuiting, blast edges and config compatibility. */
public final class AuditRefactorSmoke {
  static Unsafe unsafe;
  static void check(boolean value,String message) { if(!value)throw new AssertionError(message); }
  static <T>T blank(Class<T> type)throws Exception { return (T)unsafe.allocateInstance(type); }
  static class TestWorld extends World {
    Block block; TileEntity tile; int metadata; Vec3 start,end; List targets;
    TestWorld(){super(null,"audit",(WorldSettings)null,null,null);}
    protected IChunkProvider createChunkProvider(){return null;}
    protected int func_152379_p(){return 0;}
    public Entity getEntityByID(int id){return null;}
    public Block getBlock(int x,int y,int z){return block;}
    public TileEntity getTileEntity(int x,int y,int z){return tile;}
    public int getBlockMetadata(int x,int y,int z){return metadata;}
    public boolean setBlockMetadataWithNotify(int x,int y,int z,int meta,int flags){metadata=meta;return true;}
    public List getEntitiesWithinAABB(Class type,AxisAlignedBB box){return targets;}
    public MovingObjectPosition rayTraceBlocks(Vec3 from,Vec3 to){start=from;end=to;return null;}
  }
  static class Target extends EntityZombie {
    int burned;float damage;
    Target(){super(null);}
    public void setFire(int seconds){burned=seconds;}
    public boolean attackEntityFrom(DamageSource source,float amount){damage=amount;return true;}
  }
  static class Staff extends ItemStaffBasic {
    MovingObjectPosition hit;
    protected MovingObjectPosition getMovingObjectPositionFromPlayer(World world,EntityPlayer player,boolean liquids){return hit;}
    ItemStack dispatch(ItemStack stack,World world){return interactWithWandable(stack,world,null);}
  }
  static class WandBlock extends Block implements IWandable {
    int calls;ItemStack result;
    WandBlock(){super(Material.rock);}
    public int onWandRightClick(World w,ItemStack s,EntityPlayer p,int x,int y,int z,int side,int md){return 0;}
    public ItemStack onWandRightClick(World w,ItemStack s,EntityPlayer p){calls++;return result;}
    public void onUsingWandTick(ItemStack s,EntityPlayer p,int count){}
    public void onWandStoppedUsing(ItemStack s,World w,EntityPlayer p,int count){}
  }
  static class WandTile extends TileEntity implements IWandable {
    int calls;ItemStack result;
    public int onWandRightClick(World w,ItemStack s,EntityPlayer p,int x,int y,int z,int side,int md){return 0;}
    public ItemStack onWandRightClick(World w,ItemStack s,EntityPlayer p){calls++;return result;}
    public void onUsingWandTick(ItemStack s,EntityPlayer p,int count){}
    public void onWandStoppedUsing(ItemStack s,World w,EntityPlayer p,int count){}
  }
  public static void main(String[] args)throws Exception {
    Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);unsafe=(Unsafe)f.get(null);
    TestWorld world=blank(TestWorld.class);
    Staff staff=blank(Staff.class);staff.hit=new MovingObjectPosition(1,2,3,1,Vec3.createVectorHelper(1,2,3));
    WandBlock block=blank(WandBlock.class);WandTile tile=blank(WandTile.class);world.block=block;world.tile=tile;
    ItemStack original=blank(ItemStack.class),replacement=blank(ItemStack.class);
    block.result=original;tile.result=replacement;
    check(staff.dispatch(original,world)==original && block.calls==1 && tile.calls==0,"handled block must stop before tile, even for the same stack");
    block.result=null;check(staff.dispatch(original,world)==replacement && tile.calls==1,"unhandled block must continue to tile");
    tile.result=null;check(staff.dispatch(original,world)==null,"unhandled dispatch must remain distinguishable");
    check(staff.onItemRightClick(original,world,null)==original,"base staff must retain original unhandled stack");
    staff.hit=null;int calls=block.calls;check(staff.dispatch(original,world)==null && block.calls==calls,"ray miss must not invoke block");

    Target inside=blank(Target.class),edge=blank(Target.class),outside=blank(Target.class),dead=blank(Target.class);
    for(Target t:new Target[]{inside,edge,outside,dead}){t.posX=.5;t.posY=.5;t.posZ=.5;}
    edge.posX=4.5;outside.posX=4.50001;dead.isDead=true;
    world.targets=Arrays.asList(inside,edge,outside,dead);
    TileEntityLootContainer chest=blank(TileEntityLootContainer.class);chest.setWorldObj(world);
    ExplosionBurningAction burn=new ExplosionBurningAction();burn.particles=burn.knockback=false;burn.durationSec=9;burn.execute(chest,null);
    ExplosionInstantDamageAction damage=new ExplosionInstantDamageAction();damage.particles=damage.knockback=false;damage.damage=3;damage.execute(chest,null);
    check(inside.burned==9 && edge.burned==9 && outside.burned==0 && dead.burned==0,"burn radius must include edge and exclude outside/dead");
    check(inside.damage==3 && edge.damage==3 && outside.damage==0 && dead.damage==0,"damage must use identical target boundary");
    world.isRemote=true;inside.burned=0;burn.execute(chest,null);check(inside.burned==0,"client must not apply blast effects");world.isRemote=false;

    for(int meta=0;meta<16;meta++)for(int facing=0;facing<4;facing++){
      world.metadata=meta;inside.rotationYaw=facing*90;
      ru.givler.mbo.core.TrapdoorPlacementHooks.placedBy(world,0,0,0,inside);
      check(world.metadata==((meta&12)|new int[]{0,3,1,2}[facing]),"trapdoor direction/state bits");
    }
    Class<?> targeting=Class.forName("ru.givler.mbo.magic.spell.SpellTargeting");
    Method trace=targeting.getDeclaredMethod("blockTrace",World.class,Vec3.class,Vec3.class,double.class);trace.setAccessible(true);
    trace.invoke(null,world,Vec3.createVectorHelper(-2,3.25,9),Vec3.createVectorHelper(.5,0,-1),12.5);
    check(world.end.xCoord==4.25 && world.end.yCoord==3.25 && world.end.zCoord==-3.5,"shared ray must retain fractional endpoints");

    Path dir=Files.createTempDirectory("mbo-hud-audit");Path cfg=dir.resolve("MoreBeyondOrdinary/tooltip.cfg");Files.createDirectories(cfg.getParent());
    Files.write(cfg,"hud {\n B:bossBarAtBottom=false\n}\ncustom_frames {\n S:custom=ramka2\n}\n".getBytes("UTF-8"));
    Field home=cpw.mods.fml.relauncher.FMLInjectionData.class.getDeclaredField("minecraftHome");home.setAccessible(true);home.set(null,dir.toFile());
    ru.givler.mbo.config.HudConfig.load(dir.toFile());check(!ru.givler.mbo.config.HudConfig.bossBarAtBottom,"existing HUD choice lost");
    check(new String(Files.readAllBytes(cfg),"UTF-8").contains("S:custom=ramka2"),"tooltip settings lost when loading HUD");
    int registrations=0;
    for(String name:new String[]{"ItemGlyphBasic","ItemGlyphAmphibian","ItemGlyphDragon","ItemGlyphHawk","ItemGlyphMiner","ItemGlyphOwl","ItemGlyphWeapon","ItemGlyphCleansing","ItemGlyphMHealing","ItemStaffHealing"}) {
      ClassNode node=new ClassNode();try(java.io.InputStream in=AuditRefactorSmoke.class.getResourceAsStream("/ru/givler/mbo/item/glyph/"+name+".class")){new ClassReader(in).accept(node,0);}
      for(MethodNode method:node.methods)for(AbstractInsnNode i:method.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)i;if(call.owner.equals("cpw/mods/fml/common/registry/GameRegistry")&&call.name.equals("registerItem"))registrations++;}
    }
    check(registrations==1,"glyph hierarchy must register each instance exactly once");
    for(String transformer:new ru.givler.mbo.core.MBOCorePlugin().getASMTransformerClass())Class.forName(transformer).newInstance();
    System.out.println("PASS staff dispatch, blast edges/server ownership, trapdoor bits, ray geometry, existing HUD config, single glyph registration and coremod class links");
  }
}
