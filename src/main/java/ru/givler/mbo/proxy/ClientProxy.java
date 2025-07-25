package ru.givler.mbo.proxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import ru.givler.mbo.EnumParticleType;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.handler.ClientKeyHandler;
import ru.givler.mbo.handler.PotionClientHandler;
import ru.givler.mbo.handler.TooltipEvents;
import ru.givler.mbo.particles.ParticleDarkMagic;
import ru.givler.mbo.particles.ParticleWhiteMagic;
import ru.givler.mbo.potion.SixthSense;
import ru.givler.mbo.registry.ItemRegistry;
import ru.givler.mbo.registry.ModelRegistry;
import ru.givler.mbo.render.RenderCrossbow;
import ru.givler.mbo.render.RenderLongsword;
import ru.givler.mbo.render.RenderUchigatana;
import ru.givler.mbo.render.decormodels.TemplateModelRenderer;
import software.bernie.geckolib3.renderers.geo.RenderBlockItem;



public class ClientProxy extends CommonProxy {
    public static KeyBinding activateAmuletKey;

    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    public void init(FMLInitializationEvent event) {
        super.init(event);
        activateAmuletKey = new KeyBinding("Кнопка амулета", Keyboard.KEY_R, "MoreBeyondOrdinary");
        ClientRegistry.registerKeyBinding(activateAmuletKey);

        FMLCommonHandler.instance().bus().register(new ClientKeyHandler());

        bindDefaultRender(ModelRegistry.ModelThreads);
        bindDefaultRender(ModelRegistry.ModelCloth);
        bindDefaultRender(ModelRegistry.ModelTailorShelf);
        bindDefaultRender(ModelRegistry.ModelDummy);
        bindDefaultRender(ModelRegistry.ModelHangers);
        bindDefaultRender(ModelRegistry.ModelPillow);
        bindDefaultRender(ModelRegistry.ModelRulers);
        bindDefaultRender(ModelRegistry.ModelScissors);

        bindDefaultRender(ModelRegistry.ModelIngredients);
        bindDefaultRender(ModelRegistry.ModelCauldron);
        bindDefaultRender(ModelRegistry.ModelBottles);
        bindDefaultRender(ModelRegistry.ModelBooks);
        bindDefaultRender(ModelRegistry.ModelAlchemistShelf);
        bindDefaultRender(ModelRegistry.ModelAlchemicalFlag);

        bindDefaultRender(ModelRegistry.ModelArrow);
        bindDefaultRender(ModelRegistry.ModelBowWall);
        bindDefaultRender(ModelRegistry.ModelBow);
        bindDefaultRender(ModelRegistry.ModelDucks);
        bindDefaultRender(ModelRegistry.ModelFurKnife);
        bindDefaultRender(ModelRegistry.ModelFur);
        bindDefaultRender(ModelRegistry.ModelHorn);
        bindDefaultRender(ModelRegistry.ModelLeatherDryer);
        bindDefaultRender(ModelRegistry.ModelRabbits);
        bindDefaultRender(ModelRegistry.ModelMooseHead);

        bindDefaultRender(ModelRegistry.ModelMagnifyinGlass);
        bindDefaultRender(ModelRegistry.ModelBagGold);
        bindDefaultRender(ModelRegistry.ModelCoins);
        bindDefaultRender(ModelRegistry.ModelSmallChest);
        bindDefaultRender(ModelRegistry.ModelScales);

        bindDefaultRender(ModelRegistry.ModelBagsPotatoes);
        bindDefaultRender(ModelRegistry.ModelBasketApples);
        bindDefaultRender(ModelRegistry.ModelBasketBerries);
        bindDefaultRender(ModelRegistry.ModelBucket);
        bindDefaultRender(ModelRegistry.ModelCarrot);
        bindDefaultRender(ModelRegistry.ModelGarlic);
        bindDefaultRender(ModelRegistry.ModelHay);
        bindDefaultRender(ModelRegistry.ModelHayfork);
        bindDefaultRender(ModelRegistry.ModelJugs);
        bindDefaultRender(ModelRegistry.ModelShelfFlower);
        bindDefaultRender(ModelRegistry.ModelWateringСan);
        bindDefaultRender(ModelRegistry.ModelWheelBarrow);

        bindDefaultRender(ModelRegistry.ModelFilledChest);
        bindDefaultRender(ModelRegistry.ModelPliers);
        bindDefaultRender(ModelRegistry.ModelJewelryHammer);
        bindDefaultRender(ModelRegistry.ModelAmulet);
        bindDefaultRender(ModelRegistry.ModelInstruments);

        bindDefaultRender(ModelRegistry.ModelLute);
        bindDefaultRender(ModelRegistry.ModelBroom);
        bindDefaultRender(ModelRegistry.ModelWanted);
        bindDefaultRender(ModelRegistry.ModelPapers);
        bindDefaultRender(ModelRegistry.ModelKeys);
        bindDefaultRender(ModelRegistry.ModelDeskBell);

        bindDefaultRender(ModelRegistry.ModelSword);
        bindDefaultRender(ModelRegistry.ModelSwords);
        bindDefaultRender(ModelRegistry.ModelShield1);
        bindDefaultRender(ModelRegistry.ModelShield2);
        bindDefaultRender(ModelRegistry.ModelShield3);
        bindDefaultRender(ModelRegistry.ModelHelmet);
        bindDefaultRender(ModelRegistry.ModelHammer);
        bindDefaultRender(ModelRegistry.ModelDragonSlayer);
        bindDefaultRender(ModelRegistry.ModelAxe);

        bindDefaultRender(ModelRegistry.ModelGas);
        bindDefaultRender(ModelRegistry.ModelOiler);
        bindDefaultRender(ModelRegistry.ModelGears);
        bindDefaultRender(ModelRegistry.ModelDrawing1);
        bindDefaultRender(ModelRegistry.ModelDrawing2);
        bindDefaultRender(ModelRegistry.ModelClock);
        bindDefaultRender(ModelRegistry.ModelBrokenMechanism);
        registerRenderers();
        MinecraftForge.EVENT_BUS.register(new PotionClientHandler());
        MinecraftForge.EVENT_BUS.register(new TooltipEvents());
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
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.Uchigatana, new RenderUchigatana(ItemRegistry.Uchigatana.getScale()));
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.OldBowHunting, new RenderCrossbow());
    }

    @Override
    public void spawnParticle(EnumParticleType type, World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
        float r = 0, g = 0, b = 0;
        int textureIndex = 0;
        EntityFX particle = null;

        switch (type) {
            case SACRED:
                r = 1.0f; g = 1.0f; b = 0.6f;
                textureIndex = 145;
                particle = new ParticleWhiteMagic(world, x, y, z, motionX, motionY, motionZ, r, g, b);
                break;
            case DARK_MAGIC:
                r = 0.7f; g = 0.8f; b = 0.9f;
                textureIndex = 162;
                particle = new ParticleWhiteMagic(world, x, y, z, motionX, motionY, motionZ, r, g, b);
                break;
        }

        if (particle != null) {
            if (particle instanceof ParticleWhiteMagic) {
                ((ParticleWhiteMagic) particle).setBaseSpellTextureIndex(textureIndex);
            } else if (particle instanceof ParticleDarkMagic) {
                ((ParticleDarkMagic) particle).setBaseSpellTextureIndex(textureIndex);
            }
            Minecraft.getMinecraft().effectRenderer.addEffect(particle);
        }
    }

}


