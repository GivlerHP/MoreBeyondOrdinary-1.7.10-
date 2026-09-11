package ru.givler.mbo.dungeon;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public final class ClientDungeonAreas {
  private static int dimension = Integer.MIN_VALUE;
  private static final List<DungeonAreaRecord> areas = new ArrayList<DungeonAreaRecord>();

  private ClientDungeonAreas() {}

  public static List<DungeonAreaRecord> all() {
    return areas;
  }

  public static DungeonAreaRecord byId(String id) {
    for (DungeonAreaRecord r : areas) if (r.idString().equals(id)) return r;
    return null;
  }

  public static DungeonAreaRecord at(int x, int y, int z) {
    for (DungeonAreaRecord r : areas) if (r.occupies(x, y, z)) return r;
    return null;
  }

  public static void clear() {
    dimension = Integer.MIN_VALUE;
    areas.clear();
  }

  public static void read(int dim, NBTTagCompound tag) {
    dimension = dim;
    areas.clear();
    NBTTagList list = tag.getTagList("Areas", 10);
    for (int i = 0; i < list.tagCount(); i++)
      areas.add(DungeonAreaRecord.read(list.getCompoundTagAt(i)));
  }
}
