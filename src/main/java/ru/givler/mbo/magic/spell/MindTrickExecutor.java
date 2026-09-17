package ru.givler.mbo.magic.spell;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

public final class MindTrickExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    EntityLivingBase target = context.target();
    if (target == null)
      target = SpellTargeting.livingInSight(context.caster(), 8.0D * context.range());
    if (target == null) return SpellResult.PASS;
    if (!context.world().isRemote) {
      int duration = Math.max(1, (int) (300 * context.duration()));
      if (target instanceof EntityPlayer) {
        SyncedPotionEffects.apply(target, new PotionEffect(Potion.confusion.id, duration, 0, true));
      } else if (target instanceof EntityLiving) {
        ((EntityLiving) target).setAttackTarget(null);
        if (target instanceof EntityCreature) ((EntityCreature) target).setTarget(null);
        SyncedPotionEffects.apply(
            target, new PotionEffect(PotionRegistry.MindTrick.id, duration, 0, true));
      } else return SpellResult.PASS;
      SpellEffects.sparkleBurst(target, 15, 0.35F, 0.0F, 0.5F);
      context
          .world()
          .playSoundAtEntity(
              target, "mbo:effect", 0.7F, 0.8F + context.world().rand.nextFloat() * 0.4F);
    }
    return SpellResult.SUCCESS;
  }
}
