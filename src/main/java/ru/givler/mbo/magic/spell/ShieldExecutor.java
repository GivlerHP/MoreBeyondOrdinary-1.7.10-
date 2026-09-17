package ru.givler.mbo.magic.spell;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.entity.magic.EntityMagicShield;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class ShieldExecutor implements SpellExecutor {
  @Override
  @SuppressWarnings("unchecked")
  public SpellResult cast(SpellContext context) {
    if (!(context.caster() instanceof EntityPlayer)) return SpellResult.PASS;
    context.caster().addPotionEffect(new PotionEffect(Potion.resistance.id, 10, 0, true));
    if (!context.world().isRemote) {
      List<EntityMagicShield> shields =
          context
              .world()
              .getEntitiesWithinAABB(
                  EntityMagicShield.class, context.caster().boundingBox.expand(2, 2, 2));
      boolean exists = false;
      for (EntityMagicShield shield : shields)
        if (shield.owner() == context.caster() && shield.kind() == EntityMagicShield.Kind.FORCE) {
          exists = true;
          shield.refresh();
        }
      if (!exists)
        context
            .world()
            .spawnEntityInWorld(
                new EntityMagicShield(context.world(), (EntityPlayer) context.caster()));
    }
    if (context.ticksInUse() == 0) {
      context.world().playSoundAtEntity(context.caster(), "mbo:aura", 1.0F, 1.0F);
    }
    return SpellResult.SUCCESS;
  }
}
