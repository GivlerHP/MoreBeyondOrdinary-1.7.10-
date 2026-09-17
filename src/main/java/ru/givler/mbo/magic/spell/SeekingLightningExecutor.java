package ru.givler.mbo.magic.spell;

import ru.givler.mbo.entity.magic.EntitySeekingLightning;
import ru.givler.mbo.magic.api.*;

public final class SeekingLightningExecutor implements SpellExecutor {
  private final EntitySeekingLightning.Kind kind;

  public SeekingLightningExecutor(EntitySeekingLightning.Kind kind) {
    this.kind = kind;
  }

  @Override
  public SpellResult cast(SpellContext c) {
    if (!c.world().isRemote) {
      c.world()
          .spawnEntityInWorld(new EntitySeekingLightning(c.world(), c.caster(), kind, c.power()));
      c.world()
          .playSoundAtEntity(
              c.caster(),
              kind == EntitySeekingLightning.Kind.DISC ? "mbo:electricitya" : "mbo:aura",
              1,
              0.8F + c.world().rand.nextFloat() * 0.3F);
    }
    c.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
