package ru.givler.mbo.magic.spell;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class InvigoratingPresenceExecutor implements SpellExecutor {
  @Override
  @SuppressWarnings("unchecked")
  public SpellResult cast(SpellContext context) {
    double radius = 5 * context.area();
    List<EntityPlayer> players =
        context
            .world()
            .getEntitiesWithinAABB(
                EntityPlayer.class, context.caster().boundingBox.expand(radius, radius, radius));
    for (EntityPlayer player : players)
      if (player == context.caster() || context.caster().isOnSameTeam(player)) {
        player.addPotionEffect(
            new PotionEffect(
                Potion.damageBoost.id, Math.max(1, (int) (900 * context.duration())), 1, true));
      }
    if (context.world().isRemote)
      for (int i = 0; i < 50 * context.area(); i++) {
        double distance = (1 + context.world().rand.nextDouble() * 4) * context.area();
        double angle = context.world().rand.nextDouble() * Math.PI * 2;
        context
            .world()
            .spawnParticle(
                "reddust",
                context.caster().posX + Math.cos(angle) * distance,
                context.caster().boundingBox.minY,
                context.caster().posZ + Math.sin(angle) * distance,
                1,
                0.2D,
                0.2D);
      }
    context
        .world()
        .playSoundAtEntity(
            context.caster(), "mbo:aura", 1.0F, 1.0F + context.world().rand.nextFloat() * 0.2F);
    return SpellResult.SUCCESS;
  }
}
