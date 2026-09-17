package ru.givler.mbo.magic.spell;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

public final class FreezeExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    double range = 10.0D * context.range();
    EntityLivingBase target = context.target();
    if (target == null) target = SpellTargeting.livingInSight(context.caster(), range);
    if (target != null) {
      if (!context.world().isRemote) {
        if (target instanceof EntityBlaze || target instanceof EntityMagmaCube) {
          target.attackEntityFrom(
              new EntityDamageSource("mbo.frost", context.caster()).setMagicDamage(),
              3.0F * context.power());
        }
        target.extinguish();
        SyncedPotionEffects.apply(
            target,
            new PotionEffect(
                PotionRegistry.Frost.id, Math.max(1, (int) (200 * context.duration())), 1, true));
      }
      snowLine(context, target.posX, target.posY + target.height * 0.5D, target.posZ);
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
    Block block = context.world().getBlock(x, y, z);
    boolean changed =
        block == Blocks.water
            || block == Blocks.flowing_water
            || block == Blocks.lava
            || block == Blocks.flowing_lava;
    if (!context.world().isRemote) {
      if (block == Blocks.water || block == Blocks.flowing_water) {
        context.world().setBlock(x, y, z, Blocks.ice);
      } else if (block == Blocks.lava) {
        context.world().setBlock(x, y, z, Blocks.obsidian);
      } else if (block == Blocks.flowing_lava) {
        context.world().setBlock(x, y, z, Blocks.cobblestone);
      } else if (hit.sideHit == 1
          && block.getMaterial().isSolid()
          && context.world().isAirBlock(x, y + 1, z)) {
        context.world().setBlock(x, y + 1, z, Blocks.snow_layer);
        changed = true;
      }
    } else if (hit.sideHit == 1
        && block.getMaterial().isSolid()
        && context.world().isAirBlock(x, y + 1, z)) changed = true;
    if (!changed) return SpellResult.PASS;
    snowLine(context, x + 0.5D, y + 0.8D, z + 0.5D);
    finish(context);
    return SpellResult.SUCCESS;
  }

  private static void snowLine(SpellContext context, double tx, double ty, double tz) {
    if (!context.world().isRemote) return;
    double sx = context.caster().posX, sy = context.caster().posY + context.caster().getEyeHeight();
    double sz = context.caster().posZ;
    for (int i = 1; i < 10; i++) {
      double f = i / 10.0D;
      double x = sx + (tx - sx) * f, y = sy + (ty - sy) * f, z = sz + (tz - sz) * f;
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SNOW,
          context.world(),
          x,
          y,
          z,
          0,
          -0.02D,
          0,
          new ParticleSettings(20, 1, 1, 1, 0.6F, false));
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SPARKLE,
          context.world(),
          x,
          y,
          z,
          0,
          0,
          0,
          new ParticleSettings(16, 0.65F, 0.9F, 1.0F, 0.7F, false));
    }
  }

  private static void finish(SpellContext context) {
    context.caster().swingItem();
    context
        .world()
        .playSoundAtEntity(
            context.caster(), "mbo:ice", 1.0F, 0.8F + context.world().rand.nextFloat() * 0.4F);
  }
}
