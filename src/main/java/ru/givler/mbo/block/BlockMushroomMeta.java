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
import ru.givler.mbo.ItemBlockMetadata;
import ru.givler.mbo.main;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;

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

        this.setBlockTextureName(main.MODID + ":" + texture);

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
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < this.count; i++) {
            list.add(new ItemStack(item, 1, i));
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