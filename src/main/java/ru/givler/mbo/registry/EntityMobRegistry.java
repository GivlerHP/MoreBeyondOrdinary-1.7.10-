package ru.givler.mbo.registry;

import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.entity.EntityStoneGolem;

public class EntityMobRegistry {

    private static final int STONE_GOLEM_EGG_PRIMARY = 0x444444;
    private static final int STONE_GOLEM_EGG_SECONDARY = 0x4169E1;

    private static final int STONE_GOLEM_SPAWN_WEIGHT = 20;
    private static final int STONE_GOLEM_MIN_GROUP = 1;
    private static final int STONE_GOLEM_MAX_GROUP = 1;

    public static void registerEntities() {
        registerEntity(EntityStoneGolem.class, "StoneGolem",
                STONE_GOLEM_EGG_PRIMARY, STONE_GOLEM_EGG_SECONDARY);

        registerBiomeSpawns(EntityStoneGolem.class,
                STONE_GOLEM_SPAWN_WEIGHT, STONE_GOLEM_MIN_GROUP, STONE_GOLEM_MAX_GROUP);
    }

    private static void registerEntity(Class<? extends net.minecraft.entity.Entity> entityClass,
                                       String name, int eggPrimary, int eggSecondary) {
        int globalId = EntityRegistry.findGlobalUniqueEntityId();
        int modId = ModEntityIds.next();
        EntityRegistry.registerGlobalEntityID(entityClass, name, globalId, eggPrimary, eggSecondary);
        EntityRegistry.registerModEntity(entityClass, name, modId, MoreBeyondOrdinary.instance, 64, 1, true);
    }

    private static void registerBiomeSpawns(Class<? extends net.minecraft.entity.EntityLiving> entityClass,
                                            int weight, int min, int max) {
        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
            if (biome != null) {
                EntityRegistry.addSpawn(entityClass, weight, min, max, EnumCreatureType.monster, biome);
            }
        }
    }
}