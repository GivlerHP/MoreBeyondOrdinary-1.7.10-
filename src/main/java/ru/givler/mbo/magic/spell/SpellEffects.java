package ru.givler.mbo.magic.spell;

import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.EntityLivingBase;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.magic.PacketMagicBurst;

final class SpellEffects {
  private SpellEffects() {}

  static void sparkleBurst(EntityLivingBase target, int count, float red, float green, float blue) {
    NetworkRegistry.TargetPoint point =
        new NetworkRegistry.TargetPoint(
            target.dimension, target.posX, target.posY, target.posZ, 64.0D);
    PacketManager.INSTANCE.sendToAllAround(
        new PacketMagicBurst(target, count, red, green, blue), point);
  }
}
