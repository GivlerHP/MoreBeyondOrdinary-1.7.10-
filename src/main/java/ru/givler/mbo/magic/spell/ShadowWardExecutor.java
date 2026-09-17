package ru.givler.mbo.magic.spell;

import net.minecraft.entity.player.EntityPlayer;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class ShadowWardExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    if (!(context.caster() instanceof EntityPlayer)) return SpellResult.BLOCKED;
    if (context.world().isRemote) {
      context
          .world()
          .spawnParticle(
              "portal",
              context.caster().posX,
              context.caster().posY + context.caster().getEyeHeight(),
              context.caster().posZ,
              -1 + 2 * context.world().rand.nextFloat(),
              -1 + context.world().rand.nextFloat(),
              -1 + 2 * context.world().rand.nextFloat());
    }
    if (context.ticksInUse() % 50 == 0) {
      context.world().playSoundAtEntity(context.caster(), "portal.portal", 0.6F, 1.5F);
    }
    return SpellResult.SUCCESS;
  }
}
