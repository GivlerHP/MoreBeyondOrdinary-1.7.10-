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
    if(getOwnerBlock() instanceof ru.givler.mbo.block.model.BlockDecorCage
        || (getOwnerBlock()!=null&&getOwnerBlock()==ru.givler.mbo.registry.ModelRegistry.ModelTortureBedOnChains)) {
      float[][] shape=getBoxes();
      return shape.length==0?new float[]{0,0,0,0,0,0}:ru.givler.mbo.block.model.CollisionShapeParts.envelope(shape);
    }
    return bounds;
  }
  public float[][] getBoxes() {
    BlockModels owner=getOwnerBlock();
    if(owner!=null&&owner==ru.givler.mbo.registry.ModelRegistry.ModelTortureBedOnChains&&worldObj!=null) {
      if(yCoord!=ownerY)return new float[0][];
      float[] platform;
      switch(worldObj.getBlockMetadata(ownerX,ownerY,ownerZ)&3) {
        case 1:platform=new float[]{.125F,.2625F,.125F,1,.3875F,1.875F};break;
        case 2:platform=new float[]{-.875F,.2625F,.125F,.875F,.3875F,1};break;
        case 3:platform=new float[]{0,.2625F,-.875F,.875F,.3875F,.875F};break;
        default:platform=new float[]{.125F,.2625F,0,1.875F,.3875F,.875F};
      }
      int dx=xCoord-ownerX,dz=zCoord-ownerZ;
      float minX=Math.max(0,platform[0]-dx),minZ=Math.max(0,platform[2]-dz);
      float maxX=Math.min(1,platform[3]-dx),maxZ=Math.min(1,platform[5]-dz);
      if(minX>=maxX||minZ>=maxZ)return new float[0][];
      return new float[][]{{minX,.2625F,minZ,maxX,.3875F,maxZ}};
    }
    if(owner instanceof ru.givler.mbo.block.model.BlockDecorCage&&worldObj!=null)
      return xCoord!=ownerX||zCoord!=ownerZ?new float[0][]:((ru.givler.mbo.block.model.BlockDecorCage)owner).boxesForCell(yCoord-ownerY,worldObj.getBlockMetadata(ownerX,ownerY,ownerZ)&3);
    return boxes;
  }

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

  @Override public boolean canUpdate() { return false; }

  @Override public void validate() {
    super.validate();
    if(worldObj!=null && !worldObj.isRemote)
      worldObj.scheduleBlockUpdate(xCoord,yCoord,zCoord,
          ru.givler.mbo.registry.BlockRegistry.ModelCollisionPart,1);
  }

  public void checkOwner() {
    if(worldObj!=null && !worldObj.isRemote) {
      if(!worldObj.blockExists(ownerX,ownerY,ownerZ)) {
        worldObj.scheduleBlockUpdate(xCoord,yCoord,zCoord,
            ru.givler.mbo.registry.BlockRegistry.ModelCollisionPart,20);
        return;
      }
      BlockModels owner=getOwnerBlock();
      // Older saves may contain obsolete helpers above corpses or the cage roof.
      if(owner==null || (owner instanceof ru.givler.mbo.block.model.BlockDecorCage && (xCoord!=ownerX||zCoord!=ownerZ||yCoord!=ownerY+1))
          || owner==ru.givler.mbo.registry.ModelRegistry.ModelTortureHangedCorpse
          || (owner==ru.givler.mbo.registry.ModelRegistry.ModelTortureBedOnChains && yCoord!=ownerY)
          || (owner==ru.givler.mbo.registry.ModelRegistry.ModelTortureSuspendedCorpse && yCoord>=ownerY+2))
        worldObj.setBlockToAir(xCoord, yCoord, zCoord);
    }
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
