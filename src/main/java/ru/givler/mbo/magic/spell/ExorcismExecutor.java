package ru.givler.mbo.magic.spell;

import net.minecraft.entity.EntityLivingBase;
import ru.givler.mbo.magic.api.*;
import ru.givler.mbo.magic.possession.PossessionManager;

public final class ExorcismExecutor implements SpellExecutor {
  @Override public SpellResult cast(SpellContext context) {
    EntityLivingBase target=context.target();
    if(target==null)target=SpellTargeting.livingInSight(context.caster(),8D*context.range());
    if(target==null)return SpellResult.PASS;
    if(!context.world().isRemote) {
      if(context.caster().worldObj!=target.worldObj||context.caster().getDistanceSqToEntity(target)>64D*context.range()*context.range()
          ||!context.caster().canEntityBeSeen(target))return SpellResult.BLOCKED;
      if(!PossessionManager.exorcise(target,context.caster(),6F*context.power()))return SpellResult.PASS;
      SpellEffects.sparkleBurst(target,24,.8F,.7F,1F);
      context.world().playSoundAtEntity(target,"mbo:darkaura",1,1.5F);
      context.caster().swingItem();
    }
    return SpellResult.SUCCESS;
  }
}
