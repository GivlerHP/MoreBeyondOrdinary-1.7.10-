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
import ru.givler.mbo.client.render.weaponskin.WeaponSkinRenderer;
import ru.givler.mbo.item.skin.EquipmentSkinData;

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
            if (cpw.mods.fml.common.Loader.isModLoaded("Thaumcraft")) {
                ru.givler.mbo.integration.thaumcraft.client.ThaumcraftTooltips.decorate(event.itemStack, event.toolTip);
            }
            ru.givler.mbo.client.gui.ItemDescriptionTooltips.decorate(event.itemStack, event.toolTip,
                    Minecraft.getMinecraft().fontRenderer);
        } else {
            for (String text : ru.givler.mbo.item.ItemDescription.read(event.itemStack))
                event.toolTip.add(EnumChatFormatting.GRAY + text);
        }
        TooltipElements.expandBlocks(event.toolTip);
        if (WeaponSkinRenderer.INSTANCE.available(EquipmentSkinData.resolve(event.itemStack))
                || WeaponSkinRenderer.INSTANCE.available(ru.givler.mbo.item.skin.ItemSkinTemplate.skin(event.itemStack))) {
            String alt = Minecraft.getMinecraft().fontRenderer instanceof ModernFontRenderer ? "<mbo:alt>" : "Alt";
            event.toolTip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("mbo.skin.tooltip.hint", alt));
        }
    }
}
