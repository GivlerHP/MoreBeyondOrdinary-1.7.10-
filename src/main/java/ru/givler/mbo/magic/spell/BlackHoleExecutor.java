package ru.givler.mbo.magic.spell;

import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.entity.magic.EntityBlackHole;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class BlackHoleExecutor implements SpellExecutor {
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
                    look.xCoord * 10 * context.range(),
                    look.yCoord * 10 * context.range(),
                    look.zCoord * 10 * context.range()));
    double x, y, z;
    if (hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
      ForgeDirection side = ForgeDirection.getOrientation(hit.sideHit);
      x = hit.blockX + 0.5D + side.offsetX;
      y = hit.blockY + 0.5D + side.offsetY;
      z = hit.blockZ + 0.5D + side.offsetZ;
      if (!context
          .world()
          .isAirBlock((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z)))
        return SpellResult.PASS;
    } else {
      x = start.xCoord + look.xCoord * 8;
      y = start.yCoord + look.yCoord * 8;
      z = start.zCoord + look.zCoord * 8;
    }
    if (!context.world().isRemote)
      context
          .world()
          .spawnEntityInWorld(
              new EntityBlackHole(
                  context.world(),
                  x,
                  y,
                  z,
                  context.caster(),
                  (int) (600 * context.duration()),
                  context.power()));
    context.caster().swingItem();
    context.world().playSoundAtEntity(context.caster(), "mob.wither.spawn", 2, 0.7F);
    return SpellResult.SUCCESS;
  }
}
