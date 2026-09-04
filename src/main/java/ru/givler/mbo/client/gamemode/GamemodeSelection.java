package ru.givler.mbo.client.gamemode;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.spectator.SpectatorManager;

public enum GamemodeSelection {
    CREATIVE("creative", "mbo.gamemode.creative", new ItemStack(Blocks.grass)),
    SURVIVAL("survival", "mbo.gamemode.survival", new ItemStack(Items.iron_sword)),
    ADVENTURE("adventure", "mbo.gamemode.adventure", new ItemStack(Items.map)),
    SPECTATOR("spectator", "mbo.gamemode.spectator", new ItemStack(Items.ender_eye));

    public final String command;
    public final String translationKey;
    public final ItemStack icon;

    GamemodeSelection(String command, String translationKey, ItemStack icon) {
        this.command = command;
        this.translationKey = translationKey;
        this.icon = icon;
    }

    public GamemodeSelection next() {
        GamemodeSelection[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }

    public static GamemodeSelection current(Minecraft minecraft) {
        if (SpectatorManager.isSpectator(minecraft.thePlayer)) return SPECTATOR;
        if (minecraft.thePlayer.capabilities.isCreativeMode) return CREATIVE;
        if (!minecraft.thePlayer.capabilities.allowEdit) return ADVENTURE;
        return SURVIVAL;
    }
}
