package ru.givler.mbo.data.world;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.packet.PacketQuarterCubes;

/** Stores the extra occupancy bits only for cells containing several cubes. */
public final class QuarterCubeData extends WorldSavedData {
    private static final String NAME = "mbo_quarter_cubes";
    private static volatile World clientWorld;
    private static final Map<Long, Map<Integer, Integer>> client = new ConcurrentHashMap<>();
    private final Map<Long, Map<Integer, Integer>> chunks = new HashMap<>();

    public QuarterCubeData(String name) { super(name); }

    private static QuarterCubeData get(World world) {
        QuarterCubeData data = (QuarterCubeData) world.perWorldStorage.loadData(QuarterCubeData.class, NAME);
        if (data == null) {
            data = new QuarterCubeData(NAME);
            world.perWorldStorage.setData(NAME, data);
        }
        return data;
    }

    private static long key(int chunkX, int chunkZ) {
        return (long) chunkX << 32 | chunkZ & 0xffffffffL;
    }

    private static int position(int x, int y, int z) {
        return y << 8 | (z & 15) << 4 | x & 15;
    }

    public static int mask(IBlockAccess access, int x, int y, int z, int metadata) {
        int single = 1 << (metadata & 7);
        if ((metadata & 8) == 0) return single;
        World world = access instanceof World ? (World) access : MoreBeyondOrdinary.proxy.getClientWorld();
        if (world == null) return single;
        long chunk = key(x >> 4, z >> 4);
        Map<Integer, Integer> positions = world.isRemote
            ? (world == clientWorld ? client.get(chunk) : null) : get(world).chunks.get(chunk);
        return positions == null ? single : positions.getOrDefault(position(x, y, z), single);
    }

    public static void set(World world, int x, int y, int z, int mask) {
        QuarterCubeData data = get(world);
        int chunkX = x >> 4, chunkZ = z >> 4;
        long chunk = key(chunkX, chunkZ);
        int pos = position(x, y, z);
        Map<Integer, Integer> positions = data.chunks.get(chunk);
        if (mask == 0) {
            if (positions == null || positions.remove(pos) == null) return;
            if (positions.isEmpty()) data.chunks.remove(chunk);
        } else {
            data.chunks.computeIfAbsent(chunk, ignored -> new HashMap<>()).put(pos, mask);
        }
        data.markDirty();
        PacketQuarterCubes.broadcast(world, chunkX, chunkZ, pos, mask);
    }

    public static void apply(World world, int chunkX, int chunkZ, boolean replace, Map<Integer, Integer> positions) {
        if (clientWorld != world) {
            client.clear();
            clientWorld = world;
        }
        long chunk = key(chunkX, chunkZ);
        if (replace) {
            if (positions.isEmpty()) client.remove(chunk);
            else client.put(chunk, new ConcurrentHashMap<>(positions));
        } else {
            Map<Integer, Integer> current = client.computeIfAbsent(chunk, ignored -> new ConcurrentHashMap<>());
            for (Map.Entry<Integer, Integer> entry : positions.entrySet()) {
                if (entry.getValue() == 0) current.remove(entry.getKey());
                else current.put(entry.getKey(), entry.getValue());
            }
            if (current.isEmpty()) client.remove(chunk);
        }
        for (int pos : positions.keySet()) {
            int x = (chunkX << 4) + (pos & 15), y = pos >> 8, z = (chunkZ << 4) + (pos >> 4 & 15);
            world.markBlockRangeForRenderUpdate(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
        }
    }

    public static final class Events {
        @SubscribeEvent public void watch(ChunkWatchEvent.Watch event) {
            int x = event.chunk.chunkXPos, z = event.chunk.chunkZPos;
            Map<Integer, Integer> positions = get(event.player.worldObj).chunks.get(key(x, z));
            if (positions != null) PacketQuarterCubes.sendChunk(event.player, x, z, positions);
        }

        @SubscribeEvent public void unload(ChunkEvent.Unload event) {
            if (event.world == clientWorld) client.remove(key(event.getChunk().xPosition, event.getChunk().zPosition));
        }

        @SubscribeEvent public void unloadWorld(net.minecraftforge.event.world.WorldEvent.Unload event) {
            if (event.world == clientWorld) {
                clientWorld = null;
                client.clear();
            }
        }
    }

    @Override public void readFromNBT(NBTTagCompound tag) {
        chunks.clear();
        NBTTagList list = tag.getTagList("Chunks", 10);
        for (int index = 0; index < list.tagCount(); index++) {
            NBTTagCompound chunk = list.getCompoundTagAt(index);
            Map<Integer, Integer> positions = new HashMap<>();
            for (int entry : chunk.getIntArray("Cubes")) {
                int mask = entry & 255;
                if (Integer.bitCount(mask) > 1) positions.put(entry >> 8, mask);
            }
            if (!positions.isEmpty()) chunks.put(chunk.getLong("Chunk"), positions);
        }
    }

    @Override public void writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<Long, Map<Integer, Integer>> entry : chunks.entrySet()) {
            NBTTagCompound chunk = new NBTTagCompound();
            chunk.setLong("Chunk", entry.getKey());
            int[] cubes = new int[entry.getValue().size()];
            int index = 0;
            for (Map.Entry<Integer, Integer> cube : entry.getValue().entrySet()) {
                cubes[index++] = cube.getKey() << 8 | cube.getValue();
            }
            chunk.setIntArray("Cubes", cubes);
            list.appendTag(chunk);
        }
        tag.setTag("Chunks", list);
    }
}
