package ru.givler.mbo.registry;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import ru.givler.mbo.block.BlockMushroomBasic;
import ru.givler.mbo.block.BlockMushroomMeta;
import ru.givler.mbo.block.BlockPlantBasic;
import ru.givler.mbo.block.BlockPlantGrowable;


public class PlantRegistry {
    //ниже грибы
public static Block Glowshroom;
//ниже растения
public static Block Planttest, PlantTestGw;
    @Mod.EventHandler
    public static void preLoad(FMLInitializationEvent event) {
        Glowshroom = new BlockMushroomMeta( "Glowshroom", "mushroom/glowshroom", 5);

        Planttest = new BlockPlantBasic("Planttest", "plant/boneberry_stage_0");
        PlantTestGw = new BlockPlantGrowable("PlantTestGw", "plant/boneberry_stage", Items.wheat);
    }
}
