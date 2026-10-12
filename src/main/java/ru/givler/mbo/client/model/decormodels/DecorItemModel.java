package ru.givler.mbo.client.model.decormodels;

import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.tileentity.ModelTileBase;
import software.bernie.geckolib3.model.provider.GeoModelProvider;

/** Plain Gecko provider for world-less item rendering. */
public class DecorItemModel extends GeoModelProvider<ModelTileBase> {

    @Override
    public ResourceLocation getModelLocation(ModelTileBase tile) {
        if("instruments_of_torture/iron_cage".equals(tile.getModelName()))return DecorResources.model("instruments_of_torture/iron_cage_item");
        return DecorResources.model(tile.getModelName());
    }

    @Override
    public ResourceLocation getTextureLocation(ModelTileBase tile) {
        // The old item renderer deliberately displayed the first texture frame.
        String frameSuffix = tile.getFrameCount() > 1 ? "_0" : "";
        return DecorResources.texture(tile.getTextureName(), frameSuffix);
    }
}
