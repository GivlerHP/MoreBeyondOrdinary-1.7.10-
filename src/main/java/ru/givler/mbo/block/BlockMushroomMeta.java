package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockMushroom;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import ru.givler.mbo.EnumParticleType;
import ru.givler.mbo.ItemBlockMetadata;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;
import java.util.Random;

public class BlockMushroomMeta extends BlockMushroom {

    private final int count;
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public BlockMushroomMeta(String name, String texture, int count) {
        super();
        this.count = count;

        this.setBlockName(name);
        this.setLightLevel(0.7F);
        this.setLightOpacity(0);
        this.setHardness(0.2F);
        this.setResistance(1.0F);
        this.setStepSound(soundTypeGrass);
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.useNeighborBrightness = true;

        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texture);

        GameRegistry.registerBlock(this, ItemBlockMetadata.class, name);
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    public boolean func_149884_c(World world, int x, int y, int z, Random random) {
        if (!world.isRemote) {
            // Шанс 25%
            if (random.nextFloat() < 0.5F) {
                int meta = world.getBlockMetadata(x, y, z);
                ItemStack drop = new ItemStack(Item.getItemFromBlock(this), 1, meta);
                this.dropBlockAsItem(world, x, y, z, drop);
            }
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < this.count; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        int meta = world.getBlockMetadata(x, y, z);

        if (meta == 0) {
            double px = x + 0.5 + (rand.nextDouble() - 0.5);
            double py = y + 0.1 + rand.nextDouble() * 0.5;
            double pz = z + 0.5 + (rand.nextDouble() - 0.5);

            MoreBeyondOrdinary.proxy.spawnParticle(
                    EnumParticleType.DARK_MAGIC, world,
                    px, py, pz,
                    0.0, 0.1, 0.0
            );
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return meta < icons.length ? icons[meta] : icons[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.icons = new IIcon[this.count];
        String base = this.getTextureName();

        for (int i = 0; i < this.count; ++i) {
            this.icons[i] = reg.registerIcon(base + "_" + i);
        }
    }
}