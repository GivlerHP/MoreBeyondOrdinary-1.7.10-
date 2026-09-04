package ru.givler.mbo.command;

import net.minecraft.command.CommandGameMode;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.givler.mbo.spectator.SpectatorManager;

import java.util.ArrayList;
import java.util.List;

/** Adds the missing spectator branch while retaining vanilla gamemode handling. */
public final class CommandGameModeExtended extends CommandGameMode {

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) {
        if (arguments.length > 0 && isSpectatorArgument(arguments[0])) {
            EntityPlayerMP target = arguments.length >= 2 ? getPlayer(sender, arguments[1]) : getCommandSenderAsPlayer(sender);
            SpectatorManager.enter(target);
            func_152373_a(sender, this, "commands.gamemode.success.self", "spectator");
            return;
        }

        EntityPlayerMP target = null;
        if (arguments.length > 0 && isVanillaGameModeArgument(arguments[0])) {
            target = arguments.length >= 2 ? getPlayer(sender, arguments[1]) : getCommandSenderAsPlayer(sender);
        }
        if (target != null && SpectatorManager.isSpectator(target)) SpectatorManager.leave(target);
        super.processCommand(sender, arguments);
    }

    private static boolean isSpectatorArgument(String value) {
        return "3".equals(value) || "spectator".equalsIgnoreCase(value) || "sp".equalsIgnoreCase(value);
    }

    private static boolean isVanillaGameModeArgument(String value) {
        return "0".equals(value) || "s".equalsIgnoreCase(value) || "survival".equalsIgnoreCase(value)
                || "1".equals(value) || "c".equalsIgnoreCase(value) || "creative".equalsIgnoreCase(value)
                || "2".equals(value) || "a".equalsIgnoreCase(value) || "adventure".equalsIgnoreCase(value);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List addTabCompletionOptions(ICommandSender sender, String[] arguments) {
        List vanilla = super.addTabCompletionOptions(sender, arguments);
        if (arguments.length != 1) return vanilla;
        List result = vanilla == null ? new ArrayList() : new ArrayList(vanilla);
        for (Object entry : getListOfStringsMatchingLastWord(arguments, "spectator")) {
            if (!result.contains(entry)) result.add(entry);
        }
        return result;
    }
}
