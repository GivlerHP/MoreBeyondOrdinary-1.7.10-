package ru.givler.mbo.handler;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import ru.givler.mbo.config.MobSpawnConfig;
import ru.givler.mbo.entity.fauna.*;

/** Separate modern aquatic quotas, using Forge spawn events and existing habitat checks. */
public final class FaunaAquaticSpawner {
  public enum Group {
    FISH(new String[] {"cod", "salmon", "tropical_fish", "pufferfish"}),
    AXOLOTLS(new String[] {"axolotl"}),
    GLOW_SQUID(new String[] {"glow_squid"}),
    GUARDIANS(new String[] {"guardian"});
    final String[] species;

    Group(String[] species) {
      this.species = species;
    }
  }

  @SubscribeEvent
  public void tick(TickEvent.WorldTickEvent event) {
    if (event.phase != TickEvent.Phase.END
        || event.world.isRemote
        || !(event.world instanceof WorldServer)) return;
    WorldServer world = (WorldServer) event.world;
    if (world.getTotalWorldTime() % MobSpawnConfig.aquaticInterval() != 0
        || !world.getGameRules().getGameRuleBooleanValue("doMobSpawning")
        || world.playerEntities.isEmpty()) return;
    List<Long> chunks = eligibleChunks(world);
    if (chunks.isEmpty()) return;
    int[] counts = new int[Group.values().length];
    for (Object entry : world.loadedEntityList) {
      Group group = category((Entity) entry);
      if (group != null && countsTowardQuota((Entity) entry)) counts[group.ordinal()]++;
    }
    // Guardians share the original hostile quota with ordinary monsters.
    counts[Group.GUARDIANS.ordinal()] = world.countEntities(EnumCreatureType.monster, true);
    for (Group group : Group.values()) {
      if (group == Group.GUARDIANS && world.difficultySetting.getDifficultyId() == 0) continue;
      if (group != Group.GUARDIANS && !world.func_73046_m().getCanSpawnAnimals()) continue;
      int cap = scaledCap(MobSpawnConfig.aquaticCap(group), chunks.size());
      int budget =
          group == Group.FISH
              ? fishAttemptBudget(MobSpawnConfig.aquaticAttempts(), chunks.size())
              : MobSpawnConfig.aquaticAttempts();
      int minimum = MobSpawnConfig.minimumY(group), maximum = MobSpawnConfig.maximumY(group);
      if (maximum < minimum) continue;
      for (int attempt = 0; attempt < budget && counts[group.ordinal()] < cap; attempt++) {
        long chunk = chunks.get(world.rand.nextInt(chunks.size()));
        int x = ((int) (chunk >> 32) << 4) + world.rand.nextInt(16);
        int z = ((int) chunk << 4) + world.rand.nextInt(16);
        int y =
            group == Group.FISH
                ? fishSpawnY(world, x, z, minimum, maximum)
                : minimum + world.rand.nextInt(maximum - minimum + 1);
        if (y < minimum) continue;
        if (!awayFromPlayers(world, x, y, z) || !awayFromWorldSpawn(world, x, y, z)) continue;
        String species = chooseSpecies(group, world, x, y, z);
        if (species != null) counts[group.ordinal()] += spawnGroup(world, species, x, y, z);
      }
    }
  }

  /** A full spawn area is 17x17 chunks; caps scale with the deduplicated loaded area. */
  public static int scaledCap(int base, int chunks) {
    return base <= 0 || chunks <= 0 ? 0 : Math.max(1, base * chunks / 289);
  }

  /** Increase fish search coverage with player area, while bounding the work per pass. */
  public static int fishAttemptBudget(int base, int chunks) {
    return chunks <= 0 ? 0 : Math.min(256, Math.max(base, base * 4 * chunks / 289));
  }

  /** Select uniformly among submerged positions, instead of spending attempts in air/stone. */
  public static int fishSpawnY(World world, int x, int z, int minimum, int maximum) {
    int top = Math.min(maximum, world.getHeightValue(x, z) - 1);
    int selected = minimum - 1, candidates = 0;
    for (int y = top; y >= minimum; y--) {
      if (!world.blockExists(x, y, z) || !world.blockExists(x, y + 1, z)) continue;
      if (world.getBlock(x, y, z).getMaterial() == Material.water
          && world.getBlock(x, y + 1, z).getMaterial() == Material.water
          && world.rand.nextInt(++candidates) == 0) selected = y;
    }
    return selected;
  }

  public static Group category(Entity entity) {
    if (entity instanceof EntityMBOElderGuardian || entity instanceof EntityMBOTadpole) return null;
    if (entity instanceof EntityMBOGuardian) return Group.GUARDIANS;
    if (entity instanceof EntityMBOAxolotl) return Group.AXOLOTLS;
    if (entity instanceof EntityMBOGlowSquid) return Group.GLOW_SQUID;
    if (entity instanceof EntityMBOFish) return Group.FISH;
    return null;
  }

  public static boolean countsTowardQuota(Entity entity) {
    if (entity.isDead || !(entity instanceof EntityLiving)) return false;
    EntityLiving living = (EntityLiving) entity;
    return !living.isNoDespawnRequired()
        && !living.hasCustomNameTag()
        && (!(entity instanceof EntityMBOFish) || !((EntityMBOFish) entity).fromBucket())
        && (!(entity instanceof EntityMBOAxolotl) || !((EntityMBOAxolotl) entity).fromBucket());
  }

  private static List<Long> eligibleChunks(WorldServer world) {
    Set<Long> chunks = new HashSet<Long>();
    for (Object entry : world.playerEntities) {
      EntityPlayer player = (EntityPlayer) entry;
      if (!player.isEntityAlive()) continue;
      int cx = MathHelper.floor_double(player.posX) >> 4,
          cz = MathHelper.floor_double(player.posZ) >> 4;
      for (int dx = -8; dx <= 8; dx++)
        for (int dz = -8; dz <= 8; dz++)
          if (world.getChunkProvider().chunkExists(cx + dx, cz + dz))
            chunks.add(((long) (cx + dx) << 32) | ((cz + dz) & 0xffffffffL));
    }
    return new ArrayList<Long>(chunks);
  }

  private static boolean awayFromPlayers(World world, double x, double y, double z) {
    return world.getClosestPlayer(x, y, z, 24) == null
        && world.getClosestPlayer(x, y, z, 128) != null;
  }

  private static boolean awayFromWorldSpawn(World world, int x, int y, int z) {
    ChunkCoordinates spawn = world.getSpawnPoint();
    double dx = x - spawn.posX, dy = y - spawn.posY, dz = z - spawn.posZ;
    return dx * dx + dy * dy + dz * dz >= 576;
  }

  private static String chooseSpecies(Group group, World world, int x, int y, int z) {
    int total = 0;
    for (String id : group.species)
      if (MobSpawnConfig.allowsPosition(id, world, x, y, z))
        total += MobSpawnConfig.spawnWeight(id, world.getBiomeGenForCoords(x, z));
    if (total == 0) return null;
    int selected = world.rand.nextInt(total);
    for (String id : group.species)
      if (MobSpawnConfig.allowsPosition(id, world, x, y, z)) {
        selected -= MobSpawnConfig.spawnWeight(id, world.getBiomeGenForCoords(x, z));
        if (selected < 0) return id;
      }
    return null;
  }

  /** Preserve the original group range, even when the final group slightly exceeds the cap. */
  public static int spawnGroup(World world, String species, int x, int y, int z) {
    int minimum = MobSpawnConfig.minGroup(species, world.getBiomeGenForCoords(x, z));
    int maximum = MobSpawnConfig.maxGroup(species, world.getBiomeGenForCoords(x, z));
    int size = minimum + world.rand.nextInt(maximum - minimum + 1), spawned = 0;
    IEntityLivingData data = null;
    for (int member = 0; member < size; member++) {
      for (int attempt = 0; attempt < 4; attempt++) {
        int sx = x + world.rand.nextInt(5) - 2, sz = z + world.rand.nextInt(5) - 2;
        if (!world.blockExists(sx, y, sz)
            || !MobSpawnConfig.allowsPosition(species, world, sx, y, sz)) continue;
        EntityLiving animal = create(species, world);
        animal.setLocationAndAngles(sx + .5, y, sz + .5, world.rand.nextFloat() * 360, 0);
        // Player/spawn-distance restrictions apply to every member, not just the group centre.
        if (world instanceof WorldServer
            && !world.playerEntities.isEmpty()
            && (!awayFromPlayers(world, animal.posX, animal.posY, animal.posZ)
                || !awayFromWorldSpawn(world, sx, y, sz))) continue;
        Result result =
            ForgeEventFactory.canEntitySpawn(
                animal, world, (float) animal.posX, (float) animal.posY, (float) animal.posZ);
        if (result == Result.DENY || result == Result.DEFAULT && !animal.getCanSpawnHere())
          continue;
        if (!world.spawnEntityInWorld(animal)) continue;
        if (!ForgeEventFactory.doSpecialSpawn(
            animal, world, (float) animal.posX, (float) animal.posY, (float) animal.posZ))
          data = animal.onSpawnWithEgg(data);
        spawned++;
        break;
      }
    }
    return spawned;
  }

  private static EntityLiving create(String id, World world) {
    if (id.equals("cod")) return new EntityMBOCod(world);
    if (id.equals("salmon")) return new EntityMBOSalmon(world);
    if (id.equals("tropical_fish")) return new EntityMBOTropicalFish(world);
    if (id.equals("pufferfish")) return new EntityMBOPufferfish(world);
    if (id.equals("axolotl")) return new EntityMBOAxolotl(world);
    if (id.equals("glow_squid")) return new EntityMBOGlowSquid(world);
    if (id.equals("guardian")) return new EntityMBOGuardian(world);
    throw new IllegalArgumentException("Unknown aquatic spawn species: " + id);
  }
}
