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
        registered = true;
    }

    @Override
    public String getName() {
        return "Magic Furnace NEI Integration";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}

