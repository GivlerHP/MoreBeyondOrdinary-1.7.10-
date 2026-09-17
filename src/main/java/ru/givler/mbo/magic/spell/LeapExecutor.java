package ru.givler.mbo.magic.spell;

import net.minecraft.util.Vec3;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class LeapExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    if (!context.caster().onGround) return SpellResult.PASS;
    Vec3 look = context.caster().getLookVec();
    context.caster().motionY = 0.65D * context.power();
    context.caster().addVelocity(look.xCoord * 0.3D, 0, look.zCoord * 0.3D);
    context.caster().velocityChanged = true;
    if (context.world().isRemote)
      for (int i = 0; i < 10; i++) {
        context
            .world()
            .spawnParticle(
                "cloud",
                context.caster().posX + context.world().rand.nextFloat() - 0.5F,
                context.caster().boundingBox.minY,
                context.caster().posZ + context.world().rand.nextFloat() - 0.5F,
                0,
                0,
                0);
      }
    context.world().playSoundAtEntity(context.caster(), "mob.enderdragon.wings", 0.5F, 1.0F);
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
