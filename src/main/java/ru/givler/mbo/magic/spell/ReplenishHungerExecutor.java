package ru.givler.mbo.magic.spell;

import net.minecraft.entity.player.EntityPlayer;
import ru.givler.mbo.magic.api.SpellContext;
import ru.givler.mbo.magic.api.SpellExecutor;
import ru.givler.mbo.magic.api.SpellResult;

public final class ReplenishHungerExecutor implements SpellExecutor {
  @Override
  public SpellResult cast(SpellContext context) {
    if (!(context.caster() instanceof EntityPlayer)) return SpellResult.INVALID;
    EntityPlayer player = (EntityPlayer) context.caster();
    if (!player.getFoodStats().needFood()) return SpellResult.PASS;
    if (!context.world().isRemote) {
      int food = Math.max(1, (int) (6.0F * context.power()));
      player.getFoodStats().addStats(food, food * 0.1F);
      SpellEffects.sparkleBurst(player, 10, 1.0F, 0.7F, 0.3F);
      context
          .world()
          .playSoundAtEntity(
              player, "mbo:heal", 0.7F, 1.0F + context.world().rand.nextFloat() * 0.4F);
    }
    return SpellResult.SUCCESS;
  }
}
