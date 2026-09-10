package ru.givler.mbo.movingplatform;

import net.minecraft.entity.player.EntityPlayer;
import ru.givler.mbo.editor.BuilderAccess;

public final class PlatformAccess {
    private PlatformAccess() {}
    public static boolean canEdit(EntityPlayer player) {
        return BuilderAccess.canEdit(player);
    }
}
