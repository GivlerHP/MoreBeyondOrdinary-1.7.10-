package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockBarrier extends Block {

    private int renderType = -1;

    public BlockBarrier() {
        super(Material.rock);
        this.setBlockName("Barrier");
        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":barrier");
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.setBlockUnbreakable();
        this.setResistance(6000000.0F);
        this.setLightOpacity(0);
        this.disableStats();
        GameRegistry.registerBlock(this, "Barrier");
    }

    public void setBarrierRenderType(int renderType) {
        this.renderType = renderType;
    }

    @Override
    public int getRenderType() {
        return this.renderType;
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
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }
}
