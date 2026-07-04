package ru.givler.mbo.render.decormodels;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

public class RenderLootContainerTile extends TemplateModelRenderer {
    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (te instanceof TileEntityLootContainer) {
            TileEntityLootContainer loot = (TileEntityLootContainer) te;
            if (!loot.canRenderFor(Minecraft.getMinecraft().thePlayer)) {
                return;
            }
        }
        super.renderTileEntityAt(te, x, y, z, partialTicks);
    }
}
