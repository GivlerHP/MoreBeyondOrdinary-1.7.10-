package ru.givler.mbo.proxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.block.special.BlockDestructibleLootContainer;
import ru.givler.mbo.client.font.ModernFontSupport;
import ru.givler.mbo.client.gamemode.GamemodeSwitcherInputHandler;
import ru.givler.mbo.client.handler.BarrierVisibilityEvents;
import ru.givler.mbo.client.handler.ClientKeyEvents;
import ru.givler.mbo.client.handler.AreaEditorMouseEvents;
import ru.givler.mbo.client.handler.MovingPlatformClientEvents;
import ru.givler.mbo.client.handler.PotionRenderEvents;
import ru.givler.mbo.client.handler.SignGuiEvents;
import ru.givler.mbo.client.handler.TooltipEvents;
import ru.givler.mbo.client.render.*;
import ru.givler.mbo.client.render.banner.RenderBanner;
import ru.givler.mbo.client.render.banner.RenderBannerItem;
import ru.givler.mbo.client.render.boat.RenderMBOBoat;
import ru.givler.mbo.client.render.boat.RenderMBOBoatSeat;
import ru.givler.mbo.client.render.decormodels.RenderLootContainerItem;
import ru.givler.mbo.client.render.decormodels.RenderLootContainerTile;
import ru.givler.mbo.client.render.decormodels.TemplateItemModelRenderer;
import ru.givler.mbo.client.render.decormodels.TemplateModelRenderer;
import ru.givler.mbo.client.render.magic.RenderMagicMissile;
import ru.givler.mbo.client.render.magic.RenderSpectralItem;
import ru.givler.mbo.client.render.magic.RenderBillboardProjectile;
import ru.givler.mbo.client.render.magic.RenderIceShard;
import ru.givler.mbo.client.render.magic.RenderInvisibleProjectile;
import ru.givler.mbo.client.render.magic.RenderArcaneArrow;
import ru.givler.mbo.client.render.magic.RenderMagicBomb;
import ru.givler.mbo.client.render.magic.RenderSeekingLightning;
import ru.givler.mbo.client.render.magic.RenderMagicSigil;
import ru.givler.mbo.client.render.magic.RenderLightningArc;
import ru.givler.mbo.client.render.magic.RenderBlackHole;
import ru.givler.mbo.client.render.magic.RenderGroundMagicEffect;
import ru.givler.mbo.client.render.magic.RenderMagicBubble;
import ru.givler.mbo.client.render.magic.RenderMagicShield;
import ru.givler.mbo.core.CauldronHooks;
import ru.givler.mbo.config.PlayerPingConfig;
import ru.givler.mbo.entity.boat.EntityMBOBoat;
import ru.givler.mbo.entity.boat.EntityMBOBoatSeat;
import ru.givler.mbo.entity.boat.EntityMBOChestBoat;
import ru.givler.mbo.entity.magic.EntityMagicMissile;
import ru.givler.mbo.entity.magic.EntitySpectralArrow;
import ru.givler.mbo.entity.magic.EntityFirebolt;
import ru.givler.mbo.entity.magic.EntityIceShard;
import ru.givler.mbo.entity.magic.EntityThunderbolt;
import ru.givler.mbo.entity.magic.EntityForceOrb;
import ru.givler.mbo.entity.magic.EntityIceCharge;
import ru.givler.mbo.entity.magic.EntityFireOrb;
import ru.givler.mbo.entity.magic.EntityArcaneArrow;
import ru.givler.mbo.entity.magic.EntityMagicBomb;
import ru.givler.mbo.entity.magic.EntitySeekingLightning;
import ru.givler.mbo.entity.magic.EntityMagicSigil;
import ru.givler.mbo.entity.magic.EntityLightningArc;
import ru.givler.mbo.entity.magic.EntityBlackHole;
import ru.givler.mbo.entity.magic.EntityGroundMagicEffect;
import ru.givler.mbo.entity.magic.EntityMagicBubble;
import ru.givler.mbo.entity.magic.EntityMagicShield;
import ru.givler.mbo.entity.magic.EntityMagicStorm;
import ru.givler.mbo.entity.magic.EntityIceSpike;
import ru.givler.mbo.client.render.magic.RenderIceSpike;
import ru.givler.mbo.entity.magic.EntityMagicAura;
import ru.givler.mbo.client.render.magic.RenderMagicAura;
import ru.givler.mbo.entity.magic.EntityMeteor;
import ru.givler.mbo.client.render.magic.RenderMeteor;
import ru.givler.mbo.entity.magic.EntityMagicConstruct;
import ru.givler.mbo.client.render.magic.RenderMagicConstruct;
import ru.givler.mbo.client.sound.MovingSoundEntity;
import ru.givler.mbo.entity.magic.EntityMagicDecoy;
import ru.givler.mbo.entity.magic.EntitySpiritHorse;
import ru.givler.mbo.client.render.magic.RenderSpiritHorse;
import ru.givler.mbo.client.render.magic.RenderMagicDecoy;
import ru.givler.mbo.client.render.magic.RenderMagicLight;
import net.minecraft.client.renderer.entity.RenderArrow;
import ru.givler.mbo.registry.MagicItemRegistry;
import ru.givler.mbo.movingplatform.EntityMovingPlatform;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleDarkMagic;
import ru.givler.mbo.particles.ParticleSparkle;
import ru.givler.mbo.particles.ParticleWhiteMagic;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.particles.ParticleSpell;
import ru.givler.mbo.particles.ParticleBlizzard;
import ru.givler.mbo.particles.ParticleTextured;
import ru.givler.mbo.particles.ParticleTornado;
import ru.givler.mbo.particles.ParticlePath;
import ru.givler.mbo.registry.BannerRegistry;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.registry.ItemRegistry;
import ru.givler.mbo.registry.StonecutterRegistry;
import ru.givler.mbo.spectator.SpectatorClientHandler;
import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.tileentity.TileEntityBanner;
import ru.givler.mbo.tileentity.TileEntityLootContainer;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.renderers.geo.RenderBlockItem;

public class ClientProxy extends CommonProxy {

  @Override
  public void initPackets() {
    PacketManager.registerCommonPackets();
    PacketManager.registerClientPackets();
  }

  public static KeyBinding activateAmuletKey;

  public static final Map<String, BlockModels> MODEL_REGISTRY = new HashMap<>();

  @Override
  public World getClientWorld() {
    return Minecraft.getMinecraft().theWorld;
  }

  public void preInit(FMLPreInitializationEvent event) {
    super.preInit(event);
    PlayerPingConfig.load(event.getModConfigurationDirectory());
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
    ru.givler.mbo.client.particle.FallingLeavesHandler fallingLeaves =
        new ru.givler.mbo.client.particle.FallingLeavesHandler();
    FMLCommonHandler.instance().bus().register(fallingLeaves);
    MinecraftForge.EVENT_BUS.register(fallingLeaves);
    RenderBanner bannerRenderer = new RenderBanner();
    ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBanner.class, bannerRenderer);
    MinecraftForgeClient.registerItemRenderer(
        Item.getItemFromBlock(BannerRegistry.banner), new RenderBannerItem());
    RenderSpectralItem spectralItemRenderer = new RenderSpectralItem();
    MinecraftForgeClient.registerItemRenderer(MagicItemRegistry.spectralSword, spectralItemRenderer);
    MinecraftForgeClient.registerItemRenderer(MagicItemRegistry.spectralPickaxe, spectralItemRenderer);
    MinecraftForgeClient.registerItemRenderer(MagicItemRegistry.spectralBow, spectralItemRenderer);
    MinecraftForgeClient.registerItemRenderer(MagicItemRegistry.flamingAxe, spectralItemRenderer);
    MinecraftForgeClient.registerItemRenderer(MagicItemRegistry.frostAxe, spectralItemRenderer);
    RenderingRegistry.registerEntityRenderingHandler(EntityMBOBoat.class, new RenderMBOBoat());
    RenderingRegistry.registerEntityRenderingHandler(EntityMBOChestBoat.class, new RenderMBOBoat());
    RenderingRegistry.registerEntityRenderingHandler(
        EntityMBOBoatSeat.class, new RenderMBOBoatSeat());
    RenderingRegistry.registerEntityRenderingHandler(
        EntityMovingPlatform.class, new RenderMovingPlatform());
    RenderingRegistry.registerEntityRenderingHandler(
        EntityMagicMissile.class, new RenderMagicMissile());
    RenderingRegistry.registerEntityRenderingHandler(EntitySpectralArrow.class, new RenderArrow());
    RenderingRegistry.registerEntityRenderingHandler(EntityFirebolt.class,
        new RenderBillboardProjectile(
            new net.minecraft.util.ResourceLocation("mbo", "textures/entity/magic/firebolt.png"), 0.2F));
    RenderingRegistry.registerEntityRenderingHandler(EntityIceShard.class, new RenderIceShard());
    RenderingRegistry.registerEntityRenderingHandler(EntityThunderbolt.class, new RenderInvisibleProjectile());
    RenderingRegistry.registerEntityRenderingHandler(EntityForceOrb.class,
        new RenderBillboardProjectile(
            new net.minecraft.util.ResourceLocation("mbo", "textures/entity/magic/force_orb.png"), 0.7F));
    RenderingRegistry.registerEntityRenderingHandler(EntityIceCharge.class,
        new RenderBillboardProjectile(
            new net.minecraft.util.ResourceLocation("mbo", "textures/entity/magic/ice_charge.png"), 0.6F));
    RenderingRegistry.registerEntityRenderingHandler(EntityFireOrb.class,
        new RenderBillboardProjectile(
            new net.minecraft.util.ResourceLocation("mbo", "textures/entity/magic/firebolt.png"), 0.55F));
    RenderingRegistry.registerEntityRenderingHandler(EntityArcaneArrow.class, new RenderArcaneArrow());
    RenderingRegistry.registerEntityRenderingHandler(EntityMagicBomb.class, new RenderMagicBomb());
    RenderingRegistry.registerEntityRenderingHandler(EntitySeekingLightning.class, new RenderSeekingLightning());
    RenderingRegistry.registerEntityRenderingHandler(EntityMagicSigil.class, new RenderMagicSigil());
    RenderingRegistry.registerEntityRenderingHandler(EntityLightningArc.class, new RenderLightningArc());
    RenderingRegistry.registerEntityRenderingHandler(EntityBlackHole.class, new RenderBlackHole());
    RenderingRegistry.registerEntityRenderingHandler(
        EntityGroundMagicEffect.class, new RenderGroundMagicEffect());
    RenderingRegistry.registerEntityRenderingHandler(EntityMagicBubble.class, new RenderMagicBubble());
    RenderingRegistry.registerEntityRenderingHandler(EntityMagicShield.class, new RenderMagicShield());
    RenderingRegistry.registerEntityRenderingHandler(EntityMagicStorm.class, new RenderInvisibleProjectile());
    RenderingRegistry.registerEntityRenderingHandler(EntityIceSpike.class, new RenderIceSpike());
    RenderingRegistry.registerEntityRenderingHandler(EntityMagicAura.class, new RenderMagicAura());
    RenderingRegistry.registerEntityRenderingHandler(EntityMeteor.class, new RenderMeteor());
    RenderingRegistry.registerEntityRenderingHandler(EntityMagicConstruct.class, new RenderMagicConstruct());
    RenderingRegistry.registerEntityRenderingHandler(EntityMagicDecoy.class,
        new RenderMagicDecoy());
    RenderingRegistry.registerEntityRenderingHandler(EntitySpiritHorse.class,
        new RenderSpiritHorse());
    ClientRegistry.bindTileEntitySpecialRenderer(
        TileEntitySign.class, new RenderSign());
    ClientRegistry.bindTileEntitySpecialRenderer(
        ru.givler.mbo.tileentity.TileEntityPetrifiedStatue.class,
        new ru.givler.mbo.client.render.RenderPetrifiedStatue());
    ClientRegistry.bindTileEntitySpecialRenderer(
        ru.givler.mbo.tileentity.TileEntityTemporaryMagicBlock.class,
        new RenderMagicLight());
    MinecraftForge.EVENT_BUS.register(new SignGuiEvents());
    MinecraftForge.EVENT_BUS.register(new DungeonAreaWorldRenderer());
    MinecraftForge.EVENT_BUS.register(new WaterloggedBlockRenderer());
    MinecraftForge.EVENT_BUS.register(new ru.givler.mbo.client.render.PlatformTechnicalRenderer());
    MinecraftForge.EVENT_BUS.register(new AreaSelectionRenderer());
    MinecraftForge.EVENT_BUS.register(new AreaEditorMouseEvents());
    FMLCommonHandler.instance().bus().register(new MovingPlatformClientEvents());
    activateAmuletKey = new KeyBinding("key.mbo.amulet.desc", Keyboard.KEY_R, "MoreBeyondOrdinary");
    ClientRegistry.registerKeyBinding(activateAmuletKey);
    FMLCommonHandler.instance().bus().register(new ClientKeyEvents());
    FMLCommonHandler.instance().bus().register(new GamemodeSwitcherInputHandler());
    FMLCommonHandler.instance().bus().register(new BarrierVisibilityEvents());
    ru.givler.mbo.client.render.SmoothOpeningRenderer.configureIntegrations();
    ru.givler.mbo.client.render.SmoothOpeningRenderer smoothOpeningRenderer =
        new ru.givler.mbo.client.render.SmoothOpeningRenderer();
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

    AnimationController.addModelFetcher(
        (AnimationController.ModelFetcher<ModelTileBase>)
            animatable -> {
              if (animatable instanceof ModelTileBase) {
                return new ru.givler.mbo.client.model.decormodels.DecorBlockModel();
              }
              return null;
            });

    registerRenderers();
    MinecraftForge.EVENT_BUS.register(new PotionRenderEvents());
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
            .getMethod("setFenceRenderType", int.class)
            .invoke(null, renderId);
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
    MinecraftForgeClient.registerItemRenderer(
        blockItem, new RenderBlockItem(new TemplateItemModelRenderer(), tile));
    MODEL_REGISTRY.put(block.getModelName(), block);
  }

  public static void bindRender(
      BlockModels block, TileEntity tile, TileEntitySpecialRenderer tesr) {
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
      invokeOptional(
          "ru.givler.mbo.integration.thaumcraft.client.ThaumcraftClientRegistration", "register");
    }
    MinecraftForgeClient.registerItemRenderer(
        ItemRegistry.BrokenLongsword, new RenderWeapon(1.3F, -0.3F, -0.13F, 0.01F));
    MinecraftForgeClient.registerItemRenderer(
        ItemRegistry.BrokenDagger, new RenderWeapon(0.9F, 0.1F, 0.0F, 0.01F));
    MinecraftForgeClient.registerItemRenderer(
        ItemRegistry.Uchigatana, new RenderWeapon(1.6F, -0.43F, -0.15F, 0.01F));
    MinecraftForgeClient.registerItemRenderer(
        ItemRegistry.DragonSlayer, new RenderWeapon(1.8F, -0.68F, -0.10F, 0.01F));
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
  public void spawnParticle(
      EnumParticleType type,
      World world,
      double x,
      double y,
      double z,
      double motionX,
      double motionY,
      double motionZ) {
    spawnParticle(type, world, x, y, z, motionX, motionY, motionZ,
        ParticleSettings.defaults(type));
  }

  @Override
  public void spawnParticle(
      EnumParticleType type, World world, double x, double y, double z,
      double motionX, double motionY, double motionZ, ParticleSettings settings) {
    String vanilla = type.getVanillaName();
    if (vanilla != null) {
      world.spawnParticle(vanilla, x, y, z, motionX, motionY, motionZ);
      return;
    }
    EntityFX particle = null;

    switch (type) {
      case SACRED:
        particle = new ParticleWhiteMagic(world, x, y, z, motionX, motionY, motionZ,
            settings.red, settings.green, settings.blue);
        break;
      case DARK_MAGIC:
        particle = new ParticleDarkMagic(world, x, y, z, motionX, motionY, motionZ,
            settings.red, settings.green, settings.blue);
        break;
      case ICE:
        particle = atlas(world, x, y, z, motionX, motionY, motionZ, settings, "ice_particles.png", 4, 4, false);
        break;
      case SNOW:
        particle = atlas(world, x, y, z, motionX, motionY, motionZ, settings, "snow_particles.png", 4, 4, false);
        break;
      case BLIZZARD:
        particle = new ParticleBlizzard(world, x, y, z, settings);
        break;
      case SPARK:
        particle = atlas(world, x, y, z, motionX, motionY, motionZ, settings, "lightning_particles.png", 4, 8, true);
        break;
      case SPARKLE:
        particle = new ParticleSparkle(
            world, x, y, z, motionX, motionY, motionZ, settings);
        break;
      case DUST:
        particle = new ParticleSpell(world, x, y, z, motionX, motionY, motionZ, settings, 7);
        break;
      case MAGIC_FIRE:
        particle = new ParticleSpell(world, x, y, z, motionX, motionY, motionZ, settings, 48);
        break;
      case LEAF:
        particle = atlas(world, x, y, z, motionX, motionY, motionZ, settings, "leaf_particles.png", 4, 4, false);
        break;
      case PATH:
        particle = new ParticlePath(world, x, y, z, motionX, motionY, motionZ, settings);
        break;
      default: break;
    }

    if (particle != null) {
      if (particle instanceof ParticleWhiteMagic) {
        ((ParticleWhiteMagic) particle).setBaseSpellTextureIndex(145);
      } else if (particle instanceof ParticleDarkMagic) {
        ((ParticleDarkMagic) particle).setBaseSpellTextureIndex(162);
      }
      Minecraft.getMinecraft().effectRenderer.addEffect(particle);
    }
  }

  private static EntityFX atlas(World world, double x, double y, double z,
      double motionX, double motionY, double motionZ, ParticleSettings settings,
      String texture, int columns, int rows, boolean animated) {
    return new ParticleTextured(world, x, y, z, motionX, motionY, motionZ,
        settings, texture, columns, rows, animated);
  }

  @Override
  public void spawnSparkle(
      World world,
      double x,
      double y,
      double z,
      double motionX,
      double motionY,
      double motionZ,
      int maxAge,
      float red,
      float green,
      float blue) {
    Minecraft.getMinecraft().effectRenderer.addEffect(
        new ParticleSparkle(
            world, x, y, z, motionX, motionY, motionZ, maxAge, red, green, blue));
  }

  @Override
  public void playMovingSound(
      Entity entity, String soundName, float volume, float pitch, boolean repeat) {
    Minecraft.getMinecraft().getSoundHandler().playSound(
        new MovingSoundEntity(entity, soundName, volume, pitch, repeat));
  }

  @Override
  public void spawnTornadoParticle(World world, double centreX, double y, double centreZ,
      double velocityX, double velocityZ, double radius, net.minecraft.block.Block block,
      int metadata) {
    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleTornado(world, 48,
        centreX, centreZ, radius, y, velocityX, velocityZ, block, metadata));
  }

  @Override
  public void postInit(FMLPostInitializationEvent event) {
    super.postInit(event);
    ModernFontSupport.install();
  }
}
