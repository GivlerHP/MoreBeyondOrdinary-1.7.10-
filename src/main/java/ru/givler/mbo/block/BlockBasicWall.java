package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockBasicWall extends BlockWall {

    private IIcon textureIcon; // Переменная для хранения иконки текстуры

    public BlockBasicWall(Block baseBlock, String name, String texture) {
        super(baseBlock);

        this.setBlockName(name + "_wall");
        this.setLightLevel(0.0F);
        this.setLightOpacity(0);
        this.setHardness(1.0F);
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setResistance(10.0F);
        this.setHarvestLevel("pick_axe", 1);
        this.setStepSound(soundTypeStone);
        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        GameRegistry.registerBlock(this, name + "_wall");
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        textureIcon = reg.registerIcon(getTextureName()); // Загружаем текстуру
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return textureIcon; // Всегда возвращаем одну текстуру
    }

    @Override
    public boolean canConnectWallTo(IBlockAccess world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);

        // Проверяем, является ли соседний блок забором или стеной
        if (block instanceof BlockBasicWall) {
            return true;
        }

        return super.canConnectWallTo(world, x, y, z);
    }
}
