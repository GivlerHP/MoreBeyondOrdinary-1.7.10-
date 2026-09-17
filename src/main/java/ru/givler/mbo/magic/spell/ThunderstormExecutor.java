package ru.givler.mbo.magic.spell;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.*;
import ru.givler.mbo.entity.magic.EntityLightningArc;
import ru.givler.mbo.magic.api.*;

public final class ThunderstormExecutor implements SpellExecutor {
  @Override
  @SuppressWarnings("unchecked")
  public SpellResult cast(SpellContext c) {
    int cx = (int) Math.floor(c.caster().posX),
        cy = (int) Math.floor(c.caster().posY),
        cz = (int) Math.floor(c.caster().posZ);
    if (!c.world().canBlockSeeTheSky(cx, cy, cz)) return SpellResult.PASS;
    for (int n = 0; n < 10; n++) {
      double r = 4 + c.world().rand.nextDouble() * 6 * c.area(),
          a = c.world().rand.nextDouble() * Math.PI * 2,
          x = c.caster().posX + r * Math.cos(a),
          z = c.caster().posZ + r * Math.sin(a);
      int y = c.world().getTopSolidOrLiquidBlock((int) x, (int) z);
      if (!c.world().isRemote)
        c.world().addWeatherEffect(new EntityLightningBolt(c.world(), x, y, z));
      List<EntityLivingBase> secondary =
          c.world()
              .getEntitiesWithinAABB(
                  EntityLivingBase.class,
                  AxisAlignedBB.getBoundingBox(x - 10, y - 9, z - 10, x + 10, y + 11, z + 10));
      Set<EntityLivingBase> used = new HashSet<EntityLivingBase>(secondary);
      for (EntityLivingBase t : secondary)
        if (valid(c, t)) {
          arc(c, x, y + 1, z, t);
          damage(c, t, 10);
          List<EntityLivingBase> third =
              c.world()
                  .getEntitiesWithinAABB(EntityLivingBase.class, t.boundingBox.expand(10, 10, 10));
          int count = 0;
          for (EntityLivingBase q : third)
            if (!used.contains(q) && valid(c, q) && count++ < 3) {
              arc(c, t.posX, t.posY + t.height / 2, t.posZ, q);
              damage(c, q, 8);
            }
        }
    }
    return SpellResult.SUCCESS;
  }

  private static boolean valid(SpellContext c, EntityLivingBase t) {
    return t != c.caster() && t.isEntityAlive() && !c.caster().isOnSameTeam(t);
  }

  private static void arc(SpellContext c, double x, double y, double z, EntityLivingBase t) {
    if (!c.world().isRemote)
      c.world()
          .spawnEntityInWorld(
              new EntityLightningArc(c.world(), x, y, z, t.posX, t.posY + t.height / 2, t.posZ, 3));
    c.world().playSoundAtEntity(t, "mbo:arc", 1, 1.5F + c.world().rand.nextFloat() * .4F);
  }

  private static void damage(SpellContext c, EntityLivingBase t, float amount) {
    if (!c.world().isRemote)
      t.attackEntityFrom(
          new EntityDamageSource("mbo.thunderstorm", c.caster()).setMagicDamage(),
          amount * c.power());
  }
}
