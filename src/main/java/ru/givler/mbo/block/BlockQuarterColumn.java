package ru.givler.mbo.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.data.world.QuarterCubeData;

/** A post or half-height cube sharing the same four horizontal positions. */
public final class BlockQuarterColumn extends BlockVerticalSlab {
    private static final int[] HIGH_HALF = {0xAA, 0xF0, 0xCC};
    private static final int[] FACE_MASKS = {0x0F, 0xF0, 0x33, 0xCC, 0x55, 0xAA};
    private static final float HIT_EPSILON = 0.0001F;
    private final int copperState;

    private final boolean cube;

    public BlockQuarterColumn(BlockVerticalSlab slab) { this(slab,false); }

    public BlockQuarterColumn(BlockVerticalSlab slab, boolean cube) {
        super(slab.source,slab.getSourceMetadata());
        this.cube = cube;
        setLightOpacity(0);
        useNeighborBrightness = cube;
        copperState = slab instanceof BlockMetaVerticalSlab ? ((BlockMetaVerticalSlab)slab).getCopperState() : -1;
        setTickRandomly(copperState >= 0 && (copperState & 8) == 0 && (copperState & 3) < 3);
    }

    public int getCopperState() { return copperState; }
    public boolean isCube() { return cube; }

    public static boolean isCube(Block block) {
        return block instanceof BlockQuarterColumn && ((BlockQuarterColumn) block).isCube();
    }

    public static int cubeMask(IBlockAccess world, int x, int y, int z) {
        return QuarterCubeData.mask(world, x, y, z, world.getBlockMetadata(x, y, z));
    }

    public static boolean touchesCubeFace(int position, int side) {
        return (FACE_MASKS[side] & 1 << position) != 0;
    }

    @Override public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
        return cube && closesFace(cubeMask(world, x, y, z)) ? source.getLightOpacity() : 0;
    }

    public static int adjacentCube(int position, int side) {
        return position ^ (side < 2 ? 4 : side < 4 ? 2 : 1);
    }

    @Override public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        if (!cube) return super.isSideSolid(world, x, y, z, side);
        int index = side.ordinal();
        return index < 6 && (cubeMask(world, x, y, z) & FACE_MASKS[index]) == FACE_MASKS[index];
    }

    @Override public void setBlockBoundsBasedOnState(IBlockAccess world,int x,int y,int z) {
        if (!cube) {
            super.setBlockBoundsBasedOnState(world,x,y,z);
            return;
        }
        int mask = cubeMask(world, x, y, z);
        setBlockBounds(min(mask, 0), min(mask, 1), min(mask, 2), max(mask, 0), max(mask, 1), max(mask, 2));
    }

    private static float min(int mask, int axis) { return (mask & ~HIGH_HALF[axis]) == 0 ? .5F : 0; }

    private static float max(int mask, int axis) { return (mask & HIGH_HALF[axis]) == 0 ? .5F : 1; }

    private static AxisAlignedBB cubeBox(int x, int y, int z, int position) {
        double minX = x + (position & 1) * .5, minY = y + (position >> 2) * .5, minZ = z + (position >> 1 & 1) * .5;
        return AxisAlignedBB.getBoundingBox(minX, minY, minZ, minX + .5, minY + .5, minZ + .5);
    }

    @Override @SuppressWarnings({"rawtypes", "unchecked"})
    public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB query, List boxes, Entity entity) {
        if (!cube) {
            super.addCollisionBoxesToList(world, x, y, z, query, boxes, entity);
            return;
        }
        for (int mask = cubeMask(world, x, y, z); mask != 0; mask &= mask - 1) {
            AxisAlignedBB box = cubeBox(x, y, z, Integer.numberOfTrailingZeros(mask));
            if (query.intersectsWith(box)) boxes.add(box);
        }
    }

    @Override public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {
        if (!cube) return super.collisionRayTrace(world, x, y, z, start, end);
        int occupied = cubeMask(world, x, y, z);
        MovingObjectPosition nearest = null;
        for (int mask = occupied; mask != 0; mask &= mask - 1) {
            int position = Integer.numberOfTrailingZeros(mask);
            MovingObjectPosition hit = cubeBox(x, y, z, position).calculateIntercept(start, end);
            if (hit == null || !touchesCubeFace(position, hit.sideHit)
                    && (occupied & 1 << adjacentCube(position, hit.sideHit)) != 0) continue;
            if (nearest == null || hit.hitVec.squareDistanceTo(start) < nearest.hitVec.squareDistanceTo(start)) {
                nearest = new MovingObjectPosition(x, y, z, hit.sideHit, hit.hitVec);
            }
        }
        return nearest;
    }

    @Override public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = super.getDrops(world, x, y, z, metadata, fortune);
        if (cube && !drops.isEmpty() && (metadata & 8) != 0) {
            drops.get(0).stackSize = Integer.bitCount(QuarterCubeData.mask(world, x, y, z, metadata));
        }
        return drops;
    }

    @Override public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (cube && (world.getBlockMetadata(x, y, z) & 8) != 0 && willHarvest) return true;
        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        super.harvestBlock(world, player, x, y, z, metadata);
        if (cube && (metadata & 8) != 0 && world.getBlock(x, y, z) == this) world.setBlockToAir(x, y, z);
    }

    @Override public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int metadata) {
        return !(cube && (metadata & 8) != 0) && super.canSilkHarvest(world, player, x, y, z, metadata);
    }

    @Override public void breakBlock(World world, int x, int y, int z, Block old, int metadata) {
        if (cube && !world.isRemote && (metadata & 8) != 0) QuarterCubeData.set(world, x, y, z, 0);
        super.breakBlock(world, x, y, z, old, metadata);
    }

    @Override public void setBlockBoundsForItemRender() {
        setBlockBounds(0,0,0,.5F,cube?.5F:1,.5F);
    }

    @Override public int onBlockPlaced(World world,int x,int y,int z,int side,
                                      float hitX,float hitY,float hitZ,int metadata) {
        int quarter = (hitX >= .5F ? 1 : 0) | (hitZ >= .5F ? 2 : 0);
        switch (side) {
            case 2: quarter |= 2; break;
            case 3: quarter &= ~2; break;
            case 4: quarter |= 1; break;
            case 5: quarter &= ~1; break;
            default: break;
        }
        if (!cube) return quarter;
        boolean upper = side==0 || side>=2 && hitY>=.5F;
        return quarter | (upper?4:0);
    }

    @Override public int quantityDropped(int metadata,int fortune,Random random) { return 1; }

    @Override public void updateTick(World world,int x,int y,int z,Random random) {
        if (copperState >= 0 && !world.isRemote) CopperOxidation.tick(world,x,y,z,random);
    }

    @Override public boolean onBlockActivated(World world,int x,int y,int z,EntityPlayer player,
                                              int side,float hitX,float hitY,float hitZ) {
        return copperState >= 0 && CopperOxidation.interact(world,x,y,z,player);
    }

    public static final class ItemQuarterColumn extends ItemBlock {
        public ItemQuarterColumn(Block block) { super(block); }

        @Override public boolean func_150936_a(World world, int x, int y, int z, int side,
                                             EntityPlayer player, ItemStack stack) {
            if (isCube(field_150939_a) && side >= 0 && side < 6) {
                ForgeDirection face = ForgeDirection.getOrientation(side);
                if (canAdd(world, x, y, z)
                        || canAdd(world, x + face.offsetX, y + face.offsetY, z + face.offsetZ)) return true;
            }
            return super.func_150936_a(world, x, y, z, side, player, stack);
        }

        private boolean canAdd(World world, int x, int y, int z) {
            return world.getBlock(x, y, z) == field_150939_a && cubeMask(world, x, y, z) != 255;
        }

        @Override public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z,
                                          int side, float hitX, float hitY, float hitZ) {
            if (stack.stackSize == 0) return false;
            if (isCube(field_150939_a) && side >= 0 && side < 6) {
                ForgeDirection face = ForgeDirection.getOrientation(side);
                float targetX = hitX + face.offsetX * HIT_EPSILON;
                float targetY = hitY + face.offsetY * HIT_EPSILON;
                float targetZ = hitZ + face.offsetZ * HIT_EPSILON;
                boolean inside = (face.offsetX == 0 || targetX >= 0 && targetX < 1)
                    && (face.offsetY == 0 || targetY >= 0 && targetY < 1)
                    && (face.offsetZ == 0 || targetZ >= 0 && targetZ < 1);
                int nextX = inside ? x : x + face.offsetX;
                int nextY = inside ? y : y + face.offsetY;
                int nextZ = inside ? z : z + face.offsetZ;
                if (world.getBlock(nextX, nextY, nextZ) == field_150939_a) {
                    int slot = (targetX - (nextX - x) >= .5F ? 1 : 0)
                        | (targetY - (nextY - y) >= .5F ? 4 : 0) | (targetZ - (nextZ - z) >= .5F ? 2 : 0);
                    int occupied = cubeMask(world, nextX, nextY, nextZ);
                    if ((occupied & 1 << slot) != 0 || !player.canPlayerEdit(nextX, nextY, nextZ, side, stack)
                            || !world.canMineBlock(player, nextX, nextY, nextZ)
                            || !world.checkNoEntityCollision(cubeBox(nextX, nextY, nextZ, slot))) return false;
                    if (!world.isRemote) {
                        int metadata = world.getBlockMetadata(nextX, nextY, nextZ);
                        if ((metadata & 8) == 0
                                && !world.setBlockMetadataWithNotify(nextX, nextY, nextZ, metadata | 8, 2)) return false;
                        QuarterCubeData.set(world, nextX, nextY, nextZ, occupied | 1 << slot);
                        refreshGeometry(world, nextX, nextY, nextZ);
                        world.notifyBlocksOfNeighborChange(nextX, nextY, nextZ, field_150939_a);
                        Block.SoundType sound = field_150939_a.stepSound;
                        world.playSoundEffect(nextX + .5, nextY + .5, nextZ + .5, sound.func_150496_b(),
                            (sound.getVolume() + 1) / 2, sound.getPitch() * .8F);
                        stack.stackSize--;
                    }
                    return true;
                }
            }
            return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
        }

        @Override public String getItemStackDisplayName(ItemStack stack) {
            BlockQuarterColumn column=(BlockQuarterColumn)field_150939_a;
            String name=column.getUnlocalizedName()+".name";
            if (StatCollector.canTranslate(name)) return StatCollector.translateToLocal(name);
            return StatCollector.translateToLocalFormatted(column.isCube() ? "tile.mbo.quarterCube.format" : "tile.mbo.quarterColumn.format",
                column.ingredient().getDisplayName());
        }
    }

    private static boolean closesFace(int mask) {
        for (int face : FACE_MASKS) {
            if ((mask & face) == face) return true;
        }
        return false;
    }
}
