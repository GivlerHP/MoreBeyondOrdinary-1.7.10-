package ru.givler.mbo.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.world.World;
import ru.givler.mbo.EnumParticleType;
import ru.givler.mbo.block.blockmodels.ModelTileBase;
import ru.givler.mbo.registry.*;


public class CommonProxy {

    public void registerRenderers() {


    }
    public void preInit(FMLInitializationEvent event){
        BlockRegistry.preLoad(event);
        ItemRegistry.preLoad(event);
        ModelRegistry.init(event);
        DrinkRegistry.preLoad(event);
        FoodRegistry.preLoad(event);
        PlantRegistry.preLoad(event);
    }

    public void init(FMLInitializationEvent event){
        CreativeTabRegistry.init(event);
        GameRegistry.registerTileEntity(ModelTileBase.class, "ModelTileBase");

    }

    public void postInit(FMLInitializationEvent event){

    }

    public void spawnParticle(EnumParticleType type, World world, double x, double y, double z, double motionX, double motionY, double motionZ) {

    }
}
