package ru.givler.mbo.client.render.decormodels;

import ru.givler.mbo.client.model.decormodels.DecorItemModel;
import ru.givler.mbo.tileentity.ModelTileBase;

public class TemplateItemModelRenderer extends GeoItemBlockRenderer<ModelTileBase> {
    public TemplateItemModelRenderer() {
        super(new DecorItemModel());
    }
}
