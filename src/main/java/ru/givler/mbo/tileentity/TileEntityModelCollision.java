package ru.givler.mbo.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import ru.givler.mbo.block.BlockModels;

public class TileEntityModelCollision extends TileEntity {
  private int ownerX;
  private int ownerY;
  private int ownerZ;
  private float[] bounds = {0, 0, 0, 1, 1, 1};
  private float[][] boxes = {bounds};

  public void configure(int x, int y, int z, float[] rotatedBounds) {
    configure(x,y,z,new float[][] {rotatedBounds});
  }
  public void configure(int x, int y, int z, float[][] rotatedBoxes) {
    ownerX = x;
    ownerY = y;
    ownerZ = z;
    boxes = rotatedBoxes;
    bounds = ru.givler.mbo.block.model.CollisionShapeParts.envelope(boxes);
    markDirty();
  }

  public float[] getBounds() {
    return bounds;
  }
  public float[][] getBoxes() { return boxes; }

  public int getOwnerX() {
    return ownerX;
  }

  public int getOwnerY() {
    return ownerY;
  }

  public int getOwnerZ() {
    return ownerZ;
  }

  public BlockModels getOwnerBlock() {
    if (worldObj == null) return null;
    return worldObj.getBlock(ownerX, ownerY, ownerZ) instanceof BlockModels
        ? (BlockModels) worldObj.getBlock(ownerX, ownerY, ownerZ)
        : null;
  }

  @Override
  public void updateEntity() {
    if (!worldObj.isRemote && ticksExisted() % 20 == 0) {
      BlockModels owner=getOwnerBlock();
      // Older saves still contain the corpse's removed upper helper block.
      if(owner==null || owner==ru.givler.mbo.registry.ModelRegistry.ModelTortureHangedCorpse
          || (owner==ru.givler.mbo.registry.ModelRegistry.ModelTortureSuspendedCorpse && yCoord>=ownerY+2))
        worldObj.setBlockToAir(xCoord, yCoord, zCoord);
    }
  }

  private int ticks;

  private int ticksExisted() {
    return ++ticks;
  }

  @Override
  public void writeToNBT(NBTTagCompound tag) {
    super.writeToNBT(tag);
    tag.setInteger("ownerX", ownerX);
    tag.setInteger("ownerY", ownerY);
    tag.setInteger("ownerZ", ownerZ);
    for (int i = 0; i < 6; i++) tag.setFloat("bound" + i, bounds[i]);
    net.minecraft.nbt.NBTTagList shapes=new net.minecraft.nbt.NBTTagList();
    for(float[] box:boxes) {
      NBTTagCompound entry=new NBTTagCompound();
      for(int i=0;i<6;i++) entry.setFloat("b"+i,box[i]);
      shapes.appendTag(entry);
    }
    tag.setTag("shapeBoxes",shapes);
  }

  @Override
  public void readFromNBT(NBTTagCompound tag) {
    super.readFromNBT(tag);
    ownerX = tag.getInteger("ownerX");
    ownerY = tag.getInteger("ownerY");
    ownerZ = tag.getInteger("ownerZ");
    for (int i = 0; i < 6; i++) bounds[i] = tag.getFloat("bound" + i);
    net.minecraft.nbt.NBTTagList shapes=tag.getTagList("shapeBoxes",10);
    if(shapes.tagCount()==0) boxes=new float[][] {bounds};
    else {
      boxes=new float[shapes.tagCount()][6];
      for(int j=0;j<boxes.length;j++) for(int i=0;i<6;i++)
        boxes[j][i]=shapes.getCompoundTagAt(j).getFloat("b"+i);
    }
  }

  @Override
  public Packet getDescriptionPacket() {
    NBTTagCompound tag = new NBTTagCompound();
    writeToNBT(tag);
    return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
  }

  @Override
  public void onDataPacket(NetworkManager network, S35PacketUpdateTileEntity packet) {
    readFromNBT(packet.func_148857_g());
    if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
  }
}
