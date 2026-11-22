package ru.givler.mbo.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.waterlogging.WaterloggedFlowQueue;
import ru.givler.mbo.waterlogging.WaterloggedGeometry;
import ru.givler.mbo.waterlogging.WaterloggedWorldData;

public class BlockGroundCover extends BlockSnow {
    public static int renderId;
    public final Block source;
    private final int sourceMeta;
    private final boolean tintedTop;
    private final SurfaceEffect surfaceEffect;

    public enum SurfaceEffect {
        NONE(0), MUD(.35), QUICKSAND(1), SMOLDERING(.02);

        private final double sinkDepth;

        SurfaceEffect(double sinkDepth) {
            this.sinkDepth = sinkDepth;
        }
    }

    public BlockGroundCover(Block source, boolean tintedTop) {
        this(source, 0, tintedTop);
    }

    protected BlockGroundCover(Block source, int sourceMeta, boolean tintedTop) {
        this(source, sourceMeta, tintedTop, SurfaceEffect.NONE);
    }

    protected BlockGroundCover(Block source, int sourceMeta, boolean tintedTop, SurfaceEffect surfaceEffect) {
        this.surfaceEffect = surfaceEffect;
        this.source = source;
        this.sourceMeta = sourceMeta;
        this.tintedTop = tintedTop;
        setHardness(0.5F);
        setStepSound(source.stepSound);
        setTickRandomly(false);
        setLightOpacity(0);
        setHarvestLevel("shovel", 0);
        setCreativeTab(source.getCreativeTabToDisplayOn());
    }

    public int getSourceMetadata() { return sourceMeta; }
    public ItemStack ingredient() { return new ItemStack(source, 1, sourceMeta); }
    public static double height(int meta) { return ((meta & 7) + 1) / 8.0; }

    @Override public Material getMaterial() { return source == null ? super.getMaterial() : source.getMaterial(); }
    @Override public int getRenderType() { return renderId; }
    @Override public void updateTick(World world, int x, int y, int z, Random random) {}
    @Override public boolean isReplaceable(IBlockAccess world, int x, int y, int z) { return false; }
    @Override public Item getItemDropped(int meta, Random random, int fortune) { return Item.getItemFromBlock(this); }
    @Override public int damageDropped(int meta) { return 0; }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return y > 0 && world.isSideSolid(x, y - 1, z, ForgeDirection.UP);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!world.isRemote && !canPlaceBlockAt(world, x, y, z)) {
            dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlockToAir(x, y, z);
        }
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side == ForgeDirection.DOWN || height(world.getBlockMetadata(x, y, z)) == 1
                && surfaceEffect != SurfaceEffect.QUICKSAND;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        double height = height(world.getBlockMetadata(x, y, z));
        if (surfaceEffect == SurfaceEffect.QUICKSAND) return null;
        return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + height - Math.min(surfaceEffect.sinkDepth, height * .5), z + 1);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (entity.boundingBox.minY >= y + height(world.getBlockMetadata(x, y, z))) return;
        switch (surfaceEffect) {
            case MUD:
                if (entity instanceof EntityPlayer) {
                    ItemStack boots = ((EntityPlayer) entity).inventory.armorInventory[0];
                    if (boots != null && boots.getItem() == biomesoplenty.api.content.BOPCItems.wadingBoots) return;
                }
                entity.motionX *= .1;
                entity.motionZ *= .1;
                break;
            case QUICKSAND:
                entity.setInWeb();
                break;
            case SMOLDERING:
                if (!world.isRemote) entity.setFire(2);
                break;
            default:
                break;
        }
    }

    @Override @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random random) {
        if (surfaceEffect != SurfaceEffect.SMOLDERING || !world.isAirBlock(x, y + 1, z)) return;
        double surface = y + height(world.getBlockMetadata(x, y, z)) + .1;
        if (random.nextInt(4) == 0) {
            world.spawnParticle("smoke", x + random.nextFloat(), surface, z + random.nextFloat(), 0, 0, 0);
        }
        if (random.nextInt(6) == 0) {
            world.spawnParticle("flame", x + random.nextFloat(), surface, z + random.nextFloat(), 0, 0, 0);
        }
    }

    @Override @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return source.getIcon(side, sourceMeta);
    }

    @Override @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {}

    @SideOnly(Side.CLIENT)
    public int topColor(IBlockAccess world, int x, int y, int z) {
        return tintedTop ? source.colorMultiplier(world, x, y, z) : 0xFFFFFF;
    }

    @SideOnly(Side.CLIENT)
    public int inventoryTopColor() {
        return tintedTop ? source.getRenderColor(sourceMeta) : 0xFFFFFF;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        ItemStack held = player.getCurrentEquippedItem();
        return side == 1 && held != null && held.getItem() == Item.getItemFromBlock(this)
                && addLayer(held, player, world, x, y, z, side);
    }

    private boolean addLayer(ItemStack stack, EntityPlayer player, World world,
                             int x, int y, int z, int side) {
        if (world.getBlock(x, y, z) != this || stack.stackSize <= 0) return false;
        int meta = world.getBlockMetadata(x, y, z) & 7;
        if (meta == 7 || !world.canMineBlock(player, x, y, z)
                || !player.canPlayerEdit(x, y, z, side, stack)) return false;
        AxisAlignedBB next = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + height(meta + 1), z + 1);
        if (!world.checkNoEntityCollision(next)) return false;
        if (world.isRemote) return true;
        if (!world.setBlockMetadataWithNotify(x, y, z, meta + 1, 3)) return false;
        WaterloggedGeometry.invalidate();
        if (meta == 6) WaterloggedWorldData.get(world).set(world, x, y, z, false);
        WaterloggedFlowQueue.scheduleAround(world, x, y, z);
        world.playSoundEffect(x + .5, y + .5, z + .5, stepSound.func_150496_b(),
                (stepSound.getVolume() + 1) / 2, stepSound.getPitch() * .8F);
        if (!player.capabilities.isCreativeMode) stack.stackSize--;
        return true;
    }

    public static final class ItemGroundCover extends ItemBlock {
        public ItemGroundCover(Block block) {
            super(block);
        }

        @Override public String getItemStackDisplayName(ItemStack stack) {
            String name = field_150939_a.getUnlocalizedName() + ".name";
            if (StatCollector.canTranslate(name)) return StatCollector.translateToLocal(name);
            return StatCollector.translateToLocalFormatted("tile.mbo.groundCover.format",
                    ((BlockGroundCover) field_150939_a).ingredient().getDisplayName());
        }

        @Override
        public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                                 int side, float hitX, float hitY, float hitZ) {
            BlockGroundCover cover = (BlockGroundCover) field_150939_a;
            if (side == 1 && cover.addLayer(stack, player, world, x, y, z, side)) return true;
            ForgeDirection face = ForgeDirection.getOrientation(side);
            if (cover.addLayer(stack, player, world, x + face.offsetX, y + face.offsetY,
                    z + face.offsetZ, side)) return true;
            return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
        }
    }
}
