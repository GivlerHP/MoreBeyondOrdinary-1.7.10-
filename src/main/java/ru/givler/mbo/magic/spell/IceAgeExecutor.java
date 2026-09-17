package ru.givler.mbo.magic.spell;

import java.util.List;
import net.minecraft.entity.*;
import net.minecraft.init.Blocks;
import ru.givler.mbo.magic.api.*;

public final class IceAgeExecutor implements SpellExecutor {
  @Override
  @SuppressWarnings("unchecked")
  public SpellResult cast(SpellContext c) {
    double r = 7 * c.area();
    if (!c.world().isRemote) {
      List<EntityLivingBase> list =
          c.world()
              .getEntitiesWithinAABB(
                  EntityLivingBase.class, c.caster().boundingBox.expand(r, r, r));
      for (EntityLivingBase e : list)
        if (e != c.caster()
            && !c.caster().isOnSameTeam(e)
            && e instanceof EntityLiving
            && e.getDistanceToEntity(c.caster()) <= r)
          IceStatueExecutor.freeze(c, (EntityLiving) e, 1200);
      int cx = (int) Math.floor(c.caster().posX),
          cy = (int) Math.floor(c.caster().posY),
          cz = (int) Math.floor(c.caster().posZ);
      for (int x = cx - 7; x <= cx + 7; x++)
        for (int z = cz - 7; z <= cz + 7; z++) {
          int y = c.world().getTopSolidOrLiquidBlock(x, z);
          double d = c.caster().getDistance(x, y, z);
          if (d < 8 && c.world().rand.nextInt((int) d * 2 + 1) < 7) {
            if (c.world().getBlock(x, y - 1, z) == Blocks.water)
              c.world().setBlock(x, y - 1, z, Blocks.ice);
            else if (c.world().getBlock(x, y - 1, z) == Blocks.lava)
              c.world().setBlock(x, y - 1, z, Blocks.obsidian);
            else if (c.world().getBlock(x, y - 1, z) == Blocks.flowing_lava)
              c.world().setBlock(x, y - 1, z, Blocks.cobblestone);
            else if (c.world().isAirBlock(x, y, z)) c.world().setBlock(x, y, z, Blocks.snow_layer);
          }
        }
    }
    c.world().playSoundAtEntity(c.caster(), "mbo:ice", .7F, 1);
    c.world().playSoundAtEntity(c.caster(), "mbo:wind", 1, 1);
    return SpellResult.SUCCESS;
  }
}
