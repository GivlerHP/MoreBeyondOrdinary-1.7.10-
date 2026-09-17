package ru.givler.mbo.magic.spell;

import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import ru.givler.mbo.entity.magic.EntityGroundMagicEffect;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class DecayGroundExecutor implements SpellExecutor {
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
                    look.xCoord * 12 * context.range(),
                    look.yCoord * 12 * context.range(),
                    look.zCoord * 12 * context.range()));
    if (hit == null || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
      return SpellResult.PASS;
    }
    if (!context.world().isRemote) {
      spawn(context, hit.blockX + 0.5D, hit.blockY + 1.01D, hit.blockZ + 0.5D);
      for (int i = 0; i < 5; i++) {
        int x = hit.blockX + context.world().rand.nextInt(9) - 4;
        int z = hit.blockZ + context.world().rand.nextInt(9) - 4;
        int surface = context.world().getTopSolidOrLiquidBlock(x, z);
        if (Math.abs(surface - hit.blockY) < 6) spawn(context, x + 0.5D, surface + 0.01D, z + 0.5D);
      }
    }
    context.world().playSoundAtEntity(context.caster(), "mob.wither.shoot", 1.0F, 0.8F);
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }

  private static void spawn(SpellContext context, double x, double y, double z) {
    context
        .world()
        .spawnEntityInWorld(
            new EntityGroundMagicEffect(
                context.world(),
                x,
                y,
                z,
                context.caster(),
                EntityGroundMagicEffect.Kind.DECAY,
                400,
                context.power()));
  }
}
