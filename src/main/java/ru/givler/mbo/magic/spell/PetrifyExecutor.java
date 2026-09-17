package ru.givler.mbo.magic.spell;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.tileentity.TileEntityPetrifiedStatue;

public final class PetrifyExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    // The visual ray reaches 12.5 blocks. A generous margin compensates for the
    // server receiving the player's rotation a tick later than the client.
    EntityLivingBase target =
        SpellTargeting.livingNearSight(context.caster(), 12.5D * context.range(), 1.35D);
    if (!context.world().isRemote && target instanceof EntityLiving) {
      petrify(context, (EntityLiving) target);
    } else if (context.world().isRemote) {
      draw(context);
    }
    context.caster().swingItem();
    context
        .world()
        .playSoundAtEntity(
            context.caster(),
            "mob.wither.spawn",
            1,
            context.world().rand.nextFloat() * 0.2F + 1.0F);
    return SpellResult.SUCCESS;
  }

  private static void petrify(SpellContext context, EntityLiving target) {
    int x = (int) Math.floor(target.posX);
    int y = (int) Math.floor(target.posY);
    int z = (int) Math.floor(target.posZ);
    int sections = target.height < 1.2F || target.isChild() ? 1 : target.height < 2.5F ? 2 : 3;
    for (int section = 0; section < sections; section++) {
      Block block = context.world().getBlock(x, y + section, z);
      if (!block.isReplaceable(context.world(), x, y + section, z)) return;
    }

    target.extinguish();
    int lifetime = Math.max(1, (int) (900 * context.duration()));
    for (int section = 1; section <= sections; section++) {
      context.world().setBlock(x, y + section - 1, z, BlockRegistry.PetrifiedStatue);
      if (context.world().getTileEntity(x, y + section - 1, z)
          instanceof TileEntityPetrifiedStatue) {
        ((TileEntityPetrifiedStatue) context.world().getTileEntity(x, y + section - 1, z))
            .configure(target, section, sections, lifetime);
      }
    }
    target.setDead();
  }

  private static void draw(SpellContext context) {
    Vec3 look = context.caster().getLookVec();
    for (int i = 1; i < 25 * context.range(); i += 2) {
      double x = context.caster().posX + look.xCoord * i / 2.0D;
      double y =
          context.caster().posY + context.caster().getEyeHeight() - 0.4D + look.yCoord * i / 2.0D;
      double z = context.caster().posZ + look.zCoord * i / 2.0D;
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.DARK_MAGIC,
          context.world(),
          x,
          y,
          z,
          0,
          0,
          0,
          new ParticleSettings(12, 0.1F, 0.1F, 0.1F, 0.7F, false));
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SPARKLE,
          context.world(),
          x,
          y,
          z,
          0,
          0,
          0,
          new ParticleSettings(
              12 + context.world().rand.nextInt(8), 0.2F, 0.2F, 0.2F, 0.7F, false));
    }
  }
}
