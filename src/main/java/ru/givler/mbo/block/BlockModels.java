package ru.givler.mbo.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.block.blockmodels.ModelTileBase;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockModels extends BlockDirectional implements ITileEntityProvider {
	private String name;
	private String textureName;
	private String modelName;
	public BlockModels(Material material, String name, String texture, String model) {
		super(material);
		setBlockName(name);
		setCreativeTab(CreativeTabRegistry.tabMBOdecors);
		this.setHardness(1.0F);
		this.setHarvestLevel("axe", 1);  // Устанавливаем инструмент, необходимый для добычи блока
		this.setStepSound(soundTypeWood);              // Устанавливаем звук при размещении/разрушении блока
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
		this.textureName = texture;
		this.modelName = model;
		this.name = name;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isOpaqueCube() {
		return false;
	}

	public int getRenderType() {
		return -1;
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	public void onBlockPlacedBy(World p_149689_1_, int p_149689_2_, int p_149689_3_, int p_149689_4_, EntityLivingBase p_149689_5_, ItemStack p_149689_6_) {
		int l = MathHelper.floor_double((double)(p_149689_5_.rotationYaw * 4.0F / 360.0F) + 2.5) & 3;
		p_149689_1_.setBlockMetadataWithNotify(p_149689_2_, p_149689_3_, p_149689_4_, l, 2);

	}

	public void register(){
		GameRegistry.registerBlock(this, name);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new ModelTileBase(textureName, modelName);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return Blocks.planks.getIcon(side, metadata);
	}

	// Флаг, который управляет состоянием коллизии
	private boolean disableCollision = false;

	// Метод для установки состояния коллизии
	public void setCollisionEnabled(boolean enabled) {
		this.disableCollision = !enabled;
	}

	// Переопределение метода, чтобы включить или отключить коллизию
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		if (disableCollision) {
			return null; // Коллизия отключена
		}
		return super.getCollisionBoundingBoxFromPool(world, x, y, z); // Стандартная коллизия
	}


}
