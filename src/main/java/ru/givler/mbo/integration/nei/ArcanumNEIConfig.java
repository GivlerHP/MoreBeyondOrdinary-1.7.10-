package ru.givler.mbo.integration.nei;


import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "codechicken.nei.api.IConfigureNEI", modid = "NotEnoughItems")
public class ArcanumNEIConfig implements IConfigureNEI {
    private static boolean registered;

    @Override
    public void loadConfig() {
        registerHandlers();
    }

    public static synchronized void registerHandlers() {
        if (registered) return;
        API.registerRecipeHandler(new ArcanumRecipeHandler());
        API.registerUsageHandler(new ArcanumRecipeHandler());
        API.registerRecipeHandler(new StonecutterRecipeHandler());
        API.registerUsageHandler(new StonecutterRecipeHandler());
        registered = true;
    }

    @Override
    public String getName() {
        return "MBO Recipe NEI Integration";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}

