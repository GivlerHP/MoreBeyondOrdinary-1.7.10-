package ru.givler.mbo.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.packet.PacketActivateAmulet;
import ru.givler.mbo.network.packet.PacketLootContainerConfig;
import ru.givler.mbo.network.packet.PacketLootContainerGiveItem;
import ru.givler.mbo.network.packet.PacketLootContainerRestore;
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
        INSTANCE.registerMessage(
                PacketLootContainerConfig.Handler.class,
                PacketLootContainerConfig.class,
                nextID++,
                Side.SERVER
        );
        INSTANCE.registerMessage(
                PacketLootContainerGiveItem.Handler.class,
                PacketLootContainerGiveItem.class,
                nextID++,
                Side.SERVER
        );
        INSTANCE.registerMessage(
                PacketLootContainerRestore.Handler.class,
                PacketLootContainerRestore.class,
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