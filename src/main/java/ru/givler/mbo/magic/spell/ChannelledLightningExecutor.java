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

/** Shared continuous lightning beam used by the ray and web spells. */
public final class ChannelledLightningExecutor implements SpellExecutor {
  public enum Kind {
    RAY,
    WEB
  }

  private final Kind kind;

  public ChannelledLightningExecutor(Kind kind) {
    this.kind = kind;
  }

  @Override
  public SpellResult cast(SpellContext context) {
    EntityLivingBase primary =
        SpellTargeting.livingInSight(context.caster(), 10.0D * context.range());
    if (!context.world().isRemote) {
      if (primary != null) {
        strike(context, context.caster(), primary, kind == Kind.RAY ? 3.0F : 5.0F);
        if (kind == Kind.WEB) spread(context, primary);
      } else if (context.ticksInUse() % 2 == 0) {
        double length = 8.0D * context.range();
        arc(
            context,
            context.caster().posX,
            context.caster().posY + 1.2D,
            context.caster().posZ,
            context.caster().posX + context.caster().getLookVec().xCoord * length,
            context.caster().posY
                + context.caster().getEyeHeight()
                + context.caster().getLookVec().yCoord * length,
            context.caster().posZ + context.caster().getLookVec().zCoord * length);
      }
      if (context.ticksInUse() == 0 || context.ticksInUse() % 20 == 0) {
        context
            .world()
            .playSoundAtEntity(
                context.caster(),
                context.ticksInUse() == 0 ? "mbo:electricitya" : "mbo:electricityb",
                1,
                1);
      }
    }
    return SpellResult.SUCCESS;
  }

  private void spread(SpellContext context, EntityLivingBase primary) {
    Set<EntityLivingBase> used = new HashSet<EntityLivingBase>();
    used.add(context.caster());
    used.add(primary);
    List<EntityLivingBase> secondary = nearby(context, primary, used, 5);
    for (EntityLivingBase target : secondary) {
      used.add(target);
      strike(context, primary, target, 4.0F);
    }
    for (EntityLivingBase source : secondary) {
      for (EntityLivingBase target : nearby(context, source, used, 2)) {
        used.add(target);
        strike(context, source, target, 3.0F);
      }
    }
  }

  private void strike(
      SpellContext context, EntityLivingBase from, EntityLivingBase target, float damage) {
    if (context.ticksInUse() % 2 == 0) {
      arc(
          context,
          from.posX,
          from.posY + from.height * 0.6D,
          from.posZ,
          target.posX,
          target.posY + target.height * 0.5D,
          target.posZ);
    }
    double x = target.motionX, y = target.motionY, z = target.motionZ;
    target.attackEntityFrom(
        DamageSource.causeIndirectMagicDamage(context.caster(), context.caster()),
        damage * context.power());
    target.motionX = x;
    target.motionY = y;
    target.motionZ = z;
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
          || context.caster().isOnSameTeam(target)
          || center.getDistanceSqToEntity(target) > 25.0D) continue;
      result.add(target);
      if (result.size() >= limit) break;
    }
    return result;
  }

  private void arc(
      SpellContext context, double sx, double sy, double sz, double ex, double ey, double ez) {
    context
        .world()
        .spawnEntityInWorld(new EntityLightningArc(context.world(), sx, sy, sz, ex, ey, ez, 2));
  }
}
