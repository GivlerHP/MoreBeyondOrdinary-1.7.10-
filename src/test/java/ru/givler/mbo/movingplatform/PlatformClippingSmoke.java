package ru.givler.mbo.movingplatform;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import ru.givler.mbo.network.packet.PacketPlatformAction;

public final class PlatformClippingSmoke {
  private PlatformClippingSmoke() { }

  public static void check() {
    verifyPassengerAnimation();
    NBTTagCompound original = new NBTTagCompound();
    original.setDouble("HomeY", 100D);
    original.setInteger("SizeX", 1);
    original.setInteger("SizeY", 40);
    original.setInteger("SizeZ", 1);
    EntityMovingPlatform platform = new EntityMovingPlatform(null);
    platform.readPlatformTag(original);
    if (platform.isClipAboveSelection()) throw new AssertionError("Old elevators must keep their rendering");
    platform.setClipAboveSelection(true);
    EntityMovingPlatform restored = new EntityMovingPlatform(null);
    restored.readPlatformTag(platform.writePlatformTag());
    restored.setPosition(0, 115, 0);
    if (!restored.isClipAboveSelection() || restored.getRenderCeilingY() != 140D)
      throw new AssertionError("Saved ceiling must stay at the original selection top while moving");
    Block chain = new TestBlock();
    restored.getBlocks().add(new PlatformBlock(0, 24, 0, chain, 0));
    restored.getBlocks().add(new PlatformBlock(0, 25, 0, chain, 0));
    if (restored.isHiddenAt(139.999D) || !restored.isHiddenAt(140D)
        || restored.contains(0, 140, 0) || !restored.contains(0, 139, 0)
        || restored.hidesMaterializedBlock(chain, 0, 140, 0))
      throw new AssertionError("Hidden snapshot cells must not own or hide foreign world blocks");
    verifyCollision(restored);
    verifyWorldOperations(original, chain);
    restored.setClipAboveSelection(false);
    if (restored.hidesMaterializedBlock(chain, 0, 140, 0))
      throw new AssertionError("Disabling clipping must restore upper blocks");
    for (boolean enabled : new boolean[] {false, true}) {
      ByteBuf encoded = Unpooled.buffer();
      ByteBuf roundTrip = Unpooled.buffer();
      try {
        new PacketPlatformAction(1, PacketPlatformAction.SAVE, 0, 40, 3, 2, 0,
            "00000000-0000-0000-0000-000000000001", "", enabled).toBytes(encoded);
        int length = encoded.readableBytes();
        PacketPlatformAction decoded = new PacketPlatformAction();
        decoded.fromBytes(encoded);
        decoded.toBytes(roundTrip);
        if (roundTrip.readableBytes() != length || roundTrip.getBoolean(length - 1) != enabled)
          throw new AssertionError("Clipping option lost in the editor packet");
      } finally {
        encoded.release(); roundTrip.release();
      }
    }
    System.out.println("Elevator clipping: persistence, collision, world ownership, obstruction and editor packet passed");
  }

  private static void verifyPassengerAnimation() {
    MovingPlatformTickHandler.PassengerWalkAnimation animation=
        new MovingPlatformTickHandler.PassengerWalkAnimation(.5D,.5D,2F,0F);
    for (int tick=0;tick<60;++tick) animation.update(.5D,.5D);
    if (animation.amount!=0F || animation.phase!=2F)
      throw new AssertionError("Transport with an unchanged relative position must not animate walking");
    animation.update(.75D,.5D);
    if (Math.abs(animation.amount-.4F)>0.000001F || animation.previousAmount!=0F)
      throw new AssertionError("Walking on a platform must retain vanilla limb animation");
    float before=animation.amount;
    animation.update(.75D,.5D);
    if (Math.abs(animation.amount-before*.6F)>0.000001F || animation.previousAmount!=before)
      throw new AssertionError("Stopping on a platform must smoothly settle the walk animation");
    System.out.println("Passenger animation: stationary transport, relative walking and smooth stopping passed");
  }

  private static final class TestBlock extends Block {
    TestBlock() { super(Material.rock); }
  }

  private static void verifyCollision(EntityMovingPlatform platform) {
    try {
      java.lang.reflect.Method method = MovingPlatformTickHandler.class.getDeclaredMethod(
          "blockBox", EntityMovingPlatform.class, PlatformBlock.class);
      method.setAccessible(true);
      PlatformBlock crossing = platform.getBlocks().get(0);
      platform.setPosition(0, 115.5D, 0);
      net.minecraft.util.AxisAlignedBB box = (net.minecraft.util.AxisAlignedBB)
          method.invoke(null, platform, crossing);
      if (box == null || box.minY != 139.5D || box.maxY != 140D)
        throw new AssertionError("Crossing collision must end exactly at the ceiling");
      if (method.invoke(null, platform, platform.getBlocks().get(1)) != null)
        throw new AssertionError("Fully hidden pieces must have no collision");
      platform.setPosition(0, 114D, 0);
      box = (net.minecraft.util.AxisAlignedBB) method.invoke(null, platform, crossing);
      if (box == null || box.minY != 138D || box.maxY != 139D)
        throw new AssertionError("Descending must restore the full collision");
    } catch (ReflectiveOperationException error) {
      throw new AssertionError(error);
    }
    platform.setPosition(0, 115D, 0);
  }

  private static void verifyWorldOperations(NBTTagCompound original, Block chain) {
    try {
      java.lang.reflect.Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      TestWorld world = (TestWorld) ((sun.misc.Unsafe) field.get(null)).allocateInstance(TestWorld.class);
      world.cells = new java.util.HashMap<String, Block>();
      world.metadata = new java.util.HashMap<String, Integer>();
      verifyPaneWaterSides(world);
      verifyLightSampling(world);
      verifyPassengerTurn(world);
      verifyPlateActivation(world, original);
      EntityMovingPlatform p = new EntityMovingPlatform(null);
      p.readPlatformTag(original);
      p.worldObj = world;
      p.setPosition(0, 115, 0);
      p.getBlocks().add(new PlatformBlock(0, 24, 0, chain, 0));
      p.getBlocks().add(new PlatformBlock(0, 25, 0, chain, 0));
      world.setBlock(0, 139, 0, chain, 0, 2);
      world.setBlock(0, 140, 0, chain, 0, 2);
      java.lang.reflect.Method route = EntityMovingPlatform.class.getDeclaredMethod("routeIsClear", int.class);
      route.setAccessible(true);
      if (!((Boolean) route.invoke(p, 16)))
        throw new AssertionError("Hidden cells must not block route validation");
      if (!p.setClipAboveSelection(true) || world.getBlock(0, 140, 0) != net.minecraft.init.Blocks.air)
        throw new AssertionError("Enabling must free upper world cells");
      // Use the same type: ownership must never be inferred from block identity above the ceiling.
      world.setBlock(0, 140, 0, chain, 0, 2);
      if (p.setClipAboveSelection(false))
        throw new AssertionError("Disabling must refuse an occupied hidden cell");
      if (!invokeBoolean(p, "removeMaterializedBlocks") || world.getBlock(0, 140, 0) != chain)
        throw new AssertionError("Departure must preserve blocks in hidden cells");
      if (!invokeBoolean(p, "materialize") || world.getBlock(0, 139, 0) != chain
          || world.getBlock(0, 140, 0) != chain)
        throw new AssertionError("Arrival must materialize visible pieces only");
      world.setBlock(0, 139, 0, net.minecraft.init.Blocks.air, 0, 2);
      java.lang.reflect.Method clientArrival = EntityMovingPlatform.class.getDeclaredMethod("materializeClientBlocks");
      clientArrival.setAccessible(true);
      clientArrival.invoke(p);
      if (world.getBlock(0, 139, 0) != chain || world.getBlock(0, 140, 0) != chain)
        throw new AssertionError("Client handoff must preserve hidden world cells");
      invokeBoolean(p, "removeMaterializedBlocks");
      java.lang.reflect.Method sweep = EntityMovingPlatform.class.getDeclaredMethod(
          "sweptVisibleSpaceIsClear", double.class, double.class, double.class);
      sweep.setAccessible(true);
      if (!((Boolean) sweep.invoke(p, 0D, 116D, 0D)))
        throw new AssertionError("Blocks above ceiling must not obstruct ascent");
      world.setBlock(0, 138, 0, chain, 0, 2);
      if (((Boolean) sweep.invoke(p, 0D, 114D, 0D)))
        throw new AssertionError("Reappearing pieces must stop before an obstacle");
      world.setBlock(0, 138, 0, net.minecraft.init.Blocks.air, 0, 2);
      if (!((Boolean) sweep.invoke(p, 0D, 114D, 0D)))
        throw new AssertionError("Removing the obstacle must allow descent");
      world.setBlock(0, 140, 0, net.minecraft.init.Blocks.air, 0, 2);
      invokeBoolean(p, "materialize");
      if (!p.setClipAboveSelection(false) || world.getBlock(0, 140, 0) != chain)
        throw new AssertionError("Disabling in clear space must restore stored blocks");
    } catch (ReflectiveOperationException error) {
      throw new AssertionError(error);
    }
  }

  private static boolean invokeBoolean(EntityMovingPlatform p, String name)
      throws ReflectiveOperationException {
    java.lang.reflect.Method method = EntityMovingPlatform.class.getDeclaredMethod(name);
    method.setAccessible(true);
    return (Boolean) method.invoke(p);
  }

  private static void verifyLightSampling(TestWorld world) throws ReflectiveOperationException {
    NBTTagCompound tag=new NBTTagCompound();
    tag.setInteger("SizeX",5); tag.setInteger("SizeY",40); tag.setInteger("SizeZ",5);
    EntityMovingPlatform platform=new EntityMovingPlatform(null); platform.readPlatformTag(tag); platform.worldObj=world;
    java.lang.reflect.Method method=ru.givler.mbo.client.render.RenderMovingPlatform.class.getDeclaredMethod(
        "interpolatedLightField",EntityMovingPlatform.class,double.class,double.class,double.class);
    method.setAccessible(true);
    for (double fraction:new double[] {0D,0.375D}) {
      world.lightQueries=0;
      int[] actual=(int[])method.invoke(null,platform,10D+fraction,20D+fraction,30D+fraction);
      int queries=world.lightQueries,index=0;
      for (int x=-1;x<=5;++x) for (int y=-1;y<=40;++y) for (int z=-1;z<=5;++z) {
        double block=0D,sky=0D;
        for (int dx=0;dx<2;++dx) for (int dy=0;dy<2;++dy) for (int dz=0;dz<2;++dz) {
          double weight=(dx==0?1-fraction:fraction)*(dy==0?1-fraction:fraction)*(dz==0?1-fraction:fraction);
          int light=world.getLightBrightnessForSkyBlocks(10+x+dx,20+y+dy,30+z+dz,0);
          block+=(light&65535)*weight; sky+=(light >>> 16 & 65535)*weight;
        }
        int expected=(int)Math.round(block) | (int)Math.round(sky) << 16;
        if (actual[index++]!=expected) throw new AssertionError("Optimized light interpolation changed a vertex's packed light");
      }
      if (queries>8*43*8) throw new AssertionError("Each light coordinate must be fetched only once");
      System.out.println("Platform 5x40x5 light queries: 16464 -> "+queries+", exact interpolation retained");
    }
  }

  private static void verifyPassengerTurn(TestWorld world) throws ReflectiveOperationException {
    java.lang.reflect.Field unsafe=sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); unsafe.setAccessible(true);
    net.minecraft.entity.EntityLivingBase mob=(net.minecraft.entity.EntityLivingBase)
        ((sun.misc.Unsafe)unsafe.get(null)).allocateInstance(net.minecraft.entity.passive.EntityCow.class);
    mob.worldObj=world; mob.posX=2D; mob.posZ=3D; mob.prevPosX=1D; mob.prevPosZ=1D;
    mob.rotationYaw=45F; mob.rotationYawHead=90F;
    if (MovingPlatformTickHandler.animationPreviousX(mob)!=1D)
      throw new AssertionError("Non-passenger rotation must retain vanilla movement detection");
    java.lang.reflect.Field field=MovingPlatformTickHandler.class.getDeclaredField("ENTITY_CARRIERS"); field.setAccessible(true);
    java.util.Map map=(java.util.Map)field.get(null);
    java.util.Map carriers=new java.util.WeakHashMap(); carriers.put(mob,1); map.put(world,carriers);
    try {
      if (MovingPlatformTickHandler.animationPreviousX(mob)!=2D || MovingPlatformTickHandler.animationPreviousZ(mob)!=3D)
        throw new AssertionError("Lift transport must not put body rotation into its walking branch");
      mob.motionX=.125D; mob.motionZ=.25D;
      if (MovingPlatformTickHandler.animationPreviousX(mob)!=1.875D || MovingPlatformTickHandler.animationPreviousZ(mob)!=2.75D
          || mob.rotationYaw!=45F || mob.rotationYawHead!=90F)
        throw new AssertionError("Passenger walking and independent head rotation must remain available");
    } finally { map.remove(world); }
  }

  private static void verifyPlateActivation(TestWorld world, NBTTagCompound original)
      throws ReflectiveOperationException {
    for (Block plate : new Block[] {new TestPressurePlate(), new TestWeightedPlate()}) {
      world.cells.clear();
      world.metadata.clear();
      EntityMovingPlatform p = new EntityMovingPlatform(null);
      p.readPlatformTag(original);
      p.worldObj = world;
      p.setPosition(0, 100, 0);
      p.configure(PlatformDirection.UP.ordinal(), 1, 3, 2, 0);
      p.confirmConfiguration();
      p.getBlocks().add(new PlatformBlock(0, 0, 0, plate, 0));
      world.setBlock(0, 100, 0, plate, 0, 2);
      for (int tick = 0; tick < 21; ++tick)
        if (invokeBoolean(p, "checkOnboardControl"))
          throw new AssertionError("An unpowered plate must not start an elevator");
      // The plate's own activation already accounts for items, mobs and its sensitivity.
      world.setBlock(0, 100, 0, plate, 1, 2);
      if (!invokeBoolean(p, "checkOnboardControl") || p.getState() != EntityMovingPlatform.MOVING_TO_B)
        throw new AssertionError("A powered plate must start the elevator without a living entity");
      // Simulate arrival at the lower station with metadata reset by materialization.
      java.lang.reflect.Field state = EntityMovingPlatform.class.getDeclaredField("state");
      state.setAccessible(true);
      state.setInt(p, EntityMovingPlatform.STOPPED_A);
      world.setBlock(0, 100, 0, net.minecraft.init.Blocks.air, 0, 2);
      if (!invokeBoolean(p, "materialize")) throw new AssertionError("Arrival failed");
      if (invokeBoolean(p, "checkOnboardControl")) throw new AssertionError("Arrival must not restart");
      world.setBlock(0, 100, 0, plate, 1, 2);
      for (int tick = 0; tick < 60; ++tick)
        if (invokeBoolean(p, "checkOnboardControl"))
          throw new AssertionError("A continuously held plate must not reverse after arrival");
      world.setBlock(0, 100, 0, plate, 0, 2);
      if (invokeBoolean(p, "checkOnboardControl")) throw new AssertionError("Release must not restart");
      world.setBlock(0, 100, 0, plate, 1, 2);
      if (!invokeBoolean(p, "checkOnboardControl"))
        throw new AssertionError("A fresh press after release must start another trip");
    }
    world.cells.clear();
    world.metadata.clear();
    System.out.println("Elevator pressure plate activation, arrival guard and release/repress passed");
  }

  private static void verifyPaneWaterSides(TestWorld world) {
    Block air = new BlockForMaterial(Material.air);
    Block water = new BlockForMaterial(Material.water);
    for (boolean northSouth : new boolean[] {true, false}) {
      world.cells.clear(); world.metadata.clear();
      world.setBlock(0, 0, 0, new TestPane(northSouth), 0, 2);
      world.setBlock(-1, 0, 0, air, 0, 2); world.setBlock(1, 0, 0, air, 0, 2);
      world.setBlock(0, 0, -1, air, 0, 2); world.setBlock(0, 0, 1, air, 0, 2);
      world.setBlock(northSouth ? -1 : 0, 0, northSouth ? 0 : -1, water, 0, 2);
      boolean[] wet = ru.givler.mbo.waterlogging.WaterloggedGeometry.waterCellsForRender(world, 0, 0, 0);
      verifyPaneSideFace(world, northSouth, true);
      for (int x = 0; x < 2; x++) for (int y = 0; y < 2; y++) for (int z = 0; z < 2; z++)
        if (wet[ru.givler.mbo.waterlogging.WaterloggedGeometry.index(x, y, z)] != (northSouth ? x == 0 : z == 0))
          throw new AssertionError("One-sided water must never render in the dry half of a pane");
      world.setBlock(northSouth ? 1 : 0, 0, northSouth ? 0 : 1, water, 0, 2);
      verifyPaneSideFace(world, northSouth, false);
      for (boolean filled : ru.givler.mbo.waterlogging.WaterloggedGeometry.waterCellsForRender(world, 0, 0, 0))
        if (!filled) throw new AssertionError("Water on both sides must render the whole pane volume");
    }
    for (final int dryX : new int[] {0, 1}) for (final int dryZ : new int[] {0, 1}) {
      world.cells.clear(); world.metadata.clear();
      net.minecraft.block.BlockPane corner = new net.minecraft.block.BlockPane(
          "glass", "glass_pane_top", Material.glass, false) {
        @Override public boolean canPaneConnectTo(net.minecraft.world.IBlockAccess access,
            int x, int y, int z, net.minecraftforge.common.util.ForgeDirection side) {
          return side.offsetX == (dryX == 0 ? -1 : 1)
              || side.offsetZ == (dryZ == 0 ? -1 : 1);
        }
        @Override public void addCollisionBoxesToList(net.minecraft.world.World access,
            int x, int y, int z, net.minecraft.util.AxisAlignedBB area,
            java.util.List boxes, net.minecraft.entity.Entity entity) { }
      };
      world.setBlock(0, 0, 0, corner, 0, 2);
      world.setBlock(-1, 0, 0, air, 0, 2); world.setBlock(1, 0, 0, air, 0, 2);
      world.setBlock(0, 0, -1, air, 0, 2); world.setBlock(0, 0, 1, air, 0, 2);
      world.setBlock(dryX == 0 ? 1 : -1, 0, 0, water, 0, 2);
      world.setBlock(0, 0, dryZ == 0 ? 1 : -1, water, 0, 2);
      boolean[] wet = ru.givler.mbo.waterlogging.WaterloggedGeometry.waterCellsForRender(world, 0, 0, 0);
      for (int x = 0; x < 2; ++x) for (int y = 0; y < 2; ++y) for (int z = 0; z < 2; ++z)
        if (wet[ru.givler.mbo.waterlogging.WaterloggedGeometry.index(x, y, z)] != !(x == dryX && z == dryZ))
          throw new AssertionError("Corner water must fill three quadrants and preserve the dry inside corner");
      world.setBlock(0, 0, 0, new net.minecraft.block.BlockPane(
          "iron_bars", "iron_bars", Material.iron, true) { }, 0, 2);
      for (boolean filled : ru.givler.mbo.waterlogging.WaterloggedGeometry.waterCellsForRender(world, 0, 0, 0))
        if (!filled) throw new AssertionError("Iron rods must not partition water like glass");
      if (!ru.givler.mbo.waterlogging.WaterloggedGeometry.canReachFace(world, 0, 0, 0,
          ru.givler.mbo.waterlogging.WaterloggedGeometry.faceBit(-1, 0, 0), 1, 0, 0))
        throw new AssertionError("Water must pass through connected iron bars");
    }
    world.setBlock(-1, 0, 0, water, 0, 2);
    world.setBlock(1, 0, 0, world.getBlock(0, 0, 0), 0, 2);
    world.setBlock(2, 0, 0, air, 0, 2);
    world.setBlock(1, 0, -1, air, 0, 2); world.setBlock(1, 0, 1, air, 0, 2);
    verifyConnectedPaneSupply(world);
    world.setBlock(-1, 0, 0, air, 0, 2);
    verifyDisconnectedPaneSupply(world);
    verifyPaneCycle(world, air, water);
    world.cells.clear(); world.metadata.clear();
    System.out.println("Pane dry sides, four corner orientations and porous iron bars passed");
  }

  private static void verifyConnectedPaneSupply(TestWorld world) { verifyPaneSupply(world, true); }

  private static void verifyPaneCycle(TestWorld world, Block air, Block water) {
    net.minecraft.world.WorldProvider previousProvider = world.provider;
    boolean previousRemote = world.isRemote;
    try {
      java.lang.reflect.Field remote = net.minecraft.world.World.class.getDeclaredField("isRemote");
      remote.setAccessible(true); remote.setBoolean(world,true);
      java.lang.reflect.Field provider = net.minecraft.world.World.class.getDeclaredField("provider");
      provider.setAccessible(true); provider.set(world,new net.minecraft.world.WorldProviderSurface());
      world.cells.clear(); world.metadata.clear();
      for (int x = -1; x <= 2; ++x) for (int z = -1; z <= 1; ++z)
        world.setBlock(x,0,z,air,0,2);
      for (int x = 0; x < 2; ++x) {
        world.setBlock(x,0,0,new TestPane(false),0,2);
        world.setBlock(x,1,0,air,0,2);
        ru.givler.mbo.waterlogging.ClientWaterloggedBlocks.set(0,x,0,0,true);
      }
      world.setBlock(0,0,-1,water,0,2);
      for (int pane = 0; pane < 2; ++pane) {
        boolean[] wet = ru.givler.mbo.waterlogging.WaterloggedGeometry.waterCellsForFlow(world,pane,0,0);
        for (int x = 0; x < 2; ++x) for (int y = 0; y < 2; ++y) for (int z = 0; z < 2; ++z)
          if (wet[ru.givler.mbo.waterlogging.WaterloggedGeometry.index(x,y,z)] != (z == 0))
            throw new AssertionError("Pane cycle must carry only the wet half, regardless of traversal root");
        if (ru.givler.mbo.waterlogging.WaterloggedGeometry.canReachFace(world,pane,0,0,
            ru.givler.mbo.waterlogging.WaterloggedGeometry.faceBit(-1,0,0),0,0,1))
          throw new AssertionError("A dry pane compartment must not emit water");
      }
      for (int x = -1; x <= 2; ++x) for (int z = -1; z <= 1; ++z)
        world.setBlock(x,1,z,air,0,2);
      world.setBlock(1,1,0,water,8,2);
      if (ru.givler.mbo.client.render.WaterloggedLiquidHeightHooks.cornerHeight(world,1,0,0) != 1F
          || ru.givler.mbo.client.render.WaterloggedLiquidHeightHooks.cornerHeight(world,1,0,1) != 1F
          || ru.givler.mbo.client.render.WaterloggedLiquidHeightHooks.cornerHeight(world,0,0,0)
              != ru.givler.mbo.client.render.WaterloggedLiquidHeightHooks.sourceHeight())
        throw new AssertionError("Falling water must raise both shared edge corners, leaving the opposite edge at source height");
      world.setBlock(1,1,0,air,0,2);
      world.setBlock(0,0,-1,air,0,2);
      for (int pane = 0; pane < 2; ++pane)
        for (boolean wet : ru.givler.mbo.waterlogging.WaterloggedGeometry.waterCellsForFlow(world,pane,0,0))
          if (wet) throw new AssertionError("Connected panes without a source must not manufacture water");
      world.cells.clear(); world.metadata.clear();
      ru.givler.mbo.waterlogging.ClientWaterloggedBlocks.clear();
      int length=2048;
      for (int x=-1;x<=length;++x) for (int z=-1;z<=1;++z) world.setBlock(x,0,z,air,0,2);
      for (int x=0;x<length;++x) {
        world.setBlock(x,0,0,new TestPane(false),0,2); world.setBlock(x,1,0,air,0,2);
        ru.givler.mbo.waterlogging.ClientWaterloggedBlocks.set(0,x,0,0,true);
      }
      world.setBlock(0,0,-1,water,0,2);
      TestPane.connectionQueries=0;
      boolean scope=ru.givler.mbo.waterlogging.WaterloggedGeometry.beginScope(world);
      try {
        for (int pane=0;pane<length;++pane) {
          boolean[] wet=ru.givler.mbo.waterlogging.WaterloggedGeometry.waterCellsForFlow(world,pane,0,0);
          for (int x=0;x<2;++x) for (int y=0;y<2;++y) for (int z=0;z<2;++z)
            if (wet[ru.givler.mbo.waterlogging.WaterloggedGeometry.index(x,y,z)]!=(z==0))
              throw new AssertionError("Long pane chain changed its wet side");
        }
        if (TestPane.connectionQueries!=length*4)
          throw new AssertionError("Pane connectivity must be evaluated once per component, not once per path");
      } finally { if (scope) ru.givler.mbo.waterlogging.WaterloggedGeometry.endScope(); }
      System.out.println("2048 connected panes: 8192 connection queries, cached results, dry half preserved");
      remote.setBoolean(world,previousRemote);
    } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    finally {
      try {
        java.lang.reflect.Field provider = net.minecraft.world.World.class.getDeclaredField("provider");
        provider.setAccessible(true); provider.set(world,previousProvider);
      } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
      ru.givler.mbo.waterlogging.ClientWaterloggedBlocks.clear();
    }
  }
  private static void verifyDisconnectedPaneSupply(TestWorld world) { verifyPaneSupply(world, false); }

  private static void verifyPaneSupply(TestWorld world, boolean expected) {
    NBTTagCompound tag = new NBTTagCompound(), chunk = new NBTTagCompound();
    net.minecraft.nbt.NBTTagList chunks = new net.minecraft.nbt.NBTTagList();
    chunk.setByteArray("PositionsV2", new byte[] {0, 0, 64, 0, 32, 64});
    chunk.setByteArray("Ingress", new byte[] {1, 1});
    chunks.appendTag(chunk); tag.setTag("Chunks", chunks);
    ru.givler.mbo.waterlogging.WaterloggedWorldData data = new ru.givler.mbo.waterlogging.WaterloggedWorldData();
    data.readFromNBT(tag);
    try {
      java.lang.reflect.Method supply = ru.givler.mbo.waterlogging.WaterloggedFlowQueue.class.getDeclaredMethod(
          "hasConnectedSupply", net.minecraft.world.World.class,
          ru.givler.mbo.waterlogging.WaterloggedWorldData.class,
          int.class, int.class, int.class, int.class, int.class, int.class);
      supply.setAccessible(true);
      if ((Boolean) supply.invoke(null, world, data, 1, 0, 0, 1, 0, 0) != expected)
        throw new AssertionError("Waterlogged neighbours must supply water only while connected to a source");
    } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
  }

  private static final class BlockForMaterial extends Block {
    BlockForMaterial(Material material) { super(material); }
  }

  private static void verifyPaneSideFace(TestWorld world, boolean northSouth, boolean expected) {
    try {
      java.lang.reflect.Method face = ru.givler.mbo.client.render.WaterloggedBlockRenderer.class.getDeclaredMethod(
          "shouldRenderFace", net.minecraft.world.World.class,
          ru.givler.mbo.waterlogging.ClientWaterloggedBlocks.Position.class,
          int.class, int.class, int.class, int.class, int.class, int.class);
      face.setAccessible(true);
      Object position = new ru.givler.mbo.waterlogging.ClientWaterloggedBlocks.Position(0, 0, 0);
      boolean visible = (Boolean) face.invoke(null, world, position, 0, 0, 0,
          northSouth ? 1 : 0, 0, northSouth ? 0 : 1);
      if (visible != expected)
        throw new AssertionError("Pane water needs a side at a dry half, but none between wet halves");
    } catch (ReflectiveOperationException error) {
      throw new AssertionError(error);
    }
  }

  private static final class TestPane extends net.minecraft.block.BlockPane {
    static int connectionQueries;
    private final boolean northSouth;
    TestPane(boolean northSouth) { super("glass", "glass_pane_top", Material.glass, false); this.northSouth = northSouth; }
    @Override public boolean canPaneConnectTo(net.minecraft.world.IBlockAccess world, int x, int y, int z,
        net.minecraftforge.common.util.ForgeDirection side) {
      ++connectionQueries;
      return northSouth ? side.offsetZ != 0 : side.offsetX != 0;
    }
    @Override public void addCollisionBoxesToList(net.minecraft.world.World world, int x, int y, int z,
        net.minecraft.util.AxisAlignedBB area, java.util.List boxes, net.minecraft.entity.Entity entity) { }
  }

  private static final class TestPressurePlate extends net.minecraft.block.BlockPressurePlate {
    TestPressurePlate() { super("planks_oak", Material.wood, Sensitivity.everything); }
  }

  private static final class TestWeightedPlate extends net.minecraft.block.BlockPressurePlateWeighted {
    TestWeightedPlate() { super("gold_block", Material.iron, 15); }
  }

  // Allocate without constructing a client, chunk provider, or renderer for the headless check.
  private static final class TestWorld extends net.minecraft.client.multiplayer.WorldClient {
    java.util.Map<String, Block> cells;
    java.util.Map<String, Integer> metadata;
    int lightQueries;
    private TestWorld() { super(null, null, 0, net.minecraft.world.EnumDifficulty.NORMAL, null); }
    private String key(int x, int y, int z) { return x + ":" + y + ":" + z; }
    @Override public Block getBlock(int x, int y, int z) {
      Block b = cells.get(key(x, y, z));
      return b == null ? net.minecraft.init.Blocks.air : b;
    }
    @Override public int getBlockMetadata(int x, int y, int z) {
      Integer value = metadata.get(key(x, y, z));
      return value == null ? 0 : value;
    }
    @Override public long getTotalWorldTime() { return 0L; }
    @Override public int getLightBrightnessForSkyBlocks(int x,int y,int z,int minimum) {
      ++lightQueries;
      return ((x+y+z)&15) << 20 | ((x-y+z)&15) << 4;
    }
    @Override public boolean blockExists(int x, int y, int z) { return true; }
    @Override public boolean setBlock(int x, int y, int z, Block b, int meta, int flags) {
      cells.put(key(x, y, z), b);
      metadata.put(key(x, y, z), meta);
      return true;
    }
  }
}
