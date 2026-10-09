package ru.givler.mbo.registry;

import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.config.MobSpawnConfig;
import ru.givler.mbo.entity.monster.*;
import ru.givler.mbo.entity.fauna.EntityRabbit;
import ru.givler.mbo.entity.monster.EntityStoneGolem;
import ru.givler.mbo.entity.fauna.*;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;

public class EntityMobRegistry {

    private static final int STONE_GOLEM_EGG_PRIMARY = 0x444444;
    private static final int STONE_GOLEM_EGG_SECONDARY = 0x4169E1;


    public static void registerEntities() {
        EntityRegistry.registerModEntity(EntityMovingPlatform.class, "MovingPlatform",
                ModEntityIds.next(), MoreBeyondOrdinary.instance, 256, 1, false);
        registerEntity(EntityStoneGolem.class, "StoneGolem",
                STONE_GOLEM_EGG_PRIMARY, STONE_GOLEM_EGG_SECONDARY);
        registerEntity(EntityRabbit.class, "MBORabbit", 0x995F40, 0x734831);
        registerEntity(EntityMBOCod.class, "MBOCod", 0xb1a58a, 0x9b785e, 3);
        registerEntity(EntityMBOSalmon.class, "MBOSalmon", 0xa00f10, 0xc2ac91, 3);
        registerEntity(EntityMBOTropicalFish.class, "MBOTropicalFish", 0xef6915, 0xfff9ef, 3);
        registerEntity(EntityMBOPufferfish.class, "MBOPufferfish", 0xf6b83d, 0x5b8191, 3);
        registerEntity(EntityMBOPolarBear.class, "MBOPolarBear", 0xf2f2f2, 0x959590, 3);
        registerEntity(EntityMBOTurtle.class, "MBOTurtle", 0xe7e7cf, 0x008c53, 3);
        registerEntity(EntityMBOFrog.class, "MBOFrog", 0xd07444, 0xffc77c, 3);
        registerEntity(EntityMBOTadpole.class, "MBOTadpole", 0x6d533d, 0x160e08, 3);
        registerEntity(EntityMBOGoat.class, "MBOGoat", 0xa5947c, 0x55493e, 3);
        registerEntity(EntityMBOPanda.class, "MBOPanda", 0xe7e7e7, 0x1b1b22, 3);
        registerEntity(EntityMBOFox.class, "MBOFox", 0xd5b69d, 0xcc6920, 3);
        registerEntity(EntityMBOAxolotl.class,"MBOAxolotl",0xfbc1e3,0xa62d6b,3);
        registerEntity(EntityMBOGlowSquid.class,"MBOGlowSquid",0x095656,0x85f1bc,3);
        registerEntity(EntityMBOGuardian.class,"MBOGuardian",0x5a8272,0xf17d30,3);
        registerEntity(EntityMBOElderGuardian.class,"MBOElderGuardian",0xceccba,0x747693,3);
        registerEntity(EntityMBOCamel.class,"MBOCamel",0xc09e7d,0x8d694a,3);
        registerEntity(EntityMBOCamelHusk.class,"MBOCamelHusk",0x6b6447,0xa39b76,3);
        EntityRegistry.registerModEntity(EntityMBOCamelSeat.class,"CamelSeat",ModEntityIds.next(),MoreBeyondOrdinary.instance,64,1,false);
        registerEntity(EntityMBOZombieHorse.class, "MBOZombieHorse", 0x315234, 0x97c284, 3);
        registerEntity(EntityMBOSkeletonHorse.class, "MBOSkeletonHorse", 0x68684f, 0xe5e5d8, 3);

        registerEntity(EntityMBOHusk.class, "MBOHusk", 0x797061, 0xe6cc94, 3);
        registerEntity(EntityMBOParched.class, "MBOParched", 0xc7af86, 0x765e3f, 3);
        registerEntity(EntityMBOStray.class, "MBOStray", 0x617677, 0xddeaea, 3);
        registerEntity(EntityMBOBogged.class, "MBOBogged", 0x8a9c72, 0x314d30, 3);


    }

    private static void registerEntity(Class<? extends Entity> entityClass,
                                       String name, int eggPrimary, int eggSecondary) {
        registerEntity(entityClass, name, eggPrimary, eggSecondary, 1);
    }

    private static void registerEntity(Class<? extends Entity> entityClass,
                                       String name, int eggPrimary, int eggSecondary, int updateFrequency) {
        int globalId = EntityRegistry.findGlobalUniqueEntityId();
        int modId = ModEntityIds.next();
        EntityRegistry.registerGlobalEntityID(entityClass, name, globalId, eggPrimary, eggSecondary);
        EntityRegistry.registerModEntity(entityClass, name, modId, MoreBeyondOrdinary.instance, 64, updateFrequency, true);
    }

    public static void registerConfiguredSpawns() {
        MobSpawnConfig.registerUndeadSpawns();
        MobSpawnConfig.register("stone_golem", EntityStoneGolem.class, EnumCreatureType.monster);
        MobSpawnConfig.register("rabbit", EntityRabbit.class, EnumCreatureType.creature);
        MobSpawnConfig.register("polar_bear", EntityMBOPolarBear.class, EnumCreatureType.creature);
        MobSpawnConfig.register("turtle", EntityMBOTurtle.class, EnumCreatureType.creature);
        MobSpawnConfig.register("frog", EntityMBOFrog.class, EnumCreatureType.creature);
        MobSpawnConfig.register("goat", EntityMBOGoat.class, EnumCreatureType.creature);
        MobSpawnConfig.register("panda", EntityMBOPanda.class, EnumCreatureType.creature);
        MobSpawnConfig.register("fox", EntityMBOFox.class, EnumCreatureType.creature);
        MobSpawnConfig.register("camel",EntityMBOCamel.class,EnumCreatureType.creature);
    }

    public static EntityMBOFish createFish(World world, int type) {
        switch (type) {
            case 0: return new EntityMBOCod(world);
            case 1: return new EntityMBOSalmon(world);
            case 2: return new EntityMBOTropicalFish(world);
            case 3: return new EntityMBOPufferfish(world);
            case 4: return new EntityMBOTadpole(world);
            default: throw new IllegalArgumentException("Unknown fish: " + type);
        }
    }
}
