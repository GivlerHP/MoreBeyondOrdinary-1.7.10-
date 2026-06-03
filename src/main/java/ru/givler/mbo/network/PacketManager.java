package ru.givler.mbo.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.packet.PacketActivateAmulet;
import ru.givler.mbo.network.packet.PacketSpawnParticle;
import ru.givler.mbo.network.packet.PacketSpawnParticleHandler;

public class PacketManager {

    public static final SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel(MoreBeyondOrdinary.MODID);

    public static int nextID = 0;

    public static void registerCommonPackets() {
        System.out.println("[DEBUG] registerCommonPackets, ID=" + nextID);
        INSTANCE.registerMessage(
                PacketActivateAmulet.Handler.class,
                PacketActivateAmulet.class,
                nextID++,
                Side.SERVER
        );
    }

    public static void registerClientPackets() {
        System.out.println("[DEBUG] registerClientPackets, ID=" + nextID);
        INSTANCE.registerMessage(
                PacketSpawnParticleHandler.class,
                PacketSpawnParticle.class,
                nextID++,
                Side.CLIENT
        );
    }
}