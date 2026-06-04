package ru.givler.mbo.models;

import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.tileentity.AnimatedModelTileBase;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AnimatedBlockTemplateModel extends AnimatedGeoModel<AnimatedModelTileBase> {

    @Override
    public ResourceLocation getModelLocation(AnimatedModelTileBase tile) {
        return new ResourceLocation(MoreBeyondOrdinary.MODID, "geo/" + tile.modelName + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(AnimatedModelTileBase tile) {
        if (tile.frameCount <= 1) {
            return new ResourceLocation(MoreBeyondOrdinary.MODID,
                    "textures/models/decor/" + tile.textureName + ".png");
        }
        int frame = (int)((System.currentTimeMillis() / tile.frameSpeed) % tile.frameCount);
        return new ResourceLocation(MoreBeyondOrdinary.MODID,
                "textures/models/decor/" + tile.textureName + "_" + frame + ".png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(AnimatedModelTileBase tile) {
        return new ResourceLocation(MoreBeyondOrdinary.MODID, "animations/" + tile.modelName + ".animation.json");
    }
}