package ru.givler.mbo.models;

import ru.givler.mbo.block.blockmodels.ModelTileBase;
import ru.givler.mbo.main;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.provider.GeoModelProvider;

public class BlockTemplateModel extends GeoModelProvider<ModelTileBase> {

	@Override
	public ResourceLocation getModelLocation(ModelTileBase tileBase) {
		return new ResourceLocation(main.MODID, "geo/"+tileBase.modelName+".geo.json");
	}

	@Override
	public ResourceLocation getTextureLocation(ModelTileBase tileBase) {
		return new ResourceLocation(main.MODID, "textures/models/decor/"+ tileBase.textureName+".png");
	}
}
