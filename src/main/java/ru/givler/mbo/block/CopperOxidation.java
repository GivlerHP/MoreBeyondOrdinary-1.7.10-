package ru.givler.mbo.block;

import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketSpawnParticle;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.waterlogging.WaterloggedWorldData;

import java.util.Random;

/** Shared vanilla-style weathering and wax/axe interaction for copper blocks. */
public final class CopperOxidation {
    private CopperOxidation() {}

    public static void tick(World world, int x, int y, int z, Random random) {
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        int state = state(block, meta);
        if (state < 0 || (state & 8) != 0 || (state & 3) == 3) return;
        if (random.nextFloat() >= 0.05688889F * moistureMultiplier(world, x, y, z, block, meta)) return;
        int stage = state & 3;
        int same = 0;
        int later = 0;
        for (int dx = -4; dx <= 4; dx++) for (int dy = -4; dy <= 4; dy++) for (int dz = -4; dz <= 4; dz++) {
            if (dx == 0 && dy == 0 && dz == 0 || Math.abs(dx) + Math.abs(dy) + Math.abs(dz) > 4) continue;
            Block other = world.getBlock(x + dx, y + dy, z + dz);
            int otherState = state(other, world.getBlockMetadata(x + dx, y + dy, z + dz));
            if (otherState < 0 || (otherState & 8) != 0) continue;
            int otherStage = otherState & 3;
            if (otherStage < stage) return;
            if (otherStage == stage) same++;
            else later++;
        }
        float chance = (later + 1.0F) / (later + same + 1.0F);
        if (stage == 0) chance *= 0.75F;
        if (random.nextFloat() < chance * chance) setState(world, x, y, z, block, meta, state + 1);
    }

    private static float moistureMultiplier(World world, int x, int y, int z, Block block, int meta) {
        WaterloggedWorldData water = WaterloggedWorldData.get(world);
        boolean upperDoor = block instanceof DoorBase && (meta & 8) != 0;
        if (water.contains(x, y, z) || (upperDoor && water.contains(x, y - 1, z))) return 4.0F;

        if (touchesWater(world, water, x, y, z)
                || (upperDoor && touchesWater(world, water, x, y - 1, z))) return 3.0F;

        if (world.isRaining() && world.canBlockSeeTheSky(x, y + 1, z)
                && world.getBiomeGenForCoords(x, z).canSpawnLightningBolt()) return 2.0F;
        return 1.0F;
    }

    private static boolean touchesWater(World world, WaterloggedWorldData water, int x, int y, int z) {
        final int[][] neighbors = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0},
                {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        for (int[] offset : neighbors) {
            int nx = x + offset[0], ny = y + offset[1], nz = z + offset[2];
            if (ny < 0 || ny > 255 || !world.blockExists(nx, ny, nz)) continue;
            if (world.getBlock(nx, ny, nz).getMaterial() == Material.water || water.contains(nx, ny, nz))
                return true;
        }
        return false;
    }

    public static boolean interact(World world, int x, int y, int z, EntityPlayer player) {
        ItemStack held = player.getCurrentEquippedItem();
        if (held == null) return false;
        Block block = world.getBlock(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);
        int state = state(block, meta);
        if (state < 0) return false;
        boolean wax = isWax(held);
        boolean axe = held.getItem().getToolClasses(held).contains("axe");
        int next = state;
        String sound = null;
        EnumParticleType particles = null;
        if (wax && (state & 8) == 0) {
            next = state | 8;
            sound = "mbo:copper.wax_on";
            particles = EnumParticleType.COPPER_WAX_ON;
        } else if (axe && (state & 8) != 0) {
            next = state & 7;
            sound = "mbo:copper.wax_off";
            particles = EnumParticleType.COPPER_WAX_OFF;
        } else if (axe && (state & 3) > 0) {
            next = state - 1;
            sound = "mbo:copper.scrape";
            particles = EnumParticleType.COPPER_SCRAPE;
        }
        if (sound == null) return false;
        if (!world.isRemote) {
            setState(world, x, y, z, block, meta, next);
            world.playSoundEffect(x + .5D, y + .5D, z + .5D, sound, 1.0F,
                    0.9F + world.rand.nextInt(3) * 0.1F);
            if (block instanceof DoorBase) {
                emitParticles(world, x, y, z, particles);
                emitParticles(world, x, y + 1, z, particles);
            } else {
                emitParticles(world, x, y, z, particles);
            }
            if (!player.capabilities.isCreativeMode) {
                if (wax) held.stackSize--;
                else held.damageItem(1, player);
                if (held.stackSize <= 0) player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
            }
        }
        return true;
    }

    private static void emitParticles(World world, int x, int y, int z, EnumParticleType type) {
        NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(
                world.provider.dimensionId, x + 0.5D, y + 0.5D, z + 0.5D, 64.0D);
        for (int i = 0; i < 10; i++) {
            int side = i % 6;
            int nx = x + (side == 0 ? -1 : side == 1 ? 1 : 0);
            int ny = y + (side == 2 ? -1 : side == 3 ? 1 : 0);
            int nz = z + (side == 4 ? -1 : side == 5 ? 1 : 0);
            if (world.getBlock(nx, ny, nz).isOpaqueCube()) continue;
            double px = x + world.rand.nextDouble();
            double py = y + world.rand.nextDouble();
            double pz = z + world.rand.nextDouble();
            if (side == 0) px = x - 0.0625D;
            else if (side == 1) px = x + 1.0625D;
            else if (side == 2) py = y - 0.0625D;
            else if (side == 3) py = y + 1.0625D;
            else if (side == 4) pz = z - 0.0625D;
            else pz = z + 1.0625D;
            PacketManager.INSTANCE.sendToAllAround(
                    new PacketSpawnParticle(type, px, py, pz, 0, 0, 0), point);
        }
    }

    private static int state(Block block, int meta) {
        if (block instanceof BlockMeta && ((BlockMeta) block).isCopper()) return meta;
        if (block instanceof BlockMetaStairs) return ((BlockMetaStairs) block).getCopperState();
        if (block instanceof BlockMetaSlab) return ((BlockMetaSlab) block).getCopperState();
        if (block instanceof TrapDoorBase) return ((TrapDoorBase) block).getCopperState();
        if (block instanceof DoorBase) return ((DoorBase) block).getCopperState();
        if (block instanceof BlockCopperBulb) return ((BlockCopperBulb) block).copperState;
        return -1;
    }

    private static void setState(World world, int x, int y, int z, Block block, int meta, int state) {
        if (block instanceof BlockMeta && ((BlockMeta) block).isCopper()) world.setBlockMetadataWithNotify(x, y, z, state, 3);
        else if (block instanceof BlockMetaStairs)
            world.setBlock(x, y, z, BlockRegistry.CutCopperStairs[state], meta, 3);
        else if (block instanceof BlockMetaSlab)
            world.setBlock(x, y, z, ((BlockMetaSlab) block).isDoubleSlab()
                    ? BlockRegistry.CutCopperDoubleSlabs[state] : BlockRegistry.CutCopperSlabs[state], meta, 3);
        else if (block instanceof TrapDoorBase)
            world.setBlock(x, y, z, BlockRegistry.CopperTrapdoors[state], meta, 3);
        else if (block instanceof DoorBase) {
            int bottomY = (meta & 8) != 0 ? y - 1 : y;
            int bottomMeta = world.getBlockMetadata(x, bottomY, z);
            int topMeta = world.getBlockMetadata(x, bottomY + 1, z);
            Block replacement = BlockRegistry.CopperDoors[state];
            world.setBlock(x, bottomY, z, replacement, bottomMeta, 2);
            world.setBlock(x, bottomY + 1, z, replacement, topMeta, 2);
            world.markBlockRangeForRenderUpdate(x, bottomY, z, x, bottomY + 1, z);
        }
        else if (block instanceof BlockCopperBulb)
            world.setBlock(x, y, z, BlockRegistry.CopperBulbs[state], meta, 3);
    }

    public static boolean isWax(ItemStack stack) {
        for (int id : OreDictionary.getOreIDs(stack)) {
            String name = OreDictionary.getOreName(id);
            if ("materialHoneycomb".equals(name) || "materialWaxcomb".equals(name)
                    || "materialWax".equals(name) || "itemBeeswax".equals(name)) return true;
        }
        return false;
    }
}
