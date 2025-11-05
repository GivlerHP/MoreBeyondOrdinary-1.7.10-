package ru.givler.mbo.integration.sereneseasons;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import sereneseasons.config.FertilityConfig;

import java.util.HashMap;

@SideOnly(Side.CLIENT)
public class SereneSeasonsTooltipCompat {

    private static final HashMap<String, Integer> plantToSeasons = new HashMap<>();
    private static boolean initialized = false;

    public static void register() {
        if (!Loader.isModLoaded("sereneseasons")) {
            System.out.println("[MBO] SereneSeasonsTooltipCompat: Serene Seasons not found, skipping registration");
            return;
        }

        if (!initialized) init();
        MinecraftForge.EVENT_BUS.register(new SereneSeasonsTooltipCompat());
        System.out.println("[MBO] SereneSeasonsTooltipCompat: handler registered, entries=" + plantToSeasons.size());
    }


    private static void init() {
        if (initialized) return;
        try {
            processConfigArray(FertilityConfig.seasonal_fertility.spring_crops, 1);
            processConfigArray(FertilityConfig.seasonal_fertility.summer_crops, 2);
            processConfigArray(FertilityConfig.seasonal_fertility.autumn_crops, 4);
            processConfigArray(FertilityConfig.seasonal_fertility.winter_crops, 8);
            initialized = true;
            System.out.println("[MBO] SereneSeasonsTooltipCompat initialized with " + plantToSeasons.size() + " entries");
        } catch (Throwable t) {
            System.err.println("[MBO] Failed to initialize SereneSeasonsTooltipCompat");
            t.printStackTrace();
        }
    }

    private static void processConfigArray(String[] configs, int bitmask) {
        if (configs == null) return;
        for (String entry : configs) {
            if (entry == null || entry.isEmpty()) continue;
            try {
                if (entry.startsWith("minecraft:")) continue;

                ResourceLocation loc = new ResourceLocation(entry);
                String domain = loc.getResourceDomain();
                String path   = loc.getResourcePath();

                plantToSeasons.put(entry, plantToSeasons.getOrDefault(entry, 0) | bitmask);

                Item item = GameRegistry.findItem(domain, path);
                if (item != null) {
                    Object idObj = GameRegistry.findUniqueIdentifierFor(item);
                    if (idObj != null) {
                        String itemName = idObj.toString();
                        plantToSeasons.put(itemName, plantToSeasons.getOrDefault(itemName, 0) | bitmask);
                    }
                    if (item instanceof IPlantable) {
                        try {
                            Block plantBlock = ((IPlantable) item).getPlant(null, 0, 0, 0);
                            if (plantBlock != null) {
                                Object bId = GameRegistry.findUniqueIdentifierFor(plantBlock);
                                if (bId != null) {
                                    String blockName = bId.toString();
                                    plantToSeasons.put(blockName, plantToSeasons.getOrDefault(blockName, 0) | bitmask);
                                }
                            }
                        } catch (Throwable ignored) {}
                    }
                }

                Block block = GameRegistry.findBlock(domain, path);
                if (block != null && block != Blocks.air) {
                    Object bId = GameRegistry.findUniqueIdentifierFor(block);
                    if (bId != null) {
                        String blockName = bId.toString();
                        plantToSeasons.put(blockName, plantToSeasons.getOrDefault(blockName, 0) | bitmask);
                    }
                }

            } catch (Throwable t) {
                System.err.println("[MBO] Failed to process config entry: " + entry);
                t.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!FertilityConfig.general_category.crop_tooltips
                || !FertilityConfig.general_category.seasonal_crops
                || event.itemStack == null
                || event.itemStack.getItem() == null) return;

        try {
            Object idObj = GameRegistry.findUniqueIdentifierFor(event.itemStack.getItem());
            if (idObj == null) return;
            String itemName = idObj.toString();

            if (!itemName.startsWith("Growthcraft|")) return;

            Integer mask = plantToSeasons.get(itemName);
            if (mask == null || mask == 0) return;

            event.toolTip.add(StatCollector.translateToLocal("tooltip.mbo.fertile_seasons"));

            boolean sp = (mask & 1) != 0;
            boolean su = (mask & 2) != 0;
            boolean au = (mask & 4) != 0;
            boolean wi = (mask & 8) != 0;

            if (sp && su && au && wi) {
                event.toolTip.add(EnumChatFormatting.LIGHT_PURPLE + StatCollector.translateToLocal("tooltip.mbo.year_round"));
            } else {
                if (sp) event.toolTip.add(EnumChatFormatting.GREEN  + StatCollector.translateToLocal("tooltip.mbo.spring"));
                if (su) event.toolTip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("tooltip.mbo.summer"));
                if (au) event.toolTip.add(EnumChatFormatting.GOLD   + StatCollector.translateToLocal("tooltip.mbo.autumn"));
                if (wi) event.toolTip.add(EnumChatFormatting.AQUA   + StatCollector.translateToLocal("tooltip.mbo.winter"));
            }
        } catch (Throwable ignored) {}
    }
}
