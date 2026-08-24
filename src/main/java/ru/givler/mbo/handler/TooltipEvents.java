package ru.givler.mbo.handler;


import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import ru.givler.mbo.core.TooltipFrameHooks;
import ru.givler.mbo.client.font.TooltipElements;

public class TooltipEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemTooltip(ItemTooltipEvent event) {
        TooltipFrameHooks.markItemTooltip(event.toolTip);
        TooltipElements.expandBlocks(event.toolTip);
        if (event.itemStack != null) {
            ItemStack is = event.itemStack;
            if (is.isItemDamaged()) {
                int max = is.getMaxDamage();
                int current = max - is.getItem().getDisplayDamage(is);
                String tooltip = EnumChatFormatting.GRAY
                        + StatCollector.translateToLocal("tooltip.mbo.durability")
                        + ": " + current + " / " + max;

                int insertAt = event.toolTip.size();
                String durabilityValues = current + " / " + max;
                for (int i = event.toolTip.size() - 1; i >= 0; i--) {
                    String line = (String) event.toolTip.get(i);
                    String plain = EnumChatFormatting.getTextWithoutFormattingCodes(line);
                    if (plain != null && plain.endsWith(durabilityValues)) {
                        insertAt = i;
                        event.toolTip.remove(i);
                    }
                }
                event.toolTip.add(insertAt, tooltip);
            }
        }
    }
}
