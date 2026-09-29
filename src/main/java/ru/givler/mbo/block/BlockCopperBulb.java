package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.Random;

public final class BlockCopperBulb extends Block {
    private static final int[] LIGHT_LEVELS = {15, 12, 8, 4};
    public final int copperState;
    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public BlockCopperBulb(int state) {
        super(Material.iron);
        copperState = state;
        setBlockName("CopperBulb" + state);
        setHardness(3.0F);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        setTickRandomly((state & 8) == 0 && (state & 3) < 3);
        setStepSound(new SoundType("copper_bulb", 1.0F, 1.0F) {
            @Override public String getBreakSound() { return "mbo:copper.bulb.break"; }
            @Override public String getStepResourcePath() { return "mbo:copper.bulb.step"; }
            @Override public String func_150496_b() { return "mbo:copper.bulb.place"; }
        });
        GameRegistry.registerBlock(this, "CopperBulb" + state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        icons = new IIcon[4];
        String prefix = new String[] {"", "exposed_", "weathered_", "oxidized_"}[copperState & 3];
        icons[0] = register.registerIcon("mbo:copper/" + prefix + "copper_bulb");
        icons[1] = register.registerIcon("mbo:copper/" + prefix + "copper_bulb_lit");
        icons[2] = register.registerIcon("mbo:copper/" + prefix + "copper_bulb_powered");
        icons[3] = register.registerIcon("mbo:copper/" + prefix + "copper_bulb_lit_powered");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) { return icons[meta & 3]; }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        if ((world.getBlockMetadata(x, y, z) & 1) == 0) return 0;
        return LIGHT_LEVELS[copperState & 3];
    }

    @Override
    public boolean hasComparatorInputOverride() { return true; }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return (world.getBlockMetadata(x, y, z) & 1) != 0 ? 15 : 0;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        if (!world.isRemote && world.isBlockIndirectlyGettingPowered(x, y, z)) {
            int meta = world.getBlockMetadata(x, y, z);
            if ((meta & 2) == 0) world.setBlockMetadataWithNotify(x, y, z, meta ^ 3, 3);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;
        int meta = world.getBlockMetadata(x, y, z);
        boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
        if (powered == ((meta & 2) != 0)) return;
        int next = meta ^ 2;
        if (powered) next ^= 1;
        world.setBlockMetadataWithNotify(x, y, z, next, 3);
        if (powered) world.playSoundEffect(x + .5D, y + .5D, z + .5D,
                (next & 1) != 0 ? "mbo:copper.bulb.on" : "mbo:copper.bulb.off", 1.0F, 1.0F);
    }

    @Override
    public int damageDropped(int meta) { return 0; }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return Item.getItemFromBlock(this);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (!world.isRemote) CopperOxidation.tick(world, x, y, z, random);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        return CopperOxidation.interact(world, x, y, z, player);
    }
}
