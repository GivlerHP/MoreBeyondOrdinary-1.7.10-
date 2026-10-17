package ru.givler.mbo.waterlogging;

import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class WaterloggedStorageSmoke {
  public static void main(String[] args) {
    NBTTagCompound root = new NBTTagCompound();
    NBTTagList chunks = new NBTTagList();
    NBTTagCompound chunk = new NBTTagCompound();
    chunk.setInteger("X", 2);
    chunk.setInteger("Z", -3);
    int low = (4 << 13) | (7 << 9) | 63;  // Y=-1
    int high = (4 << 13) | (7 << 9) | 319; // Y=255
    chunk.setByteArray("PositionsV2", new byte[] {
        (byte) (low >>> 16), (byte) (low >>> 8), (byte) low,
        (byte) (high >>> 16), (byte) (high >>> 8), (byte) high});
    chunk.setByteArray("Ingress", new byte[] {1, 2});
    chunks.appendTag(chunk);
    root.setTag("Chunks", chunks);
    NBTTagList dry = new NBTTagList();
    NBTTagCompound dryPosition = new NBTTagCompound();
    dryPosition.setInteger("X", -8); dryPosition.setInteger("Y", -1); dryPosition.setInteger("Z", 12);
    dry.appendTag(dryPosition); root.setTag("Drained", dry);

    WaterloggedWorldData data = new WaterloggedWorldData();
    data.readFromNBT(root);
    int x = 36, z = -41;
    if (!data.contains(x, -1, z) || !data.contains(x, 255, z)
        || data.contains(x, -64, z) || data.contains(x, 0, z)
        || data.ingressMask(x, -1, z) != 1 || data.ingressMask(x, 255, z) != 2)
      throw new AssertionError("Negative and positive Y aliases in waterlogged storage");
    NBTTagCompound saved = new NBTTagCompound();
    data.writeToNBT(saved);
    WaterloggedWorldData restored = new WaterloggedWorldData();
    restored.readFromNBT(saved);
    if (restored.addIngress(null,-8,-1,12,1,0,0))
      throw new AssertionError("Bucket-drained pane must remain dry after save/reload");
    if (!restored.contains(x, -1, z) || !restored.contains(x, 255, z))
      throw new AssertionError("Waterlogged storage round trip failed");

    NBTTagCompound oldRoot = new NBTTagCompound();
    NBTTagList oldChunks = new NBTTagList();
    NBTTagCompound oldChunk = new NBTTagCompound();
    oldChunk.setInteger("X", 2);
    oldChunk.setInteger("Z", -3);
    int oldPosition = (4 << 12) | (7 << 8) | 255;
    oldChunk.setByteArray("Positions", new byte[] {(byte) (oldPosition >>> 8), (byte) oldPosition});
    oldChunks.appendTag(oldChunk);
    oldRoot.setTag("Chunks", oldChunks);
    restored.readFromNBT(oldRoot);
    List<WaterloggedWorldData.Position> positions = restored.all();
    if (positions.size() != 1 || positions.get(0).y != 255
        || !restored.contains(x, 255, z) || restored.contains(x, -1, z))
      throw new AssertionError("Legacy waterlogged storage migration failed");
    verifyClientChunks();
    verifyReusableLookups();
    verifyChunkPackets();
    verifyGeometryKeys();
    verifyGeometryScopes();
    AngelicaWaterloggingSmoke.run();
    System.out.println("Waterlogged negative Y storage and migration passed");
  }
  private static void verifyGeometryScopes() {
    try {
      java.lang.reflect.Field active=WaterloggedGeometry.class.getDeclaredField("SCOPE");active.setAccessible(true);
      ThreadLocal<?> scopes=(ThreadLocal<?>)active.get(null);
      if(!WaterloggedGeometry.beginScope(null))throw new AssertionError("Missing outer geometry scope");
      Object outer=scopes.get();
      WaterloggedGeometry.cacheSupply("outer",true);
      if(WaterloggedGeometry.beginScope(null))throw new AssertionError("Same world scope must share calculation");
      java.lang.reflect.Field unsafeField=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");unsafeField.setAccessible(true);
      net.minecraft.world.World other=(net.minecraft.world.World)((sun.misc.Unsafe)unsafeField.get(null)).allocateInstance(net.minecraft.client.multiplayer.WorldClient.class);
      if(!WaterloggedGeometry.beginScope(other))throw new AssertionError("Different world must isolate geometry");
      if(WaterloggedGeometry.cachedSupply("outer")!=null)throw new AssertionError("Nested world shared geometry");
      Object inner=scopes.get();WaterloggedGeometry.cacheSupply("inner",true);
      WaterloggedGeometry.endScope();
      if(scopes.get()!=outer || !Boolean.TRUE.equals(WaterloggedGeometry.cachedSupply("outer")))throw new AssertionError("Nested scope failed to restore outer calculation");
      WaterloggedGeometry.invalidate();
      if(WaterloggedGeometry.cachedSupply("outer")!=null)throw new AssertionError("Invalidation retained water state");
      WaterloggedGeometry.endScope();
      if(scopes.get()!=null)throw new AssertionError("Completed calculation retained active scope");
      java.lang.reflect.Field world=outer.getClass().getDeclaredField("world"),previous=outer.getClass().getDeclaredField("previous");world.setAccessible(true);previous.setAccessible(true);
      if(world.get(outer)!=null || world.get(inner)!=null || previous.get(inner)!=null)throw new AssertionError("Pool retained world or nested scope");
      WaterloggedGeometry.beginScope(null);
      if(scopes.get()!=outer || WaterloggedGeometry.cachedSupply("inner")!=null)throw new AssertionError("Scope reuse failed to clear data");
      for(int i=0;i<300;i++)WaterloggedGeometry.cacheSupply("large"+i,true);
      WaterloggedGeometry.invalidate(); // Clearing entries must not conceal an oversized backing table.
      WaterloggedGeometry.endScope();WaterloggedGeometry.beginScope(null);
      if(scopes.get()==outer || WaterloggedGeometry.cachedSupply("large0")!=null)throw new AssertionError("Large geometry traversal was retained in pool");
      WaterloggedGeometry.endScope();
      System.out.println("Geometry scope pool: reuse, fresh state, nested worlds, invalidation and oversized-map disposal passed");
    }catch(ReflectiveOperationException error){throw new AssertionError(error);}
    finally {WaterloggedGeometry.endScope();}
  }

  private static void verifyReusableLookups() {
    ClientWaterloggedBlocks.clear();
    java.util.Set<ClientWaterloggedBlocks.Position> reference=new java.util.HashSet<ClientWaterloggedBlocks.Position>();
    java.util.Random random=new java.util.Random(301710);
    for(int i=0;i<8000;i++) {
      int x=random.nextInt(65)-32,y=random.nextInt(384)-64,z=random.nextInt(65)-32;
      boolean wet=random.nextBoolean();
      ClientWaterloggedBlocks.Position key=new ClientWaterloggedBlocks.Position(x,y,z);
      ClientWaterloggedBlocks.set(7,x,y,z,wet);if(wet)reference.add(key);else reference.remove(key);
      if(ClientWaterloggedBlocks.contains(7,x,y,z)!=reference.contains(key))throw new AssertionError("Reusable water lookup differs from coordinate set");
      if(ClientWaterloggedBlocks.contains(8,x,y,z))throw new AssertionError("Water lookup leaked dimension");
      ClientWaterloggedBlocks.containsCurrent(x+1,y-1,z-1);
      if(ClientWaterloggedBlocks.containsCurrent(x,y,z)!=reference.contains(key))throw new AssertionError("Lookup mutation corrupted stored positions");
    }
    if(!ClientWaterloggedBlocks.all(7).equals(reference))throw new AssertionError("Water index differs after mixed updates");
    ClientWaterloggedBlocks.clear();
    if(ClientWaterloggedBlocks.containsCurrent(0,0,0))throw new AssertionError("Clear retained water");
    System.out.println("Reusable water lookup: 8000 mixed updates, negative coordinates, independent queries and dimension isolation passed");
  }

  private static void verifyGeometryKeys() {
    try {
      java.lang.reflect.Method key=WaterloggedGeometry.class.getDeclaredMethod("cellKey",int.class,int.class,int.class);
      key.setAccessible(true);
      java.util.Set<Long> keys=new java.util.HashSet<Long>();
      for(int x:new int[]{-30000000,-17,-16,-1,0,1,16,17,30000000})
        for(int z:new int[]{-30000000,-17,-16,-1,0,1,16,17,30000000})
          for(int y:new int[]{-65,-64,-1,0,1,255,256})
            if(!keys.add((Long)key.invoke(null,x,y,z)))throw new AssertionError("Water geometry coordinate alias");
      System.out.println("Water geometry keys: world borders, negative coordinates and vertical neighbours remain distinct");
    }catch(ReflectiveOperationException error){throw new AssertionError(error);}
  }

  private static void verifyClientChunks() {
    ClientWaterloggedBlocks.clear();
    ClientWaterloggedBlocks.set(7,-1,-32,-1,true);
    ClientWaterloggedBlocks.set(7,1600,0,1600,true);
    int count=0;
    for (ClientWaterloggedBlocks.Position p:ClientWaterloggedBlocks.nearby(7,-1,-1,96)) {
      if (p.x==1600) throw new AssertionError("Distant chunk included in local water rendering");
      ++count;
    }
    if (count!=1) throw new AssertionError("Negative chunk coordinates lost water");
    ClientWaterloggedBlocks.replaceChunk(7,-1,-1,java.util.Collections.<WaterloggedWorldData.Position>emptyList());
    if (ClientWaterloggedBlocks.contains(7,-1,-32,-1) || !ClientWaterloggedBlocks.contains(7,1600,0,1600))
      throw new AssertionError("Unwatching a chunk must only clear that chunk");
    ClientWaterloggedBlocks.replaceChunk(7,-1,-1,java.util.Arrays.asList(new WaterloggedWorldData.Position(-2,-1,-2)));
    if (!ClientWaterloggedBlocks.contains(7,-2,-1,-2)) throw new AssertionError("Watching a chunk must restore water");
    ClientWaterloggedBlocks.set(8,0,0,0,true);
    count=0;
    for (ClientWaterloggedBlocks.Position p:ClientWaterloggedBlocks.nearby(8,-1,-1,96)) ++count;
    if (count!=1) throw new AssertionError("Water chunk index survived a dimension change");
    ClientWaterloggedBlocks.clear();
  }

  private static void verifyChunkPackets() {
    for (boolean chunk:new boolean[] {false,true}) {
      io.netty.buffer.ByteBuf input=io.netty.buffer.Unpooled.buffer(), output=io.netty.buffer.Unpooled.buffer();
      try {
        input.writeInt(7).writeBoolean(chunk);
        if (chunk) input.writeInt(-1).writeInt(-2);
        input.writeInt(1).writeInt(-3).writeShort(-32).writeInt(-17);
        int length=input.readableBytes();
        ru.givler.mbo.network.packet.PacketWaterloggedSnapshot packet=new ru.givler.mbo.network.packet.PacketWaterloggedSnapshot();
        packet.fromBytes(input.duplicate()); packet.toBytes(output);
        if (output.readableBytes()!=length || !io.netty.buffer.ByteBufUtil.equals(input,output))
          throw new AssertionError("Chunk-scoped water snapshot lost positions or mode");
      } finally { input.release(); output.release(); }
    }
  }
}
