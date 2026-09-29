package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;
import java.util.Random;

public class TrapDoorBase extends BlockTrapDoor {

    @SideOnly(Side.CLIENT)
    private IIcon blockIcon;

    private final String trapdoorName;
    private final String textureName;
    private boolean redstoneOnly;
    private String openSound;
    private String closeSound;
    private int copperState = -1;

    public TrapDoorBase(String name, String texture) {
        this(Material.wood, name, texture);
    }

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

    public TrapDoorBase requireRedstone(String openSound, String closeSound) {
        this.redstoneOnly = true;
        this.openSound = openSound;
        this.closeSound = closeSound;
        return this;
    }

    public TrapDoorBase withCopper(int state, SoundType sound) {
        copperState = state;
        openSound = "mbo:copper.trapdoor.open";
        closeSound = "mbo:copper.trapdoor.close";
        setHardness(3.0F);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 1);
        setStepSound(sound);
        setTickRandomly((state & 8) == 0 && (state & 3) < 3);
        return this;
    }

    public int getCopperState() { return copperState; }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (copperState >= 0 && !world.isRemote) CopperOxidation.tick(world, x, y, z, random);
        else if (copperState < 0) super.updateTick(world, x, y, z, random);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        if (copperState >= 0) {
            if (CopperOxidation.interact(world, x, y, z, player)) return true;
            if (!world.isRemote) {
                int meta = world.getBlockMetadata(x, y, z);
                boolean open = (meta & 4) == 0;
                world.setBlockMetadataWithNotify(x, y, z, meta ^ 4, 2);
                world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D,
                        open ? openSound : closeSound, 1.0F, 1.0F);
            }
            return true;
        }
        return !redstoneOnly && super.onBlockActivated(world, x, y, z, player,
                side, hitX, hitY, hitZ);
    }

    @Override
    public void func_150120_a(World world, int x, int y, int z, boolean powered) {
        if (openSound == null || closeSound == null) {
            super.func_150120_a(world, x, y, z, powered);
            return;
        }
        if (world.isRemote) return;
        int meta = world.getBlockMetadata(x, y, z);
        if (((meta & 4) != 0) == powered) return;

        world.setBlockMetadataWithNotify(x, y, z, meta ^ 4, 2);
        world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D,
                powered ? openSound : closeSound, 1.0F, 1.0F);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
        int meta = world.getBlockMetadata(x, y, z);
        int openBit = meta & 4;
        int topBit  = meta & 8;

        int facing = MathHelper.floor_double((double)(player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        switch (facing) {
            case 0: meta = 0; break;
            case 1: meta = 3; break;
            case 2: meta = 1; break;
            case 3: meta = 2; break;
        }

        world.setBlockMetadataWithNotify(x, y, z, meta | openBit | topBit, 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.blockIcon = register.registerIcon(textureName.indexOf(':') >= 0
                ? textureName : MoreBeyondOrdinary.MODID + ":trapdoor/" + textureName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.blockIcon;
    }
}
