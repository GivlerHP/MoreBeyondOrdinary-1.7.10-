package ru.givler.mbo.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.tileentity.TileEntityArcanum;
import ru.givler.mbo.сontainer.ContainerArcanum;

public class GuiArcanum extends GuiContainer {
    private TileEntityArcanum tile;

    public GuiArcanum(InventoryPlayer invPlayer, TileEntityArcanum tile) {
        super(new ContainerArcanum(invPlayer, tile));
        this.tile = tile;
        xSize = 176;
        ySize = 200;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(StatCollector.translateToLocal("gui.arcanum_crucible"), 20, -10, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        mc.getTextureManager().bindTexture(new ResourceLocation(MoreBeyondOrdinary.MODID + ":" + "textures/gui/gui_infusionworkbench.png"));
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Прогресс
        int progress = (int)((float)tile.getProgress() / tile.getMaxProgress() * 80); // ширина до 80
        drawTexturedModalRect(guiLeft + 25, guiTop + 73, 0, 220, progress, 16);
    }
}
