package ru.givler.mbo;
import cpw.mods.fml.common.SidedProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import ru.givler.mbo.handler.GuiHandler;
import ru.givler.mbo.proxy.CommonProxy;
import ru.givler.mbo.registry.EntityMobRegistry;

@Mod(modid = MoreBeyondOrdinary.MODID, name = MoreBeyondOrdinary.MODNAME, version = MoreBeyondOrdinary.VERSION)
public class MoreBeyondOrdinary {



    @SidedProxy(clientSide = "ru.givler.mbo.proxy.ClientProxy", serverSide = "ru.givler.mbo.proxy.CommonProxy")
    public static CommonProxy proxy;


    public static final String MODID = "mbo";
    public static final String MODNAME = "MoreBeyondOrdinary";
    public static final String VERSION = "${version}";

    @Mod.Instance
    public static MoreBeyondOrdinary instance;

    public static final int GUI_INFUSION_WORKBENCH = 0;

    @Mod.EventHandler
    public void preLoad(FMLPreInitializationEvent event) {
        proxy.preInit(event);
        EntityMobRegistry.registerEntities();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        proxy.initPackets();
        proxy.init(event);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){
        proxy.postInit(event);
    }

}