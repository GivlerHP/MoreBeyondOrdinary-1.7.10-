package ru.givler.mbo.magic;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import ru.givler.mbo.magic.registry.MagicSpells;

/** Lifecycle boundary for MBO's magic subsystem. */
public final class MagicModule {
  private MagicModule() {}

  public static void preInit(FMLPreInitializationEvent event) {
    MagicSpells.register();
    cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(ru.givler.mbo.magic.possession.PossessionManager.INSTANCE);
    net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(ru.givler.mbo.magic.possession.PossessionManager.INSTANCE);
    net.minecraftforge.common.ForgeChunkManager.setForcedChunkLoadingCallback(ru.givler.mbo.MoreBeyondOrdinary.instance,
        (tickets,world)->{for(net.minecraftforge.common.ForgeChunkManager.Ticket ticket:tickets)net.minecraftforge.common.ForgeChunkManager.releaseTicket(ticket);});
  }
}
