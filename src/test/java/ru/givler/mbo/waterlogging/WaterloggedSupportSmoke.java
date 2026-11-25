package ru.givler.mbo.waterlogging;

import java.lang.reflect.Field;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import ru.givler.mbo.block.BlockDecorModel;
import ru.givler.mbo.block.model.BlockModelCollision;

/** Eligibility against real dependency classes, submerged multiblocks and fluid geometry. */
public final class WaterloggedSupportSmoke {
  static final class TestBlock extends Block {
    TestBlock(Material material) { super(material); }
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,Block block) {}
  }
  static final Block ROCK=new TestBlock(Material.rock), WATER=new TestBlock(Material.water);
  static final class TestTrapdoor extends net.minecraft.block.BlockTrapDoor {
    TestTrapdoor() { super(Material.wood); }
  }
  static final class TestButton extends net.minecraft.block.BlockButton {
    TestButton() { super(false); }
  }
  static final class TestSlab extends net.minecraft.block.BlockSlab {
    TestSlab() { super(false,Material.rock); }
    @Override public String func_150002_b(int meta) { return "test_slab"; }
  }
  static final class TestWorld extends WorldServer {
    Map<String,Block> cells;
    Map<String,Integer> metadata;
    TestWorld() { super(null,null,"test",0,null,null); }
    static String key(int x,int y,int z) { return x+":"+y+":"+z; }
    @Override public Block getBlock(int x,int y,int z) {
      Block block=cells.get(key(x,y,z));return block==null?ROCK:block;
    }
    @Override public int getBlockMetadata(int x,int y,int z) {
      Integer meta=metadata.get(key(x,y,z));return meta==null?0:meta;
    }
    @Override public boolean isSideSolid(int x,int y,int z,net.minecraftforge.common.util.ForgeDirection side,boolean fallback) {
      return getBlock(x,y,z)==ROCK;
    }
    @Override public boolean blockExists(int x,int y,int z) { return true; }
    @Override public int getHeight() { return 256; }
    @Override public void markBlockForUpdate(int x,int y,int z) {}
    @Override public void markBlockRangeForRenderUpdate(int a,int b,int c,int x,int y,int z) {}
    @Override public void notifyBlocksOfNeighborChange(int x,int y,int z,Block block) {}
    @Override public boolean setBlockMetadataWithNotify(int x,int y,int z,int meta,int flags) {
      return !Objects.equals(metadata.put(key(x,y,z),meta),meta);
    }
    @Override public boolean canMineBlock(net.minecraft.entity.player.EntityPlayer player,int x,int y,int z) { return true; }
    @Override public boolean checkNoEntityCollision(net.minecraft.util.AxisAlignedBB box) { return true; }
    @Override public void playSoundEffect(double x,double y,double z,String sound,float volume,float pitch) {}
    @Override public void scheduleBlockUpdate(int x,int y,int z,Block block,int delay) {}
    @Override public boolean isAirBlock(int x,int y,int z) { return false; }
    @Override public net.minecraft.tileentity.TileEntity getTileEntity(int x,int y,int z) { return null; }
  }
  static final class CubeBuilder extends net.minecraft.entity.player.EntityPlayerMP {
    CubeBuilder() { super(null, null, null, null); }
    @Override public boolean canPlayerEdit(int x,int y,int z,int side,net.minecraft.item.ItemStack stack) { return true; }
  }
  static final class TestMultiPlace extends BlockEvent.MultiPlaceEvent {
    List<BlockSnapshot> snapshots;
    TestMultiPlace() { super(null,null,null); }
    @Override public List<BlockSnapshot> getReplacedBlockSnapshots() { return snapshots; }
  }
  static void require(boolean value,String why) { if(!value) throw new AssertionError(why); }
  static void set(Object target,Class<?> owner,String name,Object value)throws Exception {
    Field field=owner.getDeclaredField(name);field.setAccessible(true);field.set(target,value);
  }
  public static void main(String[] args)throws Exception {
    Field field=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");field.setAccessible(true);
    sun.misc.Unsafe unsafe=(sun.misc.Unsafe)field.get(null);
    TestWorld world=(TestWorld)unsafe.allocateInstance(TestWorld.class);
    world.cells=new HashMap<String,Block>();
    world.metadata=new HashMap<String,Integer>();
    set(world,World.class,"provider",new WorldProviderSurface());
    set(world,World.class,"playerEntities",new ArrayList<Object>());
    set(world,World.class,"perWorldStorage",new MapStorage(null));
    String[] supported={
      "net.minecraft.block.BlockHopper", "net.minecraft.block.BlockAnvil",
      "net.minecraft.block.BlockCactus", "net.minecraft.block.BlockEndPortal",
      "net.minecraft.block.BlockEndPortalFrame", "net.minecraft.block.BlockPistonBase",
      "net.minecraft.block.BlockPistonExtension", "net.minecraft.block.BlockPistonMoving",
      "net.minecraft.block.BlockEnchantmentTable", "net.minecraft.block.BlockBrewingStand",
      "com.carpentersblocks.block.BlockCarpentersBlock",
      "com.carpentersblocks.block.BlockCarpentersSlope",
      "com.carpentersblocks.block.BlockCarpentersHatch",
      "com.carpentersblocks.block.BlockCarpentersDoor",
      "com.carpentersblocks.block.BlockCarpentersStairs",
      "com.carpentersblocks.block.BlockCarpentersBarrier",
      "noppes.npcs.blocks.BlockChair", "noppes.npcs.blocks.BlockBeam",
      "noppes.npcs.blocks.BlockTable", "noppes.npcs.blocks.BlockWeaponRack",
      "minefantasy.mf2.block.crafting.BlockAnvilMF",
      "minefantasy.mf2.block.food.BlockBerryBush",
      "minefantasy.mf2.block.decor.BlockGeoModel",
      "minefantasy.mf2.block.decor.BlockModelCollision",
      "com.jaquadro.minecraft.gardenstuff.block.BlockHeavyChain",
      "com.jaquadro.minecraft.gardenstuff.block.BlockLightChain",
      "thaumcraft.common.blocks.BlockLoot"
    };
    for(String name:supported) {
      if(name.startsWith("com.jaquadro.minecraft.gardenstuff.")) {
        // Production GardenStuff's static initializers use SRG Minecraft names;
        // inspect its real hierarchy without running it in this MCP test JVM.
        java.lang.reflect.Method kind=WaterloggedBlockSupport.class.getDeclaredMethod("optionalType",Class.class);
        kind.setAccessible(true);
        require(((Integer)kind.invoke(null,Class.forName(name,false,WaterloggedSupportSmoke.class.getClassLoader())) & 8)!=0,
            "Unsupported chain class: "+name);
        continue;
      }
      Block block=(Block)unsafe.allocateInstance(Class.forName(name));
      require(WaterloggedBlockSupport.supportsWaterlogging(block),"Unsupported block: "+name);
    }
    require(!WaterloggedBlockSupport.supportsWaterlogging(ROCK),"Full ordinary blocks must remain dry");
    ru.givler.mbo.block.BlockQuarterColumn column=new ru.givler.mbo.block.BlockQuarterColumn(
        new ru.givler.mbo.block.BlockVerticalSlab(ROCK));
    String columnPos=TestWorld.key(0,100,0);
    ru.givler.mbo.block.BlockBase greyStone=new ru.givler.mbo.block.BlockBase(
        Material.rock,"opacity_test_grey_stone","stone/stone",false);
    ru.givler.mbo.block.BlockVerticalSlab opaqueSlab=new ru.givler.mbo.block.BlockVerticalSlab(greyStone);
    world.cells.put(columnPos,opaqueSlab);
    require(opaqueSlab.getLightOpacity(world,0,100,0)==255,"Stone vertical slabs must use the source opacity");
    world.metadata.put(columnPos,4);
    require(opaqueSlab.getLightOpacity(world,0,100,0)==255,"A joined vertical slab must block light");
    require(opaqueSlab.getUseNeighborBrightness(),"Vertical slabs must render with vanilla neighbor brightness");
    world.cells.put(columnPos,column);
    for (int quarter=0;quarter<4;quarter++) {
      world.metadata.put(columnPos,quarter);
      float hitX=(quarter&1)==0?.25F:.75F,hitZ=(quarter&2)==0?.25F:.75F;
      require(column.onBlockPlaced(world,0,100,0,1,hitX,1,hitZ,0)==quarter,"Column did not select the clicked quarter");
      require(ru.givler.mbo.block.BlockVerticalSlab.shapeMask(world,0,100,0)==(1<<quarter),"Wrong solid column quarter");
      net.minecraft.util.AxisAlignedBB box=column.getCollisionBoundingBoxFromPool(world,0,100,0);
      require(box.minX==(quarter&1)*.5 && box.minZ==(quarter>>1)*.5
          && box.maxX-box.minX==.5 && box.maxZ-box.minZ==.5 && box.maxY-box.minY==1,
          "Column collision is not a full-height quarter");
      require(WaterloggedBlockSupport.canWaterlog(world,0,100,0),"Column cannot contain water in its free quarters");
      require(column.getLightOpacity(world,0,100,0)==0,"An isolated column must leave room for light");
    }
    ru.givler.mbo.block.BlockQuarterColumn cube=new ru.givler.mbo.block.BlockQuarterColumn(
        new ru.givler.mbo.block.BlockVerticalSlab(ROCK),true);
    world.cells.put(columnPos,cube);
    require(cube.getUseNeighborBrightness(),"Cube shapes must render with vanilla neighbor brightness");
    for (int meta=0;meta<8;meta++) {
      world.metadata.put(columnPos,meta);
      net.minecraft.util.AxisAlignedBB box=cube.getCollisionBoundingBoxFromPool(world,0,100,0);
      require(box.minX==(meta&1)*.5 && box.minY==100+(meta>>2)*.5 && box.minZ==(meta>>1&1)*.5
          && box.maxX-box.minX==.5 && box.maxY-box.minY==.5 && box.maxZ-box.minZ==.5,
          "Cube collision must occupy one of eight half-block positions");
      float x=(meta&1)==0?.25F:.75F,z=(meta&2)==0?.25F:.75F;
      require(cube.onBlockPlaced(world,0,100,0,(meta&4)==0?1:0,x,.5F,z,0)==meta,
          "Cube did not use the clicked quarter and half");
      require(WaterloggedBlockSupport.canWaterlog(world,0,100,0),"Cube cannot contain water around it");
      require(cube.getLightOpacity(world,0,100,0)==0,"An isolated cube must leave room for light");
    }
    world.metadata.put(columnPos,0);
    ru.givler.mbo.block.BlockQuarterColumn.ItemQuarterColumn item=
        new ru.givler.mbo.block.BlockQuarterColumn.ItemQuarterColumn(cube);
    net.minecraft.item.ItemStack stack=new net.minecraft.item.ItemStack(item,7);
    CubeBuilder builder=(CubeBuilder)unsafe.allocateInstance(CubeBuilder.class);
    float[][] placements={{5,.5F,.25F,.25F},{3,.25F,.25F,.5F},{3,.75F,.25F,.5F},
        {1,.25F,.5F,.25F},{1,.75F,.5F,.25F},{1,.25F,.5F,.75F},{1,.75F,.5F,.75F}};
    for (int i=0;i<placements.length;i++) {
      float[] hit=placements[i];
      require(item.onItemUse(stack,builder,world,0,100,0,(int)hit[0],hit[1],hit[2],hit[3]),
          "Exposed cube face refused a cube at position "+(i+1));
      require(ru.givler.mbo.block.BlockQuarterColumn.cubeMask(world,0,100,0)==(1<<(i+2))-1,
          "Adding a cube changed existing positions");
      require(stack.stackSize==6-i,"Adding a cube consumed the wrong amount");
      require(cube.getLightOpacity(world,0,100,0)==(i>=2?255:0),"A closed cube face must block light");
      if (i==1) {
        List<net.minecraft.util.AxisAlignedBB> boxes=new ArrayList<>();
        cube.addCollisionBoxesToList(world,0,100,0,net.minecraft.util.AxisAlignedBB.getBoundingBox(0,100,0,1,101,1),boxes,null);
        require(boxes.size()==3,"Three cubes must leave the fourth corner empty");
        net.minecraft.util.Vec3 start=net.minecraft.util.Vec3.createVectorHelper(.75,101.5,.75);
        net.minecraft.util.Vec3 end=net.minecraft.util.Vec3.createVectorHelper(.75,99.5,.75);
        require(cube.collisionRayTrace(world,0,100,0,start,end)==null,"Ray hit empty corner");
      }
    }
    require(!WaterloggedBlockSupport.canWaterlog(world,0,100,0),"A full cell of cubes cannot hold water");
    String cubeDataName="mbo_quarter_cubes";
    Class<ru.givler.mbo.data.world.QuarterCubeData> cubeDataType=ru.givler.mbo.data.world.QuarterCubeData.class;
    net.minecraft.nbt.NBTTagCompound savedCubes=new net.minecraft.nbt.NBTTagCompound();
    world.perWorldStorage.loadData(cubeDataType,cubeDataName).writeToNBT(savedCubes);
    ru.givler.mbo.data.world.QuarterCubeData loadedCubes=new ru.givler.mbo.data.world.QuarterCubeData(cubeDataName);
    loadedCubes.readFromNBT(savedCubes);
    world.perWorldStorage.setData(cubeDataName,loadedCubes);
    require(ru.givler.mbo.block.BlockQuarterColumn.cubeMask(world,0,100,0)==255,"Reload lost cube positions");
    require(!cube.hasTileEntity(8),"Stacked cubes must not create a tile entity");
    cube.breakBlock(world,0,100,0,cube,8);
    require(ru.givler.mbo.block.BlockQuarterColumn.cubeMask(world,0,100,0)==1,"Broken stack left stale occupancy");
    System.out.println("PASS cube stacking, empty-corner collision, full coverage, persistence and cleanup");
    world.cells.remove(columnPos);
    world.metadata.remove(columnPos);

    int extra=0;
    for(String name:new String[]{"biomesoplenty.common.blocks.BlockBones","biomesoplenty.common.blocks.BlockGrave"}) {
      try {
        Block block=(Block)unsafe.allocateInstance(Class.forName(name));
        require(WaterloggedBlockSupport.supportsWaterlogging(block),"Unsupported optional decoration: "+name);
        require(WaterloggedBlockSupport.isWaterPermeableModel(block),"Optional decoration blocks water: "+name);
        extra++;
      } catch(ClassNotFoundException absent) { System.out.println("Optional Biomes O' Plenty jar not supplied for "+name); }
    }
    require(!WaterloggedBlockSupport.supportsWaterlogging(new TestBlock(Material.lava)),"Lava must not become waterlogged");
    BlockDecorModel model=new BlockDecorModel(Material.wood,"wet_test","test","test");
    model.withMultipartCollisionBoxes(new float[][]{{0,0,0,2,1,1}});
    BlockModelCollision helper=new BlockModelCollision();
    for(Block block:new Block[]{model,helper}) {
      world.cells.put(TestWorld.key(0,64,0),block);
      for(boolean free:WaterloggedGeometry.waterCellsForFlow(world,0,64,0))
        require(free,"A model's collision envelope must not displace its water");
      require(WaterloggedGeometry.isFaceOpen(world,0,64,0,1,0,0),"Model blocked fluid ingress");
      require(WaterloggedGeometry.canReachFace(world,0,64,0,(byte)1,1,0,0),"Model blocked water supply");
      require(WaterloggedFlow.canFillFromSource(world,0,64,0,new int[]{1,0,0}),"Submerged model cannot fill");
      require(ru.givler.mbo.core.WaterloggingFlowHooks.protectsFromWaterFlow(WATER,world,0,64,0),
          "Incoming water must not destroy models/helpers");
    }
    world.cells.put(TestWorld.key(0,64,0),model);
    world.cells.put(TestWorld.key(1,64,0),helper);
    List<BlockSnapshot> snapshots=new ArrayList<BlockSnapshot>();
    for(int x=0;x<2;x++) {
      BlockSnapshot snapshot=(BlockSnapshot)unsafe.allocateInstance(BlockSnapshot.class);
      set(snapshot,BlockSnapshot.class,"x",x);set(snapshot,BlockSnapshot.class,"y",64);
      set(snapshot,BlockSnapshot.class,"z",0);set(snapshot,BlockSnapshot.class,"world",world);
      set(snapshot,BlockSnapshot.class,"replacedBlock",WATER);snapshots.add(snapshot);
    }
    TestMultiPlace event=(TestMultiPlace)unsafe.allocateInstance(TestMultiPlace.class);
    event.snapshots=snapshots;
    set(event,BlockEvent.class,"world",world);set(event,BlockEvent.class,"y",64);
    set(event,BlockEvent.PlaceEvent.class,"blockSnapshot",snapshots.get(0));
    set(event,BlockEvent.PlaceEvent.class,"placedBlock",model);
    new WaterloggingEventHandler().placeBlock(event);
    WaterloggedWorldData data=WaterloggedWorldData.get(world);
    for(int x=0;x<2;x++) require(data.contains(x,64,0)
        && (data.ingressMask(x,64,0)&WaterloggedWorldData.INTERNAL_SOURCE)!=0,
        "Replacing an isolated source must preserve water in every model/helper cell");
    WaterloggedFlowQueue.process(world);
    for(int x=0;x<2;x++) require(data.contains(x,64,0),"Submerged source was drained after placement");
    verifyTrapdoorBoundary(world);
    verifySlabBoundary(world);
    verifyDirectWaterSupply(world);
    System.out.println("PASS "+(supported.length+extra)+" vanilla/mod block types, permeable models/helpers and submerged multiblock source preservation");
  }

  private static void verifyDirectWaterSupply(TestWorld world) {
    WaterloggedWorldData data=WaterloggedWorldData.get(world);
    Block slab=new TestSlab();
    int y=80;
    for(int x=20;x<24;x++) world.cells.put(TestWorld.key(x,y,0),slab);
    world.cells.put(TestWorld.key(19,y,0),WATER);
    WaterloggedFlowQueue.scheduleAround(world,20,y,0);
    WaterloggedFlowQueue.process(world);
    require(data.contains(20,y,0),"Adjacent ordinary source must fill a slab");
    require(!data.contains(21,y,0),"Water propagated through a wet slab");

    require(data.set(world,22,y,0,true),"Cannot place a water source inside a slab");
    WaterloggedFlowQueue.process(world);
    require(data.contains(22,y,0),"Water placed directly in the slab was drained");
    require(!data.contains(21,y,0) && !data.contains(23,y,0),"Internal source flooded neighboring slabs");

    require(data.addIngress(world,23,y,0,-1,0,0),"Cannot reproduce legacy chain water");
    WaterloggedFlowQueue.process(world);
    require(!data.contains(23,y,0),"Unsupported chain water must drain when reevaluated");
    System.out.println("PASS direct water supply, contained bucket source and legacy chain drainage");
  }

  private static void verifyTrapdoorBoundary(TestWorld world)throws Exception {
    world.cells.put(TestWorld.key(0,64,0),new TestButton());
    world.cells.put(TestWorld.key(1,64,0),new TestTrapdoor());
    Class<?> renderer=ru.givler.mbo.client.render.WaterloggedBlockRenderer.class;
    java.lang.reflect.Method face=renderer.getDeclaredMethod("shouldRenderFace",World.class,
        ClientWaterloggedBlocks.Position.class,int.class,int.class,int.class,int.class,int.class,int.class);
    face.setAccessible(true);
    java.lang.reflect.Method maxY=renderer.getDeclaredMethod("cellMaxY",World.class,
        ClientWaterloggedBlocks.Position.class,int.class,int.class,int.class);
    maxY.setAccessible(true);
    ClientWaterloggedBlocks.Position button=new ClientWaterloggedBlocks.Position(0,64,0);
    ClientWaterloggedBlocks.Position hatch=new ClientWaterloggedBlocks.Position(1,64,0);
    int dimension=world.provider.dimensionId;
    ClientWaterloggedBlocks.clear();
    try {
      ClientWaterloggedBlocks.set(dimension,0,64,0,true);
      ClientWaterloggedBlocks.set(dimension,1,64,0,true);
      for(int meta=0;meta<16;meta++) {
        world.metadata.put(TestWorld.key(1,64,0),meta);
        WaterloggedGeometry.beginScope(world);
        try {
          boolean[] rendered=WaterloggedGeometry.waterCellsForRender(world,1,64,0);
          require(rendered==WaterloggedGeometry.waterCellsForRender(world,1,64,0),"Hatch render mask must be reused within a scope");
          for(boolean wet:rendered) require(wet,"Thin hatch must not create a dry half-block in rendered water");
          if((meta&4)==0) {
            boolean[] flow=WaterloggedGeometry.waterCellsForFlow(world,1,64,0);
            int blocked=(meta&8)==0?0:1;
            require(!flow[WaterloggedGeometry.index(0,blocked,0)],"Closed hatch lost its flow barrier");
            require(rendered[WaterloggedGeometry.index(0,blocked,0)],"Routing cache contaminated the rendered water mask");
          }
        } finally { WaterloggedGeometry.endScope(); }
        for(int y=0;y<2;y++) for(int z=0;z<2;z++) {
          require(!(Boolean)face.invoke(null,world,button,1,y,z,1,0,0),"False water wall facing wet hatch");
          require(!(Boolean)face.invoke(null,world,hatch,0,y,z,-1,0,0),"False water wall facing wet button");
        }
        if((meta&12)==8)
          require((Double)maxY.invoke(null,world,hatch,0,1,0)==13D/16D,"Water must stop at the upper hatch underside");
      }
      ClientWaterloggedBlocks.set(dimension,1,64,0,false);
      require((Boolean)face.invoke(null,world,button,1,0,0,1,0,0),"Actual wet/dry boundary disappeared");
      Field corners=renderer.getDeclaredField("SURFACE_CORNERS");corners.setAccessible(true);
      @SuppressWarnings("unchecked") ThreadLocal<double[]> heights=(ThreadLocal<double[]>)corners.get(null);
      java.lang.reflect.Method surface=renderer.getDeclaredMethod("surfaceY",ClientWaterloggedBlocks.Position.class,
          double.class,double.class,double.class);surface.setAccessible(true);
      heights.set(new double[]{1,1,1,1});
      try {
        require((Double)surface.invoke(null,hatch,1D,0D,13D/16D)==64+13D/16D,"Corner smoothing raised water through a closed hatch");
      } finally { heights.remove(); }
    } finally {
      ClientWaterloggedBlocks.clear();world.metadata.clear();
    }
    System.out.println("PASS button/hatch shared water faces in 16 states, real wet/dry boundary and unchanged closed-hatch flow barrier");
  }

  private static void verifySlabBoundary(TestWorld world)throws Exception {
    Class<?> renderer=ru.givler.mbo.client.render.WaterloggedBlockRenderer.class;
    java.lang.reflect.Method face=renderer.getDeclaredMethod("shouldRenderFace",World.class,
        ClientWaterloggedBlocks.Position.class,int.class,int.class,int.class,int.class,int.class,int.class);
    face.setAccessible(true);
    ClientWaterloggedBlocks.Position hatch=new ClientWaterloggedBlocks.Position(0,64,0);
    int dimension=world.provider.dimensionId;
    ClientWaterloggedBlocks.clear();
    try {
      world.cells.clear();world.cells.put(TestWorld.key(0,64,0),new TestTrapdoor());
      ClientWaterloggedBlocks.set(dimension,0,64,0,true);
      for(int[] direction:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
        int dx=direction[0],dz=direction[1];
        world.cells.put(TestWorld.key(dx,64,dz),new TestSlab());
        for(int slabMeta:new int[]{0,8}) {
          world.metadata.put(TestWorld.key(dx,64,dz),slabMeta);
          int solidHalf=slabMeta==0?0:1,freeHalf=1-solidHalf;
          for(int hatchMeta=0;hatchMeta<16;hatchMeta++) {
            world.metadata.put(TestWorld.key(0,64,0),hatchMeta);
            ClientWaterloggedBlocks.set(dimension,dx,64,dz,true);
            for(int y=0;y<2;y++) for(int other=0;other<2;other++) {
              int x=dx<0?0:dx>0?1:other,z=dz<0?0:dz>0?1:other;
              require(!(Boolean)face.invoke(null,world,hatch,x,y,z,dx,0,dz),
                  "Water curtain remains between wet hatch and slab, metadata "+hatchMeta+"/"+slabMeta);
            }
            ClientWaterloggedBlocks.set(dimension,dx,64,dz,false);
            for(int other=0;other<2;other++) {
              int x=dx<0?0:dx>0?1:other,z=dz<0?0:dz>0?1:other;
              require(!(Boolean)face.invoke(null,world,hatch,x,solidHalf,z,dx,0,dz),
                  "Opaque slab half must hide the neighboring water face even when dry");
              require((Boolean)face.invoke(null,world,hatch,x,freeHalf,z,dx,0,dz),
                  "A real water/air boundary above or below a dry slab must remain visible");
            }
          }
        }
        world.cells.remove(TestWorld.key(dx,64,dz));
      }
    } finally { ClientWaterloggedBlocks.clear();world.metadata.clear(); }
    System.out.println("PASS hatch/slab boundaries: upper/lower slabs, 16 hatch states, four sides, solid occlusion and real dry gaps");
  }
}
