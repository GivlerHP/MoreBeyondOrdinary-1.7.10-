package ru.givler.mbo.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.block.blockmodels.BlockModelFood;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class ModelTileBase extends TileEntity implements IAnimatable {
    public String textureName;
    public String modelName;

    private int eatTimer = -1;
    protected String animationName;
    protected boolean loopAnimation = true;
    public int frameCount = 1;
    public int frameSpeed = 100;
    private final AnimationFactory factory = new AnimationFactory(this);
    private int eatSoundInterval = 0; // через сколько тиков повторять звук

    public ModelTileBase(String textureName, String modelName) {
        this(textureName, modelName, null, false);
    }

    public ModelTileBase(String textureName, String modelName, String animationName, boolean loopAnimation) {
        this.textureName = textureName;
        this.modelName = modelName;
        this.animationName = animationName;
        this.loopAnimation = loopAnimation;
    }

    public ModelTileBase() {
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("texture", textureName);
        tag.setString("model", modelName);
        tag.setInteger("eatTimer", eatTimer);
        tag.setInteger("eatSoundInterval", eatSoundInterval);
        if (animationName != null) tag.setString("animation", animationName);
        tag.setBoolean("loop", loopAnimation);
        tag.setInteger("frameCount", frameCount);
        tag.setInteger("frameSpeed", frameSpeed);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        textureName = tag.getString("texture");
        modelName = tag.getString("model");
        eatTimer = tag.hasKey("eatTimer") ? tag.getInteger("eatTimer") : -1;
        eatSoundInterval = tag.getInteger("eatSoundInterval");
        animationName = tag.hasKey("animation") ? tag.getString("animation") : null;
        loopAnimation = !tag.hasKey("loop") || tag.getBoolean("loop");
        if (tag.hasKey("frameCount")) frameCount = tag.getInteger("frameCount");
        if (tag.hasKey("frameSpeed")) frameSpeed = tag.getInteger("frameSpeed");
    }

    public void startEating(int delay, int soundInterval) {
        this.eatTimer = delay;
        this.eatSoundInterval = soundInterval;
    }

    public boolean isEating() {
        return eatTimer > 0;
    }

    public void setModelName(String name) {
        this.modelName = name;
    }

    public String getModelName() {
        return modelName;
    }

    public String getTextureName() {
        return textureName;
    }

    public String getAnimationName() { return animationName; }

    public boolean isAnimated() { return animationName != null && !animationName.isEmpty(); }

    @Override
    public void registerControllers(AnimationData data) {
        if (!isAnimated()) return;
        data.addAnimationController(new AnimationController<>(this, "mbo_model_controller", 0, this::animationPredicate));
    }

    private <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
        AnimationBuilder builder = loopAnimation
            ? new AnimationBuilder().loop(animationName)
            : new AnimationBuilder().playOnce(animationName);
        event.getController().setAnimation(builder);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() { return factory; }

    @Override
    public net.minecraft.util.AxisAlignedBB getRenderBoundingBox() {
        return net.minecraft.util.AxisAlignedBB.getBoundingBox(
                xCoord - 2, yCoord - 2, zCoord - 2,
                xCoord + 3, yCoord + 3, zCoord + 3);
    }

    @Override
    public void updateEntity() {
        if (eatTimer > 0) {
            // Повторяем звук каждый eatSoundInterval тиков
            if (eatSoundInterval > 0 && eatTimer % eatSoundInterval == 0
                    && getBlockType() instanceof BlockModelFood) {
                ((BlockModelFood) getBlockType()).playEatSound(worldObj, xCoord, yCoord, zCoord);
            }

            eatTimer--;

            if (eatTimer == 0 && getBlockType() instanceof BlockModelFood) {
                ((BlockModelFood) getBlockType()).onEatingFinished(worldObj, xCoord, yCoord, zCoord);
            }
        }
    }
}
