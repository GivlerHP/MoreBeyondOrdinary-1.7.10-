package ru.givler.mbo.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import ru.givler.mbo.tileentity.TileSilverMirror;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.block.BlockMirror;

public final class ItemBlockMirror extends ItemBlock {
    public ItemBlockMirror(Block block) { super(block); }
    @Override public boolean onItemUse(ItemStack stack,EntityPlayer player,World world,int x,int y,int z,
                                       int side,float hitX,float hitY,float hitZ) {
        if(stack.stackSize==0)return false;
        BlockMirror mirror=(BlockMirror)field_150939_a;
        if (mirror.isSilver() && world.getBlock(x,y,z) == mirror) {
            int bottom = y - ((world.getBlockMetadata(x,y,z) & 4) != 0 ? 1 : 0);
            if (!player.canPlayerEdit(x,bottom,z,side,stack)) return false;
            if (!world.isRemote) rememberMirror(stack, player, world, x, bottom, z);
            return true;
        }
        ForgeDirection normal=ForgeDirection.getOrientation(side);
        x+=normal.offsetX;y+=normal.offsetY;z+=normal.offsetZ;
        int facing=side<2?(((net.minecraft.util.MathHelper.floor_double(player.rotationYaw*4F/360F+.5)&3)+2)&3):side==3?0:side==4?1:side==2?2:3;
        if(y<0 || y>=255 || !player.canPlayerEdit(x,y,z,side,stack)
            || !player.canPlayerEdit(x,y+1,z,side,stack)
            || !world.isAirBlock(x,y,z) || !world.isAirBlock(x,y+1,z))return false;
        if(world.isRemote)return true;
        TileSilverMirror destination = null;
        if (mirror.isSilver() && stack.hasTagCompound() && stack.getTagCompound().hasKey("SilverMirrorSource")) {
            int[] link = stack.getTagCompound().getIntArray("SilverMirrorSource");
            TileEntity tile = link.length == 4 && link[0] == world.provider.dimensionId
                && world.blockExists(link[1],link[2],link[3]) ? world.getTileEntity(link[1],link[2],link[3]) : null;
            if (!(tile instanceof TileSilverMirror) || !((TileSilverMirror)tile).available()) {
                player.addChatMessage(new ChatComponentTranslation("mbo.silverMirror.unavailable"));
                return false;
            }
            destination = (TileSilverMirror)tile;
        }
        // Place both halves without neighbour notifications, then expose the complete hanging.
        if(!world.setBlock(x,y,z,mirror,facing,2))return false;
        if(!world.setBlock(x,y+1,z,mirror,facing|4,2)) { world.setBlockToAir(x,y,z); return false; }
        if (destination != null) {
            TileEntity tile = world.getTileEntity(x,y,z);
            if (!(tile instanceof TileSilverMirror) || !((TileSilverMirror)tile).connect(destination)) {
                world.setBlockToAir(x,y+1,z);
                if (world.getBlock(x,y,z) == mirror) world.setBlockToAir(x,y,z);
                return false;
            }
        }
        world.notifyBlocksOfNeighborChange(x,y,z,mirror);
        world.notifyBlocksOfNeighborChange(x,y+1,z,mirror);
        world.playSoundEffect(x+.5,y+1,z+.5,"dig.wood",1,.8F);
        if(!player.capabilities.isCreativeMode)--stack.stackSize;
        if (destination != null) stack.getTagCompound().removeTag("SilverMirrorSource");
        return true;
    }

    private void rememberMirror(ItemStack stack, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x,y,z);
        if (!(tile instanceof TileSilverMirror) || !((TileSilverMirror)tile).available()) {
            player.addChatMessage(new ChatComponentTranslation("mbo.silverMirror.unavailable"));
            return;
        }
        if (stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setIntArray("SilverMirrorSource", new int[]{world.provider.dimensionId,x,y,z});
        player.addChatMessage(new ChatComponentTranslation("mbo.silverMirror.remembered"));
    }
}
