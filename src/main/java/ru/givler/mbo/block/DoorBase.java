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

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        this.iconTop = new IIcon[2];
        this.iconBottom = new IIcon[2];

        this.iconTop[0] = register.registerIcon(MoreBeyondOrdinary.MODID + ":door/" + textureName + "_top");
        this.iconBottom[0] = register.registerIcon(MoreBeyondOrdinary.MODID + ":door/" + textureName + "_bottom");
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
}
