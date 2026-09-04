package ru.givler.mbo.client.gamemode;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketGamemodeMenuRequest;

public final class GamemodeSwitcherInputHandler {
    private static boolean permissionPending;
    private static boolean debugStateBefore;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!Keyboard.getEventKeyState() || Keyboard.getEventKey() != Keyboard.KEY_F4
                || !Keyboard.isKeyDown(Keyboard.KEY_F3)) return;

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.thePlayer == null || minecraft.theWorld == null) return;

        if (minecraft.currentScreen instanceof GuiGamemodeSwitcher) return;
        if (minecraft.currentScreen != null) return;

        if (permissionPending) return;
        permissionPending = true;
        // F3 has already toggled the debug flag when Forge publishes KeyInputEvent.
        debugStateBefore = !minecraft.gameSettings.showDebugInfo;
        minecraft.gameSettings.showDebugInfo = false;
        PacketManager.INSTANCE.sendToServer(new PacketGamemodeMenuRequest());
    }

    public static void handlePermission(boolean allowed) {
        permissionPending = false;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (allowed && Keyboard.isKeyDown(Keyboard.KEY_F3) && minecraft.currentScreen == null
                && minecraft.thePlayer != null && minecraft.theWorld != null) {
            minecraft.displayGuiScreen(new GuiGamemodeSwitcher(
                    GamemodeSelection.current(minecraft), debugStateBefore));
        } else {
            minecraft.gameSettings.showDebugInfo = debugStateBefore;
        }
    }
}
