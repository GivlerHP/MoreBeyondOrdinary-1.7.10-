package ru.givler.mbo.banner;

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
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;

public class BlockLoom extends Block {
    private final IIcon[] top = new IIcon[4];
    private IIcon bottom, side, front;
    public BlockLoom() {
        super(Material.wood);
        setBlockName("Loom");
        setHardness(2.5F);
        setStepSound(soundTypeWood);
    }
    @Override public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                              int side, float hx, float hy, float hz) {
        if (!world.isRemote) player.openGui(MoreBeyondOrdinary.instance, MoreBeyondOrdinary.GUI_LOOM, world, x, y, z);
        return true;
    }
    @Override public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        world.setBlockMetadataWithNotify(x, y, z, MathHelper.floor_double(entity.rotationYaw * 4 / 360 + .5) & 3, 2);
    }
    @SideOnly(Side.CLIENT) @Override public void registerBlockIcons(IIconRegister r) {
        top[0]=r.registerIcon("mbo:loom_top");
        top[1]=r.registerIcon("mbo:loom_top_90");
        top[2]=r.registerIcon("mbo:loom_top_180");
        top[3]=r.registerIcon("mbo:loom_top_270");
        bottom=r.registerIcon("mbo:loom_bottom");
        side=r.registerIcon("mbo:loom_side"); front=r.registerIcon("mbo:loom_front");
    }
    @SideOnly(Side.CLIENT) @Override public IIcon getIcon(int face, int meta) {
        if (face==1) return top[meta & 3];
        if (face==0) return bottom;
        int frontFace = meta==0 ? 2 : meta==1 ? 5 : meta==2 ? 3 : 4;
        return face==frontFace ? front : side;
    }
}
