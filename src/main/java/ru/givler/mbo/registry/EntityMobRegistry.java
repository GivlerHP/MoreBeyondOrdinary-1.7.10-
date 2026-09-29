package ru.givler.mbo.registry;

import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.entity.EntityStoneGolem;
import ru.givler.mbo.entity.EntityRabbit;
import net.minecraftforge.common.BiomeDictionary;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;

public class EntityMobRegistry {

    private static final int STONE_GOLEM_EGG_PRIMARY = 0x444444;
    private static final int STONE_GOLEM_EGG_SECONDARY = 0x4169E1;

    private static final int STONE_GOLEM_SPAWN_WEIGHT = 20;
    private static final int STONE_GOLEM_MIN_GROUP = 1;
    private static final int STONE_GOLEM_MAX_GROUP = 1;

    public static void registerEntities() {
        EntityRegistry.registerModEntity(EntityMovingPlatform.class, "MovingPlatform",
                ModEntityIds.next(), MoreBeyondOrdinary.instance, 256, 1, false);
        registerEntity(EntityStoneGolem.class, "StoneGolem",
                STONE_GOLEM_EGG_PRIMARY, STONE_GOLEM_EGG_SECONDARY);
        registerEntity(EntityRabbit.class, "MBORabbit", 0x995F40, 0x734831);

        registerBiomeSpawns(EntityStoneGolem.class,
                STONE_GOLEM_SPAWN_WEIGHT, STONE_GOLEM_MIN_GROUP, STONE_GOLEM_MAX_GROUP);
        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
            if (biome == null || biome.getClass().getName().toLowerCase().contains("divinerpg")) continue;
            if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SANDY)) {
                EntityRegistry.addSpawn(EntityRabbit.class, 3, 1, 3, EnumCreatureType.creature, biome);
            } else if (BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.SNOWY)
                    || BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.PLAINS)
                    || BiomeDictionary.isBiomeOfType(biome, BiomeDictionary.Type.FOREST)) {
                EntityRegistry.addSpawn(EntityRabbit.class, 6, 1, 3, EnumCreatureType.creature, biome);
            }
        }
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
