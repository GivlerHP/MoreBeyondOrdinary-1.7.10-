package ru.givler.mbo.magic.spell;

import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class BlinkExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
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
                start.addVector(
                    look.xCoord * 25 * context.range(),
                    look.yCoord * 25 * context.range(),
                    look.zCoord * 25 * context.range()));
    if (hit == null
        || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
        || hit.sideHit == 0) return SpellResult.PASS;
    ForgeDirection side = ForgeDirection.getOrientation(hit.sideHit);
    double x = hit.blockX + 0.5D + side.offsetX;
    double y = hit.blockY + side.offsetY;
    double z = hit.blockZ + 0.5D + side.offsetZ;
    if (hit.sideHit > 1) {
      x = hit.blockX + 0.5D + side.offsetX * 0.5D;
      z = hit.blockZ + 0.5D + side.offsetZ * 0.5D;
    }
    if (!safe(context, x, y, z)) return SpellResult.PASS;
    portalParticles(context);
    context.world().playSoundAtEntity(context.caster(), "mob.endermen.portal", 1, 1);
    if (!context.world().isRemote) context.caster().setPositionAndUpdate(x, y, z);
    context.world().playSoundAtEntity(context.caster(), "mob.endermen.portal", 1, 1);
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }

  static boolean safe(SpellContext context, double x, double y, double z) {
    int bx = (int) Math.floor(x), by = (int) Math.floor(y), bz = (int) Math.floor(z);
    return !context.world().getBlock(bx, by, bz).getMaterial().blocksMovement()
        && !context.world().getBlock(bx, by + 1, bz).getMaterial().blocksMovement();
  }

  static void portalParticles(SpellContext context) {
    if (!context.world().isRemote) return;
    for (int i = 0; i < 10; i++)
      context
          .world()
          .spawnParticle(
              "portal",
              context.caster().posX,
              context.caster().posY + context.world().rand.nextDouble() * 2,
              context.caster().posZ,
              context.world().rand.nextDouble() - 0.5D,
              context.world().rand.nextDouble() - 0.5D,
              context.world().rand.nextDouble() - 0.5D);
  }
}
