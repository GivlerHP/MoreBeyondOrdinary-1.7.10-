package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import ru.givler.mbo.block.BlockBasic;
import ru.givler.mbo.block.BlockMushroomBasic;


public class PlantRegistry {
public static Block TestGrib;
    @Mod.EventHandler
    public static void preLoad(FMLInitializationEvent event) {
        TestGrib = new BlockMushroomBasic( "TestGrib", "mushroom/glowshroom_sky");

    }
}
