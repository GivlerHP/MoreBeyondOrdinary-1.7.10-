package ru.givler.mbo.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.block.BlockMirror;

public final class ItemBlockMirror extends ItemBlock {
    public ItemBlockMirror(Block block) { super(block); }
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,
                                       int side,float hitX,float hitY,float hitZ) {
        if(stack.stackSize==0)return false;
        ForgeDirection normal=ForgeDirection.getOrientation(side);
        x+=normal.offsetX;y+=normal.offsetY;z+=normal.offsetZ;
        int facing=side<2?(((net.minecraft.util.MathHelper.floor_double(player.rotationYaw*4F/360F+.5)&3)+2)&3):side==3?0:side==4?1:side==2?2:3;
        BlockMirror mirror=(BlockMirror)field_150939_a;
        if(y<0 || y>=255 || !player.canPlayerEdit(x,y,z,side,stack)
            || !player.canPlayerEdit(x,y+1,z,side,stack)
            || !world.isAirBlock(x,y,z) || !world.isAirBlock(x,y+1,z))return false;
        if(world.isRemote)return true;
        // Place both halves without neighbour notifications, then expose the complete hanging.
        if(!world.setBlock(x,y,z,mirror,facing,2))return false;
        if(!world.setBlock(x,y+1,z,mirror,facing|4,2)) { world.setBlockToAir(x,y,z); return false; }
        world.notifyBlocksOfNeighborChange(x,y,z,mirror);
        world.notifyBlocksOfNeighborChange(x,y+1,z,mirror);
        world.playSoundEffect(x+.5,y+1,z+.5,"dig.wood",1,.8F);
        if(!player.capabilities.isCreativeMode)--stack.stackSize;
        return true;
    }
}
