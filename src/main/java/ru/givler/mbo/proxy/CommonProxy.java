package ru.givler.mbo.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import ru.givler.mbo.integration.thaumcraft.ThaumcraftRegistry;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.block.blockmodels.ModelTileBase;
import ru.givler.mbo.handler.*;
import ru.givler.mbo.integration.biomesoplenty.DoorRegistry;
import ru.givler.mbo.recipes.registry.ArcanumRecipeRegistry;
import ru.givler.mbo.recipes.registry.BlockRecipeRegistry;
import ru.givler.mbo.recipes.registry.RoofRecipeRegistry;
import ru.givler.mbo.registry.*;
import ru.givler.mbo.tileentity.TileEntityArcanum;
import ru.givler.mbo.util.PotionArrayExpander;


public class CommonProxy {

    public void initPackets() {
        PacketManager.registerCommonPackets();
    }

    public void registerRenderers() {
    }

    public void registerPackets() {

    }

    public World getClientWorld() {
        return null;
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
        ArmorRegistry.preLoad(event);
        DoorRegistry.init();
        ThaumcraftRegistry.init();
        MinecraftForge.EVENT_BUS.register(new PotionCommonHandler());
        MinecraftForge.EVENT_BUS.register(new BeltEventHandler());
        FMLCommonHandler.instance().bus().register(new BeltEventHandler());
        SpawnHandler.registerSpawns();
    }

    public void init(FMLInitializationEvent event){
        CreativeTabRegistry.init(event);
        ModelRegistry.init(event);
        GameRegistry.registerTileEntity(ModelTileBase.class, "ModelTileBase");
        GameRegistry.registerTileEntity(TileEntityArcanum.class, "magic_furnace");
        BlockRecipeRegistry.init();
        RoofRecipeRegistry.init();
        ArcanumRecipeRegistry.init();


    }

    public void postInit(FMLPostInitializationEvent event){
    }

    public void spawnParticle(EnumParticleType type, World world, double x, double y, double z, double motionX, double motionY, double motionZ) {

    }
}
