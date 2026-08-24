package ru.givler.mbo;
import cpw.mods.fml.common.SidedProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.event.FMLInterModComms;
import ru.givler.mbo.handler.GuiHandler;
import ru.givler.mbo.proxy.CommonProxy;
import ru.givler.mbo.registry.EntityMobRegistry;
import ru.givler.mbo.util.VanillaBlockReplacer;
import ru.givler.mbo.config.IntegrationConfig;
import ru.givler.mbo.config.TooltipFrameConfig;

@Mod(
        modid = MoreBeyondOrdinary.MODID,
        name = MoreBeyondOrdinary.MODNAME,
        version = MoreBeyondOrdinary.VERSION,
        dependencies = MoreBeyondOrdinary.DEPENDENCIES
)
public class MoreBeyondOrdinary {

    @SidedProxy(clientSide = "ru.givler.mbo.proxy.ClientProxy", serverSide = "ru.givler.mbo.proxy.CommonProxy")
    public static CommonProxy proxy;


    public static final String MODID = "mbo";
    public static final String MODNAME = "MoreBeyondOrdinary";
    public static final String VERSION = "${version}";
    public static final String DEPENDENCIES =
            "required-after:geckolib3;required-after:Baubles;"
            + "after:Thaumcraft;after:BiomesOPlenty;after:minefantasy2;after:customnpcs;after:NotEnoughItems;"
            + "before:Growthcraft;before:Growthcraft|Apples";

    @Mod.Instance(value = MoreBeyondOrdinary.MODID)
    public static MoreBeyondOrdinary instance;

    public static final int GUI_INFUSION_WORKBENCH = 0;
    public static final int GUI_LOOT_CONTAINER_CONFIG = 1;
    public static final int GUI_LOOM = 2;
    public static final int GUI_STONECUTTER = 3;
    public static final int GUI_BARREL = 4;

    @Mod.EventHandler
    public void preLoad(FMLPreInitializationEvent event) {
        FMLInterModComms.sendMessage("Waila", "register", "ru.givler.mbo.integration.waila.ModelCollisionWailaProvider.register");
        VanillaBlockReplacer.replaceTrapdoor();
        proxy.preInit(event);
        proxy.initPackets();
        EntityMobRegistry.registerEntities();
        IntegrationConfig.load(event.getModConfigurationDirectory());
        TooltipFrameConfig.load(event.getModConfigurationDirectory());

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        proxy.init(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){
        proxy.postInit(event);
    }

}
