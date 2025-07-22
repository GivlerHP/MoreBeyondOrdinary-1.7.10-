package ru.givler.mbo;
import cpw.mods.fml.common.SidedProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import ru.givler.mbo.proxy.CommonProxy;

@Mod(modid = main.MODID, name = main.MODNAME, version = main.VERSION)
public class main {



    @SidedProxy(clientSide = "ru.givler.mbo.proxy.ClientProxy", serverSide = "ru.givler.mbo.proxy.CommonProxy")
    public static CommonProxy proxy;


    public static final String MODID = "mbo";
    public static final String MODNAME = "MBO";
    public static final String VERSION = "1.4.7";

    @Mod.EventHandler
    public void preLoad(FMLPreInitializationEvent event) { proxy.preInit(event); }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        proxy.initPackets();
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event){
        proxy.postInit(event);
    }

}