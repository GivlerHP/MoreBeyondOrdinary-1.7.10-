package ru.givler.mbo.integration.bukkit;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import ru.givler.mbo.command.CommandGameModeExtended;

/** Lets MBO handle its spectator arguments before an old Essentials gamemode command rejects them. */
public final class BukkitSpectatorCommandBridge {
  private static Object listener;
  private static Object executor;

  private BukkitSpectatorCommandBridge() {}

  public static void install() {
    if (listener != null) return;
    try {
      Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
      Class<?> pluginManagerClass = Class.forName("org.bukkit.plugin.PluginManager");
      Class<?> pluginClass = Class.forName("org.bukkit.plugin.Plugin");
      Class<?> listenerClass = Class.forName("org.bukkit.event.Listener");
      Class<?> eventClass = Class.forName("org.bukkit.event.Event");
      Class<?> priorityClass = Class.forName("org.bukkit.event.EventPriority");
      Class<?> executorClass = Class.forName("org.bukkit.plugin.EventExecutor");
      Class<?> commandEventClass = Class.forName("org.bukkit.event.player.PlayerCommandPreprocessEvent");

      Object pluginManager = bukkitClass.getMethod("getPluginManager").invoke(null);
      Object essentials = pluginManagerClass.getMethod("getPlugin", String.class).invoke(pluginManager, "Essentials");
      if (essentials == null
          || !((Boolean) pluginClass.getMethod("isEnabled").invoke(essentials)).booleanValue()) return;

      listener = Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[] {listenerClass}, new MarkerHandler());
      executor = Proxy.newProxyInstance(executorClass.getClassLoader(), new Class<?>[] {executorClass}, new ExecutorHandler());
      Object priority = Enum.valueOf(priorityClass.asSubclass(Enum.class), "HIGHEST");
      pluginManagerClass
          .getMethod(
              "registerEvent",
              Class.class,
              listenerClass,
              priorityClass,
              executorClass,
              pluginClass,
              boolean.class)
          .invoke(pluginManager, commandEventClass, listener, priority, executor, essentials, true);
      System.out.println("[MBO] Enabled Essentials spectator command bridge");
    } catch (ClassNotFoundException ignored) {
      // Plain Forge has no Bukkit API.
    } catch (Throwable error) {
      listener = null;
      executor = null;
      System.err.println("[MBO] Could not enable Essentials spectator command bridge");
      error.printStackTrace();
    }
  }

  private static void handle(Object event) throws Exception {
    Class<?> eventClass = event.getClass();
    String message = (String) eventClass.getMethod("getMessage").invoke(event);
    String[] arguments = spectatorArguments(message);
    if (arguments == null) return;

    Object bukkitPlayer = eventClass.getMethod("getPlayer").invoke(event);
    Method hasPermission = bukkitPlayer.getClass().getMethod("hasPermission", String.class);
    boolean allowed = ((Boolean) hasPermission.invoke(bukkitPlayer, "essentials.gamemode")).booleanValue();
    if (arguments.length >= 2)
      allowed =
          allowed
              && ((Boolean) hasPermission.invoke(bukkitPlayer, "essentials.gamemode.others"))
                  .booleanValue();
    if (!allowed) return;

    String playerName = (String) bukkitPlayer.getClass().getMethod("getName").invoke(bukkitPlayer);
    EntityPlayerMP sender =
        MinecraftServer.getServer().getConfigurationManager().func_152612_a(playerName);
    if (sender == null) return;

    eventClass.getMethod("setCancelled", boolean.class).invoke(event, true);
    try {
      new CommandGameModeExtended().processCommand(sender, arguments);
    } catch (CommandException error) {
      sender.addChatMessage(new ChatComponentTranslation(error.getMessage(), error.getErrorOjbects()));
    }
  }

  private static String[] spectatorArguments(String message) {
    if (message == null || message.length() < 2 || message.charAt(0) != '/') return null;
    String[] parts = message.substring(1).trim().split("\\s+");
    if (parts.length < 2 || parts.length > 3) return null;
    String command = parts[0].toLowerCase(Locale.ROOT);
    if (!("gamemode".equals(command)
        || "gm".equals(command)
        || "egamemode".equals(command)
        || "egm".equals(command)
        || "essentials:gamemode".equals(command))) return null;
    String mode = parts[1];
    if (!("3".equals(mode)
        || "sp".equalsIgnoreCase(mode)
        || "spectator".equalsIgnoreCase(mode))) return null;
    if (parts.length == 2) return new String[] {mode};
    return new String[] {mode, parts[2]};
  }

  private static final class MarkerHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments) {
      if ("toString".equals(method.getName())) return "MBO Bukkit spectator listener";
      if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
      if ("equals".equals(method.getName())) return proxy == arguments[0];
      return null;
    }
  }

  private static final class ExecutorHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
      if ("execute".equals(method.getName())) handle(arguments[1]);
      return null;
    }
  }
}
