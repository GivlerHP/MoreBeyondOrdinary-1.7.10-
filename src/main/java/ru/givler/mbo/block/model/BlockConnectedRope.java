package ru.givler.mbo.block.model;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.registry.ModelRegistry;

/** One placeable rope, with geometry derived from its six neighboring ropes. */
public class BlockConnectedRope extends BlockModels {
    private static ru.givler.mbo.tileentity.ModelTileBase ropeTile(IBlockAccess world,int x,int y,int z) {
        net.minecraft.tileentity.TileEntity tile=world.getTileEntity(x,y,z);
        return tile instanceof ru.givler.mbo.tileentity.ModelTileBase ? (ru.givler.mbo.tileentity.ModelTileBase)tile : null;
    }
    private float[][] connectionBoxes(IBlockAccess world,int x,int y,int z,boolean anchor) {
        int mask=getConnectionMask(world,x,y,z);
        return isFloor(world,x,y,z)?RopeConnections.floorBoxes(mask,anchor):RopeConnections.centerBoxes(mask,anchor);
    }
    public boolean isFloor(IBlockAccess world,int x,int y,int z) {
        ru.givler.mbo.tileentity.ModelTileBase tile=ropeTile(world,x,y,z);
        return tile!=null && tile.ropeFloor;
    }
    public boolean hasConnector(IBlockAccess world,int x,int y,int z) {
        ru.givler.mbo.tileentity.ModelTileBase tile=ropeTile(world,x,y,z);
        if(tile!=null && tile.ropeAnchor) return true;
        return RopeConnections.isAngledConnection(getConnectionMask(world,x,y,z));
    }
    public void initializePlacement(World world,int x,int y,int z,int side,EntityPlayer player) {
        net.minecraftforge.common.util.ForgeDirection face=net.minecraftforge.common.util.ForgeDirection.getOrientation(side);
        int sx=x-face.offsetX,sy=y-face.offsetY,sz=z-face.offsetZ;
        Block source=world.getBlock(sx,sy,sz);
        ru.givler.mbo.tileentity.ModelTileBase tile=ropeTile(world,x,y,z);
        if(tile==null) return;
        tile.ropeFloor=source instanceof BlockConnectedRope
            ? side>1 && ((BlockConnectedRope)source).isFloor(world,sx,sy,sz)
            : side==1 && player.isSneaking()
                && world.isSideSolid(x,y-1,z,net.minecraftforge.common.util.ForgeDirection.UP);
        tile.ropeAnchor=this==ModelRegistry.ModelTortureRope3
            || (tile.ropeFloor && !(source instanceof BlockConnectedRope));
        if(tile.ropeFloor && side<2) {
            int yaw=net.minecraft.util.MathHelper.floor_double(player.rotationYaw*4/360.0+0.5)&3;
            world.setBlockMetadataWithNotify(x,y,z,(yaw&1)==1?1:2,3);
        }
        tile.markDirty();world.markBlockForUpdate(x,y,z);
        world.markBlockRangeForRenderUpdate(x-1,y-1,z-1,x+1,y+1,z+1);
        world.notifyBlocksOfNeighborChange(x,y,z,this);
    }
    public boolean trySetAnchor(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,int side) {
        ru.givler.mbo.tileentity.ModelTileBase tile=ropeTile(world,x,y,z);
        if(tile==null || RopeConnections.decoration(world.getBlockMetadata(x,y,z))!=RopeConnections.NONE) return false;
        if(tile.ropeAnchor) return true;
        if(stack.stackSize<=0 || !player.canPlayerEdit(x,y,z,side,stack)) return false;
        if(!world.isRemote) {
            tile.ropeAnchor=tile.ropeAnchorExtra=true;
            tile.markDirty();world.markBlockForUpdate(x,y,z);
            if(!player.capabilities.isCreativeMode) stack.stackSize--;
        }
        return true;
    }
    public BlockConnectedRope(String name, String oldResource) {
        super(Material.cloth, name, oldResource, oldResource);
        setCollisionEnabled(true);
        withRotatingBounds(0.4375F, 0, 0.4375F, 0.5625F, 1, 0.5625F);
    }

    public int getConnectionMask(IBlockAccess world, int x, int y, int z) {
        int metadata = world.getBlockMetadata(x, y, z);
        if (RopeConnections.decoration(metadata) != RopeConnections.NONE) {
            return RopeConnections.allowedConnections(metadata);
        }
        int mask = 0;
        if (connectsBack(world, x, y - 1, z, RopeConnections.UP)) mask |= RopeConnections.DOWN;
        if (connectsBack(world, x, y + 1, z, RopeConnections.DOWN)) mask |= RopeConnections.UP;
        if (connectsBack(world, x, y, z - 1, RopeConnections.SOUTH)) mask |= RopeConnections.NORTH;
        if (connectsBack(world, x, y, z + 1, RopeConnections.NORTH)) mask |= RopeConnections.SOUTH;
        if (connectsBack(world, x - 1, y, z, RopeConnections.EAST)) mask |= RopeConnections.WEST;
        if (connectsBack(world, x + 1, y, z, RopeConnections.WEST)) mask |= RopeConnections.EAST;
        // The clicked-face axis keeps the end attached to its solid support,
        // even after a neighboring rope replaces the isolated visual shape.
        int supportAxis=RopeConnections.visibleMask(0,metadata & 3);
        for(net.minecraftforge.common.util.ForgeDirection face:net.minecraftforge.common.util.ForgeDirection.VALID_DIRECTIONS) {
            int bit=1<<face.ordinal();
            if((supportAxis & bit)==0 || (isFloor(world,x,y,z) && face.offsetY!=0)) continue;
            int sx=x+face.offsetX,sy=y+face.offsetY,sz=z+face.offsetZ;
            Block support=world.getBlock(sx,sy,sz);
            if(support!=null && !(support instanceof BlockConnectedRope)
                    && support.isSideSolid(world,sx,sy,sz,face.getOpposite())) mask|=bit;
        }
        return RopeConnections.visibleMask(mask, metadata & 3);
    }

    private boolean connectsBack(IBlockAccess world, int x, int y, int z, int direction) {
        if(direction==RopeConnections.UP) {
            Block attachment=world.getBlock(x,y,z);
            net.minecraft.tileentity.TileEntity attachmentTile=world.getTileEntity(x,y,z);
            if(attachmentTile instanceof ru.givler.mbo.tileentity.TileEntityModelCollision) {
                ru.givler.mbo.tileentity.TileEntityModelCollision part=
                    (ru.givler.mbo.tileentity.TileEntityModelCollision)attachmentTile;
                if(part.getOwnerX()==x && part.getOwnerY()==y-1 && part.getOwnerZ()==z
                        && world.getBlock(x,y-1,z)==ModelRegistry.ModelTortureSuspendedCorpse) return true;
            }
            if(attachment!=null && (attachment==ModelRegistry.ModelTortureHook
                    || attachment==ModelRegistry.ModelTortureHangedCorpse)) return true;
            // The corpse's own upper rope occupies the cell above its owner.
            if(attachment!=null && world.getBlock(x,y-1,z)==ModelRegistry.ModelTortureHangedCorpse) {
                if(attachment.isAir(world,x,y,z)) return true;
                net.minecraft.tileentity.TileEntity oldTile=world.getTileEntity(x,y,z);
                if(oldTile instanceof ru.givler.mbo.tileentity.TileEntityModelCollision) {
                    ru.givler.mbo.tileentity.TileEntityModelCollision oldPart=
                        (ru.givler.mbo.tileentity.TileEntityModelCollision)oldTile;
                    if(oldPart.getOwnerX()==x && oldPart.getOwnerY()==y-1 && oldPart.getOwnerZ()==z)
                        return true;
                }
            }
        }
        if ((direction & RopeConnections.HORIZONTAL)!=0 && world.getBlock(x,y,z) instanceof BlockConnectedRope) {
            int currentX=x+(direction==RopeConnections.EAST?1:direction==RopeConnections.WEST?-1:0);
            int currentZ=z+(direction==RopeConnections.SOUTH?1:direction==RopeConnections.NORTH?-1:0);
            if(isFloor(world,currentX,y,currentZ)!=((BlockConnectedRope)world.getBlock(x,y,z)).isFloor(world,x,y,z))
                return false; // The two horizontal ports would be at different heights.
        }
        return world.getBlock(x, y, z) instanceof BlockConnectedRope
                && (RopeConnections.allowedConnections(world.getBlockMetadata(x, y, z)) & direction) != 0;
    }

    @Override public MovingObjectPosition collisionRayTrace(World world,int x,int y,int z,
            net.minecraft.util.Vec3 start,net.minecraft.util.Vec3 end) {
        if(!isFloor(world,x,y,z) && RopeConnections.decoration(world.getBlockMetadata(x,y,z))!=RopeConnections.NONE) return super.collisionRayTrace(world,x,y,z,start,end);
        if(ru.givler.mbo.core.HangingCameraContext.ignore(world,x,y,z)) return null;
        ru.givler.mbo.tileentity.ModelTileBase tile=ropeTile(world,x,y,z);
        MovingObjectPosition closest=null;
        for(float[] b:connectionBoxes(world,x,y,z,hasConnector(world,x,y,z)
                ||RopeConnections.decoration(world.getBlockMetadata(x,y,z))==RopeConnections.KNOT)) {
            MovingObjectPosition raw=net.minecraft.util.AxisAlignedBB.getBoundingBox(
                x+b[0],y+b[1],z+b[2],x+b[3],y+b[4],z+b[5]).calculateIntercept(start,end);
            if(raw!=null && (closest==null||start.squareDistanceTo(raw.hitVec)<start.squareDistanceTo(closest.hitVec)))
                closest=new MovingObjectPosition(x,y,z,raw.sideHit,raw.hitVec);
        }
        return closest;
    }

    public boolean tryDecorate(ItemStack stack, EntityPlayer player, World world,
                               int x, int y, int z, int side) {
        // Sneaking keeps ordinary placement available, including extending a lower end.
        if (player.isSneaking()) return false;
        int metadata = world.getBlockMetadata(x, y, z);
        if (RopeConnections.decoration(metadata) != RopeConnections.NONE) return true;
        int mask = getConnectionMask(world, x, y, z);
        if(isFloor(world,x,y,z) && RopeConnections.decorationFor(mask)==RopeConnections.NOOSE) return false;
        if (RopeConnections.decorationFor(mask) == RopeConnections.NONE) return false;
        if (stack.stackSize <= 0 || !player.canPlayerEdit(x, y, z, side, stack)) return false;
        if (!world.isRemote) {
            if (!world.setBlockMetadataWithNotify(x, y, z, RopeConnections.decoratedMetadata(mask), 3)) return false;
            if (!player.capabilities.isCreativeMode) stack.stackSize--;
            world.markBlockRangeForRenderUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
            world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "step.cloth", 0.8F, 1.0F);
        }
        return true;
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ, int metadata) {
        return RopeConnections.placementAxis(side);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z,
                               EntityLivingBase player, ItemStack stack) {
        // Keep the clicked-face axis; neighboring ropes determine all actual branches.
        world.markBlockRangeForRenderUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        world.markBlockRangeForRenderUpdate(x - 3, y - 3, z - 3, x + 3, y + 3, z + 3);
    }

    @Override public void breakBlock(World world,int x,int y,int z,Block block,int meta) {
        super.breakBlock(world,x,y,z,block,meta);
        world.markBlockRangeForRenderUpdate(x-3,y-3,z-3,x+3,y+3,z+3);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        // ItemBlock asks for the prospective collision box before the tile exists.
        if (!(world.getBlock(x,y,z) instanceof BlockConnectedRope) || ropeTile(world,x,y,z)==null) {
            setBlockBounds(0.4375F,0,0.4375F,0.5625F,1,0.5625F);
            return;
        }
        int metadata = world.getBlockMetadata(x, y, z);
        int decoration = RopeConnections.decoration(metadata);
        if(isFloor(world,x,y,z) || decoration==RopeConnections.NONE) {
            ru.givler.mbo.tileentity.ModelTileBase tile=ropeTile(world,x,y,z);
            float[] b=CollisionShapeParts.envelope(connectionBoxes(world,x,y,z,
                hasConnector(world,x,y,z) || decoration==RopeConnections.KNOT));
            setBlockBounds(b[0],b[1],b[2],b[3],b[4],b[5]);return;
        }
        if (decoration == RopeConnections.NOOSE) {
            setBlockBounds(0.25F, 0.25F, 0.375F, 0.75F, 1, 0.625F);
            return;
        }
        if (decoration == RopeConnections.KNOT) {
            if ((metadata & 3) == 3) setBlockBounds(0.15F, 0, 0.3125F, 0.7F, 1, 0.6875F);
            else if ((metadata & 3) == 2) setBlockBounds(0.3125F, 0.3125F, 0, 0.6875F, 0.6875F, 1);
            else setBlockBounds(0, 0.3125F, 0.3125F, 1, 0.6875F, 0.6875F);
            return;
        }
        int mask = getConnectionMask(world, x, y, z);
        if ((mask & RopeConnections.HORIZONTAL) == 0) {
            setBlockBounds(0.4375F, 0, 0.4375F, 0.5625F, 1, 0.5625F);
        } else if (mask == (RopeConnections.WEST | RopeConnections.EAST)) {
            setBlockBounds(0, 0.75F, 0.4375F, 1, 0.875F, 0.5625F);
        } else if (mask == (RopeConnections.NORTH | RopeConnections.SOUTH)) {
            setBlockBounds(0.4375F, 0.75F, 0, 0.5625F, 0.875F, 1);
        } else {
            setBlockBounds((mask & RopeConnections.WEST) != 0 ? 0 : 0.3125F,
                    (mask & RopeConnections.DOWN) != 0 ? 0 : 0.625F,
                    (mask & RopeConnections.NORTH) != 0 ? 0 : 0.3125F,
                    (mask & RopeConnections.EAST) != 0 ? 1 : 0.6875F, 1,
                    (mask & RopeConnections.SOUTH) != 0 ? 1 : 0.6875F);
        }
    }

    @Override
    public Item getItemDropped(int metadata, java.util.Random random, int fortune) {
        return Item.getItemFromBlock(ModelRegistry.ModelTortureRope);
    }

    @Override
    public int quantityDropped(int metadata, int fortune, java.util.Random random) {
        return RopeConnections.decoration(metadata) == RopeConnections.NONE ? 1 : 2;
    }

    @Override public java.util.ArrayList<ItemStack> getDrops(World world,int x,int y,int z,int meta,int fortune) {
        java.util.ArrayList<ItemStack> drops=super.getDrops(world,x,y,z,meta,fortune);
        ru.givler.mbo.tileentity.ModelTileBase tile=ropeTile(world,x,y,z);
        if(tile!=null && tile.ropeAnchorExtra) drops.add(new ItemStack(ModelRegistry.ModelTortureRope3));
        return drops;
    }

    @Override public void addCollisionBoxesToList(World world,int x,int y,int z,net.minecraft.util.AxisAlignedBB query,
            java.util.List list,net.minecraft.entity.Entity entity) {
        if(!isFloor(world,x,y,z) && RopeConnections.decoration(world.getBlockMetadata(x,y,z))!=RopeConnections.NONE) {super.addCollisionBoxesToList(world,x,y,z,query,list,entity);return;}
        ru.givler.mbo.tileentity.ModelTileBase tile=ropeTile(world,x,y,z);
        for(float[] b:connectionBoxes(world,x,y,z,
                hasConnector(world,x,y,z) || RopeConnections.decoration(world.getBlockMetadata(x,y,z))==RopeConnections.KNOT)) {
            net.minecraft.util.AxisAlignedBB box=net.minecraft.util.AxisAlignedBB.getBoundingBox(
                x+b[0],y+b[1],z+b[2],x+b[3],y+b[4],z+b[5]);
            if(box.intersectsWith(query)) list.add(box);
        }
    }

    @Override
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z,
                                  EntityPlayer player) {
        return new ItemStack(ModelRegistry.ModelTortureRope);
    }
}
