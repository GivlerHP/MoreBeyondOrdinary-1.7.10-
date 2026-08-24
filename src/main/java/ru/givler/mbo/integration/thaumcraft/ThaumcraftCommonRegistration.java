package ru.givler.mbo.integration.thaumcraft;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;
import ru.givler.mbo.integration.thaumcraft.util.DarkMoonCastQueue;

/** Loaded reflectively only when Thaumcraft is installed. */
public final class ThaumcraftCommonRegistration {
    private ThaumcraftCommonRegistration() {}

    public static void registerHandlers() {
        FMLCommonHandler.instance().bus().register(new DarkMoonCastQueue());
        MinecraftForge.EVENT_BUS.register(new MBOThaumDamageConverter());
    }
}
