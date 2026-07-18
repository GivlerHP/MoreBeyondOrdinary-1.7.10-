package ru.givler.mbo.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import ru.givler.mbo.integration.thaumcraft.ThaumcraftRegistry;
import ru.givler.mbo.integration.thaumcraft.util.DarkMoonCastQueue;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.tileentity.AnimatedModelTileBase;
import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.handler.*;
import ru.givler.mbo.integration.biomesoplenty.DoorRegistry;
import ru.givler.mbo.integration.biomesoplenty.FenceRegistry;
import ru.givler.mbo.recipes.registry.ArcanumRecipeRegistry;
import ru.givler.mbo.recipes.registry.BlockRecipeRegistry;
import ru.givler.mbo.recipes.registry.RoofRecipeRegistry;
import ru.givler.mbo.registry.*;
import ru.givler.mbo.tileentity.TileEntityArcanum;
import ru.givler.mbo.tileentity.TileEntityLootContainer;
import ru.givler.mbo.util.PotionArrayExpander;

public class CommonProxy {

    public void initPackets() {
        PacketManager.registerCommonPackets();
        PacketManager.registerClientPackets();
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
        PotionRegistry.preLoad(event);
        ModelRegistry.preInit(event);
        DrinkRegistry.preLoad(event);
        FoodRegistry.preLoad(event);
        PlantRegistry.preLoad(event);
        ArmorRegistry.preLoad(event);
        ThaumcraftRegistry.preLoad(event);
        DoorRegistry.init();
        FenceRegistry.init();
        MinecraftForge.EVENT_BUS.register(new PotionCommonHandler());
        MinecraftForge.EVENT_BUS.register(new BeltEventHandler());
        if (Loader.isModLoaded("Thaumcraft")) {
            FMLCommonHandler.instance().bus().register(new DarkMoonCastQueue());
        }
        FMLCommonHandler.instance().bus().register(new BeltEventHandler());
    }

    public void init(FMLInitializationEvent event){
        CreativeTabRegistry.init(event);
        moveWoodIntegrationToBoPTab();
        ModelRegistry.init(event);
        ThaumcraftRegistry.init();
        GameRegistry.registerTileEntity(ModelTileBase.class, "ModelTileBase");
        GameRegistry.registerTileEntity(AnimatedModelTileBase.class, "AnimatedModelTileBase");
        GameRegistry.registerTileEntity(TileEntityArcanum.class, "magic_furnace");
        GameRegistry.registerTileEntity(TileEntityLootContainer.class, "loot_container_tile");

        BlockRecipeRegistry.init();
        RoofRecipeRegistry.init();
        ArcanumRecipeRegistry.init();
        BlockRegistry.initRecipe();

    }

    private void moveWoodIntegrationToBoPTab() {
        if (!Loader.isModLoaded("BiomesOPlenty") || biomesoplenty.BiomesOPlenty.tabBiomesOPlenty == null) {
            return;
        }

        net.minecraft.creativetab.CreativeTabs bopTab = biomesoplenty.BiomesOPlenty.tabBiomesOPlenty;
        FenceRegistry.setCreativeTab(bopTab);
        DoorRegistry.setBoPCreativeTab(bopTab);
    }

    public void postInit(FMLPostInitializationEvent event){
    }

    public void spawnParticle(EnumParticleType type, World world, double x, double y, double z, double motionX, double motionY, double motionZ) {

    }
}
