package ru.givler.mbo.dungeon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import ru.givler.mbo.editor.AreaSelection;
import ru.givler.mbo.movingplatform.PlatformBlock;
import ru.givler.mbo.network.packet.PacketDungeonAreaSync;

public final class DungeonAreaSavedData extends WorldSavedData {
  private static final String NAME = "mbo_dungeon_areas";
  private final List<DungeonAreaRecord> areas = new ArrayList<DungeonAreaRecord>();
  private final Map<String, DungeonAreaRecord> byId = new HashMap<String, DungeonAreaRecord>();
  private final Map<Long, List<DungeonAreaRecord>> byChunk =
      new HashMap<Long, List<DungeonAreaRecord>>();

  public DungeonAreaSavedData() {
    super(NAME);
  }

  public DungeonAreaSavedData(String name) {
    super(name);
  }

  public static DungeonAreaSavedData get(World world) {
    DungeonAreaSavedData data =
        (DungeonAreaSavedData) world.perWorldStorage.loadData(DungeonAreaSavedData.class, NAME);
    if (data == null) {
      data = new DungeonAreaSavedData();
      world.perWorldStorage.setData(NAME, data);
    }
    return data;
  }

  public List<DungeonAreaRecord> all() {
    return areas;
  }

  public DungeonAreaRecord byId(String id) {
    return id == null ? null : byId.get(id);
  }

  public DungeonAreaRecord at(int x, int y, int z) {
    List<DungeonAreaRecord> candidates = byChunk.get(chunkKey(x >> 4, z >> 4));
    if (candidates != null)
      for (DungeonAreaRecord r : candidates) if (r.occupies(x, y, z)) return r;
    return null;
  }

  public DungeonAreaRecord create(
      World world,
      EntityPlayer player,
      AreaSelection area,
      int type,
      int restoreMode,
      int restoreSeconds) {
    if (area == null
        || area.volume() > 32768L
        || type < 0
        || type > 3
        || type == DungeonAreaRecord.COLLIDER) return null;
    List<PlatformBlock> found = new ArrayList<PlatformBlock>();
    if (type == DungeonAreaRecord.TRIGGER) {
      int sx = area.maxX - area.minX + 1,
          sy = area.maxY - area.minY + 1,
          sz = area.maxZ - area.minZ + 1;
      DungeonAreaRecord made =
          new DungeonAreaRecord(
              world.provider.dimensionId,
              area.minX,
              area.minY,
              area.minZ,
              sx,
              sy,
              sz,
              type,
              restoreMode,
              restoreSeconds,
              found,
              new int[0]);
      areas.add(made);
      rebuildIndex();
      changed(world);
      return made;
    }
    for (int x = area.minX; x <= area.maxX; x++)
      for (int y = area.minY; y <= area.maxY; y++)
        for (int z = area.minZ; z <= area.maxZ; z++) {
          if (!world.blockExists(x, y, z)) {
            player.addChatMessage(new ChatComponentTranslation("mbo.dungeon.area.unloaded"));
            return null;
          }
          Block block = world.getBlock(x, y, z);
          if (block == null || block == Blocks.air) continue;
          int meta = world.getBlockMetadata(x, y, z);
          TileEntity tile = world.getTileEntity(x, y, z);
          AxisAlignedBB box = block.getCollisionBoundingBoxFromPool(world, x, y, z);
          boolean full =
              box != null
                  && close(box.minX, x)
                  && close(box.minY, y)
                  && close(box.minZ, z)
                  && close(box.maxX, x + 1D)
                  && close(box.maxY, y + 1D)
                  && close(box.maxZ, z + 1D);
          if (tile != null
              || block.hasTileEntity(meta)
              || !block.renderAsNormalBlock()
              || !block.isOpaqueCube()
              || !full) {
            player.addChatMessage(
                new ChatComponentTranslation("mbo.dungeon.area.unsupported", x, y, z));
            return null;
          }
          found.add(new PlatformBlock(x - area.minX, y - area.minY, z - area.minZ, block, meta));
        }
    if (found.isEmpty()) {
      player.addChatMessage(new ChatComponentTranslation("mbo.dungeon.area.empty"));
      return null;
    }
    int sx = area.maxX - area.minX + 1,
        sy = area.maxY - area.minY + 1,
        sz = area.maxZ - area.minZ + 1;
    int[] light = captureLight(world, area.minX, area.minY, area.minZ, sx, sy, sz);
    DungeonAreaRecord made =
        new DungeonAreaRecord(
            world.provider.dimensionId,
            area.minX,
            area.minY,
            area.minZ,
            sx,
            sy,
            sz,
            type,
            restoreMode,
            restoreSeconds,
            found,
            light);
    for (DungeonAreaRecord old : areas)
      if (made.overlaps(old)) {
        player.addChatMessage(new ChatComponentTranslation("mbo.dungeon.area.overlap"));
        return null;
      }
    if (type == 0)
      for (PlatformBlock b : found)
        world.setBlock(area.minX + b.x, area.minY + b.y, area.minZ + b.z, Blocks.air, 0, 3);
    areas.add(made);
    rebuildIndex();
    changed(world);
    return made;
  }

  public void remove(World world, DungeonAreaRecord area) {
    if (areas.remove(area)) {
      rebuildIndex();
      changed(world);
    }
  }

  public void changed(World world) {
    markDirty();
    PacketDungeonAreaSync.broadcast(world, this);
  }

  public void tick(World world) {
    boolean dirty = false, removed = false;
    for (Iterator<DungeonAreaRecord> it = areas.iterator(); it.hasNext(); ) {
      DungeonAreaRecord r = it.next();
      int before = r.runtimeState();
      if (r.tick(world)) {
        it.remove();
        dirty = removed = true;
      } else if (before != r.runtimeState()) dirty = true;
    }
    if (removed) rebuildIndex();
    if (dirty) changed(world);
  }

  private static int[] captureLight(World w, int ox, int oy, int oz, int sx, int sy, int sz) {
    int[] a = new int[(sx + 2) * (sy + 2) * (sz + 2)];
    int i = 0;
    for (int x = -1; x <= sx; x++)
      for (int y = -1; y <= sy; y++)
        for (int z = -1; z <= sz; z++)
          a[i++] = w.getLightBrightnessForSkyBlocks(ox + x, oy + y, oz + z, 0);
    return a;
  }

  @Override
  public void readFromNBT(NBTTagCompound tag) {
    areas.clear();
    NBTTagList list = tag.getTagList("Areas", 10);
    for (int i = 0; i < list.tagCount(); i++) {
      DungeonAreaRecord r = DungeonAreaRecord.read(list.getCompoundTagAt(i));
      if (r.getType() == DungeonAreaRecord.TRIGGER || !r.getBlocks().isEmpty()) areas.add(r);
    }
    rebuildIndex();
  }

  @Override
  public void writeToNBT(NBTTagCompound tag) {
    NBTTagList list = new NBTTagList();
    for (DungeonAreaRecord r : areas) list.appendTag(r.write());
    tag.setTag("Areas", list);
  }

  private void rebuildIndex() {
    byId.clear();
    byChunk.clear();
    for (DungeonAreaRecord r : areas) {
      byId.put(r.idString(), r);
      java.util.HashSet<Long> chunks = new java.util.HashSet<Long>();
      for (PlatformBlock b : r.getBlocks())
        chunks.add(chunkKey((r.getX() + b.x) >> 4, (r.getZ() + b.z) >> 4));
      for (Long key : chunks) {
        List<DungeonAreaRecord> list = byChunk.get(key);
        if (list == null) {
          list = new ArrayList<DungeonAreaRecord>();
          byChunk.put(key, list);
        }
        list.add(r);
      }
    }
  }

  private static long chunkKey(int x, int z) {
    return ((long) x << 32) ^ (z & 0xffffffffL);
  }

  private static boolean close(double a, double b) {
    return Math.abs(a - b) < 0.0001D;
  }
}
