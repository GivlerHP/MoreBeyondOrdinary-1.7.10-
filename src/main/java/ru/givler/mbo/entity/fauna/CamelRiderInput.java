package ru.givler.mbo.entity.fauna;

import net.minecraft.entity.player.EntityPlayer;

public final class CamelRiderInput {
  private CamelRiderInput() {}

  /** Item use slows the player on foot, but must not cancel a camel's sprint. */
  public static boolean itemUseSlowsMovement(EntityPlayer player) {
    if (player.ridingEntity instanceof EntityMBOCamel) {
      EntityMBOCamel camel = (EntityMBOCamel) player.ridingEntity;
      if (camel.riddenByEntity == player && camel.isHorseSaddled()) return false;
    }
    return player.isUsingItem();
  }
}
