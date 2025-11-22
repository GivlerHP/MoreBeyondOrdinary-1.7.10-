package ru.givler.mbo.registry;

import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import ru.givler.mbo.entity.EntityStoneGolem;
import ru.givler.mbo.MoreBeyondOrdinary;

public class EntityMobRegistry {
    private static int entityID = 0;

    public static void registerEntities() {
        registerEntity(EntityStoneGolem.class, "StoneGolem", 0x444444, 0x4169E1);
        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
            if (biome != null) {
                EntityRegistry.addSpawn(EntityStoneGolem.class,
                        20, 1, 1, EnumCreatureType.monster, biome);
            }
        }
    }

    private static void registerEntity(Class entityClass, String name, int eggPrimary, int eggSecondary) {
        int globalId = EntityRegistry.findGlobalUniqueEntityId();
        int modId = entityID++;
        EntityRegistry.registerGlobalEntityID(entityClass, name, globalId, eggPrimary, eggSecondary);
        EntityRegistry.registerModEntity(entityClass, name, modId, MoreBeyondOrdinary.instance, 64, 1, true);
    }
}
