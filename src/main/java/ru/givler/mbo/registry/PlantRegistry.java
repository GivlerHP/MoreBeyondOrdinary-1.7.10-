package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.block.Block;
import ru.givler.mbo.block.BlockMushroomBasic;
import ru.givler.mbo.block.BlockMushroomMeta;


public class PlantRegistry {
public static Block Glowshroom;
    @Mod.EventHandler
    public static void preLoad(FMLInitializationEvent event) {
        Glowshroom = new BlockMushroomMeta( "Glowshroom", "mushroom/glowshroom", 5);
    }
}
