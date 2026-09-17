package ru.givler.mbo.magic.spell;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.EntityDamageSource;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class ShockwaveExecutor implements SpellExecutor {
  @Override
  @SuppressWarnings("unchecked")
  public SpellResult cast(SpellContext context) {
    double radius = 5 * context.area();
    List<EntityLivingBase> targets =
        context
            .world()
            .getEntitiesWithinAABB(
                EntityLivingBase.class,
                context.caster().boundingBox.expand(radius, radius, radius));
    for (EntityLivingBase target : targets) {
      if (target == context.caster() || context.caster().isOnSameTeam(target)) continue;
      double distance = Math.max(1.0D, target.getDistanceToEntity(context.caster()));
      target.attackEntityFrom(
          new EntityDamageSource("mbo.shockwave", context.caster()).setMagicDamage().setExplosion(),
          Math.min(8.0F / (float) distance, 8.0F) * context.power());
      if (!context.world().isRemote) {
        double factor =
            Math.min(5.0D / Math.max(1.0D, target.getDistanceSqToEntity(context.caster())), 3.0D);
        target.motionX = factor * (target.posX - context.caster().posX);
        target.motionY = factor * (target.posY + 1 - context.caster().posY);
        target.motionZ = factor * (target.posZ - context.caster().posZ);
        target.velocityChanged = true;
        if (target instanceof EntityPlayerMP)
          ((EntityPlayerMP) target)
              .playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
      }
    }
    if (context.world().isRemote) {
      context
          .world()
          .spawnParticle(
              "largeexplode",
              context.caster().posX,
              context.caster().boundingBox.minY + 0.1D,
              context.caster().posZ,
              0,
              0,
              0);
      for (int i = 0; i < 80; i++) {
        double x = context.caster().posX - 1 + 2 * context.world().rand.nextDouble();
        double z = context.caster().posZ - 1 + 2 * context.world().rand.nextDouble();
        context
            .world()
            .spawnParticle(
                "magicCrit",
                x,
                context.caster().boundingBox.minY + 0.1D,
                z,
                x - context.caster().posX,
                0,
                z - context.caster().posZ);
      }
    }
    context.caster().swingItem();
    context.world().playSoundAtEntity(context.caster(), "mbo:boom", 1.0F, 0.7F);
    context.world().playSoundAtEntity(context.caster(), "mbo:boom", 2.0F, 0.3F);
    return SpellResult.SUCCESS;
  }
}
