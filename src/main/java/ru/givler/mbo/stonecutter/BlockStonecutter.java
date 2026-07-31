package ru.givler.mbo.stonecutter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockStonecutter extends Block {
    private IIcon side, bottom;
    public IIcon saw;
    private int renderType;

    public BlockStonecutter() {
        super(Material.rock);
        setBlockName("Stonecutter");
        setHardness(3.5F);
        setResistance(3.5F);
        setHarvestLevel("pickaxe", 0);
        setStepSound(soundTypeStone);
        setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        setBlockBounds(0, 0, 0, 1, .5625F, 1);
        setLightOpacity(0);
        useNeighborBrightness = true;
    }

    public void setStonecutterRenderType(int value) { renderType = value; }
    @Override public int getRenderType() { return renderType; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setBlockBounds(0, 0, 0, 1, .5625F, 1);
    }
    @Override public IIcon getIcon(int sideIndex, int meta) {
        return sideIndex == 1 ? blockIcon : sideIndex == 0 ? bottom : side;
    }
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister register) {
        blockIcon = register.registerIcon("mbo:stonecutter_top");
        side = register.registerIcon("mbo:stonecutter_side");
        bottom = register.registerIcon("mbo:stonecutter_bottom");
        saw = register.registerIcon("mbo:stonecutter_saw");
    }
    @Override public void onBlockPlacedBy(World world, int x, int y, int z,
                                           EntityLivingBase placer, ItemStack stack) {
        world.setBlockMetadataWithNotify(x, y, z,
                MathHelper.floor_double(placer.rotationYaw * 4F / 360F + .5D) & 3, 2);
    }
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                               int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) player.openGui(MoreBeyondOrdinary.instance,
                MoreBeyondOrdinary.GUI_STONECUTTER, world, x, y, z);
        return true;
    }
}
