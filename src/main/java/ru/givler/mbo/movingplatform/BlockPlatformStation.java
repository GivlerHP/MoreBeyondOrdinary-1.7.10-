package ru.givler.mbo.movingplatform;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockPlatformStation extends BlockContainer {
    public BlockPlatformStation(){super(Material.iron);setBlockName("PlatformStation");setBlockTextureName("minecraft:iron_block");setHardness(2F);setResistance(10F);setCreativeTab(CreativeTabRegistry.tabMBOblocks);GameRegistry.registerBlock(this,ItemBlockPlatformStation.class,"PlatformStation");}
    @Override public TileEntity createNewTileEntity(World world,int meta){return new TileEntityPlatformStation();}
    @Override public void onBlockPlacedBy(World world,int x,int y,int z,EntityLivingBase placer,ItemStack stack){
        TileEntity tile=world.getTileEntity(x,y,z);if(tile instanceof TileEntityPlatformStation&&stack!=null&&stack.hasTagCompound())((TileEntityPlatformStation)tile).readLink(stack.getTagCompound());
    }
    @Override public void onNeighborBlockChange(World world,int x,int y,int z,net.minecraft.block.Block neighbor){TileEntity tile=world.getTileEntity(x,y,z);if(tile instanceof TileEntityPlatformStation)((TileEntityPlatformStation)tile).updatePower();}
}
