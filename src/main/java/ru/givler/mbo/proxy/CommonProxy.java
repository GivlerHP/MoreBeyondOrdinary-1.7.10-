package ru.givler.mbo.proxy;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.handler.*;
import ru.givler.mbo.recipes.registry.BlockRecipeRegistry;
import ru.givler.mbo.recipes.registry.RoofRecipeRegistry;
import ru.givler.mbo.registry.*;
import ru.givler.mbo.tileentity.TileEntityArcanum;
import ru.givler.mbo.tileentity.TileEntityLootContainer;
import ru.givler.mbo.tileentity.TileEntityBarrel;
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
        if (Loader.isModLoaded("Thaumcraft")) {
            invokeOptional("ru.givler.mbo.integration.thaumcraft.ThaumcraftRegistry",
                    "preLoad", FMLPreInitializationEvent.class, event);
        }
        if (Loader.isModLoaded("BiomesOPlenty")) {
            invokeOptional("ru.givler.mbo.integration.biomesoplenty.BiomesOPlentyRegistry", "init");
        }
        BoatRegistry.init();
        BannerRegistry.init();
        StonecutterRegistry.init();
        MinecraftForge.EVENT_BUS.register(new PotionCommonHandler());
        MinecraftForge.EVENT_BUS.register(new BeltEventHandler());
        FMLCommonHandler.instance().bus().register(new BeltEventHandler());
        MinecraftForge.EVENT_BUS.register(new RingEventHandler());
        FMLCommonHandler.instance().bus().register(new RingEventHandler());
        if (Loader.isModLoaded("Thaumcraft")) {
            invokeOptional("ru.givler.mbo.integration.thaumcraft.ThaumcraftCommonRegistration",
                    "registerHandlers");
        }
    }

    public void init(FMLInitializationEvent event){
        CreativeTabRegistry.init(event);
        BoatRegistry.registerRecipes();
        moveWoodIntegrationToBoPTab();
        ModelRegistry.init(event);
        if (isMineFantasyLoaded()) {
            invokeOptional("ru.givler.mbo.integration.minefantasy2.MineFantasyRegistry", "init");
        }
        if (Loader.isModLoaded("Thaumcraft")) {
            invokeOptional("ru.givler.mbo.integration.thaumcraft.ThaumcraftRegistry", "init");
        }
        GameRegistry.registerTileEntity(ModelTileBase.class, "ModelTileBase");
        GameRegistry.registerTileEntity(ru.givler.mbo.tileentity.TileEntityModelCollision.class,
                "ModelCollisionPartTile");
        GameRegistry.registerTileEntity(TileEntityArcanum.class, "magic_furnace");
        GameRegistry.registerTileEntity(TileEntityLootContainer.class, "loot_container_tile");
        GameRegistry.registerTileEntity(TileEntityBarrel.class, "mbo_barrel");
        GameRegistry.registerTileEntity(ru.givler.mbo.banner.TileEntityBanner.class, "mbo_banner");

        BlockRecipeRegistry.init();
        RoofRecipeRegistry.init();
        if (isMineFantasyLoaded()) {
            invokeOptional("ru.givler.mbo.recipes.registry.ArcanumRecipeRegistry", "init");
        }
        BlockRegistry.initRecipe();
        StonecutterRegistry.registerRecipes();
        GameRegistry.addSmelting(net.minecraft.init.Blocks.stone,
                new net.minecraft.item.ItemStack(BlockRegistry.SmoothStone), 0.1F);

    }

    private void moveWoodIntegrationToBoPTab() {
        if (Loader.isModLoaded("BiomesOPlenty")) {
            invokeOptional("ru.givler.mbo.integration.biomesoplenty.BiomesOPlentyRegistry", "moveToModTab");
        }
    }

    public void postInit(FMLPostInitializationEvent event){
    }

    private static void invokeOptional(String className, String method) {
        invokeOptional(className, method, null, null);
    }

    private static void invokeOptional(String className, String method, Class<?> parameterType, Object argument) {
        try {
            Class<?> integration = Class.forName(className);
            if (parameterType == null) integration.getMethod(method).invoke(null);
            else integration.getMethod(method, parameterType).invoke(null, argument);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialise optional integration " + className, e);
        }
    }

    private static boolean isMineFantasyLoaded() {
        return Loader.isModLoaded("minefantasy2");
    }

    public void spawnParticle(EnumParticleType type, World world, double x, double y, double z, double motionX, double motionY, double motionZ) {

    }
}
