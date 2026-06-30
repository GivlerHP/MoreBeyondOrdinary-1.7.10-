package ru.givler.mbo.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.block.blockmodels.BlockModelFood;

public class ModelTileBase extends TileEntity {
    public String textureName;
    public String modelName;

    private int eatTimer = -1;
    private int eatSoundInterval = 0; // через сколько тиков повторять звук

    public ModelTileBase(String textureName, String modelName) {
        this.textureName = textureName;
        this.modelName = modelName;
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
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        textureName = tag.getString("texture");
        modelName = tag.getString("model");
        eatTimer = tag.hasKey("eatTimer") ? tag.getInteger("eatTimer") : -1;
        eatSoundInterval = tag.getInteger("eatSoundInterval");
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