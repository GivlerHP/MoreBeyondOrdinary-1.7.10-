package ru.givler.mbo.client.model.decormodels;

import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import ru.givler.mbo.block.model.BlockConnectedRope;
import ru.givler.mbo.block.model.RopeConnections;

public class DecorBlockModel extends AnimatedGeoModel<ModelTileBase> {

	@Override
	public ResourceLocation getModelLocation(ModelTileBase tileBase) {
		if (tileBase.getWorldObj() != null && tileBase.getBlockType() instanceof BlockConnectedRope) {
			int decoration = RopeConnections.decoration(tileBase.getBlockMetadata());
            int visibleMask=tileBase.movingPlatformRopeMask>=0?tileBase.movingPlatformRopeMask:((BlockConnectedRope)tileBase.getBlockType()).getConnectionMask(
					 tileBase.getWorldObj(),tileBase.xCoord,tileBase.yCoord,tileBase.zCoord);
            String model;
            if(decoration==RopeConnections.NONE) {
                model="instruments_of_torture/rope_connected/"+(tileBase.ropeFloor?"floor_":"center_")
                    +((tileBase.movingPlatformRopeMask>=0?tileBase.movingPlatformRopeConnector:((BlockConnectedRope)tileBase.getBlockType()).hasConnector(tileBase.getWorldObj(),
                        tileBase.xCoord,tileBase.yCoord,tileBase.zCoord))?"anchor_":"")+RopeConnections.canonicalMask(visibleMask);
            } else {
                model="instruments_of_torture/"+(decoration==RopeConnections.NOOSE?"rope_around_the_neck":"rope_5");
            }
            return DecorResources.model(model);
		}
		return DecorResources.model(tileBase.getModelName());
	}

	@Override
	public ResourceLocation getTextureLocation(ModelTileBase tileBase) {
		if (tileBase.getWorldObj() != null && tileBase.getBlockType() instanceof BlockConnectedRope) {
			int decoration = RopeConnections.decoration(tileBase.getBlockMetadata());
            String texture=decoration==RopeConnections.NONE?"rope_6"
                :decoration==RopeConnections.NOOSE?"rope_around_the_neck":"rope_5";
            return DecorResources.texture("instruments_of_torture/"+texture,"");
		}
		if (tileBase.getFrameCount() > 1) {
			int speed = Math.max(1, tileBase.getFrameSpeed());
			int frame = (int) ((System.currentTimeMillis() / speed) % tileBase.getFrameCount());
			return DecorResources.texture(tileBase.getTextureName(), "_" + frame);
		}
		return DecorResources.texture(tileBase.getTextureName(), "");
	}

	@Override
	public ResourceLocation getAnimationFileLocation(ModelTileBase tileBase) {
		return DecorResources.get(
				"animations/" + tileBase.getModelName() + ".animation.json");
	}
}
