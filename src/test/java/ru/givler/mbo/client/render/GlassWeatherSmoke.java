package ru.givler.mbo.client.render;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.EnumDifficulty;

public final class GlassWeatherSmoke {
  private static final Block AIR = new Block(Material.air) {
    @Override public boolean isOpaqueCube() { return false; }
    @Override public boolean renderAsNormalBlock() { return false; }
  };
  private static final Block STONE = new Block(Material.rock) {};
  private static final Block SLAB = new BlockStoneSlab(false);
  private static final Block STAIR = new BlockStairs(STONE, 0) {};
  private static final Block HATCH = new BlockTrapDoor(Material.wood) {};

  private static final class Scene extends WorldClient {
    Map<ChunkCoordinates, Block> blocks;

    Scene() { super(null, null, 0, EnumDifficulty.NORMAL, null); }

    @Override public int getHeight() { return 256; }
    @Override public boolean blockExists(int x, int y, int z) { return true; }
    @Override public Block getBlock(int x, int y, int z) {
      return blocks.getOrDefault(new ChunkCoordinates(x, y, z), AIR);
    }
    @Override public int getPrecipitationHeight(int x, int z) {
      int height = 0;
      for (ChunkCoordinates position : blocks.keySet()) {
        if (position.posX == x && position.posZ == z) height = Math.max(height, position.posY + 1);
      }
      return height;
    }

    void put(int y, Block block) { blocks.put(new ChunkCoordinates(0, y, 0), block); }
  }

  public static void main(String[] args) throws Exception {
    Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    field.setAccessible(true);
    Scene scene = (Scene) ((sun.misc.Unsafe) field.get(null)).allocateInstance(Scene.class);
    scene.blocks = new HashMap<>();

    check(exposed(scene), "Open glass should see precipitation");
    scene.put(64, SLAB);
    check(exposed(scene), "A slab should leave the uncovered glass exposed");
    scene.put(65, HATCH);
    check(exposed(scene), "A trapdoor above a slab should leave the exposed glass active");
    scene.put(64, STAIR);
    check(exposed(scene), "Stairs should leave the uncovered glass exposed");
    scene.put(64, STONE);
    check(!exposed(scene), "A full wall must shelter the glass");
    scene.put(64, SLAB);
    scene.put(65, STONE);
    check(!exposed(scene), "A full roof must shelter the glass");

    check(GlassRain.precipitationLevel(1, false, .1F) == 0, "Clear weather retained opaque snow or frost");
    check(GlassRain.precipitationLevel(0, true, .1F) == .1F, "Cold films no longer accumulate");
    verifyDisabledState(scene);
    System.out.println("Glass weather: clear frost, slabs, trapdoors, stairs, full cover and state release PASS");
  }

  @SuppressWarnings("unchecked")
  private static void verifyDisabledState(Scene scene) throws Exception {
    Field world = GlassRain.class.getDeclaredField("world");
    world.setAccessible(true);
    world.set(null, scene);
    Field sections = GlassRain.class.getDeclaredField("SECTIONS");
    sections.setAccessible(true);
    java.util.Set<ChunkCoordinates> previous = (java.util.Set<ChunkCoordinates>) sections.get(null);
    previous.add(new ChunkCoordinates(0, 64, 0));
    GlassRain.apply(false);
    check(world.get(null) == null && sections.get(null) != previous
        && ((java.util.Set<?>) sections.get(null)).isEmpty(), "Disabled weather retained its world or tracked sections");
    check(!GlassRain.needsPass(STONE), "Disabled weather still requests an extra render pass");
  }

  private static boolean exposed(Scene scene) { return GlassRain.exposed(scene, 0, 64, 0, 0, 1); }

  private static void check(boolean passed, String message) {
    if (!passed) throw new AssertionError(message);
  }
}
