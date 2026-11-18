package ru.givler.mbo.editor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.config.LockSecurityConfig;

/** One authorization policy for every protected world-building feature. */
public final class BuilderAccess {
  private BuilderAccess() {}

  public static boolean canEdit(EntityPlayer player) {
    return canEdit(player, "ariamis.editor.dungeon");
  }

  public static boolean canEdit(EntityPlayer player, String permission) {
    return player != null && player.capabilities.isCreativeMode
        && ru.givler.mbo.security.SecurityAccess.allowed(player, permission, LockSecurityConfig.isAuthorized(player));
  }

  public static boolean canUseTool(EntityPlayer player, Item tool) {
    ItemStack held = player == null ? null : player.getCurrentEquippedItem();
    String permission = tool instanceof ru.givler.mbo.item.ItemPlatformEditor
        ? "ariamis.editor.platform" : "ariamis.editor.dungeon";
    return canEdit(player, permission) && held != null && held.getItem() == tool;
  }
}
