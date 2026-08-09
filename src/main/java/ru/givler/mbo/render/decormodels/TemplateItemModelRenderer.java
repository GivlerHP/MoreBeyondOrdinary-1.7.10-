package ru.givler.mbo.render.decormodels;

import ru.givler.mbo.models.BlockItemTemplateModel;
import ru.givler.mbo.tileentity.ModelTileBase;

public class TemplateItemModelRenderer extends GeoItemBlockRenderer<ModelTileBase> {
    public TemplateItemModelRenderer() {
        super(new BlockItemTemplateModel());
    }
}
