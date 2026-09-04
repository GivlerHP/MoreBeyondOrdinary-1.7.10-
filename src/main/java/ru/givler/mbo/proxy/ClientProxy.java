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
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.specialblocks.BlockDestructibleLootContainer;
import ru.givler.mbo.handler.ClientKeyHandler;
import ru.givler.mbo.handler.BarrierVisibilityHandler;
import ru.givler.mbo.handler.PotionClientHandler;
import ru.givler.mbo.handler.TooltipEvents;
import ru.givler.mbo.particles.ParticleDarkMagic;
import ru.givler.mbo.particles.ParticleWhiteMagic;
import ru.givler.mbo.registry.ItemRegistry;
import ru.givler.mbo.registry.BlockRegistry;
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
import ru.givler.mbo.spectator.SpectatorClientHandler;
import ru.givler.mbo.client.gamemode.GamemodeSwitcherInputHandler;
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

import java.util.HashMap;
import java.util.Map;

public class ClientProxy extends CommonProxy {

    @Override
    public void initPackets() {
        super.initPackets();
    }
    public static KeyBinding activateAmuletKey;

    public static final Map<String, BlockModels> MODEL_REGISTRY = new HashMap<>();

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
        FMLCommonHandler.instance().bus().register(new GamemodeSwitcherInputHandler());
        FMLCommonHandler.instance().bus().register(new BarrierVisibilityHandler());
        ru.givler.mbo.client.render.SmoothOpeningRenderer.configureIntegrations();
        ru.givler.mbo.client.render.SmoothOpeningRenderer smoothOpeningRenderer = new ru.givler.mbo.client.render.SmoothOpeningRenderer();
        FMLCommonHandler.instance().bus().register(smoothOpeningRenderer);
        MinecraftForge.EVENT_BUS.register(smoothOpeningRenderer);
        F3AOcclusionFix.register();

        if (Loader.isModLoaded("NotEnoughItems")) {
            invokeOptional("ru.givler.mbo.integration.nei.ArcanumNEIConfig", "registerHandlers");
        }
        for (BlockModels model : BlockModels.getAllModels()) {
            if (model instanceof BlockDestructibleLootContainer) {
                bindLootContainerRender(model);
            } else {
                bindDefaultRender(model);
            }
        }

        AnimationController.addModelFetcher((AnimationController.ModelFetcher<ModelTileBase>) animatable -> {
            if (animatable instanceof ModelTileBase) {
                return new ru.givler.mbo.models.BlockTemplateModel();
            }
            return null;
        });



        registerRenderers();
        MinecraftForge.EVENT_BUS.register(new PotionClientHandler());
        MinecraftForge.EVENT_BUS.register(new TooltipEvents());
        MinecraftForge.EVENT_BUS.register(new SpectatorClientHandler());

    }

    private void registerFenceRenderer() {
        int renderId = RenderingRegistry.getNextAvailableRenderId();
        RenderMetaFence renderer = new RenderMetaFence(renderId);
        BlockRegistry.FenceVanilla.setFenceRenderType(renderId);
        if (Loader.isModLoaded("BiomesOPlenty")) {
            try {
                Class.forName("ru.givler.mbo.integration.biomesoplenty.BiomesOPlentyRegistry")
                        .getMethod("setFenceRenderType", int.class).invoke(null, renderId);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to initialise optional Biomes O' Plenty renderer", e);
            }
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
            invokeOptional("ru.givler.mbo.integration.thaumcraft.client.ThaumcraftClientRegistration", "register");
        }
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.BrokenLongsword, new RenderWeapon(1.3F, -0.3F, -0.13F, 0.01F));
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.BrokenDagger, new RenderWeapon(0.9F, 0.1F, 0.0F, 0.01F));
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.Uchigatana, new RenderWeapon(1.6F, -0.43F, -0.15F, 0.01F));
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.DragonSlayer, new RenderWeapon(1.8F, -0.68F, -0.10F, 0.01F));
        MinecraftForgeClient.registerItemRenderer(ItemRegistry.BrokenBowHunting, new RenderCrossbow());
        RenderStoneGolem.register();
    }

    private static void invokeOptional(String className, String method) {
        try {
            Class.forName(className).getMethod(method).invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to initialise optional integration " + className, e);
        }
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
