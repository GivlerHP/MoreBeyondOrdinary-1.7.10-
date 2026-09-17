package ru.givler.mbo.magic.spell;

import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class LightningBoltExecutor implements SpellExecutor {
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
                start, start.addVector(look.xCoord * 200, look.yCoord * 200, look.zCoord * 200));
    if (hit == null
        || hit.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK
        || !context.world().canBlockSeeTheSky(hit.blockX, hit.blockY + 1, hit.blockZ)) {
      return SpellResult.PASS;
    }
    if (!context.world().isRemote) {
      EntityLightningBolt bolt =
          new EntityLightningBolt(context.world(), hit.blockX, hit.blockY, hit.blockZ);
      bolt.getEntityData().setString("mboCaster", context.caster().getUniqueID().toString());
      context.world().addWeatherEffect(bolt);
    }
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
