package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

//Класс для создания мультекстурных блоков. Вверх и низ - одна текстура. Боковые - другая.
public class BlockMultiTexture extends Block {
    private final String textureTopBottom;
    private final String textureSides;
    private IIcon iconTopBottom;
    private IIcon iconSides;

    public BlockMultiTexture(Material material, String name, String textureTopBottom, String textureSides) {
        super(material);
        this.textureTopBottom = textureTopBottom;
        this.textureSides = textureSides;

        this.setBlockName(name);
        this.setLightLevel(0.0F);
        this.setLightOpacity(0);
        this.setHardness(1.0F);
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setResistance(10.0F);
        this.setHarvestLevel("pick_axe", 0);
        this.setStepSound(soundTypeStone);

        GameRegistry.registerBlock(this, name);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        this.iconTopBottom = iconRegister.registerIcon(MoreBeyondOrdinary.MODID + ":" + textureTopBottom);
        this.iconSides = iconRegister.registerIcon(MoreBeyondOrdinary.MODID + ":" + textureSides);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == 0 || side == 1) { // Верх и низ
            return iconTopBottom;
        } else { // Остальные стороны
            return iconSides;
        }
    }
}
