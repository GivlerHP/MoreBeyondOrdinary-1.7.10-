package ru.givler.mbo.magic.spell;

import net.minecraft.entity.EntityLivingBase;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class MindControlExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    EntityLivingBase target = context.target();
    if (target == null)
      target = SpellTargeting.livingInSight(context.caster(), 8.0D * context.range());
    if (target == null) {
      if (!context.world().isRemote && context.caster() instanceof net.minecraft.entity.player.EntityPlayer)
        ((net.minecraft.entity.player.EntityPlayer) context.caster()).addChatMessage(
            new net.minecraft.util.ChatComponentTranslation("mbo.possession.no_target"));
      return SpellResult.PASS;
    }
    if (!context.world().isRemote) {
      if (!(context.caster() instanceof net.minecraft.entity.player.EntityPlayerMP)) return SpellResult.BLOCKED;
      if (!ru.givler.mbo.magic.possession.PossessionManager.attempt(
          (net.minecraft.entity.player.EntityPlayerMP) context.caster(), target)) return SpellResult.BLOCKED;
      SpellEffects.sparkleBurst(target, 20, 0.6F, 0.1F, 0.8F);
      context.world().playSoundAtEntity(target, "mbo:darkaura", 1.0F, 1.0F);
      context.caster().swingItem();
    }
    return SpellResult.SUCCESS;
  }
}
