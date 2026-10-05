package ru.givler.mbo.command;

import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentTranslation;
import ru.givler.mbo.core.TickRateHooks;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketTickRate;

public final class CommandTick extends CommandBase {
  @Override public String getCommandName() { return "tick"; }
  @Override public String getCommandUsage(ICommandSender sender) { return "commands.mbo.tick.usage"; }
  @Override public int getRequiredPermissionLevel() { return 2; }

  @Override public void processCommand(ICommandSender sender, String[] args) {
    if (args.length == 1 && (args[0].equals("query") || args[0].equals("rate"))) {
      sender.addChatMessage(new ChatComponentTranslation("commands.mbo.tick.query", TickRateHooks.getServerRate()));
      return;
    }
    int rate;
    if (args.length == 1 && args[0].equals("reset")) rate = 20;
    else if (args.length == 2 && args[0].equals("rate")) rate = parseIntBounded(sender, args[1], 1, 1000);
    else throw new WrongUsageException(getCommandUsage(sender));
    TickRateHooks.setServerRate(rate);
    PacketManager.INSTANCE.sendToAll(new PacketTickRate(rate));
    func_152373_a(sender, this, "commands.mbo.tick.set", rate);
  }

  @Override public List addTabCompletionOptions(ICommandSender sender, String[] args) {
    if (args.length == 1) return getListOfStringsMatchingLastWord(args, "rate", "query", "reset");
    if (args.length == 2 && args[0].equals("rate"))
      return getListOfStringsMatchingLastWord(args, "20", "100", "200", "1000");
    return null;
  }
}
