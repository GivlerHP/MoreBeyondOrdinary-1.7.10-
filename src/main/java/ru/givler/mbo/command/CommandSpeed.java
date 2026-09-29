package ru.givler.mbo.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Sets a temporary movement speed for the current ground or air mode. */
public final class CommandSpeed extends CommandBase {
    private static final Map<UUID, SpeedSettings> SETTINGS = new HashMap<UUID, SpeedSettings>();

    @Override
    public String getCommandName() {
        return "speed";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.mbo.speed.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] arguments) {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        SpeedSettings settings = getSettings(player);

        if (arguments.length == 1 && isReset(arguments[0])) {
            if (player.onGround) settings.resetWalk();
            else settings.resetFly();
            apply(player, settings);
            func_152373_a(sender, this, "commands.mbo.speed.reset", player.onGround ? "walk" : "fly");
            return;
        }

        if (arguments.length != 1) throw new WrongUsageException(getCommandUsage(sender));

        final float speed = parseIntBounded(sender, arguments[0], 0, 10);

        if (player.onGround) settings.walk = speed;
        else settings.fly = speed;
        apply(player, settings);
        func_152373_a(sender, this, "commands.mbo.speed.set", speed, player.onGround ? "walk" : "fly");
    }

    private static boolean isReset(String argument) {
        return "reset".equalsIgnoreCase(argument);
    }

    private static SpeedSettings getSettings(EntityPlayerMP player) {
        UUID id = player.getUniqueID();
        SpeedSettings settings = SETTINGS.get(id);
        if (settings == null) {
            settings = new SpeedSettings();
            SETTINGS.put(id, settings);
        }
        return settings;
    }

    public static void apply(EntityPlayerMP player) {
        SpeedSettings settings = SETTINGS.get(player.getUniqueID());
        if (settings != null) apply(player, settings);
    }

    public static void clear(EntityPlayerMP player) {
        SETTINGS.remove(player.getUniqueID());
        restoreNormalSpeed(player);
    }

    public static void resetForNewSession(EntityPlayerMP player) {
        clear(player);
        player.sendPlayerAbilities();
    }

    private static void restoreNormalSpeed(EntityPlayerMP player) {
        player.capabilities.setPlayerWalkSpeed(0.1F);
        player.capabilities.setFlySpeed(0.05F);
    }

    private static void apply(EntityPlayerMP player, SpeedSettings settings) {
        player.capabilities.setPlayerWalkSpeed(scaleWalk(settings.walk));
        player.capabilities.setFlySpeed(scaleFly(settings.fly));
        player.sendPlayerAbilities();
    }

    private static float scaleWalk(float speed) {
        return speed / 10.0F * 1.0F;
    }

    private static float scaleFly(float speed) {
        return speed / 10.0F * 0.5F;
    }

    private static final class SpeedSettings {
        private float walk;
        private float fly;

        private SpeedSettings() {
            this.walk = 1.0F;
            this.fly = 1.0F;
        }

        private void resetWalk() {
            walk = 1.0F;
        }

        private void resetFly() {
            fly = 1.0F;
        }
    }
}
