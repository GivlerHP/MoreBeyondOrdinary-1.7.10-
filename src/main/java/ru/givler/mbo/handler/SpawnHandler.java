package ru.givler.mbo.handler;

import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.BiomeGenBase;
import ru.givler.mbo.entity.EntityStoneGolem;

public class SpawnHandler {
    public static void registerSpawns() {
        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
            if (biome != null) {
                EntityRegistry.addSpawn(EntityStoneGolem.class, 1, 1, 1, EnumCreatureType.monster, biome);
            }
        }
    }
}