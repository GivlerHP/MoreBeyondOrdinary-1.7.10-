package ru.givler.mbo.block.specialblocks;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.handler.MboGui;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.item.ItemBlockLootContainer;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

public class BlockDestructibleLootContainer extends BlockModels {
    public BlockDestructibleLootContainer(Material material, String name) {
        super(material, name, "jugs", "jugs");
        setCollisionEnabled(false);
        setBlockUnbreakable();
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityLootContainer) {
            float[] bounds = ((TileEntityLootContainer) tile).getCollisionBounds();
            setBlockBounds(bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5]);
        } else {
            setBlockBounds(0, 0, 0, 1, 1, 1);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        // The configured bounds are a trigger volume, not a solid obstacle.
        return null;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityLootContainer();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        boolean canEditInPlace = ItemBlockLootContainer.isEditor(player) && player != null && player.capabilities.isCreativeMode;
        if (world.isRemote) {
            if (canEditInPlace) {
                player.openGui(MoreBeyondOrdinary.instance, MboGui.LOOT_CONTAINER_CONFIG.id, world, x, y, z);
            }
            return true;
        }
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityLootContainer)) return true;

        TileEntityLootContainer loot = (TileEntityLootContainer) tile;
        if (canEditInPlace) {
            return true;
        }
        loot.tryDestroy(player, true);
        return true;
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote || player == null) return;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityLootContainer) {
            ((TileEntityLootContainer) tile).tryDestroy(player, true);
        }
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (world.isRemote || entity == null) return;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityLootContainer)) return;
        TileEntityLootContainer loot = (TileEntityLootContainer) tile;
        if (!loot.isEntityInsideTrigger(entity)) return;
        if (entity instanceof EntityPlayer) {
            if (loot.shouldDestroyOnPlayerCollide()) {
                loot.tryDestroy(entity, true);
            }
            return;
        }
        if ((entity instanceof EntityArrow || entity instanceof EntityThrowable || entity instanceof EntityFireball)
                && loot.shouldDestroyOnProjectileHit()) {
            loot.tryDestroy(entity, true);
            return;
        }
        if (loot.shouldDestroyOnEntityCollide()
                && entity instanceof EntityLivingBase
                && !(entity instanceof EntityPlayer)) {
            loot.tryDestroy(entity, true);
        }
    }

    @Override
    public void onBlockExploded(World world, int x, int y, int z, net.minecraft.world.Explosion explosion) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityLootContainer) {
            TileEntityLootContainer loot = (TileEntityLootContainer) tile;
            if (loot.shouldDestroyOnExplosion()) {
                loot.tryDestroy(explosion == null ? null : explosion.exploder, true);
                return;
            }
        }
        super.onBlockExploded(world, x, y, z, explosion);
    }

    @Override
    public int quantityDropped(int meta, int fortune, java.util.Random random) {
        return 0;
    }

    @Override
    public void register() {
        GameRegistry.registerBlock(this, ItemBlockLootContainer.class, "LootContainer");
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, net.minecraft.entity.EntityLivingBase player, ItemStack stack) {
        ru.givler.mbo.lootcontainer.LootContainerData data =
                ru.givler.mbo.lootcontainer.LootContainerData.fromItemStackNbt(
                        stack == null ? null : stack.getTagCompound());
        int facing = 0;
        world.setBlockMetadataWithNotify(x, y, z, facing, 3);
        if (world.isRemote || stack == null) return;
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityLootContainer) {
            TileEntityLootContainer loot = (TileEntityLootContainer) tile;
            loot.setPlacementFacing(facing);
            loot.applyConfig(data, true);
        }
    }
}
