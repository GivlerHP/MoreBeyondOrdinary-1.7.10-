package ru.givler.mbo.magic.spell;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class IgniteExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    double range = 10.0D * context.range();
    EntityLivingBase target = context.target();
    if (target == null) target = SpellTargeting.livingInSight(context.caster(), range);
    if (target != null) {
      if (!context.world().isRemote) target.setFire(Math.max(1, (int) (10 * context.duration())));
      flameLine(context, target.posX, target.posY + target.height * 0.5D, target.posZ);
      finish(context);
      return SpellResult.SUCCESS;
    }
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
    if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
      return SpellResult.PASS;
    int x = hit.blockX, y = hit.blockY, z = hit.blockZ;
    switch (hit.sideHit) {
      case 0:
        y--;
        break;
      case 1:
        y++;
        break;
      case 2:
        z--;
        break;
      case 3:
        z++;
        break;
      case 4:
        x--;
        break;
      case 5:
        x++;
        break;
      default:
        break;
    }
    if (!context.world().isAirBlock(x, y, z)) return SpellResult.PASS;
    if (!context.world().isRemote) context.world().setBlock(x, y, z, Blocks.fire);
    flameLine(context, x + 0.5D, y + 0.5D, z + 0.5D);
    finish(context);
    return SpellResult.SUCCESS;
  }

  private static void flameLine(SpellContext context, double tx, double ty, double tz) {
    if (!context.world().isRemote) return;
    double sx = context.caster().posX, sy = context.caster().posY + context.caster().getEyeHeight();
    double sz = context.caster().posZ;
    for (int i = 1; i < 5; i++)
      for (int n = 0; n < 2; n++) {
        double f = i / 5.0D;
        context
            .world()
            .spawnParticle(
                "flame",
                sx + (tx - sx) * f + context.world().rand.nextFloat() / 5F,
                sy + (ty - sy) * f + context.world().rand.nextFloat() / 5F,
                sz + (tz - sz) * f + context.world().rand.nextFloat() / 5F,
                0,
                0,
                0);
      }
  }

  private static void finish(SpellContext context) {
    context.caster().swingItem();
    context
        .world()
        .playSoundAtEntity(
            context.caster(), "fire.ignite", 1.0F, 0.8F + context.world().rand.nextFloat() * 0.4F);
  }
}
