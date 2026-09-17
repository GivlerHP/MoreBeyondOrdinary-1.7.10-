package ru.givler.mbo.magic;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import ru.givler.mbo.magic.registry.MagicSpells;

/** Lifecycle boundary for MBO's magic subsystem. */
public final class MagicModule {
  private MagicModule() {}

  public static void preInit(FMLPreInitializationEvent event) {
    MagicSpells.register();
  }
}
