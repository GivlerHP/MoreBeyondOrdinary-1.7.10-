package ru.givler.mbo.tileentity;


import net.minecraft.nbt.NBTTagCompound;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class AnimatedModelTileBase extends ModelTileBase implements IAnimatable {

    private final AnimationFactory factory = new AnimationFactory(this);
    private final String animationName;
    public boolean isItemRender = false;
    public int frameCount = 1;
    public int frameSpeed = 100;

    public AnimatedModelTileBase(String textureName, String modelName, String animationName) {
        super(textureName, modelName);
        this.animationName = animationName;
    }

    public static AnimatedModelTileBase createForItemRender(String textureName, String modelName, String animationName) {
        AnimatedModelTileBase tile = new AnimatedModelTileBase(textureName, modelName, animationName);
        tile.isItemRender = true;
        return tile;
    }

    public static AnimatedModelTileBase createAnimated(String textureName, String modelName, String animationName, int frameCount, int frameSpeed) {
        AnimatedModelTileBase tile = new AnimatedModelTileBase(textureName, modelName, animationName);
        tile.frameCount = frameCount;
        tile.frameSpeed = frameSpeed;
        return tile;
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(
                new AnimationBuilder().addAnimation(animationName, true)
        );
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        if (isItemRender) return;
        data.addAnimationController(
                new AnimationController<>(this, "controller", 0, this::predicate)
        );
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("animation", animationName);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
    }
}