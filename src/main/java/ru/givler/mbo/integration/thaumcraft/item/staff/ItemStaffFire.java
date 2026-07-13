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
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.entities.projectile.EntityExplosiveOrb;

public class ItemStaffFire extends ItemStaffBasic {
    @SideOnly(Side.CLIENT)
    private IIcon fireIcon;

    private static final float FIREBALL_STRENGTH = 2.0F;

    public ItemStaffFire() {
        super();
        this.setUnlocalizedName("staffFire");
        this.setCreativeTab(Thaumcraft.tabTC);
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.staffFire.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.fireIcon = reg.registerIcon("mbo:staff/staffFire");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.fireIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return this.fireIcon;
    }


    @Override
    public int getMaxVis(ItemStack stack) {
        return 125 * 100;
    }

    public AspectList getVisCost(ItemStack stack) {
        return (new AspectList()).add(Aspect.FIRE, 150).add(Aspect.ENTROPY, 50);
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
                EntityExplosiveOrb orb = new EntityExplosiveOrb(world, player);
                orb.strength = FIREBALL_STRENGTH;
                orb.onFire = false;
                world.spawnEntityInWorld(orb);
                world.playAuxSFXAtEntity(null, 1009, (int) player.posX, (int) player.posY, (int) player.posZ, 0);
            }
            player.swingItem();
        } else if (!world.isRemote) {
        }

        return itemstack;
    }
}
