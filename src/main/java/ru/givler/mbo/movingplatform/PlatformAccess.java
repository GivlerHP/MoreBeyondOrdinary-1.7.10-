package ru.givler.mbo.movingplatform;

import net.minecraft.entity.player.EntityPlayer;
import ru.givler.mbo.config.LockSecurityConfig;

public final class PlatformAccess {
    private PlatformAccess() {}
    public static boolean canEdit(EntityPlayer player) {
        return player != null && player.capabilities.isCreativeMode && LockSecurityConfig.isAuthorized(player);
    }
}
