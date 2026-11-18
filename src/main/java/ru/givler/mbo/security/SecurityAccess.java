package ru.givler.mbo.security;

import java.io.*;
import java.util.Properties;
import java.util.UUID;
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
   catch (IOException | IllegalArgumentException error) { return false; }
  }
  String mode = policy.getProperty("mode", "legacy").trim();
  if ("legacy".equals(mode)) return legacy;
  if ("op".equals(mode)) return player.canCommandSenderUseCommand(2, "ariamis.security");
  if ("uuid".equals(mode)) {
   if (isListedUuid(player.getUniqueID(), policy.getProperty("allowedUuids", ""))) return true;
   return Boolean.parseBoolean(policy.getProperty("uuid.includeOp", "false"))
       && player.canCommandSenderUseCommand(2, "ariamis.security");
  }
  boolean roles = "pex".equals(mode);
  if (!roles && !"bukkit".equals(mode)) return false;
  try {
   Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
   Object target = bukkit.getMethod("getPlayer", java.util.UUID.class).invoke(null, player.getUniqueID());
   if (target == null) return false;
   if (roles) {
    String groups = policy.getProperty(permission + ".roles", policy.getProperty("pex.roles", ""));
    if (groups.trim().isEmpty()) return false;
    Object manager = bukkit.getMethod("getPluginManager").invoke(null);
    Object plugin = Class.forName("org.bukkit.plugin.PluginManager")
        .getMethod("getPlugin", String.class).invoke(manager, "PermissionsEx");
    if (plugin == null || !Boolean.TRUE.equals(Class.forName("org.bukkit.plugin.Plugin")
        .getMethod("isEnabled").invoke(plugin))) return false;
    Class<?> pex = plugin.getClass();
    Class<?> bukkitPlayer = Class.forName("org.bukkit.entity.Player");
    Object user = pex.getMethod("getUser", bukkitPlayer).invoke(null, target);
    if (user == null) return false;
    java.lang.reflect.Method inGroup = user.getClass().getMethod("inGroup", String.class, String.class, boolean.class);
    Object world = bukkitPlayer.getMethod("getWorld").invoke(target);
    String worldName = (String) Class.forName("org.bukkit.World").getMethod("getName").invoke(world);
    for (String value : groups.split(",")) {
     String group = value.trim();
     if (!group.isEmpty() && Boolean.TRUE.equals(inGroup.invoke(user, group, worldName, true))) return true;
    }
    return false;
   }

   return Boolean.TRUE.equals(Class.forName("org.bukkit.permissions.Permissible")
       .getMethod("hasPermission", String.class).invoke(target, permission));
  } catch (ReflectiveOperationException | LinkageError error) {
   // Explicit fallback only; a missing bridge must not silently grant access.
   return Boolean.parseBoolean(policy.getProperty("opFallback", "false"))
       && player.canCommandSenderUseCommand(2, "ariamis.security");
  }
 }
 static boolean isListedUuid(UUID playerId, String list) {
  if (playerId == null) return false;
  String canonical = playerId.toString();
  String compact = canonical.replace("-", "");
  for (String value : list.split(",")) {
   String entry = value.trim();
   if (canonical.equalsIgnoreCase(entry) || compact.equalsIgnoreCase(entry)) return true;
  }
  return false;
 }
}
