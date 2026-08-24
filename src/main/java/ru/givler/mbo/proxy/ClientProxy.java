package ru.givler.mbo.proxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
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
import ru.givler.mbo.integration.thaumcraft.ThaumcraftRegistry;
import ru.givler.mbo.integration.nei.ArcanumNEIConfig;
import ru.givler.mbo.integration.thaumcraft.client.render.ItemStaffRenderer;
import ru.givler.mbo.integration.thaumcraft.client.render.entity.RenderEldritchOrbWhite;
import ru.givler.mbo.integration.thaumcraft.client.render.entity.RenderEntityDarkMoonOrb;
import ru.givler.mbo.integration.thaumcraft.entities.*;
import ru.givler.mbo.integration.thaumcraft.client.render.entity.RenderEntityDiffusion;
import ru.givler.mbo.integration.thaumcraft.client.render.entity.RenderEntityHomingShard;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.handler.ClientKeyHandler;
import ru.givler.mbo.handler.BarrierVisibilityHandler;
import ru.givler.mbo.handler.PotionClientHandler;
import ru.givler.mbo.handler.TooltipEvents;
import ru.givler.mbo.particles.ParticleDarkMagic;
import ru.givler.mbo.particles.ParticleWhiteMagic;
import ru.givler.mbo.registry.ItemRegistry;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.registry.ModelRegistry;
import ru.givler.mbo.registry.BannerRegistry;
import ru.givler.mbo.registry.StonecutterRegistry;
import ru.givler.mbo.entity.boat.EntityMBOBoat;
import ru.givler.mbo.entity.boat.EntityMBOChestBoat;
import ru.givler.mbo.entity.boat.EntityMBOBoatSeat;
import ru.givler.mbo.client.render.boat.RenderMBOBoat;
import ru.givler.mbo.client.render.boat.RenderMBOBoatSeat;
import ru.givler.mbo.client.render.*;
import ru.givler.mbo.client.font.ModernFontSupport;
import ru.givler.mbo.core.CauldronHooks;
import ru.givler.mbo.client.render.decormodels.RenderLootContainerItem;
import ru.givler.mbo.client.render.decormodels.RenderLootContainerTile;
import ru.givler.mbo.client.render.decormodels.TemplateModelRenderer;
import ru.givler.mbo.client.render.decormodels.TemplateItemModelRenderer;
import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.tileentity.TileEntityLootContainer;
import ru.givler.mbo.banner.TileEntityBanner;
import ru.givler.mbo.banner.client.RenderBanner;
import ru.givler.mbo.banner.client.RenderBannerItem;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.renderers.geo.RenderBlockItem;
import thaumcraft.client.renderers.entity.RenderEldritchOrb;
import thaumcraft.client.renderers.entity.RenderPechBlast;

import java.util.HashMap;
import java.util.Map;

public class ClientProxy extends CommonProxy {
    public static KeyBinding activateAmuletKey;

    public static final Map<String, BlockModels> MODEL_REGISTRY = new HashMap<>();

    public void initPackets() {
        super.initPackets();
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getMinecraft().theWorld;
    }

    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    public void init(FMLInitializationEvent event) {
        super.init(event);
        registerFenceRenderer();
        registerBarrierRenderer();
        int slimeRenderId = RenderingRegistry.getNextAvailableRenderId();
        BlockRegistry.SlimeBlock.setSlimeRenderType(slimeRenderId);
        RenderingRegistry.registerBlockHandler(new RenderSlimeBlock(slimeRenderId));
        int stonecutterRenderId = RenderingRegistry.getNextAvailableRenderId();
        StonecutterRegistry.stonecutter.setStonecutterRenderType(stonecutterRenderId);
        RenderingRegistry.registerBlockHandler(new RenderStonecutter(stonecutterRenderId));
        int barrelRenderId = RenderingRegistry.getNextAvailableRenderId();
        BlockRegistry.Barrel.setBarrelRenderType(barrelRenderId);
        RenderingRegistry.registerBlockHandler(new RenderBarrel(barrelRenderId));
        int cauldronRenderId = RenderingRegistry.getNextAvailableRenderId();
        CauldronHooks.setRenderType(cauldronRenderId);
        RenderingRegistry.registerBlockHandler(new RenderConnectedCauldron(cauldronRenderId));
        int ladderRenderId = RenderingRegistry.getNextAvailableRenderId();
        ru.givler.mbo.core.LadderHooks.setRenderType(ladderRenderId);
        RenderLadderBlock ladderRenderer = new RenderLadderBlock(ladderRenderId);
        RenderingRegistry.registerBlockHandler(ladderRenderer);
        MinecraftForge.EVENT_BUS.register(ladderRenderer);
        int railRenderId = RenderingRegistry.getNextAvailableRenderId();
        ru.givler.mbo.core.RailHooks.setRenderType(railRenderId);
        RenderRailBlock railRenderer = new RenderRailBlock(railRenderId);
        RenderingRegistry.registerBlockHandler(railRenderer);
        MinecraftForge.EVENT_BUS.register(railRenderer);
        ru.givler.mbo.client.particle.FallingLeavesHandler fallingLeaves = new ru.givler.mbo.client.particle.FallingLeavesHandler();
        FMLCommonHandler.instance().bus().register(fallingLeaves);
        MinecraftForge.EVENT_BUS.register(fallingLeaves);
        RenderBanner bannerRenderer = new RenderBanner();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBanner.class, bannerRenderer);
        MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(BannerRegistry.banner), new RenderBannerItem());
        RenderingRegistry.registerEntityRenderingHandler(EntityMBOBoat.class, new RenderMBOBoat());
        RenderingRegistry.registerEntityRenderingHandler(EntityMBOChestBoat.class, new RenderMBOBoat());
        RenderingRegistry.registerEntityRenderingHandler(EntityMBOBoatSeat.class, new RenderMBOBoatSeat());
        activateAmuletKey = new KeyBinding("key.mbo.amulet.desc", Keyboard.KEY_R, "MoreBeyondOrdinary");
        ClientRegistry.registerKeyBinding(activateAmuletKey);
        FMLCommonHandler.instance().bus().register(new ClientKeyHandler());
        FMLCommonHandler.instance().bus().register(new BarrierVisibilityHandler());
        ru.givler.mbo.client.render.SmoothOpeningRenderer.configureIntegrations();
        ru.givler.mbo.client.render.SmoothOpeningRenderer smoothOpeningRenderer = new ru.givler.mbo.client.render.SmoothOpeningRenderer();
        FMLCommonHandler.instance().bus().register(smoothOpeningRenderer);
        MinecraftForge.EVENT_BUS.register(smoothOpeningRenderer);
        F3AOcclusionFix.register();

        if (Loader.isModLoaded("NotEnoughItems")) {
            ArcanumNEIConfig.registerHandlers();
        }
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
        bindDefaultRender(ModelRegistry.ModelDeerHead);
        bindDefaultRender(ModelRegistry.ModelDeerLegendHead);

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
        bindDefaultRender(ModelRegistry.ModelWateringCan);
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

        bindDefaultRender(ModelRegistry.ModelBook0);
        bindDefaultRender(ModelRegistry.ModelBook1);
        bindDefaultRender(ModelRegistry.ModelBook2);
        bindDefaultRender(ModelRegistry.ModelBook3);
        bindDefaultRender(ModelRegistry.ModelBook4);
        bindDefaultRender(ModelRegistry.ModelBook5);
        bindDefaultRender(ModelRegistry.ModelBook6);
        bindDefaultRender(ModelRegistry.ModelBook7);
        bindDefaultRender(ModelRegistry.ModelBook8);
        bindDefaultRender(ModelRegistry.ModelBook9);

        bindDefaultRender(ModelRegistry.ModelVishroom);

        bindDefaultRender(ModelRegistry.ModelPlateVoid);
        bindDefaultRender(ModelRegistry.ModelPlate1);
        bindDefaultRender(ModelRegistry.ModelPlate2);
        bindDefaultRender(ModelRegistry.ModelPlate3);
        bindDefaultRender(ModelRegistry.ModelPlate4);
        bindDefaultRender(ModelRegistry.ModelPlate5);
        bindDefaultRender(ModelRegistry.ModelPlate6);
        bindDefaultRender(ModelRegistry.ModelPlate7);
        bindDefaultRender(ModelRegistry.ModelPlate8);
        bindDefaultRender(ModelRegistry.ModelPlate9);
        bindDefaultRender(ModelRegistry.ModelPlate10);

        bindDefaultRender(ModelRegistry.ModelBricks1);
        bindDefaultRender(ModelRegistry.ModelBricks2);
        bindDefaultRender(ModelRegistry.ModelBricks3);
        bindDefaultRender(ModelRegistry.ModelBricks4);
        bindDefaultRender(ModelRegistry.ModelBricks5);
        bindDefaultRender(ModelRegistry.ModelBricks6);
        bindDefaultRender(ModelRegistry.ModelBricks7);

        bindDefaultRender(ModelRegistry.ModelPileBones0);
        bindDefaultRender(ModelRegistry.ModelPileBones1);
        bindDefaultRender(ModelRegistry.ModelPileBones2);
        bindDefaultRender(ModelRegistry.ModelPileBones3);
        bindDefaultRender(ModelRegistry.ModelPileBones4);
        bindDefaultRender(ModelRegistry.ModelPileBones5);
        bindDefaultRender(ModelRegistry.ModelPileBones6);
        bindDefaultRender(ModelRegistry.ModelPileBones7);
        bindDefaultRender(ModelRegistry.ModelPileBones8);
        bindDefaultRender(ModelRegistry.ModelPileBones9);

        bindDefaultRender(ModelRegistry.ModelUrn0);
        bindDefaultRender(ModelRegistry.ModelUrn1);
        bindDefaultRender(ModelRegistry.ModelUrn2);
        bindDefaultRender(ModelRegistry.ModelUrn3);
        bindDefaultRender(ModelRegistry.ModelUrn4);
        bindDefaultRender(ModelRegistry.ModelFuneraryUrn0);
        bindDefaultRender(ModelRegistry.ModelFuneraryUrn1);
        bindDefaultRender(ModelRegistry.ModelFuneraryUrn2);
        bindDefaultRender(ModelRegistry.ModelFuneraryUrn3);
        bindDefaultRender(ModelRegistry.ModelAltar);
        bindDefaultRender(ModelRegistry.ModelStonePedestal);
        bindDefaultRender(ModelRegistry.ModelStoneCoffin);
        bindDefaultRender(ModelRegistry.ModelStatue);

        bindDefaultRender(ModelRegistry.ModelBottle);
        bindDefaultRender(ModelRegistry.ModelCup);
        bindLootContainerRender(ModelRegistry.LootContainer);


        bindDefaultRender(ModelRegistry.ModelWisp);

        AnimationController.addModelFetcher((AnimationController.ModelFetcher<ModelTileBase>) animatable -> {
            if (animatable instanceof ModelTileBase) {
                return new ru.givler.mbo.models.BlockTemplateModel();
            }
            return null;
        });



        registerRenderers();
        MinecraftForge.EVENT_BUS.register(new PotionClientHandler());
        MinecraftForge.EVENT_BUS.register(new TooltipEvents());

    }

    private void registerFenceRenderer() {
        int renderId = RenderingRegistry.getNextAvailableRenderId();
        RenderMetaFence renderer = new RenderMetaFence(renderId);
        BlockRegistry.FenceVanilla.setFenceRenderType(renderId);
        ru.givler.mbo.block.BlockBasicFence bopFence =
                ru.givler.mbo.integration.biomesoplenty.BiomesOPlentyRegistry.FenceBoP;
        if (bopFence != null) {
            bopFence.setFenceRenderType(renderId);
        }
        RenderingRegistry.registerBlockHandler(renderer);
    }

    private void registerBarrierRenderer() {
        int renderId = RenderingRegistry.getNextAvailableRenderId();
        BlockRegistry.Barrier.setBarrierRenderType(renderId);
        RenderingRegistry.registerBlockHandler(new RenderBarrier(renderId));
    }


    public static void bindDefaultRender(BlockModels block) {
        TileEntity tile = block.createNewTileEntity(null, 0);
        ClientRegistry.bindTileEntitySpecialRenderer(tile.getClass(), new TemplateModelRenderer());
        Item blockItem = ItemBlock.getItemFromBlock(block);
        MinecraftForgeClient.registerItemRenderer(blockItem,
                new RenderBlockItem(new TemplateItemModelRenderer(), tile));
        MODEL_REGISTRY.put(block.getModelName(), block);
    }


    public static void bindRender(BlockModels block, TileEntity tile, TileEntitySpecialRenderer tesr) {
        ClientRegistry.bindTileEntitySpecialRenderer(tile.getClass(), tesr);
        Item blockItem = ItemBlock.getItemFromBlock(block);
        MinecraftForgeClient.registerItemRenderer(blockItem, new RenderBlockItem(tesr, tile));
        MODEL_REGISTRY.put(block.getModelName(), block);
    }

    public static void bindLootContainerRender(BlockModels block) {
        RenderLootContainerTile tesr = new RenderLootContainerTile();
        TileEntityLootContainer tile = new TileEntityLootContainer();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLootContainer.class, tesr);
        Item blockItem = ItemBlock.getItemFromBlock(block);
        MinecraftForgeClient.registerItemRenderer(blockItem, new RenderLootContainerItem(tesr, tile));
    }



    @Override
    public void registerRenderers() {
        if (Loader.isModLoaded("Thaumcraft")) {
            RenderingRegistry.registerEntityRenderingHandler(EntityDarkMatter.class, new RenderEldritchOrb());
            RenderingRegistry.registerEntityRenderingHandler(EntityHomingShard.class, new RenderEntityHomingShard());
            RenderingRegistry.registerEntityRenderingHandler(EntityDiffusion.class, new RenderEntityDiffusion());
            RenderingRegistry.registerEntityRenderingHandler(EntityLightMatter.class, new RenderEldritchOrbWhite());
            RenderingRegistry.registerEntityRenderingHandler(EntityPechShatter.class, new RenderPechBlast());
            RenderingRegistry.registerEntityRenderingHandler(EntityPechShard.class, new RenderPechBlast());
            RenderingRegistry.registerEntityRenderingHandler(EntityDarkMoonOrb.class, new RenderEntityDarkMoonOrb());

            MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffFire, new ItemStaffRenderer());
            MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffNature, new ItemStaffRenderer());
            MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffFrost, new ItemStaffRenderer());
            MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffLantern, new ItemStaffRenderer());
            MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffLight, new ItemStaffRenderer());
            MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffChillSorrow, new ItemStaffRenderer());
            MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffNaturalMoon, new ItemStaffRenderer());
            MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffLightningDragon, new ItemStaffRenderer());
            MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffDarkMoon, new ItemStaffRenderer());
            MinecraftForgeClient.registerItemRenderer(ThaumcraftRegistry.StaffDemonic, new ItemStaffRenderer());
        }
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.BrokenLongsword, new RenderLongsword(ItemRegistry.BrokenLongsword.getScale()));
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.BrokenDagger, new RenderDagger(ItemRegistry.BrokenDagger.getScale()));
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.Uchigatana, new RenderUchigatana(ItemRegistry.Uchigatana.getScale()));
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.DragonSlayer, new RenderDragonSlayer(ItemRegistry.DragonSlayer.getScale()));
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.BrokenBowHunting, new RenderCrossbow());
        RenderStoneGolem.register();
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

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
        ModernFontSupport.install();
    }

}
