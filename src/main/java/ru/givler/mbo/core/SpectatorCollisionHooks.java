package ru.givler.mbo.core;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import ru.givler.mbo.spectator.SpectatorManager;

public final class SpectatorCollisionHooks {
    private SpectatorCollisionHooks() {}

    public static boolean filterCollision(boolean vanillaResult, EntityLivingBase entity) {
        return vanillaResult && !(entity instanceof EntityPlayer
                && SpectatorManager.isSpectator((EntityPlayer) entity));
    }
}
