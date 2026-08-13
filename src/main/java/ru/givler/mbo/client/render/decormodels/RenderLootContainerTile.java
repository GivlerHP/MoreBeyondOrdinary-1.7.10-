package ru.givler.mbo.client.render.decormodels;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

public class RenderLootContainerTile extends TemplateModelRenderer {
    private static final EnumFacing[] HORIZONTAL_FACES = {
            EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.EAST
    };

    @Override
    protected EnumFacing getFacing(TileEntity tile) {
        if (tile instanceof TileEntityLootContainer) {
            int facing;
            if (tile.getWorldObj() != null) {
                facing = tile.getWorldObj().getBlockMetadata(
                        tile.xCoord, tile.yCoord, tile.zCoord) & 3;
            } else {
                facing = ((TileEntityLootContainer) tile).getPlacementFacing();
            }
            return HORIZONTAL_FACES[facing];
        }
        return super.getFacing(tile);
    }

    @Override
    protected void rotateBlock(EnumFacing facing) {
        switch (facing) {
            case SOUTH:
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                break;
            case WEST:
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case EAST:
                GL11.glRotatef(270.0F, 0.0F, 1.0F, 0.0F);
                break;
            case NORTH:
            default:
                break;
        }
    }

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
