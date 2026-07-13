package ru.givler.mbo.integration.thaumcraft.item.staff;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.tileentity.TileEntityTimer;
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

public class ItemStaffLantern extends ItemStaffBasic {
    @SideOnly(Side.CLIENT)
    private IIcon frostIcon;

    private static final int LIGHT_DURATION_TICKS = 600;

    public ItemStaffLantern() {
        super();
        this.setUnlocalizedName("staffLantern");
        this.setCreativeTab(Thaumcraft.tabTC);
        this.setMaxStackSize(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.staffLantern.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.frostIcon = reg.registerIcon("mbo:staff/staffLantern");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return this.frostIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return this.frostIcon;
    }


    @Override
    public int getMaxVis(ItemStack stack) {
        return 175 * 100;
    }

    public AspectList getVisCost(ItemStack stack) {
        return (new AspectList()).add(Aspect.FIRE, 50).add(Aspect.ORDER, 30);
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
            boolean placed = false;

            if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                int blockHitX = mop.blockX;
                int blockHitY = mop.blockY;
                int blockHitZ = mop.blockZ;

                switch (mop.sideHit) {
                    case 0: blockHitY--; break;
                    case 1: blockHitY++; break;
                    case 2: blockHitZ--; break;
                    case 3: blockHitZ++; break;
                    case 4: blockHitX--; break;
                    case 5: blockHitX++; break;
                }

                if (world.isAirBlock(blockHitX, blockHitY, blockHitZ)) {
                    if (!world.isRemote) {
                        world.setBlock(blockHitX, blockHitY, blockHitZ, Wizardry.magicLight);
                        if (world.getTileEntity(blockHitX, blockHitY, blockHitZ) instanceof TileEntityTimer) {
                            ((TileEntityTimer) world.getTileEntity(blockHitX, blockHitY, blockHitZ))
                                    .setLifetime(LIGHT_DURATION_TICKS);
                        }
                    }
                    placed = true;
                }
            } else {
                int x = (int) (Math.floor(player.posX) + player.getLookVec().xCoord * 4);
                int y = (int) (Math.floor(player.posY) + player.eyeHeight + player.getLookVec().yCoord * 4);
                int z = (int) (Math.floor(player.posZ) + player.getLookVec().zCoord * 4);

                if (world.isAirBlock(x, y, z)) {
                    if (!world.isRemote) {
                        world.setBlock(x, y, z, Wizardry.magicLight);
                        if (world.getTileEntity(x, y, z) instanceof TileEntityTimer) {
                            ((TileEntityTimer) world.getTileEntity(x, y, z)).setLifetime(LIGHT_DURATION_TICKS);
                        }
                    }
                    placed = true;
                }
            }

            if (placed) {
                player.swingItem();
                world.playSoundAtEntity(player, "wizardry:aura", 1.0F, 1.0F);
            }
        }
        return itemstack;
    }
}
