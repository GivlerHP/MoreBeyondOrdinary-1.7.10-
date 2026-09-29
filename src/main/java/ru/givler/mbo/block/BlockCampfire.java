package ru.givler.mbo.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.tileentity.TileEntityCampfire;

import java.util.Random;

/** Four-slot campfire. Metadata bit 0 stores whether the fire is lit. */
public final class BlockCampfire extends BlockContainer {
    private int renderType;
    @SideOnly(Side.CLIENT) private IIcon logIcon;
    @SideOnly(Side.CLIENT) private IIcon litLogIcon;
    @SideOnly(Side.CLIENT) private IIcon fireIcon;

    public BlockCampfire() {
        super(Material.wood);
        setBlockName("Campfire");
        setHardness(2.0F);
        setStepSound(soundTypeWood);
        setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        setBlockBounds(0, 0, 0, 1, 0.4375F, 1);
    }

    public void setCampfireRenderType(int id) { renderType = id; }
    @Override public int getRenderType() { return renderType; }
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return (world.getBlockMetadata(x, y, z) & 1) != 0 ? 15 : 0;
    }
    @Override public int onBlockPlaced(World world, int x, int y, int z, int side,
                                       float hitX, float hitY, float hitZ, int meta) { return 1; }
    @Override public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityCampfire(); }
    @Override public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + .4375, z + 1);
    }
    @Override public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (entity instanceof EntityLivingBase && (world.getBlockMetadata(x, y, z) & 1) != 0)
            entity.attackEntityFrom(DamageSource.inFire, 1.0F);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getCurrentEquippedItem();
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityCampfire)) return false;
        TileEntityCampfire fire = (TileEntityCampfire) tile;
        if (held != null && held.getItem() == Items.flint_and_steel && (world.getBlockMetadata(x, y, z) & 1) == 0) {
            if (!world.isRemote) {
                world.setBlockMetadataWithNotify(x, y, z, 1, 3);
                if (!player.capabilities.isCreativeMode) held.damageItem(1, player);
                world.playSoundEffect(x + .5, y + .5, z + .5, "fire.ignite", 1, 1);
            }
            return true;
        }
        if (held != null && held.getItem() == Items.water_bucket && (world.getBlockMetadata(x, y, z) & 1) != 0) {
            if (!world.isRemote) {
                world.setBlockMetadataWithNotify(x, y, z, 0, 3);
                if (!player.capabilities.isCreativeMode) player.inventory.setInventorySlotContents(player.inventory.currentItem, new ItemStack(Items.bucket));
                world.playSoundEffect(x + .5, y + .5, z + .5, "random.fizz", .8F, 1);
            }
            return true;
        }
        if (held != null) {
            ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(new ItemStack(held.getItem(), 1, held.getItemDamage()));
            if (result == null || !(result.getItem() instanceof ItemFood)) return false;
            if (!world.isRemote && fire.addFood(held) && !player.capabilities.isCreativeMode) {
                --held.stackSize;
                if (held.stackSize == 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
            return true;
        }
        if (!world.isRemote) fire.takeFood(player);
        return true;
    }

    @Override public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!world.isRemote && tile instanceof TileEntityCampfire) ((TileEntityCampfire) tile).dropFood();
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        logIcon = register.registerIcon("mbo:campfire/campfire_log");
        litLogIcon = register.registerIcon("mbo:campfire/campfire_log_lit");
        fireIcon = register.registerIcon("mbo:campfire/campfire_fire");
        blockIcon = logIcon;
    }
    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) { return logIcon; }
    @SideOnly(Side.CLIENT) public IIcon getLitLogIcon() { return litLogIcon; }
    @SideOnly(Side.CLIENT) public IIcon getFireIcon() { return fireIcon; }

    @Override @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        if ((world.getBlockMetadata(x, y, z) & 1) == 0) return;
        if (random.nextInt(3) == 0) world.spawnParticle("flame", x + .3 + random.nextDouble() * .4,
                y + .35, z + .3 + random.nextDouble() * .4, 0, .01, 0);
        if (random.nextInt(24) == 0) world.playSound(x + .5, y + .5, z + .5,
                "mbo:campfire.crackle", .5F, .8F + random.nextFloat() * .4F, false);
    }
}
