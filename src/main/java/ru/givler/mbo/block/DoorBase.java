package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.IconFlipped;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.Random;

public class DoorBase extends BlockDoor {

    @SideOnly(Side.CLIENT)
    private IIcon[] iconTop;

    @SideOnly(Side.CLIENT)
    private IIcon[] iconBottom;

    private final String doorName;
    private final String textureName;
    private Item dropItem;
    private int copperState = -1;

    /**
     * Универсальный класс для создания дверей
     *
     * @param material Материал двери (Material.wood или Material.iron)
     * @param name Внутреннее имя двери (например, "ruby_door")
     * @param texture Имя текстуры без префикса мода (например, "door_ruby")
     * @param dropItem Предмет, который выпадает при разрушении двери
     */
    public DoorBase(Material material, String name, String texture, Item dropItem) {
        super(material);

        this.doorName = name;
        this.textureName = texture;
        this.dropItem = dropItem;

        this.setBlockName(name);
        this.setHardness(material == Material.wood ? 3.0F : 5.0F);
        this.setResistance(material == Material.wood ? 5.0F : 25.0F);
        this.setStepSound(material == Material.wood ? soundTypeWood : soundTypeMetal);
        this.setCreativeTab(null);

        GameRegistry.registerBlock(this, name);
    }

    /**
     * Устанавливает предмет, который будет выпадать при разрушении двери
     */
    public void setDropItem(Item item) {
        this.dropItem = item;
    }

    public DoorBase withCopper(int state, SoundType sound) {
        copperState = state;
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
        if (copperState >= 0 && !world.isRemote && (world.getBlockMetadata(x, y, z) & 8) != 0)
            CopperOxidation.tick(world, x, y, z, random);
        else if (copperState < 0) super.updateTick(world, x, y, z, random);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.iconTop = new IIcon[2];
        this.iconBottom = new IIcon[2];

        if (textureName.indexOf(':') >= 0) {
            this.iconTop[0] = register.registerIcon(textureName + "_upper");
            this.iconBottom[0] = register.registerIcon(textureName + "_lower");
        } else {
            this.iconTop[0] = register.registerIcon(MoreBeyondOrdinary.MODID + ":door/" + textureName + "_top");
            this.iconBottom[0] = register.registerIcon(MoreBeyondOrdinary.MODID + ":door/" + textureName + "_bottom");
        }
        this.iconTop[1] = new IconFlipped(this.iconTop[0], true, false);
        this.iconBottom[1] = new IconFlipped(this.iconBottom[0], true, false);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.iconBottom[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        if (side != 0 && side != 1) {
            int doorData = this.getDoorData(world, x, y, z);
            int direction = doorData & 3;
            boolean isOpen = (doorData & 4) != 0;
            boolean isHingeLeft = (doorData & 16) != 0;
            boolean isTopHalf = (doorData & 8) != 0;

            boolean flipU = false;

            if (isOpen) {
                if (direction == 0 && side == 2) flipU = !flipU;
                else if (direction == 1 && side == 5) flipU = !flipU;
                else if (direction == 2 && side == 3) flipU = !flipU;
                else if (direction == 3 && side == 4) flipU = !flipU;
            } else {
                if (direction == 0 && side == 5) flipU = !flipU;
                else if (direction == 1 && side == 3) flipU = !flipU;
                else if (direction == 2 && side == 4) flipU = !flipU;
                else if (direction == 3 && side == 2) flipU = !flipU;

                if (isHingeLeft) flipU = !flipU;
            }

            return isTopHalf ? this.iconTop[flipU ? 1 : 0] : this.iconBottom[flipU ? 1 : 0];
        }

        return this.iconBottom[0];
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return (meta & 8) != 0 ? null : this.dropItem;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World world, int x, int y, int z) {
        return this.dropItem;
    }

    /**
     * Получает полные данные о двери (включая информацию о петлях)
     */
    private int getDoorData(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        boolean isTopHalf = (meta & 8) != 0;

        int bottomMeta;
        int topMeta;

        if (isTopHalf) {
            bottomMeta = world.getBlockMetadata(x, y - 1, z);
            topMeta = meta;
        } else {
            bottomMeta = meta;
            topMeta = world.getBlockMetadata(x, y + 1, z);
        }

        boolean isHingeLeft = (topMeta & 1) != 0;

        return (bottomMeta & 7) | (isTopHalf ? 8 : 0) | (isHingeLeft ? 16 : 0);
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (copperState >= 0) {
            int bottomY = (world.getBlockMetadata(x, y, z) & 8) != 0 ? y - 1 : y;
            if (CopperOxidation.interact(world, x, bottomY, z, player)) return true;
            if (!world.isRemote) {
                int bottomMeta = world.getBlockMetadata(x, bottomY, z);
                boolean open = (bottomMeta & 4) == 0;
                world.setBlockMetadataWithNotify(x, bottomY, z, bottomMeta ^ 4, 2);
                world.markBlockRangeForRenderUpdate(x, bottomY, z, x, bottomY + 1, z);
                playCopperSound(world, x, bottomY, z, open);
            }
            return true;
        }
        if (this.blockMaterial == Material.iron) {
            return false; // Железные двери не открываются вручную
        }

        int meta = this.getDoorData(world, x, y, z);
        int bottomMeta = meta & 7;
        bottomMeta ^= 4; // Переключаем состояние открыто/закрыто

        if ((meta & 8) == 0) {
            world.setBlockMetadataWithNotify(x, y, z, bottomMeta, 2);
            world.markBlockRangeForRenderUpdate(x, y, z, x, y, z);
        } else {
            world.setBlockMetadataWithNotify(x, y - 1, z, bottomMeta, 2);
            world.markBlockRangeForRenderUpdate(x, y - 1, z, x, y, z);
        }

        world.playAuxSFXAtEntity(player, 1003, x, y, z, 0);
        return true;
    }

    @Override
    public void func_150014_a(World world, int x, int y, int z, boolean powered) {
        if (copperState < 0) {
            super.func_150014_a(world, x, y, z, powered);
            return;
        }
        if (world.isRemote) return;
        int bottomY = (world.getBlockMetadata(x, y, z) & 8) != 0 ? y - 1 : y;
        int meta = world.getBlockMetadata(x, bottomY, z);
        if (((meta & 4) != 0) == powered) return;
        world.setBlockMetadataWithNotify(x, bottomY, z, meta ^ 4, 2);
        world.markBlockRangeForRenderUpdate(x, bottomY, z, x, bottomY + 1, z);
        playCopperSound(world, x, bottomY, z, powered);
    }

    private void playCopperSound(World world, int x, int y, int z, boolean open) {
        world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D,
                open ? "mbo:copper.door.open" : "mbo:copper.door.close", 1.0F, 1.0F);
    }
}
