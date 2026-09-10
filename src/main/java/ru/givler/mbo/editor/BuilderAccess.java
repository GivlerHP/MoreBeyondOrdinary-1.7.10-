package ru.givler.mbo.editor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.config.LockSecurityConfig;

/** One authorization policy for every protected world-building feature. */
public final class BuilderAccess {
    private BuilderAccess() {}
    public static boolean canEdit(EntityPlayer player){return player!=null&&player.capabilities.isCreativeMode&&LockSecurityConfig.isAuthorized(player);}
    public static boolean canUseTool(EntityPlayer player,Item tool){ItemStack held=player==null?null:player.getCurrentEquippedItem();return canEdit(player)&&held!=null&&held.getItem()==tool;}
}
