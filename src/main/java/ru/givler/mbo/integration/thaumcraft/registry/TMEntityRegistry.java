package ru.givler.mbo.integration.thaumcraft.registry;

import cpw.mods.fml.common.registry.EntityRegistry;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.integration.thaumcraft.entities.EntityDarkMatter;
import ru.givler.mbo.integration.thaumcraft.entities.EntityDiffusion;
import ru.givler.mbo.integration.thaumcraft.entities.EntityHomingShard;
import ru.givler.mbo.registry.ModEntityIds;

public class TMEntityRegistry {

    public static void initEntities() {
        EntityRegistry.registerModEntity(EntityDarkMatter.class, "EntityDarkMatter",
                ModEntityIds.next(), MoreBeyondOrdinary.instance, 64, 21, true);
        EntityRegistry.registerModEntity(EntityHomingShard.class, "EntityHomingShard",
                ModEntityIds.next(), MoreBeyondOrdinary.instance, 64, 3, true);
        EntityRegistry.registerModEntity(EntityDiffusion.class, "EntityDiffusion",
                ModEntityIds.next(), MoreBeyondOrdinary.instance, 64, 20, true);
    }
}