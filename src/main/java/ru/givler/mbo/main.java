package ru.givler.mbo;
import cpw.mods.fml.common.SidedProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import ru.givler.mbo.proxy.CommonProxy;

@Mod(modid = main.MODID, name = main.MODNAME, version = main.VERSION)
public class main {



    @SidedProxy(clientSide = "ru.givler.mbo.proxy.ClientProxy", serverSide = "ru.givler.mbo.proxy.CommonProxy")
    public static CommonProxy proxy;


    public static final String MODID = "mbo";
    public static final String MODNAME = "MBO";
    public static final String VERSION = "1.1.3";

    @Mod.EventHandler
    public void preLoad(FMLInitializationEvent event) { proxy.preInit(event); }
    @Mod.EventHandler
    public void init(FMLInitializationEvent event){
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLInitializationEvent event){
        proxy.postInit(event);
    }

}