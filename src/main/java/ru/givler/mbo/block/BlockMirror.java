package ru.givler.mbo.block;

import java.util.Random;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import ru.givler.mbo.client.model.decormodels.DecorResources;
import ru.givler.mbo.client.render.decormodels.DecorAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.tileentity.TileMirror;

/** A two-block wall hanging, one texture pixel deep. Metadata keeps the original mirror facings. */
public final class BlockMirror extends BlockContainer {
    public static final int[] NX = {0, -1, 0, 1}, NZ = {1, 0, -1, 0};
    private final boolean silver;
    @SideOnly(Side.CLIENT) private IIcon modelParticleIcon;
    public BlockMirror() { this(false); }
    public BlockMirror(boolean silver) {
        super(Material.wood);
        this.silver = silver;
        setBlockName(silver ? "ModelSilverMirror" : "ModelMirror");
        setBlockTextureName("mbo:mirror");
        setCreativeTab(CreativeTabRegistry.tabMBOdecors);
        setHardness(.5F);
        setStepSound(soundTypeGlass);
        setLightOpacity(0);
    }
    public boolean isSilver() { return silver; }
    @SideOnly(Side.CLIENT)
    @Override public void registerBlockIcons(IIconRegister icons) {
        modelParticleIcon = DecorAtlasSprite.registerParticles(icons,
            DecorResources.texture(silver ? "mirror_silver" : "mirror", ""));
        blockIcon = modelParticleIcon;
    }

    @SideOnly(Side.CLIENT)
    @Override public IIcon getIcon(int side, int metadata) {
        return modelParticleIcon != null ? modelParticleIcon : Blocks.glass.getIcon(side, 0);
    }

    @Override public TileEntity createNewTileEntity(World world, int meta) {
        if ((meta & 4) != 0) return null;
        return silver ? new ru.givler.mbo.tileentity.TileSilverMirror() : new TileMirror();
    }
    @Override public boolean hasTileEntity(int meta) { return (meta & 4) == 0; }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getRenderType() { return -1; }
    @Override public boolean isSideSolid(IBlockAccess w,int x,int y,int z,ForgeDirection side) { return false; }
    @Override public void setBlockBoundsBasedOnState(IBlockAccess world,int x,int y,int z) {
        int f=world.getBlockMetadata(x,y,z)&3;
        float d=1F/16;
        setBlockBounds(f==1?1-d:0,0,f==2?1-d:0,f==3?d:1,1,f==0?d:1);
    }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World w,int x,int y,int z) {
        int facing=w.getBlockMetadata(x,y,z)&3;
        double depth=1.0/16;
        return AxisAlignedBB.getBoundingBox(x+(facing==1?1-depth:0),y,z+(facing==2?1-depth:0),
            x+(facing==3?depth:1),y+1,z+(facing==0?depth:1));
    }
    @Override public void onNeighborBlockChange(World w,int x,int y,int z,Block changed) {
        if(w.isRemote)return;
        int meta=w.getBlockMetadata(x,y,z),bottom=y-((meta&4)!=0?1:0),f=meta&3;
        boolean pair=w.getBlock(x,bottom,z)==this && w.getBlockMetadata(x,bottom,z)==f
            && w.getBlock(x,bottom+1,z)==this && w.getBlockMetadata(x,bottom+1,z)==(f|4);
        if(!pair) {
            if((meta&4)==0)dropBlockAsItem(w,x,y,z,meta,0);
            w.setBlockToAir(x,y,z);
        }
    }
    @Override public void onBlockHarvested(World w,int x,int y,int z,int meta,net.minecraft.entity.player.EntityPlayer player) {
        if (!w.isRemote && (meta & 4) != 0 && player.capabilities.isCreativeMode
                && w.getBlock(x,y-1,z)==this) w.setBlockToAir(x,y-1,z);
        super.onBlockHarvested(w,x,y,z,meta,player);
    }
    @Override public int quantityDropped(int meta,int fortune,Random random) { return (meta&4)==0?1:0; }
    @Override public Item getItemDropped(int meta,Random random,int fortune) { return Item.getItemFromBlock(this); }
    @Override public int damageDropped(int meta) { return 0; }
}
