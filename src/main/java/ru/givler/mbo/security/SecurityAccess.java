package ru.givler.mbo.security;

import java.io.*;
import java.util.Properties;
import net.minecraft.entity.player.EntityPlayer;

/** Shared server policy file; Bukkit is optional and never linked on plain Forge. */
public final class SecurityAccess {
 private SecurityAccess() {}
 public static boolean allowed(EntityPlayer player, String permission, boolean legacy) {
  if (player == null) return false;
  if (player.worldObj != null && player.worldObj.isRemote) return legacy;
  Properties policy = new Properties();
  File file = new File(cpw.mods.fml.common.Loader.instance().getConfigDir(), "ariamis-security.properties");
  if (file.exists()) {
   try (InputStream in = new FileInputStream(file)) { policy.load(in); }
   catch (IOException error) { return false; }
  }
  String mode = policy.getProperty("mode", "legacy").trim();
  if ("legacy".equals(mode)) return legacy;
  if ("op".equals(mode)) return player.canCommandSenderUseCommand(2, "ariamis.security");
  if (!"bukkit".equals(mode)) return false;
  try {
   Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
   Object target = bukkit.getMethod("getPlayer", java.util.UUID.class).invoke(null, player.getUniqueID());
   if (target == null) return false;
   return Boolean.TRUE.equals(Class.forName("org.bukkit.permissions.Permissible")
       .getMethod("hasPermission", String.class).invoke(target, permission));
  } catch (ReflectiveOperationException | LinkageError error) {
   // Explicit fallback only; a missing bridge must not silently grant access.
   return Boolean.parseBoolean(policy.getProperty("opFallback", "false"))
       && player.canCommandSenderUseCommand(2, "ariamis.security");
  }
 }
}
