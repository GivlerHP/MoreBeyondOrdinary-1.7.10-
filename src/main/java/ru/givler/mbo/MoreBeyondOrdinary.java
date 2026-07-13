package ru.givler.mbo;
import cpw.mods.fml.common.SidedProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import electroblob.wizardry.Wizardry;
import ru.givler.mbo.handler.GuiHandler;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.proxy.CommonProxy;
import ru.givler.mbo.registry.EntityMobRegistry;
import ru.givler.mbo.util.VanillaBlockReplacer;
import ru.givler.mbo.config.IntegrationConfig;

@Mod(modid = MoreBeyondOrdinary.MODID, name = MoreBeyondOrdinary.MODNAME, version = MoreBeyondOrdinary.VERSION, dependencies = "before:Growthcraft;before:Growthcraft|Apples")
public class MoreBeyondOrdinary {

    @SidedProxy(clientSide = "ru.givler.mbo.proxy.ClientProxy", serverSide = "ru.givler.mbo.proxy.CommonProxy")
    public static CommonProxy proxy;


    public static final String MODID = "mbo";
    public static final String MODNAME = "MoreBeyondOrdinary";
    public static final String VERSION = "${version}";

    @Mod.Instance(value = MoreBeyondOrdinary.MODID)
    public static MoreBeyondOrdinary instance;

    public static final int GUI_INFUSION_WORKBENCH = 0;
    public static final int GUI_LOOT_CONTAINER_CONFIG = 1;

    @Mod.EventHandler
    public void preLoad(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        proxy.initPackets();
        VanillaBlockReplacer.replaceTrapdoor();
        EntityMobRegistry.registerEntities();
        IntegrationConfig.load(event.getModConfigurationDirectory());

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