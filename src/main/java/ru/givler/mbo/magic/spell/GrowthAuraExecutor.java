package ru.givler.mbo.magic.spell;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class GrowthAuraExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    boolean found = false;
    int cx = (int) context.caster().posX,
        cy = (int) context.caster().posY,
        cz = (int) context.caster().posZ;
    if (!context.world().isRemote)
      for (int dx = -2; dx < 1; dx++)
        for (int dz = -1; dz < 2; dz++) {
          int x = cx + dx, z = cz + dz, y = nearestOpenFloor(context, x, cy, z, 2) - 1;
          if (y < 0 || context.caster().getDistance(x, y, z) > 2) continue;
          Block block = context.world().getBlock(x, y, z);
          if (!(block instanceof IGrowable)) continue;
          IGrowable growable = (IGrowable) block;
          if (growable.func_149851_a(context.world(), x, y, z, false)) {
            if (growable.func_149852_a(context.world(), context.world().rand, x, y, z))
              growable.func_149853_b(context.world(), context.world().rand, x, y, z);
            context.world().playAuxSFX(2005, x, y, z, 0);
            found = true;
          }
        }
    return found ? SpellResult.SUCCESS : SpellResult.PASS;
  }

  private static int nearestOpenFloor(SpellContext c, int x, int y, int z, int range) {
    int result = -2;
    for (int i = y - range; i <= y + range; i++)
      if (c.world().isAirBlock(x, i + 1, z) && (result == -2 || i - y < result - y)) result = i;
    return result + 1;
  }
}
