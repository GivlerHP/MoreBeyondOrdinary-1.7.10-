package ru.givler.mbo.magic.spell;

import ru.givler.mbo.entity.magic.EntityMagicAura;
import ru.givler.mbo.magic.api.*;

public final class MagicAuraExecutor implements SpellExecutor {
  private final EntityMagicAura.Kind kind;

  public MagicAuraExecutor(EntityMagicAura.Kind k) {
    kind = k;
  }

  @Override
  public SpellResult cast(SpellContext c) {
    if (!c.caster().onGround) return SpellResult.PASS;
    if (!c.world().isRemote)
      c.world()
          .spawnEntityInWorld(
              new EntityMagicAura(
                  c.world(),
                  c.caster().posX,
                  c.caster().posY,
                  c.caster().posZ,
                  c.caster(),
                  kind,
                  Math.max(1, (int) (600 * c.duration())),
                  c.power()));
    if (kind == EntityMagicAura.Kind.FORCEFIELD)
      c.world().playSoundAtEntity(c.caster(), "mbo:largeaura", 1, 1);
    return SpellResult.SUCCESS;
  }
}
