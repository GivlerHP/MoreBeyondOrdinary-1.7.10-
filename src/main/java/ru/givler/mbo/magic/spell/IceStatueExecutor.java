package ru.givler.mbo.magic.spell;

import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.util.Vec3;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.magic.api.*;
import ru.givler.mbo.particles.*;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.tileentity.TileEntityPetrifiedStatue;

public final class IceStatueExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext c) {
    EntityLivingBase t =
        c.target() != null
            ? c.target()
            : SpellTargeting.livingNearSight(c.caster(), 10 * c.range(), .6);
    if (!c.world().isRemote && t instanceof EntityLiving) freeze(c, (EntityLiving) t, 400);
    else if (c.world().isRemote) draw(c);
    c.caster().swingItem();
    c.world().playSoundAtEntity(c.caster(), "mbo:ice", 1, 1.2F + c.world().rand.nextFloat() * .4F);
    return t instanceof EntityLiving || c.world().isRemote ? SpellResult.SUCCESS : SpellResult.PASS;
  }

  static boolean freeze(SpellContext c, EntityLiving t, int base) {
    int x = (int) Math.floor(t.posX),
        y = (int) Math.floor(t.posY),
        z = (int) Math.floor(t.posZ),
        n = t.height < 1.2F || t.isChild() ? 1 : t.height < 2.5F ? 2 : 3;
    for (int i = 0; i < n; i++) {
      Block b = c.world().getBlock(x, y + i, z);
      if (!b.isReplaceable(c.world(), x, y + i, z)) return false;
    }
    t.extinguish();
    t.hurtTime = 0;
    for (int i = 1; i <= n; i++) {
      c.world().setBlock(x, y + i - 1, z, BlockRegistry.FrozenStatue);
      if (c.world().getTileEntity(x, y + i - 1, z) instanceof TileEntityPetrifiedStatue)
        ((TileEntityPetrifiedStatue) c.world().getTileEntity(x, y + i - 1, z))
            .configure(t, i, n, Math.max(1, (int) (base * c.duration())));
    }
    t.setDead();
    c.world().playSoundAtEntity(t, "mbo:freeze", 1, .8F + c.world().rand.nextFloat() * .4F);
    return true;
  }

  private static void draw(SpellContext c) {
    Vec3 l = c.caster().getLookVec();
    for (int i = 1; i < 25 * c.range(); i += 2) {
      double x = c.caster().posX + l.xCoord * i / 2,
          y = c.caster().posY + c.caster().getEyeHeight() - .4 + l.yCoord * i / 2,
          z = c.caster().posZ + l.zCoord * i / 2;
      MoreBeyondOrdinary.proxy.spawnParticle(
          EnumParticleType.SNOW,
          c.world(),
          x,
          y,
          z,
          0,
          -.02,
          0,
          new ParticleSettings(20, 1, 1, 1, .6F, false));
    }
  }
}
