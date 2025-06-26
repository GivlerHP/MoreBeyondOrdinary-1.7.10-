package ru.givler.mbo.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import ru.givler.mbo.EnumParticleType;
import ru.givler.mbo.block.blockmodels.ModelTileBase;
import ru.givler.mbo.handler.BashStunHandler;
import ru.givler.mbo.handler.VulnerabilityHandler;
import ru.givler.mbo.potion.ApplyStun;
import ru.givler.mbo.potion.Dodge;
import ru.givler.mbo.potion.Hex;
import ru.givler.mbo.recipes.BlockRecipes;
import ru.givler.mbo.recipes.RoofRecipes;
import ru.givler.mbo.registry.*;
import ru.givler.mbo.util.PotionArrayExpander;


public class CommonProxy {

    public void registerRenderers() {


    }
    public void preInit(FMLPreInitializationEvent event){
        PotionArrayExpander.expand(128);
        BlockRegistry.preLoad(event);
        ItemRegistry.preLoad(event);
        ModelRegistry.preInit(event);
        PotionRegistry.preLoad(event);
        DrinkRegistry.preLoad(event);
        FoodRegistry.preLoad(event);
        PlantRegistry.preLoad(event);
        MinecraftForge.EVENT_BUS.register(new Dodge.DodgeServerHandler());
        MinecraftForge.EVENT_BUS.register(new Hex.Handler());
        MinecraftForge.EVENT_BUS.register(new BashStunHandler());
        MinecraftForge.EVENT_BUS.register(new ApplyStun.Handler());
        MinecraftForge.EVENT_BUS.register(new VulnerabilityHandler());
    }

    public void init(FMLInitializationEvent event){
        CreativeTabRegistry.init(event);
        ModelRegistry.init(event);
        GameRegistry.registerTileEntity(ModelTileBase.class, "ModelTileBase");
        BlockRecipes.init();
        RoofRecipes.init();
    }

    public void postInit(FMLPostInitializationEvent event){

    }

    public void spawnParticle(EnumParticleType type, World world, double x, double y, double z, double motionX, double motionY, double motionZ) {

    }
}
