package ru.givler.mbo.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import ru.givler.mbo.EnumParticleType;
import ru.givler.mbo.registry.*;
import ru.givler.mbo.render.RenderCrossbow;
import ru.givler.mbo.render.RenderLongsword;
import software.bernie.geckolib3.network.NetworkHandler;

public class CommonProxy {

    public void registerRenderers() {


    }
    public void preInit(FMLInitializationEvent event){
        BlockRegistry.preLoad(event);
        ItemRegistry.preLoad(event);
        ModelRegistry.init(event);
        DrinkRegistry.preLoad(event);
        FoodRegistry.preLoad(event);
    }

    public void init(FMLInitializationEvent event){


    }

    public void postInit(FMLInitializationEvent event){

    }

    public void spawnParticle(EnumParticleType type, World world, double x, double y, double z, double motionX, double motionY, double motionZ) {

    }
}
