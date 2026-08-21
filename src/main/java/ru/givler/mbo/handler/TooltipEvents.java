package ru.givler.mbo.handler;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import ru.givler.mbo.core.TooltipFrameHooks;
import ru.givler.mbo.client.font.TooltipElements;

public class TooltipEvents {
    String localized = StatCollector.translateToLocal("tooltip.durability");

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        TooltipFrameHooks.markItemTooltip(event.toolTip);
        TooltipElements.expandBlocks(event.toolTip);
        if (!event.showAdvancedItemTooltips && event.itemStack != null) {
            ItemStack is = event.itemStack;
            if (is.isItemDamaged()) {
                int max = is.getMaxDamage();
                int current = max - is.getItemDamage();
                String tooltip = EnumChatFormatting.GRAY + localized + ": " + current + " / " + max;
                if (!event.toolTip.contains(tooltip)) {
                    event.toolTip.add(tooltip);
                }
            }
        }
    }
}
