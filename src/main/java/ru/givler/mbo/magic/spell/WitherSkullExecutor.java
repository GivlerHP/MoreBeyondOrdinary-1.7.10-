package ru.givler.mbo.magic.spell;

import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.util.Vec3;
import ru.givler.mbo.magic.api.*;

public final class WitherSkullExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext c) {
    Vec3 look = c.caster().getLookVec();
    if (!c.world().isRemote) {
      EntityWitherSkull skull =
          new EntityWitherSkull(c.world(), c.caster(), look.xCoord, look.yCoord, look.zCoord);
      skull.setPosition(
          c.caster().posX + look.xCoord,
          c.caster().posY + c.caster().getEyeHeight() + look.yCoord,
          c.caster().posZ + look.zCoord);
      skull.accelerationX = look.xCoord * 0.1;
      skull.accelerationY = look.yCoord * 0.1;
      skull.accelerationZ = look.zCoord * 0.1;
      c.world().spawnEntityInWorld(skull);
      c.world()
          .playSoundAtEntity(
              c.caster(), "mob.wither.shoot", 1, c.world().rand.nextFloat() * 0.2F + 1);
    }
    c.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
