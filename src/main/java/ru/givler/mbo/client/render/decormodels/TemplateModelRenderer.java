package ru.givler.mbo.client.render.decormodels;

import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.client.model.decormodels.DecorBlockModel;
import ru.givler.mbo.client.model.decormodels.DecorItemModel;

public class TemplateModelRenderer extends GeoAnimatedBlockRenderer<ModelTileBase> {
    public TemplateModelRenderer() {
        super(new DecorBlockModel(), new DecorItemModel());
    }
}
