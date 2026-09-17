package ru.givler.mbo.magic.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/** Mirrors caster damage to the currently soulbound victim. */
public final class SoulbindingEvents {
  private boolean mirroring;

  @SubscribeEvent
  public void onHurt(LivingHurtEvent event) {
    if (mirroring || event.entityLiving.worldObj.isRemote) return;
    String id = event.entityLiving.getEntityData().getString("mboSoulboundVictim");
    if (id.isEmpty()) return;
    EntityLivingBase victim = find(event.entityLiving, id);
    if (victim == null || !victim.isEntityAlive()) {
      event.entityLiving.getEntityData().removeTag("mboSoulboundVictim");
      return;
    }
    mirroring = true;
    try {
      victim.attackEntityFrom(
          new net.minecraft.util.EntityDamageSource("mbo.soulbinding", event.entityLiving)
              .setMagicDamage(),
          event.ammount);
    } finally {
      mirroring = false;
    }
  }

  private static EntityLivingBase find(EntityLivingBase source, String value) {
    try {
      UUID id = UUID.fromString(value);
      for (Object object : source.worldObj.loadedEntityList)
        if (object instanceof EntityLivingBase && id.equals(((Entity) object).getUniqueID()))
          return (EntityLivingBase) object;
    } catch (IllegalArgumentException ignored) {
    }
    return null;
  }
}
