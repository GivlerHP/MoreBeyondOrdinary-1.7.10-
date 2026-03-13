package ru.givler.mbo.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.packet.PacketSpawnParticle;

public class PacketManager {

    public static final SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel(MoreBeyondOrdinary.MODID);

    private static int nextID = 0;

    public static void registerPackets() {
        INSTANCE.registerMessage(
                PacketSpawnParticle.Handler.class,
                PacketSpawnParticle.class,
                nextID++,
                Side.CLIENT
        );
    }
}