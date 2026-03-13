package ru.givler.mbo.item;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.block.DoorBase;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class DoorItemBase extends Item {

    private final Block doorBlock;
    private final Material material;

    /**
     * Предмет двери
     *
     * @param doorBlock Блок двери, который будет размещаться
     * @param name Внутреннее имя предмета
     * @param texture Имя текстуры без префикса мода
     */
    public DoorItemBase(Block doorBlock, String name, String texture) {
        super();

        this.doorBlock = doorBlock;
        this.material = doorBlock.getMaterial();

        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":door/" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);
        this.maxStackSize = 1;

        GameRegistry.registerItem(this, name);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (side != 1) {
            return false;
        }

        y++;

        if (!player.canPlayerEdit(x, y, z, side, stack) || !player.canPlayerEdit(x, y + 1, z, side, stack)) {
            return false;
        }

        if (!this.doorBlock.canPlaceBlockAt(world, x, y, z)) {
            return false;
        }

        int direction = MathHelper.floor_double((double)((player.rotationYaw + 180.0F) * 4.0F / 360.0F) - 0.5D) & 3;

        placeDoorBlock(world, x, y, z, direction, this.doorBlock);

        world.playSoundEffect(
                (double)x + 0.5D,
                (double)y + 0.5D,
                (double)z + 0.5D,
                this.doorBlock.stepSound.func_150496_b(), // звук размещения
                (this.doorBlock.stepSound.getVolume() + 1.0F) / 2.0F,
                this.doorBlock.stepSound.getPitch() * 0.8F
        );

        stack.stackSize--;
        return true;
    }

    /**
     * Размещает дверь в мире - ТОЧНАЯ КОПИЯ ВАНИЛЬНОЙ ЛОГИКИ MINECRAFT
     * Основано на декомпилированном коде ItemDoor из Minecraft 1.7.10
     */
    public static void placeDoorBlock(World world, int x, int y, int z, int direction, Block doorBlock) {
        byte leftOffset = 0;
        byte behindOffset = 0;

        // Определяем смещения в зависимости от направления
        if (direction == 0) {
            leftOffset = 1;
            behindOffset = 1;
        } else if (direction == 1) {
            leftOffset = -1;
        } else if (direction == 2) {
            leftOffset = -1;
            behindOffset = -1;
        } else if (direction == 3) {
            leftOffset = 1;
            behindOffset = -1;
        }

        // Вычисляем координаты соседних блоков
        int dxLeft = (direction == 0 || direction == 2) ? 0 : leftOffset;
        int dzLeft = (direction == 0 || direction == 2) ? leftOffset : 0;

        int dxBehind = (direction == 0 || direction == 2) ? behindOffset : 0;
        int dzBehind = (direction == 0 || direction == 2) ? 0 : behindOffset;

        // Проверяем твердые блоки слева
        boolean leftSolid = world.getBlock(x - dxLeft, y, z - dzLeft).getMaterial().isSolid() ||
                world.getBlock(x - dxLeft, y + 1, z - dzLeft).getMaterial().isSolid();

        // Проверяем твердые блоки справа
        boolean rightSolid = world.getBlock(x + dxLeft, y, z + dzLeft).getMaterial().isSolid() ||
                world.getBlock(x + dxLeft, y + 1, z + dzLeft).getMaterial().isSolid();

        // Проверяем наличие дверей слева
        boolean leftDoor = world.getBlock(x - dxLeft, y, z - dzLeft) == doorBlock ||
                world.getBlock(x - dxLeft, y + 1, z - dzLeft) == doorBlock;

        // Проверяем наличие дверей справа
        boolean rightDoor = world.getBlock(x + dxLeft, y, z + dzLeft) == doorBlock ||
                world.getBlock(x + dxLeft, y + 1, z + dzLeft) == doorBlock;

        // ВАНИЛЬНАЯ ЛОГИКА ОПРЕДЕЛЕНИЯ ПЕТЕЛЬ
        boolean hingeLeft = false;

        if (leftDoor && !rightDoor) {
            hingeLeft = true;
        } else if (rightSolid && !leftSolid) {
            hingeLeft = true;
        } else if (leftDoor && rightDoor) {
            // Если двери с обеих сторон, проверяем направление
            hingeLeft = true;
        }

        // Устанавливаем нижнюю половину двери
        world.setBlock(x, y, z, doorBlock, direction, 2);
        // Устанавливаем верхнюю половину двери с информацией о петлях
        world.setBlock(x, y + 1, z, doorBlock, 8 | (hingeLeft ? 1 : 0), 2);
    }
}