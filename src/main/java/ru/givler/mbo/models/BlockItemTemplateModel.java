package ru.givler.mbo.models;

import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.tileentity.ModelTileBase;
import software.bernie.geckolib3.model.provider.GeoModelProvider;

/** Plain Gecko provider for world-less item rendering. */
public class BlockItemTemplateModel extends GeoModelProvider<ModelTileBase> {

    @Override
    public ResourceLocation getModelLocation(ModelTileBase tile) {
        return new ResourceLocation(MoreBeyondOrdinary.MODID,
                "geo/" + tile.getModelName() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ModelTileBase tile) {
        // The old item renderer deliberately displayed the first texture frame.
        String frameSuffix = tile.frameCount > 1 ? "_0" : "";
        return new ResourceLocation(MoreBeyondOrdinary.MODID,
                "textures/models/decor/" + tile.getTextureName() + frameSuffix + ".png");
    }
}
