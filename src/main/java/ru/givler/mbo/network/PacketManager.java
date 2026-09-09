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
import ru.givler.mbo.network.packet.PacketBoatMove;
import ru.givler.mbo.network.packet.PacketLockpickPin;
import ru.givler.mbo.network.packet.PacketLockpickResult;
import ru.givler.mbo.network.packet.PacketLockOpenConfig;
import ru.givler.mbo.network.packet.PacketSetLockDifficulty;
import ru.givler.mbo.network.packet.PacketLockpickSuccess;
import ru.givler.mbo.network.packet.PacketLockLootSettings;
import ru.givler.mbo.network.packet.PacketLockBarrierSettings;
import ru.givler.mbo.network.packet.PacketApplyLockTemplate;
import ru.givler.mbo.network.packet.PacketSpectatorState;
import ru.givler.mbo.network.packet.PacketGamemodeMenuRequest;
import ru.givler.mbo.network.packet.PacketGamemodeMenuPermission;
import ru.givler.mbo.network.packet.PacketPlatformAction;
import ru.givler.mbo.network.packet.PacketPlatformSync;
import ru.givler.mbo.network.packet.PacketPlatformOpen;
import ru.givler.mbo.network.packet.PacketPlatformRemove;
import ru.givler.mbo.network.packet.PacketPlatformInteract;

public class PacketManager {

    public static final SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel(MoreBeyondOrdinary.MODID);

    public static int nextID = 0;

    public static void registerCommonPackets() {
        System.out.println("[DEBUG] registerCommonPackets, ID=" + nextID);
        INSTANCE.registerMessage(PacketActivateAmulet.Handler.class,PacketActivateAmulet.class,nextID++,Side.SERVER);
        INSTANCE.registerMessage(PacketLootContainerConfig.Handler.class,PacketLootContainerConfig.class,nextID++,Side.SERVER);
        INSTANCE.registerMessage(PacketLootContainerGiveItem.Handler.class,PacketLootContainerGiveItem.class,nextID++,Side.SERVER);
        INSTANCE.registerMessage(PacketLootContainerRestore.Handler.class,PacketLootContainerRestore.class,nextID++,Side.SERVER);
        INSTANCE.registerMessage(PacketBoatMove.Handler.class, PacketBoatMove.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketLockpickPin.Handler.class, PacketLockpickPin.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketLockOpenConfig.Handler.class, PacketLockOpenConfig.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketSetLockDifficulty.Handler.class, PacketSetLockDifficulty.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketLockpickSuccess.Handler.class, PacketLockpickSuccess.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketLockLootSettings.Handler.class, PacketLockLootSettings.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketLockBarrierSettings.Handler.class, PacketLockBarrierSettings.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketApplyLockTemplate.Handler.class, PacketApplyLockTemplate.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketGamemodeMenuRequest.Handler.class, PacketGamemodeMenuRequest.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketPlatformAction.Handler.class, PacketPlatformAction.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketPlatformInteract.Handler.class, PacketPlatformInteract.class, nextID++, Side.SERVER);
    }

    public static void registerClientPackets() {
        System.out.println("[DEBUG] registerClientPackets, ID=" + nextID);
        INSTANCE.registerMessage(PacketSpawnParticleHandler.class,PacketSpawnParticle.class,nextID++,Side.CLIENT);
        INSTANCE.registerMessage(PacketLockpickResult.Handler.class, PacketLockpickResult.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketSpectatorState.Handler.class, PacketSpectatorState.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketGamemodeMenuPermission.Handler.class, PacketGamemodeMenuPermission.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketPlatformSync.Handler.class, PacketPlatformSync.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketPlatformOpen.Handler.class, PacketPlatformOpen.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketPlatformRemove.Handler.class, PacketPlatformRemove.class, nextID++, Side.CLIENT);
    }
}
