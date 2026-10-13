package ru.givler.mbo.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.block.model.BlockModelFood;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class ModelTileBase extends TileEntity implements IAnimatable {
    public boolean ropeFloor, ropeAnchor, ropeAnchorExtra;
    public boolean cageOpen;
    // Render-only connectivity captured when a rope becomes part of a moving platform.
    public int movingPlatformRopeMask=-1;
    public boolean movingPlatformRopeConnector;
    public int movingPlatformLight=-1;
    public double movingPlatformLightX=Double.NaN, movingPlatformLightY, movingPlatformLightZ;
    private Boolean lastCageAnimationOpen;
    private String cageAnimation;
    public String textureName;
    public String modelName;

    private int eatTimer = -1;
    protected String animationName;
    protected boolean loopAnimation = true;
    public int frameCount = 1;
    public int frameSpeed = 100;
    private AnimationFactory factory;

    private ru.givler.mbo.block.BlockModels registeredModel() {
        if(getClass()!=ModelTileBase.class || (worldObj==null && blockType==null)) return null;
        net.minecraft.block.Block block=getBlockType();
        return block instanceof ru.givler.mbo.block.BlockModels ? (ru.givler.mbo.block.BlockModels)block : null;
    }

    @Override public boolean canUpdate() {
        if(getClass()!=ModelTileBase.class) return true;
        ru.givler.mbo.block.BlockModels block=registeredModel();
        return block!=null ? block.needsModelLogicTicks() : eatTimer>0;
    }

    public boolean usesDynamicRender() {
        if(getClass()!=ModelTileBase.class) return true;
        ru.givler.mbo.block.BlockModels block=registeredModel();
        return block!=null ? block.usesDynamicModelRender() : isAnimated() || getFrameCount()>1;
    }

    public int getFrameCount() {
        ru.givler.mbo.block.BlockModels block=registeredModel();
        return block==null ? frameCount : block.getModelFrameCount();
    }
    public int getFrameSpeed() {
        ru.givler.mbo.block.BlockModels block=registeredModel();
        return block==null ? frameSpeed : block.getModelFrameSpeed();
    }
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

    @Override public net.minecraft.network.Packet getDescriptionPacket() {
        NBTTagCompound tag=new NBTTagCompound();
        writeToNBT(tag);
        return new net.minecraft.network.play.server.S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,0,tag);
    }
    @Override public void onDataPacket(net.minecraft.network.NetworkManager manager,
            net.minecraft.network.play.server.S35PacketUpdateTileEntity packet) {
        readFromNBT(packet.func_148857_g());
        if(worldObj!=null) {
            int radius=getBlockType() instanceof ru.givler.mbo.block.model.BlockConnectedRope?3:1;
            worldObj.markBlockRangeForRenderUpdate(xCoord-radius,yCoord-radius,zCoord-radius,
                xCoord+radius,yCoord+radius,zCoord+radius);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("texture", textureName);
        tag.setString("model", modelName);
        tag.setBoolean("ropeFloor", ropeFloor);
        tag.setBoolean("cageOpen",cageOpen);
        tag.setBoolean("ropeAnchor", ropeAnchor);
        tag.setBoolean("ropeAnchorExtra", ropeAnchorExtra);
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
        cageOpen=tag.getBoolean("cageOpen");
        modelName = tag.getString("model");
        ropeFloor = tag.getBoolean("ropeFloor");
        ropeAnchor = tag.getBoolean("ropeAnchor");
        ropeAnchorExtra = tag.getBoolean("ropeAnchorExtra");
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
        ru.givler.mbo.block.BlockModels block=registeredModel();
        return block==null ? modelName : block.getModelName();
    }

    public String getTextureName() {
        ru.givler.mbo.block.BlockModels block=registeredModel();
        return block==null ? textureName : block.getTextureName();
    }

    public String getAnimationName() {
        if((worldObj!=null||blockType!=null)&&getBlockType() instanceof ru.givler.mbo.block.model.BlockDecorCage) return "animation.iron_cage.closed";
        ru.givler.mbo.block.BlockModels block=registeredModel();
        return block==null ? animationName : block.getModelAnimationName();
    }

    public boolean isAnimated() { String animation=getAnimationName(); return animation!=null && !animation.isEmpty(); }

    @Override
    public void registerControllers(AnimationData data) {
        if (!isAnimated()) return;
        data.addAnimationController(new AnimationController<>(this, "mbo_model_controller", 0, this::animationPredicate));
    }

    private <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
        if(getBlockType() instanceof ru.givler.mbo.block.model.BlockDecorCage) {
            if(lastCageAnimationOpen==null) {
                cageAnimation=cageOpen?"animation.iron_cage.opened":"animation.iron_cage.closed";
            } else if(lastCageAnimationOpen.booleanValue()!=cageOpen) {
                cageAnimation=cageOpen?"animation.iron_cage.open":"animation.iron_cage.close";
                event.getController().markNeedsReload();
            }
            lastCageAnimationOpen=cageOpen;
            event.getController().setAnimation(new AnimationBuilder().playAndHold(cageAnimation));
            return PlayState.CONTINUE;
        }
        ru.givler.mbo.block.BlockModels block=registeredModel();
        boolean loop=block==null ? loopAnimation : block.isModelAnimationLooped();
        AnimationBuilder builder = loop
            ? new AnimationBuilder().loop(getAnimationName())
            : new AnimationBuilder().playOnce(getAnimationName());
        event.getController().setAnimation(builder);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        if(factory==null) factory=new AnimationFactory(this);
        return factory;
    }

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
