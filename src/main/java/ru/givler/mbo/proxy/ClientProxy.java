package ru.givler.mbo.proxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import ru.givler.mbo.EnumParticleType;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.particles.ParticleDarkMagic;
import ru.givler.mbo.registry.ItemRegistry;
import ru.givler.mbo.render.RenderCrossbow;
import ru.givler.mbo.render.RenderLongsword;
import ru.givler.mbo.render.decormodels.TemplateModelRenderer;
import software.bernie.geckolib3.renderers.geo.RenderBlockItem;

public class ClientProxy extends CommonProxy {



    public void preInit(FMLInitializationEvent event){
        super.preInit(event);

    }
    public void init(FMLInitializationEvent event) {
        super.init(event);

    }



    public static void bindDefaultRender(BlockModels block) {
        bindRender(block, block.createNewTileEntity(null, 0), new TemplateModelRenderer());
    }
    public static void bindRender(Block block, TileEntity tile, TileEntitySpecialRenderer tesr) {
        ClientRegistry.bindTileEntitySpecialRenderer(tile.getClass(), tesr);
        Item blockItem = ItemBlock.getItemFromBlock(block);
        MinecraftForgeClient.registerItemRenderer(blockItem, new RenderBlockItem(tesr, tile));
    }
    @Override
    public void registerRenderers() {
        // Используем метод регистрации рендерера для предмета
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.RustyLongsword, new RenderLongsword(ItemRegistry.RustyLongsword.getScale()));
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.OldBowHunting, new RenderCrossbow());
    }

    @Override
    public void spawnParticle(EnumParticleType type, World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        switch (type) {
            case DARK_MAGIC:
                double velX = 0.1, velY = 0.1, velZ = 0.1;
                float r = 1.0f, g = 0.0f, b = 0.0f;
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDarkMagic(world, x, y, z, velX, velY, velZ, r, g, b));
                break;
        }
        }
    }


