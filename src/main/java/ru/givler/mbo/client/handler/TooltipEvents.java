package ru.givler.mbo.client.handler;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import ru.givler.mbo.client.gui.TooltipFrameHooks;
import ru.givler.mbo.client.font.TooltipElements;
import ru.givler.mbo.client.font.ModernFontRenderer;
import ru.givler.mbo.config.TooltipFrameConfig;
import ru.givler.mbo.integration.minefantasy2.client.MineFantasyTooltips;
import net.minecraft.client.Minecraft;

public class TooltipEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemTooltip(ItemTooltipEvent event) {
        TooltipFrameHooks.markItemTooltip(event.toolTip);
        if (TooltipFrameConfig.mf2EquipmentIcons
                && Minecraft.getMinecraft().fontRenderer instanceof ModernFontRenderer) {
            MineFantasyTooltips.decorate(event.itemStack, event.toolTip);
        }
        ru.givler.mbo.client.gui.EquipmentTooltipDetails.decorate(event.itemStack, event.toolTip,
                TooltipFrameConfig.mf2EquipmentIcons && Minecraft.getMinecraft().fontRenderer instanceof ModernFontRenderer);
        if (Minecraft.getMinecraft().fontRenderer instanceof ModernFontRenderer) {
            ru.givler.mbo.client.gui.ItemDescriptionTooltips.decorate(event.itemStack, event.toolTip,
                    Minecraft.getMinecraft().fontRenderer);
        } else {
            for (String text : ru.givler.mbo.item.ItemDescription.read(event.itemStack))
                event.toolTip.add(EnumChatFormatting.GRAY + text);
        }
        TooltipElements.expandBlocks(event.toolTip);
    }
}
