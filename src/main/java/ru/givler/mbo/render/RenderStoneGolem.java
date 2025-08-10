package ru.givler.mbo.render;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.model.ModelIronGolem;
import net.minecraft.client.renderer.entity.RenderIronGolem;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import ru.givler.mbo.entity.EntityStoneGolem;
import ru.givler.mbo.MoreBeyondOrdinary;

public class RenderStoneGolem extends RenderIronGolem {
    private static final ResourceLocation texture = new ResourceLocation(MoreBeyondOrdinary.MODID + ":" + "textures/entity/stone_golem.png");

    public RenderStoneGolem() {
        super();
        this.mainModel = new ModelIronGolem();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return texture;
    }

    public static void register() {
        RenderingRegistry.registerEntityRenderingHandler(EntityStoneGolem.class, new RenderStoneGolem());
    }
}