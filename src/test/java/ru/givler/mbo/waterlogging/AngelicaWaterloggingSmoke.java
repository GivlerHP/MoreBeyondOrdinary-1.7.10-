package ru.givler.mbo.waterlogging;

import java.lang.reflect.*;
import java.util.Set;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.*;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.core.WaterloggingRenderHooks;
import ru.givler.mbo.client.render.WaterloggedPaneChunkHooks;
import ru.givler.mbo.client.render.WaterloggedLiquidHeightHooks;

/** No Angelica or GL classes are required: the absent-mod path is tested by the same suite. */
public final class AngelicaWaterloggingSmoke {
  static class Pane extends BlockPane { Pane(){super("glass","glass",Material.glass,false);} }
  static class Water extends BlockStaticLiquid { Water(){super(Material.water);} }
  static class View implements IBlockAccess,WaterloggingRenderHooks.ClientView {
    Set<ClientWaterloggedBlocks.Position> snapshot;
    View(Set<ClientWaterloggedBlocks.Position> s){snapshot=s;}
    public boolean mbo$isWaterlogged(int x,int y,int z){return ClientWaterloggedBlocks.snapshotContains(snapshot,x,y,z);}
    public boolean mbo$hasWaterlogging(){return ClientWaterloggedBlocks.snapshotHasWater(snapshot);}
    public Block getBlock(int x,int y,int z){throw new AssertionError("Dry worker accessed terrain through water height override");}
    public TileEntity getTileEntity(int x,int y,int z){return null;}
    public int getLightBrightnessForSkyBlocks(int x,int y,int z,int n){return n;}
    public int getBlockMetadata(int x,int y,int z){return 0;}
    public int isBlockProvidingPowerTo(int x,int y,int z,int side){return 0;}
    public boolean isAirBlock(int x,int y,int z){return false;}
    public BiomeGenBase getBiomeGenForCoords(int x,int z){return null;}
    public int getHeight(){return 256;}
    public boolean extendedLevelsInChunkCache(){return false;}
    public boolean isSideSolid(int x,int y,int z,ForgeDirection side,boolean defaultValue){return defaultValue;}
  }
  public static void run() {
    try {
      Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);sun.misc.Unsafe unsafe=(sun.misc.Unsafe)f.get(null);
      WorldClient world=(WorldClient)unsafe.allocateInstance(WorldClient.class);
      Field provider=World.class.getDeclaredField("provider");provider.setAccessible(true);
      WorldProvider p=new WorldProviderSurface();p.dimensionId=77;provider.set(world,p);
      Field remote=World.class.getDeclaredField("isRemote");remote.setAccessible(true);remote.setBoolean(world,true);
      ClientWaterloggedBlocks.clear();ClientWaterloggedBlocks.set(77,-1,-64,-1,true);
      ClientWaterloggedBlocks.set(77,17,-47,17,true);ClientWaterloggedBlocks.set(77,18,-64,0,true);
      final Set<ClientWaterloggedBlocks.Position> snapshot=ClientWaterloggedBlocks.snapshotForRender(world,-2,-66,-2);
      if(snapshot.size()!=2 || !ClientWaterloggedBlocks.snapshotContains(snapshot,-1,-64,-1)
          || ClientWaterloggedBlocks.snapshotContains(snapshot,18,-64,0))throw new AssertionError("Section halo or negative Y lost");
      ClientWaterloggedBlocks.set(77,-1,-64,-1,false);
      if(!ClientWaterloggedBlocks.snapshotContains(snapshot,-1,-64,-1))throw new AssertionError("Published mesh snapshot changed with packet");
      try {snapshot.clear();throw new AssertionError("Mutable snapshot");}catch(UnsupportedOperationException expected){}
      View wet=new View(snapshot),dry=new View(java.util.Collections.<ClientWaterloggedBlocks.Position>emptySet());
      if(!WaterloggingRenderHooks.isWaterlogged(wet,-1,-64,-1) || WaterloggingRenderHooks.isWaterlogged(dry,-1,-64,-1))
        throw new AssertionError("Non-World/ChunkCache view ignored");
      Pane pane=new Pane();Water water=new Water();
      if(!WaterloggedPaneChunkHooks.filterAngelicaPass(false,pane,1,wet,-1,-64,-1)
          || WaterloggedPaneChunkHooks.filterAngelicaPass(false,pane,0,wet,-1,-64,-1)
          || WaterloggedPaneChunkHooks.filterAngelicaPass(false,pane,1,dry,-1,-64,-1)
          || !WaterloggedPaneChunkHooks.filterAngelicaPass(true,pane,0,dry,0,0,0))throw new AssertionError("Translucent pass selection");
      if(!WaterloggedPaneChunkHooks.deferAngelicaBlock(pane,dry) || !WaterloggedPaneChunkHooks.deferAngelicaBlock(water,wet)
          || WaterloggedPaneChunkHooks.deferAngelicaBlock(water,dry))throw new AssertionError("Worker scheduling changed dry ordinary water");
      if(WaterloggedLiquidHeightHooks.getHeightOverride(new RenderBlocks(dry),0,0,0,Material.water)!=-1F)
        throw new AssertionError("Dry worker queried live world");
      p.dimensionId=78;if(!ClientWaterloggedBlocks.snapshotForRender(world,-2,-66,-2).isEmpty())throw new AssertionError("Dimension leak");p.dimensionId=77;
      final Throwable[] failure={null};
      Thread writer=new Thread(()->{try{for(int i=0;i<2000;i++)ClientWaterloggedBlocks.set(77,2,-64,2,(i&1)==0);}catch(Throwable e){failure[0]=e;}});
      writer.start();for(int i=0;i<500;i++)ClientWaterloggedBlocks.snapshotForRender(world,-2,-66,-2);writer.join();
      if(failure[0]!=null)throw new AssertionError("Concurrent render publication",failure[0]);
      ClientWaterloggedBlocks.clear();if(!ClientWaterloggedBlocks.snapshotForRender(world,-2,-66,-2).isEmpty())throw new AssertionError("Unload retained water");
      System.out.println("Angelica water: immutable negative-height snapshot/halo, packet isolation, pass selection, safe worker routing, dry fast path, dimensions and concurrent publication PASS");
    }catch(ReflectiveOperationException | InterruptedException e){throw new AssertionError(e);}
    finally {ClientWaterloggedBlocks.clear();}
  }
}
