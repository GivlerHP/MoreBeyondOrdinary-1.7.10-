package ru.givler.mbo.magic.spell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import ru.givler.mbo.entity.magic.EntitySpiritHorse;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class SummonSpiritHorseExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    if (!(context.caster() instanceof EntityPlayer)) return SpellResult.BLOCKED;
    EntityPlayer player = (EntityPlayer) context.caster();
    if (hasHorse(player)) return SpellResult.PASS;

    if (!context.world().isRemote) {
      double x = player.posX + context.world().rand.nextDouble() * 4.0D - 2.0D;
      double z = player.posZ + context.world().rand.nextDouble() * 4.0D - 2.0D;
      int floor =
          nearestFloor(
              context,
              MathHelper.floor_double(x),
              MathHelper.floor_double(player.posY),
              MathHelper.floor_double(z));
      if (floor < 0) return SpellResult.PASS;

      EntitySpiritHorse horse = new EntitySpiritHorse(context.world());
      horse.setLocationAndAngles(x, Math.max(player.posY, floor), z, player.rotationYaw, 0.0F);
      horse.setTamedBy(player);
      horse.setHorseSaddled(true);
      horse.setHealth(horse.getMaxHealth());
      context.world().spawnEntityInWorld(horse);
    }
    context
        .world()
        .playSoundAtEntity(
            player, "mbo:heal", 0.7F, context.world().rand.nextFloat() * 0.4F + 1.0F);
    player.swingItem();
    return SpellResult.SUCCESS;
  }

  private static boolean hasHorse(EntityPlayer player) {
    for (Object object : player.worldObj.loadedEntityList) {
      if (object instanceof EntitySpiritHorse
          && !((EntitySpiritHorse) object).isDead
          && ((EntitySpiritHorse) object).belongsTo(player)) return true;
    }
    return false;
  }

  private static int nearestFloor(SpellContext context, int x, int y, int z) {
    int result = -1;
    int distance = Integer.MAX_VALUE;
    for (int floorY = y - 5; floorY <= y + 5; floorY++) {
      if (!context.world().doesBlockHaveSolidTopSurface(context.world(), x, floorY, z)) continue;
      if (!context.world().isAirBlock(x, floorY + 1, z)) continue;
      int current = Math.abs(floorY - y);
      if (current < distance) {
        result = floorY + 1;
        distance = current;
      }
    }
    return result;
  }
}
