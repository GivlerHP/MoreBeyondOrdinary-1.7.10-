package ru.givler.mbo.integration.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.event.NEIRegisterHandlerInfosEvent;
import codechicken.nei.recipe.GuiRecipeTab;
import codechicken.nei.recipe.HandlerInfo;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.registry.StonecutterRegistry;

@Optional.Interface(iface = "codechicken.nei.api.IConfigureNEI", modid = "NotEnoughItems")
public class ArcanumNEIConfig implements IConfigureNEI {
    private static boolean registered;
    private static final List<HandlerInfo> HANDLER_INFOS = new ArrayList<HandlerInfo>();

    @Override public void loadConfig() { registerHandlers(); }

    public static synchronized void registerHandlers() {
        if (registered) return;
        MinecraftForge.EVENT_BUS.register(new ArcanumNEIConfig());
        register(new ArcanumRecipeHandler(), new ItemStack(BlockRegistry.MagicFurnace), 176, 90);
        register(new StonecutterRecipeHandler(), new ItemStack(StonecutterRegistry.stonecutter), 166, 83);
        registered = true;
    }

    private static void register(TemplateRecipeHandler handler, ItemStack station, int width, int height) {
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
        API.addRecipeCatalyst(station, handler);
        HandlerInfo info = new HandlerInfo.Builder(handler.getClass(), "More Beyond Ordinary", MoreBeyondOrdinary.MODID)
                .setDisplayStack(station).setWidth(width).setHeight(height).setMaxRecipesPerPage(1).build();
        HANDLER_INFOS.add(info);
        GuiRecipeTab.handlerMap.put(info.getHandlerName(), info);
    }

    @SubscribeEvent
    public void registerHandlerInfos(NEIRegisterHandlerInfosEvent event) {
        for (HandlerInfo info : HANDLER_INFOS) event.registerHandlerInfo(info);
    }

    @Override public String getName() { return "MBO Recipe NEI Integration"; }
    @Override public String getVersion() { return MoreBeyondOrdinary.VERSION; }
}
