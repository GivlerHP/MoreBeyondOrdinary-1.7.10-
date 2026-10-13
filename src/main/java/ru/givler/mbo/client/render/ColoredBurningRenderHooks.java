package ru.givler.mbo.client.render;

import net.minecraft.block.BlockFire;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import ru.givler.mbo.registry.BlockRegistry;

/** Selects generated colored fire sprites for entity and first-person flames. */
public final class ColoredBurningRenderHooks {
  private static final String COLOR = "mboClientFireColor";
  private static final String UNTIL = "mboClientFireUntil";

  private ColoredBurningRenderHooks() {}

  public static void record(Entity entity, int color, int remaining) {
    NBTTagCompound data = entity.getEntityData();
    if (color < 0 || remaining <= 0) {
      data.setLong(UNTIL, 0);
    } else {
      data.setInteger(COLOR, color & 15);
      data.setLong(UNTIL, entity.worldObj.getTotalWorldTime() + remaining);
    }
  }

  public static IIcon entityIcon(BlockFire vanilla, int layer, Entity entity) {
    return icon(vanilla, layer, entity);
  }

  public static IIcon firstPersonIcon(BlockFire vanilla, int layer) {
    return icon(vanilla, layer, Minecraft.getMinecraft().thePlayer);
  }
  public static int color(Entity entity) {
    if (entity == null || entity.worldObj == null || !entity.isBurning()) return -1;
    NBTTagCompound data = entity.getEntityData();
    return data.getLong(UNTIL) > entity.worldObj.getTotalWorldTime() ? data.getInteger(COLOR) & 15 : -1;
  }

  private static IIcon icon(BlockFire vanilla, int layer, Entity entity) {
    int color = color(entity);
    if (color >= 0) {
        IIcon colored = BlockRegistry.ColourFire.getColoredFireIcon(color, layer);
        if (colored != null) return colored;
    }
    return vanilla.getFireIcon(layer);
  }
}
