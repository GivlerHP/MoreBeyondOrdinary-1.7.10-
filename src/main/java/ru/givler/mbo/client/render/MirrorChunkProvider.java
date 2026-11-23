package ru.givler.mbo.client.render;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;

/** Holds streamed chunks without invoking the main world's renderer or loading hooks. */
final class MirrorChunkProvider implements IChunkProvider {
    private final World world;
    private final Chunk empty;
    private final Map<Long,Chunk> chunks = new HashMap<>();

    MirrorChunkProvider(World world) {
        this.world = world;
        empty = new EmptyChunk(world,0,0);
    }

    @Override public boolean chunkExists(int x,int z) {
        return chunks.containsKey(ChunkCoordIntPair.chunkXZ2Int(x,z));
    }

    @Override public Chunk provideChunk(int x,int z) {
        return chunks.getOrDefault(ChunkCoordIntPair.chunkXZ2Int(x,z),empty);
    }

    @Override public Chunk loadChunk(int x,int z) {
        return chunks.computeIfAbsent(ChunkCoordIntPair.chunkXZ2Int(x,z),key -> new Chunk(world,x,z));
    }

    @Override public int getLoadedChunkCount() { return chunks.size(); }
    @Override public boolean canSave() { return false; }
    @Override public boolean saveChunks(boolean all,IProgressUpdate progress) { return true; }
    @Override public void saveExtraData() {}
    @Override public boolean unloadQueuedChunks() { return false; }
    @Override public void populate(IChunkProvider provider,int x,int z) {}
    @Override public void recreateStructures(int x,int z) {}
    @Override public String makeString() { return "Silver mirror chunks: "+chunks.size(); }
    @Override public List<?> getPossibleCreatures(EnumCreatureType type,int x,int y,int z) {
        return Collections.emptyList();
    }
    @Override public ChunkPosition func_147416_a(World world,String name,int x,int y,int z) { return null; }
}
