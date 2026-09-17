package ru.givler.mbo.magic.spell;

import net.minecraft.util.*;
import ru.givler.mbo.entity.magic.EntityIceSpike;
import ru.givler.mbo.magic.api.*;

public final class IceSpikesExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext c) {
    Vec3
        s =
            Vec3.createVectorHelper(
                c.caster().posX, c.caster().posY + c.caster().getEyeHeight(), c.caster().posZ),
        l = c.caster().getLookVec();
    double r = 20 * c.range();
    MovingObjectPosition h =
        c.world().rayTraceBlocks(s, s.addVector(l.xCoord * r, l.yCoord * r, l.zCoord * r));
    if (h == null || h.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || h.sideHit != 1)
      return SpellResult.PASS;
    if (!c.world().isRemote)
      for (int i = 0; i < (int) (18 * c.area()); i++) {
        double a = c.world().rand.nextDouble() * Math.PI * 2,
            d = .5 + c.world().rand.nextDouble() * 2 * c.area(),
            x = h.blockX + Math.sin(a) * d,
            z = h.blockZ + Math.cos(a) * d;
        int bx = MathHelper.floor_double(x),
            bz = MathHelper.floor_double(z),
            y = findFloor(c, bx, h.blockY, bz);
        if (y >= 0)
          c.world()
              .spawnEntityInWorld(
                  new EntityIceSpike(
                      c.world(), x, y, z, c.caster(), 30 + c.world().rand.nextInt(15), c.power()));
      }
    c.caster().swingItem();
    c.world().playSoundAtEntity(c.caster(), "mbo:ice", 1, 1);
    return SpellResult.SUCCESS;
  }

  private static int findFloor(SpellContext c, int x, int y, int z) {
    int best = -1, bestDistance = Integer.MAX_VALUE;
    for (int floorY = y - 2; floorY <= y + 2; floorY++) {
      if (c.world().doesBlockHaveSolidTopSurface(c.world(), x, floorY, z)
          && (c.world().isAirBlock(x, floorY + 1, z)
              || !c.world().doesBlockHaveSolidTopSurface(c.world(), x, floorY + 1, z))) {
        int distance = Math.abs(floorY - y);
        if (distance < bestDistance) {
          best = floorY;
          bestDistance = distance;
        }
      }
    }
    return best;
  }
}
