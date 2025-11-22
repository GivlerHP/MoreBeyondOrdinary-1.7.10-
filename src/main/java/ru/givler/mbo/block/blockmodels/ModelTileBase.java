package ru.givler.mbo.block.blockmodels;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class ModelTileBase extends TileEntity {
    public String textureName;
    public String modelName;

    public ModelTileBase(String textureName, String modelName){
        this.textureName = textureName;
        this.modelName = modelName;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("texture", textureName);
        tag.setString("model", modelName);
    }

    @Override
    public void readFromNBT(NBTTagCompound p_145839_1_) {
        textureName = p_145839_1_.getString("texture");
        modelName = p_145839_1_.getString("model");
        super.readFromNBT(p_145839_1_);
    }
}
