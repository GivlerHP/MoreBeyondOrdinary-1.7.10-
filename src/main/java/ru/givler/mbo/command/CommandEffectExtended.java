package ru.givler.mbo.command;

import net.minecraft.command.CommandEffect;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;

import java.util.ArrayList;
import java.util.List;

/** Adds clear operations without duplicating vanilla 1.7.10's effect command. */
public final class CommandEffectExtended extends CommandEffect {

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) {
        if (arguments.length >= 1 && "clear".equalsIgnoreCase(arguments[0])) {
            clear(sender, getCommandSenderAsPlayer(sender), arguments, 1);
            return;
        }

        if (arguments.length >= 2 && "clear".equalsIgnoreCase(arguments[1])) {
            EntityPlayerMP target = getPlayer(sender, arguments[0]);
            clear(sender, target, arguments, 2);
            return;
        }

        super.processCommand(sender, arguments);
    }

    private void clear(ICommandSender sender, EntityPlayerMP target, String[] arguments, int potionIndex) {
        if (arguments.length == potionIndex) {
            if (target.getActivePotionEffects().isEmpty()) {
                throw new CommandException("mbo.commands.effect.failure.notActive.all", target.getCommandSenderName());
            }
            target.clearActivePotions();
            func_152373_a(sender, this, "mbo.commands.effect.success.removed.all", target.getCommandSenderName());
            return;
        }

        if (arguments.length == potionIndex + 1) {
            int potionId = parseIntBounded(sender, arguments[potionIndex], 0, Potion.potionTypes.length - 1);
            Potion potion = Potion.potionTypes[potionId];
            if (potion == null) throw new CommandException("commands.effect.notFound", potionId);
            if (!target.isPotionActive(potionId)) {
                throw new CommandException("commands.effect.failure.notActive", potionId, target.getCommandSenderName());
            }
            target.removePotionEffect(potionId);
            func_152373_a(sender, this, "mbo.commands.effect.success.removed", potionId, target.getCommandSenderName());
            return;
        }

        throw new WrongUsageException(getCommandUsage(sender));
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List addTabCompletionOptions(ICommandSender sender, String[] arguments) {
        List vanilla = super.addTabCompletionOptions(sender, arguments);
        if (arguments.length != 1 && arguments.length != 2) return vanilla;

        List result = vanilla == null ? new ArrayList() : new ArrayList(vanilla);
        List clear = getListOfStringsMatchingLastWord(arguments, "clear");
        for (Object entry : clear) if (!result.contains(entry)) result.add(entry);
        return result;
    }
}
