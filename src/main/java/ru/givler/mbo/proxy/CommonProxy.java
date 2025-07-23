package ru.givler.mbo.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import ru.givler.mbo.EnumParticleType;
import ru.givler.mbo.block.blockmodels.ModelTileBase;
import ru.givler.mbo.handler.PacketActivateAmulet;
import ru.givler.mbo.handler.PotionCommonHandler;
import ru.givler.mbo.recipes.BlockRecipes;
import ru.givler.mbo.recipes.RoofRecipes;
import ru.givler.mbo.registry.*;
import ru.givler.mbo.util.PotionArrayExpander;


public class CommonProxy {
    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel("Amulet");

    public void initPackets() {
        NETWORK.registerMessage(PacketActivateAmulet.Handler.class, PacketActivateAmulet.class, 0, Side.SERVER);
    }

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
        MinecraftForge.EVENT_BUS.register(new PotionCommonHandler());
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
