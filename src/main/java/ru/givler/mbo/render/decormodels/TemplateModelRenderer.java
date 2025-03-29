package ru.givler.mbo.render.decormodels;

import ru.givler.mbo.block.blockmodels.ModelTileBase;
import ru.givler.mbo.models.BlockTemplateModel;

public class TemplateModelRenderer extends GeoDefaultBlockRenderer<ModelTileBase> {
    public TemplateModelRenderer() {
        super(new BlockTemplateModel());
    }
}
