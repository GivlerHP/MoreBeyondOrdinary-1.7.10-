package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class TrapDoorBase extends BlockTrapDoor {

    @SideOnly(Side.CLIENT)
    private IIcon blockIcon;

    private final String trapdoorName;
    private final String textureName;

    /**
     * Универсальный класс для создания люков
     *
     * @param material Материал люка (Material.wood или Material.iron)
     * @param name Внутреннее имя люка (например, "ruby_trapdoor")
     * @param texture Имя текстуры без префикса мода (например, "trapdoor_ruby")
     */
    public TrapDoorBase(Material material, String name, String texture) {
        super(material);

        this.trapdoorName = name;
        this.textureName = texture;

        this.disableValidation = true;

        this.setBlockName(name);
        this.setHardness(material == Material.wood ? 3.0F : 5.0F);
        this.setResistance(material == Material.wood ? 5.0F : 25.0F);
        this.setStepSound(material == Material.wood ? soundTypeWood : soundTypeMetal);
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);

        GameRegistry.registerBlock(this, name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.blockIcon = register.registerIcon(MoreBeyondOrdinary.MODID + ":trapdoor/" + textureName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.blockIcon;
    }
}