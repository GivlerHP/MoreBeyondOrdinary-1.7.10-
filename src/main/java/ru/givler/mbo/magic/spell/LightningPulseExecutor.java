package ru.givler.mbo.magic.spell;

import cpw.mods.fml.common.network.NetworkRegistry;
import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.DamageSource;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.magic.PacketLightningArc;

public final class LightningPulseExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    if (!context.caster().onGround) return SpellResult.PASS;
    if (!context.world().isRemote) pulse(context);
    context.caster().swingItem();
    context.world().playSoundAtEntity(context.caster(), "mbo:electricitya", 1, 1);
    context.world().playSoundAtEntity(context.caster(), "mbo:boom", 2, 1);
    return SpellResult.SUCCESS;
  }

  @SuppressWarnings("unchecked")
  private void pulse(SpellContext context) {
    double radius = 3.0D * context.area();
    List<EntityLivingBase> targets =
        context
            .world()
            .getEntitiesWithinAABB(
                EntityLivingBase.class,
                context.caster().boundingBox.expand(radius, radius, radius));
    for (EntityLivingBase target : targets) {
      if (target == context.caster()
          || context.caster().isOnSameTeam(target)
          || context.caster().getDistanceSqToEntity(target) > radius * radius) continue;
      target.attackEntityFrom(
          DamageSource.causeIndirectMagicDamage(context.caster(), context.caster()),
          8.0F * context.power());
      double dx = target.posX - context.caster().posX;
      double dz = target.posZ - context.caster().posZ;
      double length = Math.sqrt(dx * dx + dz * dz);
      if (length > 0.001D) {
        target.motionX = 0.8D * dx / length;
        target.motionY = 0;
        target.motionZ = 0.8D * dz / length;
        target.velocityChanged = true;
        if (target instanceof EntityPlayerMP) {
          ((EntityPlayerMP) target)
              .playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
        }
      }
    }
    for (int i = 0; i < 24; i++) {
      double angle = Math.PI * 2 * i / 24.0D;
      double ex = context.caster().posX + Math.cos(angle) * radius;
      double ez = context.caster().posZ + Math.sin(angle) * radius;
      PacketManager.INSTANCE.sendToAllAround(
          new PacketLightningArc(
              context.caster().posX,
              context.caster().boundingBox.minY + 0.05D,
              context.caster().posZ,
              ex,
              context.caster().boundingBox.minY + 0.05D,
              ez),
          new NetworkRegistry.TargetPoint(
              context.world().provider.dimensionId,
              context.caster().posX,
              context.caster().posY,
              context.caster().posZ,
              64));
    }
  }
}
