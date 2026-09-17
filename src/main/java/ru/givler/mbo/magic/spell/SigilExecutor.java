package ru.givler.mbo.magic.spell;

import net.minecraft.util.*;
import ru.givler.mbo.entity.magic.EntityMagicSigil;
import ru.givler.mbo.magic.api.*;

public final class SigilExecutor implements SpellExecutor {
  private final EntityMagicSigil.Kind kind;

  public SigilExecutor(EntityMagicSigil.Kind kind) {
    this.kind = kind;
  }

  @Override
  public SpellResult cast(SpellContext c) {
    double range = 10 * c.range();
    Vec3
        start =
            Vec3.createVectorHelper(
                c.caster().posX, c.caster().posY + c.caster().getEyeHeight(), c.caster().posZ),
        look = c.caster().getLookVec();
    MovingObjectPosition hit =
        c.world()
            .rayTraceBlocks(
                start,
                start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range));
    if (hit == null
        || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
        || hit.sideHit != 1) return SpellResult.PASS;
    if (!c.world().isRemote)
      c.world()
          .spawnEntityInWorld(
              new EntityMagicSigil(
                  c.world(),
                  hit.blockX + .5,
                  hit.blockY + 1,
                  hit.blockZ + .5,
                  c.caster(),
                  kind,
                  c.power()));
    c.world()
        .playSoundAtEntity(
            c.caster(),
            kind == EntityMagicSigil.Kind.FIRE
                ? "fire.ignite"
                : kind == EntityMagicSigil.Kind.FROST ? "mbo:ice" : "mbo:aura",
            1,
            kind == EntityMagicSigil.Kind.LIGHTNING ? .3F : 1);
    c.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
