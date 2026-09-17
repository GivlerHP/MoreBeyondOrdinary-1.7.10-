package ru.givler.mbo.magic.spell;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class GroupHealExecutor implements SpellExecutor {
  @Override
  @SuppressWarnings("unchecked")
  public SpellResult cast(SpellContext context) {
    double radius = 5.0D * context.area();
    List<EntityPlayer> players =
        context
            .world()
            .getEntitiesWithinAABB(
                EntityPlayer.class, context.caster().boundingBox.expand(radius, radius, radius));
    boolean found = false;
    for (EntityPlayer player : players) {
      if (!isAlly(context.caster(), player) || player.getHealth() >= player.getMaxHealth())
        continue;
      found = true;
      if (!context.world().isRemote) {
        player.heal(6.0F * context.power());
        SpellEffects.sparkleBurst(player, 10, 1.0F, 1.0F, 0.3F);
      }
    }
    if (found && !context.world().isRemote)
      context
          .world()
          .playSoundAtEntity(
              context.caster(), "mbo:heal", 0.7F, 1.0F + context.world().rand.nextFloat() * 0.4F);
    return found ? SpellResult.SUCCESS : SpellResult.PASS;
  }

  private static boolean isAlly(EntityLivingBase caster, EntityPlayer player) {
    return caster == player || caster.isOnSameTeam(player);
  }
}
