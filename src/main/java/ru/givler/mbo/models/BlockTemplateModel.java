package ru.givler.mbo.models;

import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class BlockTemplateModel extends AnimatedGeoModel<ModelTileBase> {

	@Override
	public ResourceLocation getModelLocation(ModelTileBase tileBase) {
		return new ResourceLocation(MoreBeyondOrdinary.MODID, "geo/"+tileBase.modelName+".geo.json");
	}

	@Override
	public ResourceLocation getTextureLocation(ModelTileBase tileBase) {
		if (tileBase.frameCount > 1) {
			int speed = Math.max(1, tileBase.frameSpeed);
			int frame = (int) ((System.currentTimeMillis() / speed) % tileBase.frameCount);
			return new ResourceLocation(MoreBeyondOrdinary.MODID,
					"textures/models/decor/" + tileBase.textureName + "_" + frame + ".png");
		}
		return new ResourceLocation(MoreBeyondOrdinary.MODID, "textures/models/decor/" + tileBase.textureName + ".png");
	}

	@Override
	public ResourceLocation getAnimationFileLocation(ModelTileBase tileBase) {
		return new ResourceLocation(MoreBeyondOrdinary.MODID,
				"animations/" + tileBase.modelName + ".animation.json");
	}
}
