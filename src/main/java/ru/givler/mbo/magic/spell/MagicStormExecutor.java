package ru.givler.mbo.magic.spell;

import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import ru.givler.mbo.entity.magic.EntityMagicStorm;
import ru.givler.mbo.magic.api.*;

public final class MagicStormExecutor implements SpellExecutor {
  private final EntityMagicStorm.Kind kind;

  public MagicStormExecutor(EntityMagicStorm.Kind kind) {
    this.kind = kind;
  }

  @Override
  public SpellResult cast(SpellContext c) {
    Vec3 start =
        Vec3.createVectorHelper(
            c.caster().posX, c.caster().posY + c.caster().getEyeHeight(), c.caster().posZ);
    Vec3 look = c.caster().getLookVec();
    double range = 20 * c.range();
    MovingObjectPosition hit =
        c.world()
            .rayTraceBlocks(
                start,
                start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range));
    if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
      return SpellResult.PASS;
    if (!c.world().isRemote) {
      double x = hit.blockX + .5, y = hit.blockY, z = hit.blockZ + .5;
      int life;
      if (kind == EntityMagicStorm.Kind.BLIZZARD) {
        y += 1.5;
        life = (int) (600 * c.duration());
      } else {
        double dx = c.caster().posX - x, dz = c.caster().posZ - z, d = Math.sqrt(dx * dx + dz * dz);
        if (d > 0) {
          x += dx * 3 / d;
          z += dz * 3 / d;
        }
        y += 5;
        life = (int) (120 * c.duration());
      }
      c.world()
          .spawnEntityInWorld(
              new EntityMagicStorm(
                  c.world(),
                  x,
                  y,
                  z,
                  c.caster(),
                  kind,
                  life,
                  c.power(),
                  c.caster().rotationYawHead));
    }
    c.caster().swingItem();
    c.world()
        .playSoundAtEntity(
            c.caster(),
            kind == EntityMagicStorm.Kind.ARROW_RAIN ? "mbo:darkaura" : "mbo:ice",
            1,
            1);
    return SpellResult.SUCCESS;
  }
}
