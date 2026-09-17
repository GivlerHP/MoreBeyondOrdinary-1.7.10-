package ru.givler.mbo.magic.spell;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;
import ru.givler.mbo.potion.MindControl;
import ru.givler.mbo.potion.SyncedPotionEffects;
import ru.givler.mbo.registry.PotionRegistry;

public final class MindControlExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    EntityLivingBase target = context.target();
    if (target == null)
      target = SpellTargeting.livingInSight(context.caster(), 8.0D * context.range());
    if (target == null) return SpellResult.PASS;
    if (!context.world().isRemote) {
      if (target instanceof EntityPlayer
          || target instanceof IBossDisplayData
          || target instanceof INpc
          || !(target instanceof EntityLiving)) return SpellResult.BLOCKED;
      MindControl.setController(target, context.caster());
      SyncedPotionEffects.apply(
          target,
          new PotionEffect(
              PotionRegistry.MindControl.id,
              Math.max(1, (int) (600 * context.duration())),
              0,
              true));
      SpellEffects.sparkleBurst(target, 20, 0.6F, 0.1F, 0.8F);
      context.world().playSoundAtEntity(target, "mbo:darkaura", 1.0F, 1.0F);
      context.caster().swingItem();
    }
    return SpellResult.SUCCESS;
  }
}
