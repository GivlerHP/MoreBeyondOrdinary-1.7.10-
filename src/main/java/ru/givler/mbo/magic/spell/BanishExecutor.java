package ru.givler.mbo.magic.spell;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;

public final class BanishExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    EntityLivingBase target = context.target();
    if (target == null)
      target = SpellTargeting.livingInSight(context.caster(), 10.0D * context.range());
    if (context.world().isRemote) drawBeam(context, target);
    if (target != null && !context.world().isRemote) teleport(context, target);
    context.world().playSoundAtEntity(context.caster(), "mob.endermen.portal", 1.0F, 1.0F);
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }

  private static void teleport(SpellContext context, EntityLivingBase target) {
    World world = context.world();
    for (int attempt = 0; attempt < 16; attempt++) {
      double radius = (8.0D + world.rand.nextDouble() * 8.0D) * context.range();
      double angle = world.rand.nextDouble() * Math.PI * 2.0D;
      int x = MathHelper.floor_double(target.posX + Math.sin(angle) * radius);
      int z = MathHelper.floor_double(target.posZ - Math.cos(angle) * radius);
      int top = Math.min(world.getHeight() - 3, MathHelper.floor_double(target.posY + radius));
      int bottom = Math.max(1, MathHelper.floor_double(target.posY - radius));
      for (int y = top; y >= bottom; y--) {
        if (world.getBlock(x, y, z).getMaterial().blocksMovement()
            && !world.getBlock(x, y + 1, z).getMaterial().blocksMovement()
            && !world.getBlock(x, y + 2, z).getMaterial().blocksMovement()) {
          target.setPositionAndUpdate(x + 0.5D, y + 1.0D, z + 0.5D);
          world.playSoundAtEntity(target, "mob.endermen.portal", 1.0F, 1.0F);
          return;
        }
      }
    }
  }

  private static void drawBeam(SpellContext context, EntityLivingBase target) {
    Vec3 look = context.caster().getLookVec();
    double distance =
        target == null ? 10.0D * context.range() : context.caster().getDistanceToEntity(target);
    for (double d = 0.5D; d < distance; d += 1.0D) {
      double x = context.caster().posX + look.xCoord * d;
      double y = context.caster().posY + context.caster().getEyeHeight() - 0.4D + look.yCoord * d;
      double z = context.caster().posZ + look.zCoord * d;
      context.world().spawnParticle("portal", x, y - 0.5D, z, 0, 0, 0);
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.DARK_MAGIC,
          context.world(),
          x,
          y,
          z,
          0,
          0,
          0,
          new ParticleSettings(16, 0.2F, 0, 0.2F, 0.8F, false));
    }
    if (target != null)
      for (int i = 0; i < 10; i++)
        context
            .world()
            .spawnParticle(
                "portal",
                target.posX,
                target.boundingBox.minY + target.height * context.world().rand.nextFloat(),
                target.posZ,
                context.world().rand.nextDouble() - 0.5D,
                context.world().rand.nextDouble() - 0.5D,
                context.world().rand.nextDouble() - 0.5D);
  }
}
