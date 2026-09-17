package ru.givler.mbo.magic.spell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import ru.givler.mbo.entity.magic.EntityLightningArc;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

/** Shared direct and chained lightning logic. */
public final class LightningArcExecutor implements SpellExecutor {
  public enum Kind {
    ARC,
    CHAIN
  }

  private final Kind kind;

  public LightningArcExecutor(Kind kind) {
    this.kind = kind;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    double range = (kind == Kind.ARC ? 8.0D : 10.0D) * context.range();
    EntityLivingBase primary = SpellTargeting.livingInSight(context.caster(), range);
    if (primary == null) return SpellResult.PASS;

    arc(context, context.caster(), primary);
    damage(context, primary, (kind == Kind.ARC ? 3.0F : 10.0F) * context.power());
    if (kind == Kind.CHAIN) chain(context, primary);
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }

  private void chain(SpellContext context, EntityLivingBase primary) {
    Set<EntityLivingBase> used = new HashSet<EntityLivingBase>();
    used.add(context.caster());
    used.add(primary);
    List<EntityLivingBase> secondary = nearby(context, primary, used, 5);
    for (EntityLivingBase target : secondary) {
      used.add(target);
      arc(context, primary, target);
      damage(context, target, 8.0F * context.power());
    }
    for (EntityLivingBase source : secondary) {
      List<EntityLivingBase> tertiary = nearby(context, source, used, 2);
      for (EntityLivingBase target : tertiary) {
        used.add(target);
        arc(context, source, target);
        damage(context, target, 6.0F * context.power());
      }
    }
  }

  @SuppressWarnings("unchecked")
  private List<EntityLivingBase> nearby(
      SpellContext context, EntityLivingBase center, Set<EntityLivingBase> excluded, int limit) {
    List<EntityLivingBase> found =
        context
            .world()
            .getEntitiesWithinAABB(EntityLivingBase.class, center.boundingBox.expand(5, 5, 5));
    List<EntityLivingBase> result = new ArrayList<EntityLivingBase>();
    for (EntityLivingBase target : found) {
      if (excluded.contains(target)
          || !target.isEntityAlive()
          || target == context.caster()
          || context.caster().isOnSameTeam(target)
          || center.getDistanceSqToEntity(target) > 25.0D) continue;
      result.add(target);
      if (result.size() == limit) break;
    }
    return result;
  }

  private void damage(SpellContext context, EntityLivingBase target, float amount) {
    if (!context.world().isRemote) {
      target.attackEntityFrom(
          DamageSource.causeIndirectMagicDamage(context.caster(), context.caster()), amount);
    }
  }

  private void arc(SpellContext context, EntityLivingBase from, EntityLivingBase to) {
    if (!context.world().isRemote) {
      context
          .world()
          .spawnEntityInWorld(
              new EntityLightningArc(
                  context.world(),
                  from.posX,
                  from.posY + from.height * 0.55D,
                  from.posZ,
                  to.posX,
                  to.posY + to.height * 0.5D,
                  to.posZ,
                  3));
    }
    context
        .world()
        .playSoundAtEntity(to, "mbo:arc", 1.0F, context.world().rand.nextFloat() * 0.4F + 1.5F);
  }
}
