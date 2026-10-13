package ru.givler.mbo.client.render.decormodels;

import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.client.model.decormodels.DecorBlockModel;
import ru.givler.mbo.client.model.decormodels.DecorItemModel;
import net.minecraft.util.EnumFacing;
import ru.givler.mbo.block.model.BlockConnectedRope;
import ru.givler.mbo.block.model.RopeConnections;
import net.geckominecraft.client.renderer.GlStateManager;

public class TemplateModelRenderer extends GeoAnimatedBlockRenderer<ModelTileBase> {
    public TemplateModelRenderer() {
        super(new DecorBlockModel(), new DecorItemModel());
        software.bernie.geckolib3.core.controller.AnimationController.addModelFetcher((software.bernie.geckolib3.core.controller.AnimationController.ModelFetcher<ModelTileBase>)(animatable ->
            animatable instanceof ModelTileBase ? (DecorBlockModel)getGeoModelProvider() : null));
    }

    @Override
    protected void applyModelTransform(ModelTileBase tile) {
        if (tile.getWorldObj() != null && tile.getBlockType() instanceof ru.givler.mbo.block.BlockModels) {
            GlStateManager.translate(0,
                    ((ru.givler.mbo.block.BlockModels) tile.getBlockType()).getModelOffsetY(), 0);
        }
        if (tile.getWorldObj() != null && tile.getBlockType() instanceof BlockConnectedRope
                && RopeConnections.decoration(tile.getBlockMetadata()) == RopeConnections.KNOT
                && (tile.getBlockMetadata() & 3) == 3) {
            GlStateManager.translate(0, 0.5, 0);
            GlStateManager.rotate(90, 0, 0, 1);
            GlStateManager.translate(0, -0.8125, 0);
            return;
        }
        super.applyModelTransform(tile);
        if(tile.ropeFloor && tile.getWorldObj()!=null && tile.getBlockType() instanceof BlockConnectedRope
                && RopeConnections.decoration(tile.getBlockMetadata())==RopeConnections.KNOT)
            GlStateManager.translate(0,-.5625,0);
        else if(tile.getWorldObj()!=null && tile.getBlockType() instanceof BlockConnectedRope
                && RopeConnections.decoration(tile.getBlockMetadata())==RopeConnections.KNOT)
            GlStateManager.translate(0,-.3125,0);
        if ("instruments_of_torture/hook".equals(tile.getModelName())) {
            GlStateManager.rotate(180, 0, 1, 0);
        }
        if ("instruments_of_torture/hourglass".equals(tile.getModelName())) {
            // Baked geometry is centred at (+1,+1); move it onto the placement origin.
            GlStateManager.translate(-1, 0, -1);
        }
        if ("instruments_of_torture/hanged_corpse".equals(tile.getModelName())) {
            // The hanging rope occupies Z=0..2 in the source model; center it
            // on the block's rope axis. Apply after facing so the offset rotates too.
            GlStateManager.translate(0, 0, -1.0 / 16.0);
        }
    }

    @Override
    protected EnumFacing getFacing(net.minecraft.tileentity.TileEntity tile) {
        if (tile.getWorldObj() != null && tile.getBlockType() instanceof BlockConnectedRope) {
            int decoration = RopeConnections.decoration(tile.getBlockMetadata());
            if (decoration == RopeConnections.NOOSE) return EnumFacing.NORTH;
            if (decoration == RopeConnections.KNOT) {
                return (tile.getBlockMetadata() & 3) == 2 ? EnumFacing.WEST : EnumFacing.NORTH;
            }
            int mask = tile instanceof ModelTileBase&&((ModelTileBase)tile).movingPlatformRopeMask>=0
                ?((ModelTileBase)tile).movingPlatformRopeMask:((BlockConnectedRope) tile.getBlockType()).getConnectionMask(
                    tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
            switch (RopeConnections.rotationQuarterTurns(mask)) {
                case 1: return EnumFacing.WEST;  // +90 degrees around Y
                case 2: return EnumFacing.SOUTH; // +180 degrees
                case 3: return EnumFacing.EAST;  // +270 degrees
                default: return EnumFacing.NORTH;
            }
        }
        return super.getFacing(tile);
    }
}
