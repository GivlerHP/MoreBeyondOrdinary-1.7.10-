package ru.givler.mbo.magic.spell;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.Vec3;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class WhirlwindExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    EntityLivingBase target = context.target();
    if (target == null)
      target = SpellTargeting.livingInSight(context.caster(), 10.0D * context.range());
    if (target == null) return SpellResult.PASS;
    Vec3 look = context.caster().getLookVec();
    if (!context.world().isRemote) {
      target.motionX = look.xCoord * 2.0D;
      target.motionY = look.yCoord * 2.0D + 1.0D;
      target.motionZ = look.zCoord * 2.0D;
      target.velocityChanged = true;
      if (target instanceof EntityPlayerMP)
        ((EntityPlayerMP) target)
            .playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
    } else {
      double distance = context.caster().getDistanceToEntity(target) * 0.5D;
      for (int i = 0; i < 10; i++)
        context
            .world()
            .spawnParticle(
                "cloud",
                context.caster().posX
                    + context.world().rand.nextFloat()
                    - 0.5D
                    + look.xCoord * distance,
                context.caster().posY
                    + context.caster().getEyeHeight()
                    + context.world().rand.nextFloat()
                    - 0.5D
                    + look.yCoord * distance,
                context.caster().posZ
                    + context.world().rand.nextFloat()
                    - 0.5D
                    + look.zCoord * distance,
                look.xCoord,
                look.yCoord,
                look.zCoord);
    }
    context.caster().swingItem();
    context
        .world()
        .playSoundAtEntity(
            context.caster(), "mbo:ice", 0.8F, 0.6F + context.world().rand.nextFloat() * 0.2F);
    return SpellResult.SUCCESS;
  }
}
