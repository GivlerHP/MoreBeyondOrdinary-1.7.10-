package ru.givler.mbo.potion;

import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketPotionEffectSync;

/** Applies a potion effect on the server and mirrors its full integer ID to nearby clients. */
public final class SyncedPotionEffects {
  private SyncedPotionEffects() {}

  public static void apply(EntityLivingBase target, PotionEffect effect) {
    target.addPotionEffect(effect);
    if (target.worldObj.isRemote) return;
    PacketManager.INSTANCE.sendToAllAround(new PacketPotionEffectSync(target, effect),
        new NetworkRegistry.TargetPoint(target.dimension,
            target.posX, target.posY, target.posZ, 64.0D));
  }
}
