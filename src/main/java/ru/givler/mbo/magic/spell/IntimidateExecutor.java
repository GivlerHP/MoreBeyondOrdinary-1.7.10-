package ru.givler.mbo.magic.spell;

import java.util.List;
import net.minecraft.entity.EntityCreature;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.potion.Fear;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

public final class IntimidateExecutor implements SpellExecutor {
  @Override
  @SuppressWarnings("unchecked")
  public SpellResult cast(SpellContext context) {
    double radius = 8.0D * context.range();
    if (!context.world().isRemote) {
      List<EntityCreature> targets =
          context
              .world()
              .getEntitiesWithinAABB(
                  EntityCreature.class,
                  context.caster().boundingBox.expand(radius, radius, radius));
      for (EntityCreature target : targets) {
        Fear.setSource(target, context.caster());
        SyncedPotionEffects.apply(
            target,
            new PotionEffect(
                PotionRegistry.Fear.id, Math.max(1, (int) (600 * context.duration())), 0, true));
      }
      SpellEffects.sparkleBurst(context.caster(), 30, 0.25F, 0.0F, 0.15F);
    }
    context.world().playSoundAtEntity(context.caster(), "mob.enderdragon.growl", 1.0F, 1.0F);
    context.caster().swingItem();
    return SpellResult.SUCCESS;
  }
}
