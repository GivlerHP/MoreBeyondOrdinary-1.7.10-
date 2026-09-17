package ru.givler.mbo.magic.spell;

import net.minecraft.entity.Entity;
import ru.givler.mbo.entity.magic.EntityFireOrb;
import ru.givler.mbo.entity.magic.EntityFirebolt;
import ru.givler.mbo.entity.magic.EntityForceOrb;
import ru.givler.mbo.entity.magic.EntityIceCharge;
import ru.givler.mbo.entity.magic.EntityIceShard;
import ru.givler.mbo.entity.magic.EntityThunderbolt;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

/** Creates one of the native MBO elemental projectiles. */
public final class ElementalProjectileExecutor implements SpellExecutor {
  public enum Kind {
    FIREBOLT,
    ICE_SHARD,
    THUNDERBOLT,
    FORCE_ORB,
    ICE_CHARGE,
    FIREBALL,
    GREATER_FIREBALL
  }

  private final Kind kind;

  public ElementalProjectileExecutor(Kind kind) {
    this.kind = kind;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    if (!context.world().isRemote) {
      Entity projectile;
      if (kind == Kind.FIREBOLT) {
        projectile = new EntityFirebolt(context.world(), context.caster(), context.power());
        context
            .world()
            .playAuxSFX(
                1009,
                (int) context.caster().posX,
                (int) context.caster().posY,
                (int) context.caster().posZ,
                0);
      } else if (kind == Kind.ICE_SHARD) {
        projectile =
            new EntityIceShard(
                context.world(), context.caster(), 2.0F * context.range(), context.power());
      } else if (kind == Kind.THUNDERBOLT) {
        projectile = new EntityThunderbolt(context.world(), context.caster(), context.power());
      } else if (kind == Kind.FORCE_ORB) {
        projectile =
            new EntityForceOrb(context.world(), context.caster(), context.power(), context.area());
      } else if (kind == Kind.ICE_CHARGE) {
        projectile =
            new EntityIceCharge(context.world(), context.caster(), context.power(), context.area());
      } else {
        projectile =
            new EntityFireOrb(
                context.world(),
                context.caster(),
                context.power(),
                kind == Kind.GREATER_FIREBALL ? context.area() : 0.0F);
      }
      context.world().spawnEntityInWorld(projectile);
    }
    if (kind == Kind.ICE_SHARD) {
      context
          .world()
          .playSoundAtEntity(
              context.caster(), "mbo:ice", 1.0F, context.world().rand.nextFloat() * 0.4F + 1.4F);
    } else if (kind == Kind.ICE_CHARGE) {
      context
          .world()
          .playSoundAtEntity(
              context.caster(), "mbo:ice", 1.0F, context.world().rand.nextFloat() * 0.4F + 1.4F);
    } else if ((kind == Kind.FIREBALL || kind == Kind.GREATER_FIREBALL)
        && !context.world().isRemote) {
      context
          .world()
          .playAuxSFX(
              1009,
              (int) context.caster().posX,
              (int) context.caster().posY,
              (int) context.caster().posZ,
              0);
    } else if (kind == Kind.THUNDERBOLT && !context.world().isRemote) {
      context
          .world()
          .playSoundAtEntity(
              context.caster(), "mbo:ice", 0.8F, context.world().rand.nextFloat() * 0.2F + 0.8F);
    }
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
