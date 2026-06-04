package ru.givler.mbo.render.decormodels;

import ru.givler.mbo.tileentity.AnimatedModelTileBase;
import ru.givler.mbo.models.AnimatedBlockTemplateModel;

public class AnimatedTemplateModelRenderer extends GeoAnimatedBlockRenderer<AnimatedModelTileBase> {
    public AnimatedTemplateModelRenderer() {
        super(new AnimatedBlockTemplateModel());
    }
}