package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButtonWood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.registry.CreativeTabRegistry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

import java.util.Random;

public class BlockBasicWoodButton extends BlockButtonWood {
    private final Block planks;
    private final int plankMeta;

    public BlockBasicWoodButton(String name, Block planks, int plankMeta) {
        this(name, planks, plankMeta, true, CreativeTabRegistry.tabMBOblocks);
    }

    public BlockBasicWoodButton(String name, Block planks, int plankMeta,
                                boolean register, CreativeTabs tab) {
        this.planks = planks;
        this.plankMeta = plankMeta;
        setBlockName(name);
        setHardness(0.5F);
        setStepSound(Block.soundTypeWood);
        setCreativeTab(tab);
        if (register) {
            GameRegistry.registerBlock(this, name);
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return planks.getIcon(side, plankMeta);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
        if (side == 1) {
            return world.isSideSolid(x, y - 1, z, ForgeDirection.UP);
        }
        if (side == 0) {
            return world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN);
        }
        return super.canPlaceBlockOnSide(world, x, y, z, side);
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return world.isSideSolid(x, y - 1, z, ForgeDirection.UP)
                || world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN)
                || super.canPlaceBlockAt(world, x, y, z);
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ, int meta) {
        if (side == 1 && world.isSideSolid(x, y - 1, z, ForgeDirection.UP)) {
            return 5;
        }
        if (side == 0 && world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN)) {
            return 0;
        }
        return super.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, meta);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z,
                                EntityLivingBase placer, ItemStack stack) {
        int placedDirection = world.getBlockMetadata(x, y, z) & 7;
        if (placedDirection == 5 || placedDirection == 0) {
            int facing = MathHelper.floor_double(
                    placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
            int direction;
            if (placedDirection == 5) {
                direction = (facing & 1) == 0 ? 5 : 6;
            } else {
                direction = (facing & 1) == 0 ? 0 : 7;
            }
            world.setBlockMetadataWithNotify(x, y, z, direction, 2);
        }
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        int direction = meta & 7;
        boolean pressed = (meta & 8) != 0;
        if (direction == 5 || direction == 6 || direction == 0 || direction == 7) {
            float halfWidth = 0.1875F;
            float halfDepth = 0.125F;
            float height = pressed ? 0.0625F : 0.125F;
            float minY = direction == 0 || direction == 7 ? 1.0F - height : 0.0F;
            float maxY = direction == 0 || direction == 7 ? 1.0F : height;
            if (direction == 5 || direction == 0) {
                setBlockBounds(0.5F - halfWidth, minY, 0.5F - halfDepth,
                        0.5F + halfWidth, maxY, 0.5F + halfDepth);
            } else {
                setBlockBounds(0.5F - halfDepth, minY, 0.5F - halfWidth,
                        0.5F + halfDepth, maxY, 0.5F + halfWidth);
            }
        } else {
            super.setBlockBoundsBasedOnState(world, x, y, z);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        int direction = world.getBlockMetadata(x, y, z) & 7;
        if (direction == 5 || direction == 6 || direction == 0 || direction == 7) {
            boolean ceiling = direction == 0 || direction == 7;
            boolean supported = ceiling
                    ? world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN)
                    : world.isSideSolid(x, y - 1, z, ForgeDirection.UP);
            if (!supported) {
                dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
                world.setBlockToAir(x, y, z);
            }
            return;
        }
        super.onNeighborBlockChange(world, x, y, z, neighbor);
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        int direction = meta & 7;
        if (direction == 5 || direction == 6 || direction == 0 || direction == 7) {
            return (meta & 8) != 0 ? 15 : 0;
        }
        return super.isProvidingStrongPower(world, x, y, z, side);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side,
                                    float hitX, float hitY, float hitZ) {
        boolean result = super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
        notifyCeilingSupport(world, x, y, z);
        return result;
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        super.updateTick(world, x, y, z, random);
        notifyCeilingSupport(world, x, y, z);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        super.onEntityCollidedWithBlock(world, x, y, z, entity);
        notifyCeilingSupport(world, x, y, z);
    }

    private void notifyCeilingSupport(World world, int x, int y, int z) {
        int direction = world.getBlockMetadata(x, y, z) & 7;
        if (direction == 0 || direction == 7) {
            world.notifyBlocksOfNeighborChange(x, y + 1, z, this);
        }
    }

    public void addStandardRecipe() {
        GameRegistry.addRecipe(new ItemStack(this), "P",
                'P', new ItemStack(planks, 1, plankMeta));
    }
}
