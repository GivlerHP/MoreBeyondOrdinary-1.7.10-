package ru.givler.mbo.client.render.decormodels;

import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.models.BlockTemplateModel;
import ru.givler.mbo.models.BlockItemTemplateModel;

public class TemplateModelRenderer extends GeoAnimatedBlockRenderer<ModelTileBase> {
    public TemplateModelRenderer() {
        super(new BlockTemplateModel(), new BlockItemTemplateModel());
    }
}
