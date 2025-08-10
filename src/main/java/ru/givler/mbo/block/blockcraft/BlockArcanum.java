package ru.givler.mbo.block.blockcraft;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.tileentity.TileEntityArcanum;

    public class BlockArcanum extends BlockContainer {
        private IIcon iconTopBottom;
        private IIcon iconBack;
        private IIcon iconLeft;
        private IIcon iconRight;
        private IIcon iconFrontIdle;
        private IIcon iconFrontActive;

        public BlockArcanum(Material material, String name) {
            super(material);

            this.setBlockName(name);
            this.setLightLevel(0.0F);
            this.setLightOpacity(0);
            this.setHardness(1.0F);
            this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
            this.setResistance(10.0F);
            this.setHarvestLevel("pick_axe", 0);
            this.setStepSound(soundTypeStone);

            GameRegistry.registerBlock(this, name);
        }

        @Override
        public TileEntity createNewTileEntity(World world, int meta) {
            return new TileEntityArcanum();
        }

        @Override
        public boolean onBlockActivated(World world, int x, int y, int z,
                                        EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
            if (!world.isRemote) {
                player.openGui(MoreBeyondOrdinary.instance, MoreBeyondOrdinary.GUI_INFUSION_WORKBENCH, world, x, y, z);
            }
            return true;
        }

        @SideOnly(Side.CLIENT)
        @Override
        public void registerBlockIcons(IIconRegister iconRegister) {
            this.iconTopBottom = iconRegister.registerIcon(MoreBeyondOrdinary.MODID + ":magicfurnace_topbottom");
            this.iconBack = iconRegister.registerIcon(MoreBeyondOrdinary.MODID + ":magicfurnace_back");
            this.iconLeft = iconRegister.registerIcon(MoreBeyondOrdinary.MODID + ":magicfurnace_left");
            this.iconRight = iconRegister.registerIcon(MoreBeyondOrdinary.MODID + ":magicfurnace_right");
            this.iconFrontIdle = iconRegister.registerIcon(MoreBeyondOrdinary.MODID + ":magicfurnace_front_idle");
            this.iconFrontActive = iconRegister.registerIcon(MoreBeyondOrdinary.MODID + ":magicfurnace_front_active");
        }

        @Override
        public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
            int direction = determineFurnaceDirection(placer);
            world.setBlockMetadataWithNotify(x, y, z, direction, 2);
        }

        private int determineFurnaceDirection(EntityLivingBase placer) {
            int facing = MathHelper.floor_double((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            // 2 — север (Z-), 3 — юг (Z+), 4 — запад (X-), 5 — восток (X+)
            switch (facing) {
                case 0: return 2; // North (front на север)
                case 1: return 5; // East  (front на восток)
                case 2: return 3; // South (front на юг)
                case 3: return 4; // West  (front на запад)
                default: return 2;
            }
        }

        @SideOnly(Side.CLIENT)
        @Override
        public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
            int meta = world.getBlockMetadata(x, y, z);

            if (side == 0 || side == 1) return iconTopBottom;

            boolean isActive = false;
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityArcanum) {
                isActive = ((TileEntityArcanum) te).isActive();
            }

            if (side == meta) {
                return isActive ? iconFrontActive : iconFrontIdle;
            }
            switch (side) {
                case 2: return iconBack;
                case 3: return iconBack;
                case 4: return iconLeft;
                case 5: return iconRight;
                default: return iconTopBottom;
            }
        }

        @SideOnly(Side.CLIENT)
        @Override
        public IIcon getIcon(int side, int meta) {
            if (side == 0 || side == 1) return iconTopBottom;
            if (side == meta) return iconFrontIdle;
            switch (side) {
                case 2: return iconBack;
                case 3: return iconBack;
                case 4: return iconLeft;
                case 5: return iconRight;
                default: return iconTopBottom;
            }
        }


        @Override
        public int getLightValue(IBlockAccess world, int x, int y, int z) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityArcanum) {
                TileEntityArcanum furnace = (TileEntityArcanum) te;
                if (furnace.getProgress() > 0) {
                    return 12;
                }
            }
            return 0;
        }

        @Override
        public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityArcanum) {
                IInventory inv = (IInventory) tile;
                for (int i = 0; i < inv.getSizeInventory(); ++i) {
                    ItemStack stack = inv.getStackInSlot(i);
                    if (stack != null) {
                        float dx = world.rand.nextFloat() * 0.8F + 0.1F;
                        float dy = world.rand.nextFloat() * 0.8F + 0.1F;
                        float dz = world.rand.nextFloat() * 0.8F + 0.1F;
                        EntityItem entity = new EntityItem(world, x + dx, y + dy, z + dz, stack.copy());
                        world.spawnEntityInWorld(entity);
                    }
                }
            }
            super.breakBlock(world, x, y, z, block, meta);
        }

    }