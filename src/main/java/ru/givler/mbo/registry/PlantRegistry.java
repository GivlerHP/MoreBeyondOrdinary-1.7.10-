package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.block.Block;
import ru.givler.mbo.block.BlockMushroomMeta;

public class PlantRegistry {
    //ниже грибы
public static Block Glowshroom;
    //ниже растения
public static Block Planttest, PlantTestGw;
    @Mod.EventHandler
    public static void preLoad(FMLPreInitializationEvent event) {
        Glowshroom = new BlockMushroomMeta( "Glowshroom", "mushroom/glowshroom", 3);


    }
}
