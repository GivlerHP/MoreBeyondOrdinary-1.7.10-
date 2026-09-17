package ru.givler.mbo.magic.spell;

import net.minecraft.util.*;
import ru.givler.mbo.entity.magic.EntityMeteor;
import ru.givler.mbo.magic.api.*;

public final class MeteorExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext c) {
    Vec3
        s =
            Vec3.createVectorHelper(
                c.caster().posX, c.caster().posY + c.caster().getEyeHeight(), c.caster().posZ),
        l = c.caster().getLookVec();
    double r = 40 * c.range();
    MovingObjectPosition h =
        c.world().rayTraceBlocks(s, s.addVector(l.xCoord * r, l.yCoord * r, l.zCoord * r));
    if (h == null
        || h.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
        || !c.world().canBlockSeeTheSky(h.blockX, h.blockY + 1, h.blockZ)) return SpellResult.PASS;
    if (!c.world().isRemote)
      c.world()
          .spawnEntityInWorld(
              new EntityMeteor(c.world(), h.blockX + .5, h.blockY + 50, h.blockZ + .5, c.area()));
    c.caster().swingItem();
    c.world().playSoundAtEntity(c.caster(), "mbo:darkaura", 3, 1);
    return SpellResult.SUCCESS;
  }
}
