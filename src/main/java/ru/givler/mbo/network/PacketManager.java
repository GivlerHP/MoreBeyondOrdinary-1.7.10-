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
import ru.givler.mbo.network.packet.PacketAreaEditorInteract;
import ru.givler.mbo.network.packet.PacketDungeonEditorOpen;
import ru.givler.mbo.network.packet.PacketDungeonEditorSettings;
import ru.givler.mbo.network.packet.PacketDungeonAreaDelete;
import ru.givler.mbo.network.packet.PacketDungeonAreaSync;
import ru.givler.mbo.network.packet.PacketDungeonAreaOpenRequest;
import ru.givler.mbo.network.packet.PacketDungeonTriggerOpen;
import ru.givler.mbo.network.packet.PacketDungeonTriggerSettings;

public class PacketManager {

    public static final SimpleNetworkWrapper INSTANCE =
            NetworkRegistry.INSTANCE.newSimpleChannel(MoreBeyondOrdinary.MODID);

    public static int nextID = 0;

    public static void registerCommonPackets() {
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
        INSTANCE.registerMessage(PacketAreaEditorInteract.Handler.class, PacketAreaEditorInteract.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketDungeonEditorSettings.Handler.class, PacketDungeonEditorSettings.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketDungeonAreaDelete.Handler.class, PacketDungeonAreaDelete.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketDungeonAreaOpenRequest.Handler.class, PacketDungeonAreaOpenRequest.class, nextID++, Side.SERVER);
        INSTANCE.registerMessage(PacketDungeonTriggerSettings.Handler.class, PacketDungeonTriggerSettings.class, nextID++, Side.SERVER);
    }

    public static void registerClientPackets() {
        INSTANCE.registerMessage(PacketSpawnParticleHandler.class,PacketSpawnParticle.class,nextID++,Side.CLIENT);
        INSTANCE.registerMessage(PacketLockpickResult.Handler.class, PacketLockpickResult.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketSpectatorState.Handler.class, PacketSpectatorState.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketGamemodeMenuPermission.Handler.class, PacketGamemodeMenuPermission.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketPlatformSync.Handler.class, PacketPlatformSync.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketPlatformOpen.Handler.class, PacketPlatformOpen.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketPlatformRemove.Handler.class, PacketPlatformRemove.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketDungeonEditorOpen.Handler.class, PacketDungeonEditorOpen.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketDungeonAreaSync.Handler.class, PacketDungeonAreaSync.class, nextID++, Side.CLIENT);
        INSTANCE.registerMessage(PacketDungeonTriggerOpen.Handler.class, PacketDungeonTriggerOpen.class, nextID++, Side.CLIENT);
    }
}
