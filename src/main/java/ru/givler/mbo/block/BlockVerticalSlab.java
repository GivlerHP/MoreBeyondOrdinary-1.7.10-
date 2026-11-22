package ru.givler.mbo.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.waterlogging.WaterloggedFlowQueue;
import ru.givler.mbo.waterlogging.WaterloggedGeometry;
import ru.givler.mbo.waterlogging.WaterloggedWorldData;

public class BlockVerticalSlab extends Block {
    private static final int DOUBLE = 4;
    private static final int DISCONNECTED = 8;
    private static final int[] HALF_MASKS = {3, 12, 5, 10};
    public static int renderId;
    public final Block source;
    private final int sourceMeta;

    public BlockVerticalSlab(Block source) {
        this(source, 0);
    }

    protected BlockVerticalSlab(Block source, int sourceMeta) {
        super(source.getMaterial());
        this.source = source;
        this.sourceMeta = sourceMeta;
        setStepSound(source.stepSound);
        setLightOpacity(0);
        useNeighborBrightness = true;
        setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        String tool = source.getHarvestTool(sourceMeta);
        int level = source.getHarvestLevel(sourceMeta);
        if (source.getMaterial() == Material.rock && !"pickaxe".equals(tool)) tool = "pickaxe";
        if (source.getMaterial() == Material.wood && tool == null) tool = "axe";
        if (tool != null) setHarvestLevel(tool, Math.max(0, level));
    }

    public int getSourceMetadata() {
        return sourceMeta;
    }

    public ItemStack ingredient() {
        return new ItemStack(source, 1, sourceMeta);
    }

    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        return source.getBlockHardness(world, x, y, z);
    }

    @Override
    public float getExplosionResistance(Entity entity) {
        return source.getExplosionResistance(entity);
    }

    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
    @Override public int damageDropped(int meta) { return 0; }

    @Override
    public int quantityDropped(int meta, int fortune, Random random) {
        return (meta & DOUBLE) != 0 ? 2 : 1;
    }

    @Override public int getRenderType() { return renderId; }

    public static boolean isDouble(int meta) {
        return (meta & DOUBLE) != 0;
    }

    public boolean toggleConnections(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        int next = meta ^ DISCONNECTED;
        if (!world.setBlockMetadataWithNotify(x, y, z, next, 3)) return false;
        refreshGeometry(world, x, y, z);
        return true;
    }

    public static boolean connectionsDisabled(int meta) {
        return (meta & DISCONNECTED) != 0;
    }

    private static int baseMask(int meta) {
        return isDouble(meta) ? 15 : HALF_MASKS[meta & 3];
    }

    // NW, NE, SW, SE. Use only the original halves when finding joins,
    // so extensions cannot feed back into neighboring connections.
    public static int shapeMask(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        int own = baseMask(meta);
        if (isDouble(meta) || connectionsDisabled(meta)) return own;
        int additions = 0;
        for (int side = 2; side <= 5; side++) {
            ForgeDirection direction = ForgeDirection.getOrientation(side);
            int nx = x + direction.offsetX, nz = z + direction.offsetZ;
            if (!(world.getBlock(nx, y, nz) instanceof BlockVerticalSlab)) continue;
            int neighbor = world.getBlockMetadata(nx, y, nz);
            if (connectionsDisabled(neighbor)) continue;
            int edge = HALF_MASKS[side - 2];
            int ownEdge = own & edge;
            int otherEdge = baseMask(neighbor) & HALF_MASKS[(side ^ 1) - 2];
            int shift = side < 4 ? 2 : 1;
            int opposite = (side & 1) == 0 ? otherEdge >> shift : otherEdge << shift;
            if ((ownEdge & opposite) != 0) continue;
            int candidate = opposite != 0 ? opposite : ownEdge == 0 ? edge : 0;
            additions |= Integer.lowestOneBit(candidate);
        }
        return own | additions;
    }

    public static int adjacentQuarter(int quarter, int side) {
        return side < 2 ? quarter : quarter ^ (side < 4 ? 2 : 1);
    }

    public static boolean touchesEdge(int quarter, int side) {
        return side < 2 || (HALF_MASKS[side - 2] & 1 << quarter) != 0;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        int index = side.ordinal();
        if (index >= 6) return false;
        int edge = index < 2 ? 15 : HALF_MASKS[index - 2];
        return (shapeMask(world, x, y, z) & edge) == edge;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        setMaskBounds(shapeMask(world, x, y, z));
    }

    @Override
    public void setBlockBoundsForItemRender() {
        setMaskBounds(HALF_MASKS[0]);
    }

    private void setMaskBounds(int mask) {
        setBlockBounds((mask & 5) == 0 ? .5F : 0, 0, (mask & 3) == 0 ? .5F : 0,
                (mask & 10) == 0 ? .5F : 1, 1, (mask & 12) == 0 ? .5F : 1);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        setBlockBoundsBasedOnState(world, x, y, z);
        return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB query,
                                        List boxes, Entity entity) {
        int mask = shapeMask(world, x, y, z);
        if (Integer.bitCount(mask) != 3) {
            super.addCollisionBoxesToList(world, x, y, z, query, boxes, entity);
            return;
        }
        for (int quarter = 0; quarter < 4; quarter++) {
            if ((mask & 1 << quarter) == 0) continue;
            double minX = x + (quarter & 1) * .5D, minZ = z + (quarter >> 1) * .5D;
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(minX, y, minZ, minX + .5D, y + 1, minZ + .5D);
            if (query.intersectsWith(box)) boxes.add(box);
        }
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {
        int mask = shapeMask(world, x, y, z);
        if (Integer.bitCount(mask) != 3) return super.collisionRayTrace(world, x, y, z, start, end);
        MovingObjectPosition nearest = null;
        for (int quarter = 0; quarter < 4; quarter++) {
            if ((mask & 1 << quarter) == 0) continue;
            double minX = x + (quarter & 1) * .5D, minZ = z + (quarter >> 1) * .5D;
            MovingObjectPosition hit = AxisAlignedBB.getBoundingBox(minX, y, minZ,
                    minX + .5D, y + 1, minZ + .5D).calculateIntercept(start, end);
            if (hit == null || !touchesEdge(quarter, hit.sideHit)
                    && (mask & 1 << adjacentQuarter(quarter, hit.sideHit)) != 0) continue;
            if (nearest == null || hit.hitVec.squareDistanceTo(start) < nearest.hitVec.squareDistanceTo(start)) {
                nearest = new MovingObjectPosition(x, y, z, hit.sideHit, hit.hitVec);
            }
        }
        return nearest;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        refreshGeometry(world, x, y, z);
    }

    private static void refreshGeometry(World world, int x, int y, int z) {
        world.markBlockRangeForRenderUpdate(x - 1, y, z - 1, x + 1, y, z + 1);
        if (!world.isRemote) {
            WaterloggedGeometry.invalidate();
            if (shapeMask(world, x, y, z) == 15) {
                WaterloggedWorldData.get(world).set(world, x, y, z, false);
            }
            WaterloggedFlowQueue.scheduleAround(world, x, y, z);
        }
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side,
                             float hitX, float hitY, float hitZ, int meta) {
        if (side >= 2) return (side ^ 1) - 2;
        float dx = hitX - .5F, dz = hitZ - .5F;
        if (Math.abs(dx) > Math.abs(dz)) return dx < 0 ? 2 : 3;
        return dz < 0 ? 0 : 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return source.getIcon(side, sourceMeta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        // Icons belong to the source block.
    }

    public static class ItemVerticalSlab extends ItemBlock {
        public ItemVerticalSlab(Block block) {
            super(block);
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack) {
            BlockVerticalSlab slab = (BlockVerticalSlab) field_150939_a;
            String name = slab.getUnlocalizedName() + ".name";
            if (StatCollector.canTranslate(name)) return StatCollector.translateToLocal(name);
            return StatCollector.translateToLocalFormatted("tile.mbo.verticalSlab.format",
                    slab.ingredient().getDisplayName());
        }

        @Override
        public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                                 int side, float hitX, float hitY, float hitZ) {
            Block block = field_150939_a;
            if (stack.stackSize == 0) return false;
            if (world.getBlock(x, y, z) == block) {
                int meta = world.getBlockMetadata(x, y, z);
                int occupied = meta & 3;
                boolean emptyHalf = occupied == 0 ? hitZ > .5F : occupied == 1 ? hitZ < .5F
                        : occupied == 2 ? hitX > .5F : hitX < .5F;
                if ((meta & DOUBLE) == 0 && (side == ((occupied + 2) ^ 1) || side < 2 && emptyHalf)) {
                    if (!world.canMineBlock(player, x, y, z) || !player.canPlayerEdit(x, y, z, side, stack)) return false;
                    AxisAlignedBB full = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
                    if (!world.checkNoEntityCollision(full)) return false;
                    if (world.setBlockMetadataWithNotify(x, y, z, meta | DOUBLE, 3)) {
                        refreshGeometry(world, x, y, z);
                        world.playSoundEffect(x + .5, y + .5, z + .5, block.stepSound.func_150496_b(),
                                (block.stepSound.getVolume() + 1) / 2, block.stepSound.getPitch() * .8F);
                        stack.stackSize--;
                        return true;
                    }
                    return false;
                }
            }
            return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
        }
    }
}
