package ru.givler.mbo.magic.spell;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

/** Focuses a non-destructive blast on the first block in the caster's sight. */
public final class DetonateExecutor implements SpellExecutor {
  @Override
  @SuppressWarnings("unchecked")
  public SpellResult cast(SpellContext context) {
    double range = 16.0D * context.range();
    Vec3 start =
        Vec3.createVectorHelper(
            context.caster().posX,
            context.caster().posY + context.caster().getEyeHeight(),
            context.caster().posZ);
    Vec3 look = context.caster().getLookVec();
    MovingObjectPosition hit =
        context
            .world()
            .rayTraceBlocks(
                start,
                start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range));
    if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
      return SpellResult.PASS;
    }
    double x = hit.blockX + 0.5D;
    double y = hit.blockY + 0.5D;
    double z = hit.blockZ + 0.5D;
    double radius = 3.0D * context.area();
    if (!context.world().isRemote) {
      List<EntityLivingBase> targets =
          context
              .world()
              .getEntitiesWithinAABB(
                  EntityLivingBase.class,
                  AxisAlignedBB.getBoundingBox(
                      x - radius, y - radius, z - radius, x + radius, y + radius, z + radius));
      for (EntityLivingBase target : targets) {
        double distance = target.getDistance(x, y, z);
        if (distance > radius) continue;
        float damage = Math.max(0.0F, 12.0F - (float) distance * 4.0F) * context.power();
        if (damage > 0.0F)
          target.attackEntityFrom(
              new EntityDamageSource("mbo.blast", context.caster()).setMagicDamage(), damage);
      }
      context
          .world()
          .playSoundEffect(
              x,
              y,
              z,
              "random.explode",
              4.0F,
              (1.0F + (context.world().rand.nextFloat() - context.world().rand.nextFloat()) * 0.2F)
                  * 0.7F);
    } else {
      context.world().spawnParticle("hugeexplosion", x, y, z, 0, 0, 0);
      for (int i = 1; i < 5; i++) {
        double progress = i / 5.0D;
        for (int particle = 0; particle < 2; particle++)
          context
              .world()
              .spawnParticle(
                  "flame",
                  start.xCoord
                      + (x - start.xCoord) * progress
                      + context.world().rand.nextFloat() / 5.0F,
                  start.yCoord
                      + (y - start.yCoord) * progress
                      + context.world().rand.nextFloat() / 5.0F,
                  start.zCoord
                      + (z - start.zCoord) * progress
                      + context.world().rand.nextFloat() / 5.0F,
                  0,
                  0,
                  0);
      }
    }
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
