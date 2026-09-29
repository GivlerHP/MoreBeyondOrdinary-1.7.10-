package ru.givler.mbo.integration.bukkit;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.givler.mbo.command.CommandSpeed;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;

/** Keeps MBO's speed command available when a Bukkit plugin also registers /speed. */
public final class BukkitSpeedCommandBridge {
  private BukkitSpeedCommandBridge() {}

  public static void install() {
    try {
      Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
      Class<?> pluginManagerClass = Class.forName("org.bukkit.plugin.PluginManager");
      Class<?> listenerClass = Class.forName("org.bukkit.event.Listener");
      Class<?> eventPriorityClass = Class.forName("org.bukkit.event.EventPriority");
      Class<?> executorClass = Class.forName("org.bukkit.plugin.EventExecutor");
      Class<?> commandEventClass = Class.forName("org.bukkit.event.player.PlayerCommandPreprocessEvent");
      Class<?> eventClass = Class.forName("org.bukkit.event.Event");
      Class<?> pluginClass = Class.forName("org.bukkit.plugin.Plugin");
      Object pluginManager = bukkitClass.getMethod("getPluginManager").invoke(null);
      Object plugin = pluginManagerClass.getMethod("getPlugin", String.class).invoke(pluginManager, "MoreBeyondOrdinary");
      if (plugin == null || !((Boolean) pluginClass.getMethod("isEnabled").invoke(plugin)).booleanValue()) return;

      Object listener = Proxy.newProxyInstance(listenerClass.getClassLoader(), new Class<?>[] {listenerClass},
          new ObjectMethods("MBO-Speed-Command-Bridge"));
      Object executor = Proxy.newProxyInstance(executorClass.getClassLoader(), new Class<?>[] {executorClass},
          new SpeedCommandExecutor());
      @SuppressWarnings({"unchecked", "rawtypes"})
      Object priority = Enum.valueOf((Class<? extends Enum>) eventPriorityClass, "LOWEST");
      pluginManagerClass.getMethod("registerEvent", Class.class, listenerClass, eventPriorityClass,
          executorClass, pluginClass, boolean.class)
          .invoke(pluginManager, commandEventClass, listener, priority, executor, plugin, true);
      System.out.println("[MBO] Enabled speed command bridge");
    } catch (ClassNotFoundException ignored) {
      // Bukkit is not installed.
    } catch (Throwable error) {
      System.err.println("[MBO] Could not enable speed command bridge");
      error.printStackTrace();
    }
  }

  private static final class ObjectMethods implements InvocationHandler {
    private final String name;

    private ObjectMethods(String name) { this.name = name; }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
      if ("toString".equals(method.getName())) return name;
      if ("hashCode".equals(method.getName())) return System.identityHashCode(proxy);
      if ("equals".equals(method.getName())) return proxy == args[0];
      return null;
    }
  }

  private static final class SpeedCommandExecutor implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (!"execute".equals(method.getName())) return null;
      Object event = args[1];
      String message = (String) event.getClass().getMethod("getMessage").invoke(event);
      String[] parts = message.trim().split("\\s+");
      if (parts.length == 0) return null;
      String command = parts[0].substring(1).toLowerCase(Locale.ROOT);
      if (!("speed".equals(command) || command.endsWith(":speed"))) return null;

      Object bukkitPlayer = event.getClass().getMethod("getPlayer").invoke(event);
      EntityPlayerMP player = (EntityPlayerMP) bukkitPlayer.getClass().getMethod("getHandle").invoke(bukkitPlayer);
      if (player == null || !player.canCommandSenderUseCommand(2, "speed")) return null;

      event.getClass().getMethod("setCancelled", boolean.class).invoke(event, true);
      try {
        new CommandSpeed().processCommand(player, tail(parts));
      } catch (CommandException error) {
        player.addChatMessage(new net.minecraft.util.ChatComponentText(error.getMessage()));
      }
      return null;
    }

    private static String[] tail(String[] parts) {
      String[] result = new String[parts.length - 1];
      System.arraycopy(parts, 1, result, 0, result.length);
      return result;
    }
  }

}
