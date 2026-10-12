package ru.givler.mbo.decor;

import java.lang.reflect.*;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.*;
import net.minecraft.entity.player.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.*;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.model.BlockModelCollision;
import ru.givler.mbo.tileentity.TileEntityModelCollision;
import sun.misc.Unsafe;

public final class CollisionBreakSmoke {
 static class Owner extends BlockModels {
  int drops;IIcon icon;
  Owner(){super(Material.wood,"test","test","test");}
  public void dropBlockAsItemWithChance(World world,int x,int y,int z,int meta,float chance,int fortune){drops++;}
  public IIcon getIcon(int side,int meta){return icon;}
  public int colorMultiplier(IBlockAccess world,int x,int y,int z){return 0xffffff;}
 }
 static class TestWorld extends CollisionHelperLifecycleSmoke.TestWorld {
  BlockModelCollision helper;TileEntityModelCollision part;boolean live;int removedCount;
  public Block getBlock(int x,int y,int z){return live?(x==0?owner:helper):null;}
  public TileEntity getTileEntity(int x,int y,int z){return live&&x==1?part:null;}
  public int getBlockMetadata(int x,int y,int z){return 2;}
  public boolean setBlockToAir(int x,int y,int z){live=false;removedCount++;return true;}
  public boolean func_147480_a(int x,int y,int z,boolean drops){throw new AssertionError("Duplicate destroy effect");}
 }
 static class Effects extends EffectRenderer {
  int count;EntityFX last;
  Effects(){super(null,null);}
  public void addEffect(EntityFX fx){count++;last=fx;}
 }
 static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception {
  Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);Unsafe unsafe=(Unsafe)f.get(null);
  TestWorld world=(TestWorld)unsafe.allocateInstance(TestWorld.class);
  Owner owner=(Owner)unsafe.allocateInstance(Owner.class);world.owner=owner;
  owner.icon=(IIcon)Proxy.newProxyInstance(IIcon.class.getClassLoader(),new Class[]{IIcon.class},(p,m,a)->{
   if(m.getReturnType()==float.class)return 0F;if(m.getReturnType()==int.class)return 32;return "test";
  });
  world.rand=new Random(0);f=World.class.getDeclaredField("provider");f.setAccessible(true);f.set(world,new WorldProviderSurface());
  world.helper=(BlockModelCollision)unsafe.allocateInstance(BlockModelCollision.class);
  world.part=new TileEntityModelCollision();world.part.configure(0,0,0,new float[]{0,0,0,1,1,1});world.part.setWorldObj(world);
  EntityPlayerMP player=(EntityPlayerMP)unsafe.allocateInstance(EntityPlayerMP.class);player.capabilities=new PlayerCapabilities();
  player.capabilities.isCreativeMode=true;world.live=true;
  check(world.helper.removedByPlayer(world,player,1,0,0,false)&&owner.drops==0&&world.removedCount==1,"creative helper break must be silent and drop nothing");
  player.capabilities.isCreativeMode=false;world.live=true;
  check(world.helper.removedByPlayer(world,player,1,0,0,true)&&owner.drops==1,"survival must drop owner once");
  world.isRemote=true;world.live=true;
  Method source=BlockModelCollision.class.getDeclaredMethod("particleSource",World.class,int.class,int.class,int.class);source.setAccessible(true);
  check(source.invoke(world.helper,world,1,0,0)!=null,"selection data missing");
  check(world.helper.getIcon(world,1,0,0,2)==owner.icon,"helper world icon must use owner texture");
  world.helper.removedByPlayer(world,player,1,0,0,true);check(owner.drops==1,"client spawned a drop");
  check(world.helper.getIcon(world,1,0,0,2)==owner.icon,"removed helper lost cached owner texture");
  Effects effects=(Effects)unsafe.allocateInstance(Effects.class);
  world.helper.addDestroyEffects(world,1,0,0,0,effects);check(effects.count==64,"particles lost after helper removal");
  Field particleIcon=EntityFX.class.getDeclaredField("particleIcon");particleIcon.setAccessible(true);
  check(particleIcon.get(effects.last)==owner.icon,"destroy particle does not use owner atlas icon");
  world.live=true;
  world.helper.addHitEffects(world,new MovingObjectPosition(1,0,0,5,Vec3.createVectorHelper(2,.5,.5)),effects);
  check(effects.count==65&&effects.last.posX>2,"hit particle hidden inside model");
  check(particleIcon.get(effects.last)==owner.icon,"hit particle does not use owner atlas icon");
  EntityBlockDustFX dust=(EntityBlockDustFX)unsafe.allocateInstance(EntityBlockDustFX.class);dust.worldObj=world;
  check(ru.givler.mbo.core.DecorParticleHooks.resolve(dust,"blockdust_123_0",1.5,1,.5)==dust&&particleIcon.get(dust)==owner.icon,"landing dust lost owner texture");
  particleIcon.set(dust,null);
  ru.givler.mbo.core.DecorParticleHooks.resolve(dust,"blockcrack_123_0",1.5,.1,.5);
  check(particleIcon.get(dust)==owner.icon,"sprint dust lost owner texture");
  particleIcon.set(dust,null);world.live=false;
  ru.givler.mbo.core.DecorParticleHooks.resolve(dust,"blockdust_123_0",1.5,1,.5);
  check(particleIcon.get(dust)==null,"ordinary block particle changed");
  byte[] original;
  try(java.io.InputStream input=net.minecraft.client.renderer.RenderGlobal.class.getResourceAsStream("RenderGlobal.class")) {
   java.io.ByteArrayOutputStream output=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;
   while((count=input.read(buffer))!=-1)output.write(buffer,0,count);original=output.toByteArray();
  }
  byte[] transformed=new ru.givler.mbo.core.DecorParticleTransformer().transform("net.minecraft.client.renderer.RenderGlobal","net.minecraft.client.renderer.RenderGlobal",original);
  org.objectweb.asm.tree.ClassNode node=new org.objectweb.asm.tree.ClassNode();new org.objectweb.asm.ClassReader(transformed).accept(node,0);
  int hooked=0;
  for(org.objectweb.asm.tree.MethodNode method:node.methods)if(method.name.equals("doSpawnParticle")) {
   new org.objectweb.asm.tree.analysis.Analyzer<>(new org.objectweb.asm.tree.analysis.BasicVerifier()).analyze(node.name,method);
   for(org.objectweb.asm.tree.AbstractInsnNode ins:method.instructions.toArray())if(ins.getOpcode()==org.objectweb.asm.Opcodes.ARETURN) {
    check(ins.getPrevious() instanceof org.objectweb.asm.tree.MethodInsnNode&&((org.objectweb.asm.tree.MethodInsnNode)ins.getPrevious()).owner.equals("ru/givler/mbo/core/DecorParticleHooks"),"particle return bypasses hook");hooked++;
   }
  }
  check(hooked>0,"real vanilla landing particle bytecode not patched");
  System.out.println("PASS creative no drops, survival one drop, client no drops, silent owner removal, 64 particles after TE removal and hit particles outside face");
 }
}
