package ru.givler.mbo.lighting;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import ru.givler.mbo.client.render.OpeningJsonRenderer;

/** Execute the animated model path in dark, torch-lit and sky-lit worlds for every metadata. */
public final class AnimatedTrapdoorLightingSmoke {
  public static void run() throws Exception {
    Field brightness=Tessellator.class.getDeclaredField("brightness");brightness.setAccessible(true);
    SampleTrapdoor block=new SampleTrapdoor();
    ProbeRenderer renderer=new ProbeRenderer();renderer.brightness=brightness;
    // RenderBlocks.setRenderBounds consults AO settings; supply them without opening a GL window.
    Field unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
    sun.misc.Unsafe unsafe=(sun.misc.Unsafe)unsafeField.get(null);
    net.minecraft.client.Minecraft mc=(net.minecraft.client.Minecraft)unsafe.allocateInstance(net.minecraft.client.Minecraft.class);
    mc.gameSettings=(net.minecraft.client.settings.GameSettings)unsafe.allocateInstance(net.minecraft.client.settings.GameSettings.class);
    for(Field field:RenderBlocks.class.getDeclaredFields())if(field.getType()==net.minecraft.client.Minecraft.class) {
      field.setAccessible(true);field.set(renderer,mc);
    }
    for(int meta=0;meta<16;meta++) {
      final int metadata=meta;
      renderer.blockAccess=(IBlockAccess)Proxy.newProxyInstance(IBlockAccess.class.getClassLoader(),
          new Class<?>[]{IBlockAccess.class},(proxy,method,args)-> {
            if(method.getName().equals("getBlockMetadata"))return metadata;
            if(method.getReturnType()==int.class)return 0;
            if(method.getReturnType()==boolean.class)return false;
            return null;
          });
      block.func_150117_b(meta);
      for(int light:new int[]{0,0x00F0,0xF00000,0xF000F0}) {
        block.light=renderer.expected=light;renderer.faces=0;renderer.enableAO=true;
        OpeningJsonRenderer.renderAnimatedTrapdoor(renderer,block,4,64,8);
        if(renderer.faces!=6 || !renderer.enableAO)throw new AssertionError("Face/AO scope lost");
        double[] actual={renderer.renderMinX,renderer.renderMinY,renderer.renderMinZ,renderer.renderMaxX,renderer.renderMaxY,renderer.renderMaxZ};
        double[] vanilla={block.getBlockBoundsMinX(),block.getBlockBoundsMinY(),block.getBlockBoundsMinZ(),block.getBlockBoundsMaxX(),block.getBlockBoundsMaxY(),block.getBlockBoundsMaxZ()};
        for(int i=0;i<6;i++)if(actual[i]!=vanilla[i])throw new AssertionError("Vanilla trapdoor bounds changed: "+meta);
      }
    }
    System.out.println("Animated trapdoor: actual model path preserves dark/torch/sky lightmap, all 16 vanilla bounds and AO isolation");
  }
  private static final class SampleTrapdoor extends BlockTrapDoor {
    int light;
    SampleTrapdoor(){super(Material.wood);}
    @Override public int getMixedBrightnessForBlock(IBlockAccess access,int x,int y,int z){return light;}
    @Override public int colorMultiplier(IBlockAccess access,int x,int y,int z){return 0xFFFFFF;}
  }
  private static final class ProbeRenderer extends RenderBlocks {
    Field brightness;int expected,faces;
    @Override public IIcon getBlockIcon(Block block,IBlockAccess access,int x,int y,int z,int side){return null;}
    void face(){
      try {
        if(enableAO || brightness.getInt(Tessellator.instance)!=expected)throw new AssertionError("Animated trapdoor replaced world light with AO/default light");
        faces++;
      }catch(IllegalAccessException e){throw new AssertionError(e);}
    }
    @Override public void renderFaceYNeg(Block b,double x,double y,double z,IIcon icon){face();}
    @Override public void renderFaceYPos(Block b,double x,double y,double z,IIcon icon){face();}
    @Override public void renderFaceZNeg(Block b,double x,double y,double z,IIcon icon){face();}
    @Override public void renderFaceZPos(Block b,double x,double y,double z,IIcon icon){face();}
    @Override public void renderFaceXNeg(Block b,double x,double y,double z,IIcon icon){face();}
    @Override public void renderFaceXPos(Block b,double x,double y,double z,IIcon icon){face();}
  }
}
