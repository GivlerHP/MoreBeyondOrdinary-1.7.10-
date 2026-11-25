package ru.givler.mbo.client.gui;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketWardrobe;

public final class WardrobeInput {
    private final KeyBinding key = new KeyBinding("key.mbo.wardrobe", Keyboard.KEY_K, "MoreBeyondOrdinary");

    public static void register() {
        WardrobeInput input = new WardrobeInput();
        ClientRegistry.registerKeyBinding(input.key);
        FMLCommonHandler.instance().bus().register(input);
    }

    @SubscribeEvent public void press(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (key.isPressed() && mc.currentScreen == null && mc.thePlayer != null) {
            PacketManager.INSTANCE.sendToServer(new PacketWardrobe.Open());
        }
    }
}
