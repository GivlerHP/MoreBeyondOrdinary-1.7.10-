package ru.givler.mbo.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EffectRenderer;
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
import cpw.mods.fml.common.registry.GameRegistry;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.tileentity.ModelTileBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.block.model.ModelCollisionPart;
import ru.givler.mbo.block.model.ModelCollisionPart.Axis;
import ru.givler.mbo.item.ItemBlockModels;
import ru.givler.mbo.registry.BlockRegistry;
import ru.givler.mbo.tileentity.TileEntityModelCollision;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockModels extends BlockDirectional implements ITileEntityProvider {
	private static final List<BlockModels> ALL_MODELS = new ArrayList<BlockModels>();

	private String name;
	private String textureName;
	private String modelName;

	private boolean disableCollision = false;
	private int blockHeight = 1;
	private float[][] rotationBounds = null;
	private float[] simpleBounds = null;
	private String animationName = null;
	private boolean loopAnimation = true;
	private int textureFrameCount = 1;
	private int textureFrameSpeed = 100;
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
		} else if (material == Material.glass || material == Material.ice || material == Material.packedIce) {
			setStepSound(soundTypeGlass);
			particleBlock = Blocks.glass;
		} else if (material == Material.ground) {
			setStepSound(soundTypeGravel);
			particleBlock = Blocks.dirt;
		} else if (material == Material.grass || material == Material.leaves
				|| material == Material.plants || material == Material.vine) {
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
		if (bounds == null || bounds.length < 4) throw new IllegalArgumentException("Need bounds for all 4 directions");
		this.simpleBounds = null;
		this.rotationBounds = bounds;
		return this;
	}

	/**
	 * Sets one bounding box in the model's local NORTH-facing coordinates.
	 * The other three horizontal orientations are calculated automatically.
	 */
	public BlockModels withRotatingBounds(float minX, float minY, float minZ,
			float maxX, float maxY, float maxZ) {
		this.simpleBounds = new float[]{minX, minY, minZ, maxX, maxY, maxZ};
		this.rotationBounds = null;
		super.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
		return this;
	}

	public boolean isModelCollisionEnabled() { return !disableCollision; }

	public BlockModels addVerticalCollision(int offset) {
		return addVerticalCollision(offset, 0, 0, 0, 1, 1, 1);
	}

	public BlockModels addVerticalCollision(int offset, float minX, float minY, float minZ,
			float maxX, float maxY, float maxZ) {
		return addCollisionPart(Axis.VERTICAL, offset, minX, minY, minZ, maxX, maxY, maxZ);
	}

	public BlockModels addVerticalCollisionUsingBaseBounds(int offset) {
		if (simpleBounds == null) {
			throw new IllegalStateException("base bounds must be set before reusing them");
		}
		return addVerticalCollision(offset, simpleBounds[0], simpleBounds[1], simpleBounds[2],
				simpleBounds[3], simpleBounds[4], simpleBounds[5]);
	}

	public BlockModels addSideCollision(int offset) {
		return addSideCollision(offset, 0, 0, 0, 1, 1, 1);
	}

	public BlockModels addSideCollision(int offset, float minX, float minY, float minZ,
			float maxX, float maxY, float maxZ) {
		return addCollisionPart(Axis.SIDE, offset, minX, minY, minZ, maxX, maxY, maxZ);
	}

	public BlockModels addForwardCollision(int offset) {
		return addForwardCollision(offset, 0, 0, 0, 1, 1, 1);
	}

	public BlockModels addForwardCollision(int offset, float minX, float minY, float minZ,
			float maxX, float maxY, float maxZ) {
		return addCollisionPart(Axis.FORWARD, offset, minX, minY, minZ, maxX, maxY, maxZ);
	}

	private BlockModels addCollisionPart(Axis axis, int offset, float minX, float minY, float minZ,
			float maxX, float maxY, float maxZ) {
		if (minX < 0 || minY < 0 || minZ < 0 || maxX > 1 || maxY > 1 || maxZ > 1
				|| minX >= maxX || minY >= maxY || minZ >= maxZ) {
			throw new IllegalArgumentException("collision bounds must be inside 0..1 and min < max");
		}
		for (ModelCollisionPart existing : collisionParts) {
			if (existing.axis == axis && existing.offset == offset) {
				throw new IllegalArgumentException("collision part already exists at " + axis + " " + offset);
			}
		}
		collisionParts.add(new ModelCollisionPart(axis, offset,
				new float[]{minX, minY, minZ, maxX, maxY, maxZ}));
		return this;
	}

	public BlockModels withDrop(Item item) { return withDrop(item, 0); }

	public BlockModels withDrop(Item item, int meta) {
		this.dropItem = item;
		this.dropMeta = meta;
		return this;
	}

	public BlockModels withPickup(Item item) { return withPickup(item, 0); }

	public BlockModels withPickup(Item item, int meta) {
		this.pickupItem = item;
		this.pickupMeta = meta;
		return this;
	}

	public BlockModels withParticle(Block block) { return withParticle(block, 0); }

	public BlockModels withParticle(Block block, int meta) {
		this.particleBlock = block;
		this.particleMeta = meta;
		this.customParticleBlock = true;
		return this;
	}

	/** Uses another model texture as the source for vanilla breaking particles. */
	public BlockModels withParticleTexture(String texture) {
		if (texture == null || texture.isEmpty()) throw new IllegalArgumentException("texture must not be empty");
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
		return this;
	}

	/**
	 * Enables manual texture animation for Gecko models. GeckoLib 1.7.10 does
	 * not apply vanilla PNG .mcmeta animation to model textures, so frames are
	 * loaded from texture_0.png, texture_1.png, ... texture_N.png.
	 */
	public BlockModels withAnimatedTexture(int frameCount, int frameSpeedMs) {
		if (frameCount < 1) throw new IllegalArgumentException("frameCount must be >= 1");
		if (frameSpeedMs < 1) throw new IllegalArgumentException("frameSpeedMs must be >= 1");
		this.textureFrameCount = frameCount;
		this.textureFrameSpeed = frameSpeedMs;
		return this;
	}

	public BlockModels withBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return withRotatingBounds(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public boolean renderAsNormalBlock() { return false; }

	@Override
	public boolean isOpaqueCube() { return false; }

	@Override
	public int getRenderType() { return -1; }

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		if (!customParticleBlock && modelParticleIcon != null) return modelParticleIcon;
		return particleBlock.getIcon(side, particleMeta);
	}

	/**
	 * Gecko textures are normally bound directly by the TESR. Registering the
	 * same PNG in the block atlas gives vanilla EntityDiggingFX a real IIcon.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		modelParticleIcon = iconRegister.registerIcon(
				MoreBeyondOrdinary.MODID + ":models/decor/" + particleTextureName);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
			int side, float hitX, float hitY, float hitZ) {
		if (pickupItem == null) return false;
		if (world.isRemote) return true;
		world.setBlockToAir(x, y, z);
		ItemStack pickup = new ItemStack(pickupItem, 1, pickupMeta);
		if (!player.inventory.addItemStackToInventory(pickup)) {
			world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, pickup));
		}
		player.inventoryContainer.detectAndSendChanges();
		world.playSoundAtEntity(player, "random.pop", 0.2F,
				((world.rand.nextFloat() - world.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
		super.onNeighborBlockChange(world, x, y, z, neighbor);
		if (!requiresSupport || world.isRemote || isTopPart(world.getBlockMetadata(x, y, z))) return;
		Block below = world.getBlock(x, y - 1, z);
		if (below.isSideSolid(world, x, y - 1, z, ForgeDirection.UP)) return;
		Item item = unsupportedDropItem != null ? unsupportedDropItem
				: (dropItem != null ? dropItem : Item.getItemFromBlock(this));
		int meta = unsupportedDropItem != null ? 0 : (dropItem != null ? dropMeta : 0);
		if (item != null) world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5,
				new ItemStack(item, 1, meta)));
		world.setBlockToAir(x, y, z);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
		for (int i = 0; i < 4; ++i) {
			for (int j = 0; j < 4; ++j) {
				for (int k = 0; k < 4; ++k) {
					double px = x + (i + 0.5D) / 4.0D;
					double py = y + (j + 0.5D) / 4.0D;
					double pz = z + (k + 0.5D) / 4.0D;
					effectRenderer.addEffect((new EntityDiggingFX(world, px, py, pz,
							px - x - 0.5D, py - y - 0.5D, pz - z - 0.5D, this, meta))
							.applyColourMultiplier(x, y, z));
				}
			}
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer) {
		int x = target.blockX;
		int y = target.blockY;
		int z = target.blockZ;
		int side = target.sideHit;
		int meta = world.getBlockMetadata(x, y, z);
		float inset = 0.1F;

		double px = x + world.rand.nextDouble() * (this.getBlockBoundsMaxX() - this.getBlockBoundsMinX() - 0.2F)
				+ 0.1F + this.getBlockBoundsMinX();
		double py = y + world.rand.nextDouble() * (this.getBlockBoundsMaxY() - this.getBlockBoundsMinY() - 0.2F)
				+ 0.1F + this.getBlockBoundsMinY();
		double pz = z + world.rand.nextDouble() * (this.getBlockBoundsMaxZ() - this.getBlockBoundsMinZ() - 0.2F)
				+ 0.1F + this.getBlockBoundsMinZ();

		if (side == 0) py = y + this.getBlockBoundsMinY() - inset;
		if (side == 1) py = y + this.getBlockBoundsMaxY() + inset;
		if (side == 2) pz = z + this.getBlockBoundsMinZ() - inset;
		if (side == 3) pz = z + this.getBlockBoundsMaxZ() + inset;
		if (side == 4) px = x + this.getBlockBoundsMinX() - inset;
		if (side == 5) px = x + this.getBlockBoundsMaxX() + inset;

		effectRenderer.addEffect((new EntityDiggingFX(world, px, py, pz, 0.0D, 0.0D, 0.0D, this, meta))
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
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		int facing = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 2.5) & 3;
		world.setBlockMetadataWithNotify(x, y, z, facing, 2);
		for (int i = 1; i < blockHeight; i++) {
			world.setBlock(x, y + i, z, this, facing + TOP_META_OFFSET, 2);
		}
		placeCollisionParts(world, x, y, z, facing);
	}

	public boolean canPlaceStructureAt(World world, int x, int y, int z, EntityLivingBase placer) {
		int facing = MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 2.5) & 3;
		for (int i = 1; i < blockHeight; i++) {
			if (y + i < 0 || y + i >= world.getHeight()) return false;
			Block block = world.getBlock(x, y + i, z);
			if (!block.isAir(world, x, y + i, z) && !block.isReplaceable(world, x, y + i, z)) return false;
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
				((TileEntityModelCollision) tile).configure(x, y, z, rotateBounds(part.bounds, facing));
				world.markBlockForUpdate(pos[0], pos[1], pos[2]);
			}
		}
	}

	private int[] collisionPosition(int x, int y, int z, int facing, ModelCollisionPart part) {
		int localX = part.axis == Axis.SIDE ? -part.offset : 0;
		int localY = part.axis == Axis.VERTICAL ? part.offset : 0;
		int localZ = part.axis == Axis.FORWARD ? part.offset : 0;
		int worldX;
		int worldZ;
		switch (facing & 3) {
			case 1: worldX = -localZ; worldZ = localX; break;
			case 2: worldX = -localX; worldZ = -localZ; break;
			case 3: worldX = localZ; worldZ = -localX; break;
			case 0:
			default: worldX = localX; worldZ = localZ; break;
		}
		return new int[]{x + worldX, y + localY, z + worldZ};
	}

	private float[] rotateBounds(float[] b, int facing) {
		float minX = 1, minZ = 1, maxX = 0, maxZ = 0;
		float[] xs = {b[0], b[3]};
		float[] zs = {b[2], b[5]};
		for (float px : xs) for (float pz : zs) {
			float x = px - 0.5F;
			float z = pz - 0.5F;
			float rx, rz;
			switch (facing & 3) {
				case 1: rx = -z; rz = x; break;
				case 2: rx = -x; rz = -z; break;
				case 3: rx = z; rz = -x; break;
				case 0:
				default: rx = x; rz = z; break;
			}
			rx += 0.5F; rz += 0.5F;
			minX = Math.min(minX, rx); maxX = Math.max(maxX, rx);
			minZ = Math.min(minZ, rz); maxZ = Math.max(maxZ, rz);
		}
		return new float[]{minX, b[1], minZ, maxX, b[4], maxZ};
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
					if (collision.getOwnerX() == x && collision.getOwnerY() == y && collision.getOwnerZ() == z) {
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
	public int damageDropped(int meta) { return dropItem != null ? dropMeta : 0; }

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
			return world.getBlock(x, y - 1, z)
					.getSelectedBoundingBoxFromPool(world, x, y - 1, z);
		}
		setBlockBoundsBasedOnState(world, x, y, z);
		return super.getSelectedBoundingBoxFromPool(world, x, y, z);
	}

	public String getTextureName() { return textureName; }

	public String getModelName() { return modelName; }

	public void register() {
		GameRegistry.registerBlock(this, ItemBlockModels.class, name);
	}

	private boolean isTopPart(int meta) {
		return (meta & TOP_META_OFFSET) != 0;
	}
}
