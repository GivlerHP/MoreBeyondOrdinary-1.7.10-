package ru.givler.mbo.movingplatform;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

public final class PlatformMotionSmoke {
  static class Item extends EntityItem {
    Item() { super(null); }
    @Override public void moveEntity(double x, double y, double z) {
      setPosition(posX+x, posY+y, posZ+z);
    }
  }

  public static void check() throws Exception {
    EntityMovingPlatform platform = new EntityMovingPlatform(null);
    NBTTagCompound tag = new NBTTagCompound();
    tag.setInteger("SizeX", 1); tag.setInteger("SizeZ", 1);
    tag.setInteger("SizeY", 160); platform.readPlatformTag(tag);
    platform.setPosition(10, -100, 30);
    platform.lastTickPosX=10; platform.lastTickPosY=-99; platform.lastTickPosZ=30;
    AxisAlignedBB visual=ru.givler.mbo.client.render.PlatformVisibilityHooks.renderBounds(platform);
    if (visual.minY!=-100 || visual.maxY!=61 || visual.minX!=10 || visual.maxZ!=31
        || platform.boundingBox.maxY-platform.boundingBox.minY>2 || platform.getBoundingBox()!=null)
      throw new AssertionError("Lift chain visibility must cover the whole selection without a solid shaft collision");

    Block floor = new Block(Material.rock) {};
    platform.getTopBlocks().add(new PlatformBlock(0,0,0,floor,0));
    Method carry=MovingPlatformTickHandler.class.getDeclaredMethod("processEntity",
        Entity.class,EntityMovingPlatform.class,Map.class,Map.class,boolean.class);
    carry.setAccessible(true);
    for (double direction : new double[]{-1,1}) {
      platform.prevPosX=0; platform.prevPosY=0; platform.prevPosZ=0;
      platform.setPosition(direction*.5, -.5, 0);
      Item item=new Item(); item.setPosition(direction>0?.05:.95, 1+item.yOffset, .5);
      double initialX=item.posX;
      item.prevPosY=item.posY; item.motionY=-.04; item.motionX=.1; item.motionZ=.05;
      Map<Entity,Integer> carriers=new HashMap<>(); carriers.put(item,platform.getEntityId());
      carry.invoke(null,item,platform,carriers,null,false);
      if (Math.abs(item.posX-initialX-direction*.5)>1E-6 || Math.abs(item.boundingBox.minY-.5)>1E-6
          || !item.onGround || item.motionY!=0 || Math.abs(item.motionX-.1*floor.slipperiness)>1E-6)
        throw new AssertionError("Dropped item lost the descending floor at its horizontal trailing edge or missed ground friction");
      platform.prevPosX=platform.posX; platform.prevPosY=platform.posY;
      double groundedMotion=item.motionX;
      carry.invoke(null,item,platform,carriers,null,false);
      if (item.motionX!=groundedMotion)
        throw new AssertionError("Already grounded items must not receive duplicate friction");
    }
    verifyAngelica();
    System.out.println("Platform motion: full chain visual bounds, separate physics, dropped-item edge transport, descent and material friction passed");
  }

  private static void verifyAngelica() throws Exception {
    String name="com.gtnewhorizons.angelica.rendering.celeritas.CeleritasWorldRenderer";
    try (InputStream in=PlatformMotionSmoke.class.getClassLoader().getResourceAsStream(name.replace('.','/')+".class")) {
      if (in==null) throw new AssertionError("Angelica renderer missing from visibility verification");
      ClassWriter original=new ClassWriter(0); new ClassReader(in).accept(original,0);
      ru.givler.mbo.core.PlatformVisibilityTransformer transformer=new ru.givler.mbo.core.PlatformVisibilityTransformer();
      byte[] patched=transformer.transform(name,name,original.toByteArray());
      ClassNode node=new ClassNode(); new ClassReader(patched).accept(node,0);
      int hooks=0;
      for (MethodNode method:node.methods) if (method.name.equals("isEntityVisible")) {
        new Analyzer<>(new BasicVerifier()).analyze(node.name,method);
        for (AbstractInsnNode ins:method.instructions.toArray())
          if (ins instanceof MethodInsnNode && ((MethodInsnNode)ins).name.equals("renderBounds")) hooks++;
      }
      if (hooks!=1 || !java.util.Arrays.equals(patched,transformer.transform(name,name,patched)))
        throw new AssertionError("Actual Angelica entity visibility hook absent or not idempotent");
    }
  }
}
