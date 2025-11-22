package ru.givler.mbo.integration.nei;


import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import cpw.mods.fml.common.Optional;

@Optional.Interface(iface = "codechicken.nei.api.IConfigureNEI", modid = "NotEnoughItems")
public class ArcanumNEIConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        ArcanumRecipeHandler handler = new ArcanumRecipeHandler();

        API.registerRecipeHandler(new ArcanumRecipeHandler());
        API.registerUsageHandler(new ArcanumRecipeHandler());
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

