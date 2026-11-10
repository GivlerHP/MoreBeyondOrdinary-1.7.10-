package ru.givler.mbo.client.lighting;

import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import ru.givler.mbo.lighting.ColoredLightApi;

/** Loaded only with Thaumcraft. No polling or changes to vanilla light strength. */
public final class ThaumcraftNodeLight {
  private ThaumcraftNodeLight() {}
  public static void register() {
    if(!Block.blockRegistry.containsKey("Thaumcraft:blockAiry"))return;
    final Block airy=(Block)Block.blockRegistry.getObject("Thaumcraft:blockAiry");
    ColoredLightApi.register(airy,(world,x,y,z)->emission(airy,world,x,y,z));
    if(Block.blockRegistry.containsKey("Thaumcraft:blockManaPod")) {
      final Block pod=(Block)Block.blockRegistry.getObject("Thaumcraft:blockManaPod");
      ColoredLightApi.register(pod,(world,x,y,z)->emission(pod,world,x,y,z));
    }
    if(Block.blockRegistry.containsKey("Thaumcraft:blockJar")) {
      final Block jar=(Block)Block.blockRegistry.getObject("Thaumcraft:blockJar");
      ColoredLightApi.register(jar,(world,x,y,z)->emission(jar,world,x,y,z));
    }
  }
  private static int emission(Block block,net.minecraft.world.IBlockAccess world,int x,int y,int z) {
    int level=block.getLightValue(world,x,y,z);if(level<=0)return 0;
    TileEntity tile=world.getTileEntity(x,y,z);AspectList aspects=null;
    // getAspects() hides the bean's aspect until maturity; its visual hue exists earlier.
    if(tile instanceof thaumcraft.common.tiles.TileManaPod) {
      Aspect aspect=((thaumcraft.common.tiles.TileManaPod)tile).aspect;
      return ColoredLightApi.emission(aspect==null?0xFFFFFF:aspect.getColor(),level);
    }
    if(tile instanceof thaumcraft.api.aspects.IAspectContainer)aspects=((thaumcraft.api.aspects.IAspectContainer)tile).getAspects();
    if((aspects==null || aspects.visSize()==0) && tile instanceof INode)aspects=((INode)tile).getAspectsBase();
    if((aspects==null || aspects.visSize()==0) && tile instanceof thaumcraft.common.tiles.TileNodeEnergized)
      aspects=((thaumcraft.common.tiles.TileNodeEnergized)tile).getAuraBase();
    return ColoredLightApi.emission(color(aspects),level);
  }
  public static int color(AspectList aspects) {
    if(aspects==null)return 0xFFFFFF;
    long red=0,green=0,blue=0,total=0;
    for(Map.Entry<Aspect,Integer> entry:aspects.aspects.entrySet()) {
      if(entry.getKey()==null || entry.getValue()==null || entry.getValue()<=0)continue;
      int weight=entry.getValue(),rgb=entry.getKey().getColor();
      total+=weight;red+=(long)(rgb>>16&255)*weight;green+=(long)(rgb>>8&255)*weight;blue+=(long)(rgb&255)*weight;
    }
    return total==0?0xFFFFFF:(int)(red/total)<<16|(int)(green/total)<<8|(int)(blue/total);
  }
  public static void updated(TileEntity tile) {
    if(tile instanceof thaumcraft.api.aspects.IAspectContainer && tile.getWorldObj()!=null && tile.getWorldObj().isRemote)
      ColoredLightClient.INSTANCE.changed(tile.xCoord,tile.yCoord,tile.zCoord);
  }
}
