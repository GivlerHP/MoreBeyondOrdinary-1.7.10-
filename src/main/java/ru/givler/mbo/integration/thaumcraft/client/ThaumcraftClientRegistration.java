package ru.givler.mbo.integration.thaumcraft.client;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.MinecraftForgeClient;
import ru.givler.mbo.integration.thaumcraft.ThaumcraftRegistry;
import ru.givler.mbo.integration.thaumcraft.client.render.ItemStaffRenderer;
import ru.givler.mbo.integration.thaumcraft.client.render.entity.RenderEldritchOrbWhite;
import ru.givler.mbo.integration.thaumcraft.client.render.entity.RenderEntityDarkMoonOrb;
import ru.givler.mbo.integration.thaumcraft.client.render.entity.RenderEntityDiffusion;
import ru.givler.mbo.integration.thaumcraft.client.render.entity.RenderEntityHomingShard;
import ru.givler.mbo.integration.thaumcraft.entities.EntityDarkMatter;
import ru.givler.mbo.integration.thaumcraft.entities.EntityDarkMoonOrb;
import ru.givler.mbo.integration.thaumcraft.entities.EntityDiffusion;
import ru.givler.mbo.integration.thaumcraft.entities.EntityHomingShard;
import ru.givler.mbo.integration.thaumcraft.entities.EntityLightMatter;
import ru.givler.mbo.integration.thaumcraft.entities.EntityPechShard;
import ru.givler.mbo.integration.thaumcraft.entities.EntityPechShatter;
import thaumcraft.client.renderers.entity.RenderEldritchOrb;
import thaumcraft.client.renderers.entity.RenderPechBlast;

/** Loaded reflectively only when Thaumcraft is installed. */
public final class ThaumcraftClientRegistration {
    private ThaumcraftClientRegistration() {}

    public static void register() {
        RenderingRegistry.registerEntityRenderingHandler(EntityDarkMatter.class, new RenderEldritchOrb());
        RenderingRegistry.registerEntityRenderingHandler(EntityHomingShard.class, new RenderEntityHomingShard());
        RenderingRegistry.registerEntityRenderingHandler(EntityDiffusion.class, new RenderEntityDiffusion());
        RenderingRegistry.registerEntityRenderingHandler(EntityLightMatter.class, new RenderEldritchOrbWhite());
        RenderingRegistry.registerEntityRenderingHandler(EntityPechShatter.class, new RenderPechBlast());
        RenderingRegistry.registerEntityRenderingHandler(EntityPechShard.class, new RenderPechBlast());
        RenderingRegistry.registerEntityRenderingHandler(EntityDarkMoonOrb.class, new RenderEntityDarkMoonOrb());

        ItemStaffRenderer renderer = new ItemStaffRenderer();
        MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffFire, renderer);
        MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffNature, renderer);
        MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffFrost, renderer);
        MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffLantern, renderer);
        MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffLight, renderer);
        MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffChillSorrow, renderer);
        MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffNaturalMoon, renderer);
        MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffLightningDragon, renderer);
        MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffDarkMoon, renderer);
        MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffDemonic, renderer);
    }
}
