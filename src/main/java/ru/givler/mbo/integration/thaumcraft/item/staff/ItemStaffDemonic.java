package ru.givler.mbo.integration.thaumcraft.item.staff;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import ru.givler.mbo.integration.thaumcraft.entities.EntityDarkMatter;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;

public class ItemStaffDemonic extends ItemStaffBasic {
    @SideOnly(Side.CLIENT)
    private IIcon demonicIcon;

    private static final float DAMAGE = 5.0F;
    private static final int ENLARGE = 0;
    private static final boolean CORROSIVE = false;

    public ItemStaffDemonic() {
        super();
        this.setUnlocalizedName("staffDemonic");
        this.setCreativeTab(Thaumcraft.tabTC);
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.staffDemonic.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.demonicIcon = reg.registerIcon("mbo:staff/staffDemonic");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.demonicIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return this.demonicIcon;
    }


    @Override
    public int getMaxVis(ItemStack stack) {
        return 130 * 100;
    }

    public AspectList getVisCost(ItemStack stack) {
        return (new AspectList()).add(Aspect.ENTROPY, 80).add(Aspect.EARTH, 80);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
        MovingObjectPosition mop = this.getMovingObjectPositionFromPlayer(world, player, true);

        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            int i = mop.blockX;
            int j = mop.blockY;
            int k = mop.blockZ;
            Block bi = world.getBlock(i, j, k);

            if (bi instanceof IWandable) {
                ItemStack is = ((IWandable) bi).onWandRightClick(world, itemstack, player);
                if (is != null) {
                    return is;
                }
            }

            TileEntity tile = world.getTileEntity(i, j, k);
            if (tile != null && tile instanceof IWandable) {
                ItemStack is = ((IWandable) tile).onWandRightClick(world, itemstack, player);
                if (is != null) {
                    return is;
                }
            }
        }

        if (this.consumeAllVis(itemstack, player, this.getVisCost(itemstack), !world.isRemote, false)) {
            if (!world.isRemote) {
                EntityDarkMatter darkMatter = new EntityDarkMatter(world, player, DAMAGE, ENLARGE, CORROSIVE);
                world.spawnEntityInWorld(darkMatter);
                world.playSoundAtEntity(player, "thaumcraft:egattack", 1.0F,
                        0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
            }
            player.swingItem();
        }

        return itemstack;
    }
}