package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockLog;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockRotatableWood extends BlockLog {
    private final String topTexture;
    private final String sideTexture;

    private IIcon topIcon;
    private IIcon sideIcon;

    public BlockRotatableWood(String name, String textureTop, String textureSide) {
        super();
        this.topTexture = textureTop;
        this.sideTexture = textureSide;

        this.setBlockName(name);
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setHardness(2.0F);
        this.setResistance(5.0F);
        this.setStepSound(soundTypeWood);

        GameRegistry.registerBlock(this, name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        this.topIcon = iconRegister.registerIcon(MoreBeyondOrdinary.MODID + ":" + topTexture);
        this.sideIcon = iconRegister.registerIcon(MoreBeyondOrdinary.MODID + ":" + sideTexture);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected IIcon getSideIcon(int meta) {
        return this.sideIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected IIcon getTopIcon(int meta) {
        return this.topIcon;
    }
}
