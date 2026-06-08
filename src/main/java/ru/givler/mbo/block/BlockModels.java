package ru.givler.mbo.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;
import ru.givler.mbo.registry.CreativeTabRegistry;
import ru.givler.mbo.tileentity.ModelTileBase;

public class BlockModels extends BlockDirectional implements ITileEntityProvider {

	private String name;
	private String textureName;
	private String modelName;

	private boolean disableCollision = false;
	private int blockHeight = 1;
	private float[][] rotationBounds = null;

	private static final int TOP_META_OFFSET = 4;

	public BlockModels(Material material, String name, String texture, String model) {
		super(material);
		setBlockName(name);
		setCreativeTab(CreativeTabRegistry.tabMBOdecors);
		this.setHardness(1.0F);
		this.setHarvestLevel("axe", 1);
		this.setStepSound(soundTypeWood);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
		this.textureName = texture;
		this.modelName = model;
		this.name = name;
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
		this.rotationBounds = bounds;
		return this;
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
		return Blocks.planks.getIcon(side, metadata);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
		return true;
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return !isTopPart(metadata);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		if (isTopPart(metadata)) return null;
		return new ModelTileBase(textureName, modelName);
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
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		if (isTopPart(meta)) {
			// Разрушили верх — убираем низ
			if (world.getBlock(x, y - 1, z) == this) {
				world.setBlockToAir(x, y - 1, z);
			}
		} else {
			for (int i = 1; i < blockHeight; i++) {
				if (world.getBlock(x, y + i, z) == this) {
					world.setBlockToAir(x, y + i, z);
				}
			}
		}
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public int quantityDropped(int meta, int fortune, java.util.Random random) {
		return isTopPart(meta) ? 0 : 1;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		if (rotationBounds == null) return;
		int meta = world.getBlockMetadata(x, y, z) & 3;
		if (meta < rotationBounds.length) {
			float[] b = rotationBounds[meta];
			this.setBlockBounds(b[0], b[1], b[2], b[3], b[4], b[5]);
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

	public void register() {
		GameRegistry.registerBlock(this, name);
	}

	private boolean isTopPart(int meta) {
		return meta >= TOP_META_OFFSET;
	}
}