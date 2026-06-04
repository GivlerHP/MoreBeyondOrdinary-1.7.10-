package ru.givler.mbo.render.decormodels;

import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.tileentity.AnimatedModelTileBase;
import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.models.BlockTemplateModel;

public class TemplateModelRenderer extends GeoDefaultBlockRenderer<ModelTileBase> {
    public TemplateModelRenderer() {
        super(new BlockTemplateModel());
    }

    @Override
    public ResourceLocation getTextureLocation(ModelTileBase tile) {
        if (tile instanceof AnimatedModelTileBase) {
            AnimatedModelTileBase animated = (AnimatedModelTileBase) tile;
            if (animated.frameCount > 1) {
                return new ResourceLocation(MoreBeyondOrdinary.MODID,
                        "textures/models/decor/" + animated.textureName + "_0.png");
            }
        }
        return super.getTextureLocation(tile);
    }
}
