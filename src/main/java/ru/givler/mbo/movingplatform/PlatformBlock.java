package ru.givler.mbo.movingplatform;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public final class PlatformBlock {
  public final int x, y, z;
  public int meta;
  public final Block block;
  public boolean collidable = true;
  public NBTTagCompound tileData;
  public int ownerX,ownerY,ownerZ;
  public double[][] collisionBoxes;
  public double minX, minY, minZ, maxX = 1D, maxY = 1D, maxZ = 1D;

  public PlatformBlock(int x, int y, int z, Block block, int meta) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.block = block;
    this.meta = meta;
  }

  public void captureCollision(World world, int worldX, int worldY, int worldZ) {
    net.minecraft.tileentity.TileEntity tile=block instanceof ru.givler.mbo.block.BlockModels
        ||block instanceof ru.givler.mbo.block.model.BlockModelCollision?world.getTileEntity(worldX,worldY,worldZ):null;
    if(tile instanceof ru.givler.mbo.tileentity.ModelTileBase||tile instanceof ru.givler.mbo.tileentity.TileEntityModelCollision) {
      tileData=new NBTTagCompound();tile.writeToNBT(tileData);
      if(block instanceof ru.givler.mbo.block.model.BlockConnectedRope) {
        ru.givler.mbo.block.model.BlockConnectedRope rope=(ru.givler.mbo.block.model.BlockConnectedRope)block;
        tileData.setInteger("PlatformRopeMask",rope.getConnectionMask(world,worldX,worldY,worldZ));
        tileData.setBoolean("PlatformRopeConnector",rope.hasConnector(world,worldX,worldY,worldZ));
      }
      if(tile instanceof ru.givler.mbo.tileentity.TileEntityModelCollision) {
        ru.givler.mbo.tileentity.TileEntityModelCollision helper=(ru.givler.mbo.tileentity.TileEntityModelCollision)tile;
        ownerX=helper.getOwnerX()-worldX+x;ownerY=helper.getOwnerY()-worldY+y;ownerZ=helper.getOwnerZ()-worldZ+z;
      }
      java.util.List<AxisAlignedBB> boxes=new java.util.ArrayList<>();
      block.addCollisionBoxesToList(world,worldX,worldY,worldZ,AxisAlignedBB.getBoundingBox(worldX-16,worldY-16,worldZ-16,worldX+16,worldY+16,worldZ+16),boxes,null);
      collisionBoxes=new double[boxes.size()][6];
      for(int i=0;i<boxes.size();i++) {
        AxisAlignedBB b=boxes.get(i);collisionBoxes[i]=new double[]{b.minX-worldX,b.minY-worldY,b.minZ-worldZ,b.maxX-worldX,b.maxY-worldY,b.maxZ-worldZ};
      }
    }
    block.setBlockBoundsBasedOnState(world, worldX, worldY, worldZ);
    AxisAlignedBB box = block.getCollisionBoundingBoxFromPool(world, worldX, worldY, worldZ);
    collidable = box != null;
    if (box == null) return;
    minX = box.minX - worldX; minY = box.minY - worldY; minZ = box.minZ - worldZ;
    maxX = box.maxX - worldX; maxY = box.maxY - worldY; maxZ = box.maxZ - worldZ;
  }

  public NBTTagCompound write() {
    NBTTagCompound tag = new NBTTagCompound();
    tag.setInteger("X", x);
    tag.setInteger("Y", y);
    tag.setInteger("Z", z);
    tag.setString("Block", String.valueOf(GameData.getBlockRegistry().getNameForObject(block)));
    tag.setByte("Meta", (byte) meta);
    tag.setBoolean("Collidable", collidable);
    if(tileData!=null)tag.setTag("Tile",tileData.copy());
    tag.setInteger("OwnerX",ownerX);tag.setInteger("OwnerY",ownerY);tag.setInteger("OwnerZ",ownerZ);
    if(collisionBoxes!=null) {
      net.minecraft.nbt.NBTTagList boxes=new net.minecraft.nbt.NBTTagList();
      for(double[] b:collisionBoxes){NBTTagCompound entry=new NBTTagCompound();for(int i=0;i<6;i++)entry.setDouble("B"+i,b[i]);boxes.appendTag(entry);}
      tag.setTag("Boxes",boxes);
    }
    tag.setDouble("MinX", minX); tag.setDouble("MinY", minY); tag.setDouble("MinZ", minZ);
    tag.setDouble("MaxX", maxX); tag.setDouble("MaxY", maxY); tag.setDouble("MaxZ", maxZ);
    return tag;
  }

  public static PlatformBlock read(NBTTagCompound tag) {
    Block block = GameData.getBlockRegistry().getObject(tag.getString("Block"));
    if (block == null) return null;
    PlatformBlock result = new PlatformBlock(
            tag.getInteger("X"),
            tag.getInteger("Y"),
            tag.getInteger("Z"),
            block,
            tag.getByte("Meta") & 15);
    if(tag.hasKey("Tile"))result.tileData=(NBTTagCompound)tag.getCompoundTag("Tile").copy();
    result.ownerX=tag.getInteger("OwnerX");result.ownerY=tag.getInteger("OwnerY");result.ownerZ=tag.getInteger("OwnerZ");
    if(tag.hasKey("Boxes")) {
      net.minecraft.nbt.NBTTagList boxes=tag.getTagList("Boxes",10);result.collisionBoxes=new double[boxes.tagCount()][6];
      for(int j=0;j<boxes.tagCount();j++)for(int i=0;i<6;i++)result.collisionBoxes[j][i]=boxes.getCompoundTagAt(j).getDouble("B"+i);
    }
    if (tag.hasKey("Collidable")) {
      result.collidable = tag.getBoolean("Collidable");
      result.minX = tag.getDouble("MinX"); result.minY = tag.getDouble("MinY"); result.minZ = tag.getDouble("MinZ");
      result.maxX = tag.getDouble("MaxX"); result.maxY = tag.getDouble("MaxY"); result.maxZ = tag.getDouble("MaxZ");
    }
    return result;
  }

  public void restoreTile(World world,int ox,int oy,int oz) {
    if(tileData==null)return;
    NBTTagCompound tag=(NBTTagCompound)tileData.copy();tag.setInteger("x",ox+x);tag.setInteger("y",oy+y);tag.setInteger("z",oz+z);
    if(block instanceof ru.givler.mbo.block.model.BlockModelCollision) {
      tag.setInteger("ownerX",ox+ownerX);tag.setInteger("ownerY",oy+ownerY);tag.setInteger("ownerZ",oz+ownerZ);
    }
    net.minecraft.tileentity.TileEntity tile=net.minecraft.tileentity.TileEntity.createAndLoadEntity(tag);
    if(tile!=null) {world.setTileEntity(ox+x,oy+y,oz+z,tile);world.markBlockForUpdate(ox+x,oy+y,oz+z);}
  }
}
