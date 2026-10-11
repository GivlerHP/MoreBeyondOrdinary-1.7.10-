package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.block.model.ModelCollisionPart;
import ru.givler.mbo.block.model.ModelCollisionPart.Axis;
import ru.givler.mbo.item.ItemBlockModels;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.tileentity.TileEntityModelCollision;

public class BlockModels extends BlockDirectional implements ITileEntityProvider {
  private static final List<BlockModels> ALL_MODELS = new ArrayList<BlockModels>();

  private String name;
  private String textureName;
  private String modelName;
  private float modelOffsetY;

  /** Vertical render offset from the placement block's lower face. */
  public BlockModels withModelOffsetY(float offset) {
    this.modelOffsetY = offset;
    return this;
  }

  public float getModelOffsetY() {
    return modelOffsetY;
  }

  private boolean disableCollision = false;
  private int blockHeight = 1;
  private float[][] rotationBounds = null;
  private float[] simpleBounds = null;
  private String animationName = null;
  private boolean loopAnimation = true;
  private int textureFrameCount = 1;
  private int textureFrameSpeed = 100;
  private boolean dynamicModelRender;
  private boolean modelLogicTicks;
  // Assigned only by the client after atlas/geometry validation. Never persisted in worlds.
  public static int decorChunkRenderId = -1;
  public static boolean decorChunkRendering = true;
  public static boolean decorInstancedRendering;
  private boolean decorChunkReady;
  private boolean cameraSensitiveDecor() {
    return modelName!=null && (modelName.endsWith("/hook") || modelName.endsWith("/hanged_corpse")
        || modelName.endsWith("/suspended_corpse") || modelName.endsWith("/rope_around_the_neck"));
  }
  @Override public void onBlockAdded(World world,int x,int y,int z) {
    super.onBlockAdded(world,x,y,z);
    if(cameraSensitiveDecor())world.markBlockRangeForRenderUpdate(x-3,y-3,z-3,x+3,y+3,z+3);
  }
  public void setDecorChunkReady(boolean ready) { decorChunkReady = ready; }
  public boolean isDecorChunkReady() { return decorChunkReady && !usesDynamicModelRender(); }
  public boolean usesDecorChunkRendering() {
    return decorChunkRendering && !decorInstancedRendering && isDecorChunkReady();
  }

  /** Visual animation and gameplay ticks are independent capabilities. */
  public boolean usesDynamicModelRender() {
    return dynamicModelRender || (animationName != null && !animationName.isEmpty()) || textureFrameCount > 1;
  }

  public boolean needsModelLogicTicks() {
    return modelLogicTicks || this instanceof ru.givler.mbo.block.model.BlockModelFood;
  }

  /** Optional capability for future renderers that change geometry continuously. */
  public BlockModels withDynamicModelRender() { dynamicModelRender = true; return this; }
  public BlockModels withModelLogicTicks() { modelLogicTicks = true; return this; }
  public String getModelAnimationName() { return animationName; }
  public boolean isModelAnimationLooped() { return loopAnimation; }
  public int getModelFrameCount() { return textureFrameCount; }
  public int getModelFrameSpeed() { return textureFrameSpeed; }
  private Item dropItem = null;
  private int dropMeta = 0;
  private Item pickupItem = null;
  private int pickupMeta = 0;
  private Block particleBlock = Blocks.planks;
  private int particleMeta = 0;
  private boolean customParticleBlock = false;
  private String particleTextureName;

  @SideOnly(Side.CLIENT)
  private IIcon modelParticleIcon;

  private boolean requiresSupport = false;
  private Item unsupportedDropItem = null;
  private final List<ModelCollisionPart> collisionParts = new ArrayList<ModelCollisionPart>();
  private float[][] collisionBoxes;

  public BlockModels withMultipartCollisionBoxes(float[][] boxes) {
    java.util.Map<String,float[][]> cells = ru.givler.mbo.block.model.CollisionShapeParts.cells(boxes);
    collisionBoxes = cells.remove("0,0,0");
    if (collisionBoxes == null) throw new IllegalArgumentException("shape misses placement cell: " + modelName);
    collisionParts.clear();
    blockHeight = 1;
    float[] b = ru.givler.mbo.block.model.CollisionShapeParts.envelope(collisionBoxes);
    withRotatingBounds(b[0],b[1],b[2],b[3],b[4],b[5]);
    for (java.util.Map.Entry<String,float[][]> cell : cells.entrySet()) {
      String[] xyz=cell.getKey().split(",");
      collisionParts.add(new ModelCollisionPart(Integer.parseInt(xyz[0]),Integer.parseInt(xyz[1]),
          Integer.parseInt(xyz[2]),cell.getValue()));
    }
    return this;
  }

  @Override
  public void addCollisionBoxesToList(World world,int x,int y,int z,AxisAlignedBB query,
      java.util.List list,net.minecraft.entity.Entity entity) {
    if (collisionBoxes == null) { super.addCollisionBoxesToList(world,x,y,z,query,list,entity); return; }
    if (disableCollision) return;
    for(float[] box:collisionBoxes) {
      float[] b=rotateBounds(box,world.getBlockMetadata(x,y,z)&3);
      AxisAlignedBB a=AxisAlignedBB.getBoundingBox(x+b[0],y+b[1],z+b[2],x+b[3],y+b[4],z+b[5]);
      if(a.intersectsWith(query)) list.add(a);
    }
  }

  private static final int TOP_META_OFFSET = 4;

  public BlockModels(Material material, String name, String texture, String model) {
    super(material);
    ALL_MODELS.add(this);
    setBlockName(name);
    setCreativeTab(CreativeTabRegistry.tabMBOdecors);
    this.setHardness(1.0F);
    this.setHarvestLevel("axe", 1);
    applyMaterialDefaults(material);
    this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
    this.textureName = texture;
    this.particleTextureName = texture;
    this.modelName = model;
    this.name = name;
  }

  public static List<BlockModels> getAllModels() {
    return Collections.unmodifiableList(ALL_MODELS);
  }

  /** Sets the default sound and breaking-particle texture for the material. */
  private void applyMaterialDefaults(Material material) {
    // Unknown/custom materials intentionally retain the old planks fallback.
    particleBlock = Blocks.planks;
    particleMeta = 0;
    setStepSound(soundTypeWood);

    if (material == Material.rock) {
      setStepSound(soundTypeStone);
      particleBlock = Blocks.stone;
    } else if (material == Material.iron || material == Material.anvil) {
      setStepSound(soundTypeMetal);
      particleBlock = Blocks.iron_block;
    } else if (material == Material.glass
        || material == Material.ice
        || material == Material.packedIce) {
      setStepSound(soundTypeGlass);
      particleBlock = Blocks.glass;
    } else if (material == Material.ground) {
      setStepSound(soundTypeGravel);
      particleBlock = Blocks.dirt;
    } else if (material == Material.grass
        || material == Material.leaves
        || material == Material.plants
        || material == Material.vine) {
      setStepSound(soundTypeGrass);
      particleBlock = material == Material.leaves ? Blocks.leaves : Blocks.grass;
    } else if (material == Material.sand) {
      setStepSound(soundTypeSand);
      particleBlock = Blocks.sand;
    } else if (material == Material.cloth) {
      setStepSound(soundTypeCloth);
      particleBlock = Blocks.wool;
    } else if (material == Material.snow || material == Material.craftedSnow) {
      setStepSound(soundTypeSnow);
      particleBlock = Blocks.snow;
    } else if (material == Material.clay) {
      setStepSound(soundTypeGravel);
      particleBlock = Blocks.clay;
    }
  }

  public BlockModels setModelHeight(int height) {
    this.blockHeight = height;
    return this;
  }

  public BlockModels setCollisionEnabled(boolean enabled) {
    this.disableCollision = !enabled;
    return this;
  }

  public BlockModels setRotationBounds(float[][] bounds) {
    if (bounds == null || bounds.length < 4)
      throw new IllegalArgumentException("Need bounds for all 4 directions");
    this.simpleBounds = null;
    this.rotationBounds = bounds;
    return this;
  }

  /**
   * Sets one bounding box in the model's local NORTH-facing coordinates. The other three horizontal
   * orientations are calculated automatically.
   */
  public BlockModels withRotatingBounds(
      float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    this.simpleBounds = new float[] {minX, minY, minZ, maxX, maxY, maxZ};
    this.rotationBounds = null;
    super.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
    return this;
  }

  public boolean isModelCollisionEnabled() {
    return !disableCollision;
  }

  @Override
  public net.minecraft.util.MovingObjectPosition collisionRayTrace(World world, int x, int y, int z,
      net.minecraft.util.Vec3 start, net.minecraft.util.Vec3 end) {
    if (ru.givler.mbo.core.HangingCameraContext.ignore(world, x, y, z)) return null;
    if (collisionBoxes != null) {
      MovingObjectPosition closest=null;
      for(float[] box:collisionBoxes) {
        float[] b=rotateBounds(box,world.getBlockMetadata(x,y,z)&3);
        setBlockBounds(b[0],b[1],b[2],b[3],b[4],b[5]);
        MovingObjectPosition raw=AxisAlignedBB.getBoundingBox(x+b[0],y+b[1],z+b[2],x+b[3],y+b[4],z+b[5]).calculateIntercept(start,end);
        MovingObjectPosition hit=raw==null?null:new MovingObjectPosition(x,y,z,raw.sideHit,raw.hitVec);
        if(hit!=null && (closest==null || start.squareDistanceTo(hit.hitVec)<start.squareDistanceTo(closest.hitVec))) closest=hit;
      }
      setBlockBoundsBasedOnState(world,x,y,z);
      return closest;
    }
    return super.collisionRayTrace(world, x, y, z, start, end);
  }

  /** Divides a manually chosen model envelope into actual occupied blocks. */
  public BlockModels withMultipartBounds(
      float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    if (!collisionParts.isEmpty() || blockHeight != 1)
      throw new IllegalStateException("multipart bounds require an unconfigured structure");
    if (!(minX < maxX && minY < maxY && minZ < maxZ)
        || minX >= 1 || maxX <= 0 || minY >= 1 || maxY <= 0 || minZ >= 1 || maxZ <= 0)
      throw new IllegalArgumentException("bounds must intersect the owner block");
    withRotatingBounds(Math.max(0, minX), Math.max(0, minY), Math.max(0, minZ),
        Math.min(1, maxX), Math.min(1, maxY), Math.min(1, maxZ));
    collisionParts.addAll(ModelCollisionPart.partition(
        new float[] {minX, minY, minZ, maxX, maxY, maxZ}));
    return this;
  }

  public BlockModels addVerticalCollision(int offset) {
    return addVerticalCollision(offset, 0, 0, 0, 1, 1, 1);
  }

  public BlockModels addVerticalCollision(
      int offset, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    return addCollisionPart(Axis.VERTICAL, offset, minX, minY, minZ, maxX, maxY, maxZ);
  }

  public BlockModels addVerticalCollisionUsingBaseBounds(int offset) {
    if (simpleBounds == null) {
      throw new IllegalStateException("base bounds must be set before reusing them");
    }
    return addVerticalCollision(
        offset,
        simpleBounds[0],
        simpleBounds[1],
        simpleBounds[2],
        simpleBounds[3],
        simpleBounds[4],
        simpleBounds[5]);
  }

  public BlockModels addSideCollision(int offset) {
    return addSideCollision(offset, 0, 0, 0, 1, 1, 1);
  }

  public BlockModels addSideCollision(
      int offset, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    return addCollisionPart(Axis.SIDE, offset, minX, minY, minZ, maxX, maxY, maxZ);
  }

  public BlockModels addForwardCollision(int offset) {
    return addForwardCollision(offset, 0, 0, 0, 1, 1, 1);
  }

  public BlockModels addForwardCollision(
      int offset, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    return addCollisionPart(Axis.FORWARD, offset, minX, minY, minZ, maxX, maxY, maxZ);
  }

  private BlockModels addCollisionPart(
      Axis axis,
      int offset,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ) {
    if (minX < 0
        || minY < 0
        || minZ < 0
        || maxX > 1
        || maxY > 1
        || maxZ > 1
        || minX >= maxX
        || minY >= maxY
        || minZ >= maxZ) {
      throw new IllegalArgumentException("collision bounds must be inside 0..1 and min < max");
    }
    ModelCollisionPart part = new ModelCollisionPart(axis, offset,
        new float[] {minX, minY, minZ, maxX, maxY, maxZ});
    for (ModelCollisionPart existing : collisionParts) {
      if (existing.localX == part.localX && existing.localY == part.localY
          && existing.localZ == part.localZ) {
        throw new IllegalArgumentException(
            "collision part already exists at " + axis + " " + offset);
      }
    }
    collisionParts.add(part);
    return this;
  }

  public BlockModels withDrop(Item item) {
    return withDrop(item, 0);
  }

  public BlockModels withDrop(Item item, int meta) {
    this.dropItem = item;
    this.dropMeta = meta;
    return this;
  }

  public BlockModels withPickup(Item item) {
    return withPickup(item, 0);
  }

  public BlockModels withPickup(Item item, int meta) {
    this.pickupItem = item;
    this.pickupMeta = meta;
    return this;
  }

  public BlockModels withParticle(Block block) {
    return withParticle(block, 0);
  }

  public BlockModels withParticle(Block block, int meta) {
    this.particleBlock = block;
    this.particleMeta = meta;
    this.customParticleBlock = true;
    return this;
  }

  /** Uses another model texture as the source for vanilla breaking particles. */
  public BlockModels withParticleTexture(String texture) {
    if (texture == null || texture.isEmpty())
      throw new IllegalArgumentException("texture must not be empty");
    this.particleTextureName = texture;
    this.customParticleBlock = false;
    return this;
  }

  public BlockModels withRequiredSupport() {
    this.requiresSupport = true;
    return this;
  }

  public BlockModels withRequiredSupport(Item collapseDrop) {
    this.requiresSupport = true;
    this.unsupportedDropItem = collapseDrop;
    return this;
  }

  public BlockModels withAnimation(String animation, boolean loop) {
    this.animationName = animation;
    this.loopAnimation = loop;
    if(animation!=null && !animation.isEmpty()) dynamicModelRender=true;
    return this;
  }

  /**
   * Enables manual texture animation for Gecko models. GeckoLib 1.7.10 does not apply vanilla PNG
   * .mcmeta animation to model textures, so frames are loaded from texture_0.png, texture_1.png,
   * ... texture_N.png.
   */
  public BlockModels withAnimatedTexture(int frameCount, int frameSpeedMs) {
    if (frameCount < 1) throw new IllegalArgumentException("frameCount must be >= 1");
    if (frameSpeedMs < 1) throw new IllegalArgumentException("frameSpeedMs must be >= 1");
    this.textureFrameCount = frameCount;
    this.textureFrameSpeed = frameSpeedMs;
    if(frameCount>1) dynamicModelRender=true;
    return this;
  }

  public BlockModels withBounds(
      float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    return withRotatingBounds(minX, minY, minZ, maxX, maxY, maxZ);
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public int getRenderType() {
    return usesDecorChunkRendering() ? decorChunkRenderId : -1;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IIcon getIcon(int side, int metadata) {
    if (!customParticleBlock && modelParticleIcon != null) return modelParticleIcon;
    return particleBlock.getIcon(side, particleMeta);
  }

  /**
   * Gecko textures are normally bound directly by the TESR. Registering the same PNG in the block
   * atlas gives vanilla EntityDiggingFX a real IIcon.
   */
  @Override
  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(IIconRegister iconRegister) {
    modelParticleIcon = ru.givler.mbo.client.render.decormodels.DecorAtlasSprite.registerParticles(iconRegister,
        ru.givler.mbo.client.model.decormodels.DecorResources.texture(particleTextureName, ""));
  }

  @Override
  public boolean onBlockActivated(
      World world,
      int x,
      int y,
      int z,
      EntityPlayer player,
      int side,
      float hitX,
      float hitY,
      float hitZ) {
    if (ru.givler.mbo.handler.HangingEvents.activate(world, x, y, z, player)) return true;
    if (pickupItem == null) return false;
    if (world.isRemote) return true;
    world.setBlockToAir(x, y, z);
    ItemStack pickup = new ItemStack(pickupItem, 1, pickupMeta);
    if (!player.inventory.addItemStackToInventory(pickup)) {
      world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, pickup));
    }
    player.inventoryContainer.detectAndSendChanges();
    world.playSoundAtEntity(
        player,
        "random.pop",
        0.2F,
        ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
    return true;
  }

  @Override
  public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
    super.onNeighborBlockChange(world, x, y, z, neighbor);
    if (!requiresSupport || world.isRemote || isTopPart(world.getBlockMetadata(x, y, z))) return;
    Block below = world.getBlock(x, y - 1, z);
    if (below.isSideSolid(world, x, y - 1, z, ForgeDirection.UP)) return;
    Item item =
        unsupportedDropItem != null
            ? unsupportedDropItem
            : (dropItem != null ? dropItem : Item.getItemFromBlock(this));
    int meta = unsupportedDropItem != null ? 0 : (dropItem != null ? dropMeta : 0);
    if (item != null)
      world.spawnEntityInWorld(
          new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, new ItemStack(item, 1, meta)));
    world.setBlockToAir(x, y, z);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public boolean addDestroyEffects(
      World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
    for (int i = 0; i < 4; ++i) {
      for (int j = 0; j < 4; ++j) {
        for (int k = 0; k < 4; ++k) {
          double px = x + (i + 0.5D) / 4.0D;
          double py = y + (j + 0.5D) / 4.0D;
          double pz = z + (k + 0.5D) / 4.0D;
          effectRenderer.addEffect(
              (new EntityDiggingFX(
                      world, px, py, pz, px - x - 0.5D, py - y - 0.5D, pz - z - 0.5D, this, meta))
                  .applyColourMultiplier(x, y, z));
        }
      }
    }
    return true;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public boolean addHitEffects(
      World world, MovingObjectPosition target, EffectRenderer effectRenderer) {
    int x = target.blockX;
    int y = target.blockY;
    int z = target.blockZ;
    int side = target.sideHit;
    int meta = world.getBlockMetadata(x, y, z);
    setBlockBoundsBasedOnState(world,x,y,z);
    float inset = 0.1F;

    double px =
        x
            + world.rand.nextDouble()
                * (this.getBlockBoundsMaxX() - this.getBlockBoundsMinX() - 0.2F)
            + 0.1F
            + this.getBlockBoundsMinX();
    double py =
        y
            + world.rand.nextDouble()
                * (this.getBlockBoundsMaxY() - this.getBlockBoundsMinY() - 0.2F)
            + 0.1F
            + this.getBlockBoundsMinY();
    double pz =
        z
            + world.rand.nextDouble()
                * (this.getBlockBoundsMaxZ() - this.getBlockBoundsMinZ() - 0.2F)
            + 0.1F
            + this.getBlockBoundsMinZ();

    if (side == 0) py = y + this.getBlockBoundsMinY() - inset;
    if (side == 1) py = y + this.getBlockBoundsMaxY() + inset;
    if (side == 2) pz = z + this.getBlockBoundsMinZ() - inset;
    if (side == 3) pz = z + this.getBlockBoundsMaxZ() + inset;
    if (side == 4) px = x + this.getBlockBoundsMinX() - inset;
    if (side == 5) px = x + this.getBlockBoundsMaxX() + inset;

    effectRenderer.addEffect(
        (new EntityDiggingFX(world, px, py, pz, 0.0D, 0.0D, 0.0D, this, meta))
            .applyColourMultiplier(x, y, z)
            .multiplyVelocity(0.2F)
            .multipleParticleScaleBy(0.6F));
    return true;
  }

  @Override
  public boolean hasTileEntity(int metadata) {
    return !isTopPart(metadata);
  }

  @Override
  public TileEntity createNewTileEntity(World world, int metadata) {
    if (isTopPart(metadata)) return null;
    ModelTileBase tile = new ModelTileBase(textureName, modelName, animationName, loopAnimation);
    tile.frameCount = textureFrameCount;
    tile.frameSpeed = textureFrameSpeed;
    return tile;
  }

  @Override
  public boolean canPlaceBlockAt(World world, int x, int y, int z) {
    if (!super.canPlaceBlockAt(world, x, y, z)) return false;
    for (int i = 1; i < blockHeight; i++) {
      if (!world.getBlock(x, y + i, z).isAir(world, x, y + i, z)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void onBlockPlacedBy(
      World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
    int facing = MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 2.5) & 3;
    world.setBlockMetadataWithNotify(x, y, z, facing, 2);
    for (int i = 1; i < blockHeight; i++) {
      world.setBlock(x, y + i, z, this, facing + TOP_META_OFFSET, 2);
    }
    placeCollisionParts(world, x, y, z, facing);
  }

  public boolean canPlaceStructureAt(World world, int x, int y, int z, EntityLivingBase placer) {
    int facing = MathHelper.floor_double((double) (placer.rotationYaw * 4.0F / 360.0F) + 2.5) & 3;
    for (int i = 1; i < blockHeight; i++) {
      if (y + i < 0 || y + i >= world.getHeight()) return false;
      Block block = world.getBlock(x, y + i, z);
      if (!block.isAir(world, x, y + i, z) && !block.isReplaceable(world, x, y + i, z))
        return false;
    }
    for (ModelCollisionPart part : collisionParts) {
      int[] pos = collisionPosition(x, y, z, facing, part);
      if (pos[1] < 0 || pos[1] >= world.getHeight()) return false;
      Block block = world.getBlock(pos[0], pos[1], pos[2]);
      if (!block.isAir(world, pos[0], pos[1], pos[2])
          && !block.isReplaceable(world, pos[0], pos[1], pos[2])) return false;
    }
    return true;
  }

  private void placeCollisionParts(World world, int x, int y, int z, int facing) {
    for (ModelCollisionPart part : collisionParts) {
      int[] pos = collisionPosition(x, y, z, facing, part);
      world.setBlock(pos[0], pos[1], pos[2], BlockRegistry.ModelCollisionPart, 0, 3);
      TileEntity tile = world.getTileEntity(pos[0], pos[1], pos[2]);
      if (tile instanceof TileEntityModelCollision) {
        float[][] boxes=new float[part.boxes.length][];
        for(int i=0;i<boxes.length;i++) boxes[i]=rotateBounds(part.boxes[i],facing);
        ((TileEntityModelCollision) tile).configure(x, y, z, boxes);
        world.markBlockForUpdate(pos[0], pos[1], pos[2]);
      }
    }
  }

  private int[] collisionPosition(int x, int y, int z, int facing, ModelCollisionPart part) {
    int localX = part.localX;
    int localY = part.localY;
    int localZ = part.localZ;
    int worldX;
    int worldZ;
    switch (facing & 3) {
      case 1:
        worldX = -localZ;
        worldZ = localX;
        break;
      case 2:
        worldX = -localX;
        worldZ = -localZ;
        break;
      case 3:
        worldX = localZ;
        worldZ = -localX;
        break;
      case 0:
      default:
        worldX = localX;
        worldZ = localZ;
        break;
    }
    return new int[] {x + worldX, y + localY, z + worldZ};
  }

  private float[] rotateBounds(float[] b, int facing) {
    float minX = 1, minZ = 1, maxX = 0, maxZ = 0;
    float[] xs = {b[0], b[3]};
    float[] zs = {b[2], b[5]};
    for (float px : xs)
      for (float pz : zs) {
        float x = px - 0.5F;
        float z = pz - 0.5F;
        float rx, rz;
        switch (facing & 3) {
          case 1:
            rx = -z;
            rz = x;
            break;
          case 2:
            rx = -x;
            rz = -z;
            break;
          case 3:
            rx = z;
            rz = -x;
            break;
          case 0:
          default:
            rx = x;
            rz = z;
            break;
        }
        rx += 0.5F;
        rz += 0.5F;
        minX = Math.min(minX, rx);
        maxX = Math.max(maxX, rx);
        minZ = Math.min(minZ, rz);
        maxZ = Math.max(maxZ, rz);
      }
    return new float[] {minX, b[1], minZ, maxX, b[4], maxZ};
  }

  @Override
  public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
    if (isTopPart(meta)) {
      // Разрушили верх — убираем низ
      for (int i = 1; i < blockHeight; i++) {
        int checkY = y - i;
        if (world.getBlock(x, checkY, z) == this) world.setBlockToAir(x, checkY, z);
        else break;
      }
    } else {
      removeCollisionParts(world, x, y, z, meta & 3);
      for (int i = 1; i < blockHeight; i++) {
        if (world.getBlock(x, y + i, z) == this && isTopPart(world.getBlockMetadata(x, y + i, z))) {
          world.setBlockToAir(x, y + i, z);
        } else break;
      }
    }
    super.breakBlock(world, x, y, z, block, meta);
    if(cameraSensitiveDecor())world.markBlockRangeForRenderUpdate(x-3,y-3,z-3,x+3,y+3,z+3);
  }

  @Override
  public int quantityDropped(int meta, int fortune, java.util.Random random) {
    return isTopPart(meta) ? 0 : 1;
  }

  private void removeCollisionParts(World world, int x, int y, int z, int facing) {
    for (ModelCollisionPart part : collisionParts) {
      int[] pos = collisionPosition(x, y, z, facing, part);
      if (world.getBlock(pos[0], pos[1], pos[2]) == BlockRegistry.ModelCollisionPart) {
        TileEntity tile = world.getTileEntity(pos[0], pos[1], pos[2]);
        if (tile instanceof TileEntityModelCollision) {
          TileEntityModelCollision collision = (TileEntityModelCollision) tile;
          if (collision.getOwnerX() == x
              && collision.getOwnerY() == y
              && collision.getOwnerZ() == z) {
            world.setBlockToAir(pos[0], pos[1], pos[2]);
          }
        }
      }
    }
  }

  @Override
  public Item getItemDropped(int meta, java.util.Random random, int fortune) {
    if (isTopPart(meta)) return null;
    return dropItem != null ? dropItem : Item.getItemFromBlock(this);
  }

  @Override
  public int damageDropped(int meta) {
    return dropItem != null ? dropMeta : 0;
  }

  @Override
  public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
    if (simpleBounds != null) {
      float[] b = rotateBounds(simpleBounds, world.getBlockMetadata(x, y, z) & 3);
      super.setBlockBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
      return;
    }
    if (rotationBounds == null) return;
    int meta = world.getBlockMetadata(x, y, z) & 3;
    if (meta < rotationBounds.length) {
      float[] b = rotationBounds[meta];
      super.setBlockBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
    }
  }

  @Override
  public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
    if (disableCollision) return null;
    setBlockBoundsBasedOnState(world, x, y, z);
    return super.getCollisionBoundingBoxFromPool(world, x, y, z);
  }

  @Override
  public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
    int meta = world.getBlockMetadata(x, y, z);
    if (isTopPart(meta)) {
      return world.getBlock(x, y - 1, z).getSelectedBoundingBoxFromPool(world, x, y - 1, z);
    }
    setBlockBoundsBasedOnState(world, x, y, z);
    return super.getSelectedBoundingBoxFromPool(world, x, y, z);
  }

  public String getTextureName() {
    return textureName;
  }

  public String getModelName() {
    return modelName;
  }

  public void register() {
    GameRegistry.registerBlock(this, ItemBlockModels.class, name);
  }

  private boolean isTopPart(int meta) {
    return (meta & TOP_META_OFFSET) != 0;
  }
}
