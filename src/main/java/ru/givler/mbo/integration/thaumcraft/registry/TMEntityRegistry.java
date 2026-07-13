package ru.givler.mbo.integration.thaumcraft.registry;

import cpw.mods.fml.common.registry.EntityRegistry;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.integration.thaumcraft.entities.*;
import ru.givler.mbo.registry.ModEntityIds;

public class TMEntityRegistry {

    public static void initEntities() {
        EntityRegistry.registerModEntity(EntityDarkMatter.class, "EntityDarkMatter",
                ModEntityIds.next(), MoreBeyondOrdinary.instance, 64, 21, true);
        EntityRegistry.registerModEntity(EntityHomingShard.class, "EntityHomingShard",
                ModEntityIds.next(), MoreBeyondOrdinary.instance, 64, 3, true);
        EntityRegistry.registerModEntity(EntityDiffusion.class, "EntityDiffusion",
                ModEntityIds.next(), MoreBeyondOrdinary.instance, 64, 20, true);
        EntityRegistry.registerModEntity(EntityLightMatter.class, "EntityLightMatter",
                ModEntityIds.next(), MoreBeyondOrdinary.instance, 64, 22, true);
        EntityRegistry.registerModEntity(EntityPechShard.class, "EntityPechShard",
                ModEntityIds.next(), MoreBeyondOrdinary.instance, 64, 24, true);
        EntityRegistry.registerModEntity(EntityPechShatter.class, "EntityPechShatter",
                ModEntityIds.next(), MoreBeyondOrdinary.instance, 64, 25, true);
        EntityRegistry.registerModEntity(EntityDarkMoonOrb.class, "EntityDarkMoonOrb",
                ModEntityIds.next(), MoreBeyondOrdinary.instance, 64, 26, true);
    }
}