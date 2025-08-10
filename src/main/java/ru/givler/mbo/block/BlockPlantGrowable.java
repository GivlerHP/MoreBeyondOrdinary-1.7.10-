package ru.givler.mbo.block;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;

import java.util.Random;

public class BlockPlantGrowable extends BlockPlantBasic {
    private IIcon[] icons;
    private final int maxGrowthStage = 3;
    private final String textureBase;
    private final Item dropItem;

    public BlockPlantGrowable(String name, String textureBase, Item dropItem) {
        super(name, textureBase + "_0");
        this.textureBase = textureBase;
        this.dropItem = dropItem;
        this.setTickRandomly(true);
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        icons = new IIcon[maxGrowthStage + 1];
        for (int i = 0; i <= maxGrowthStage; i++) {
            icons[i] = reg.registerIcon(MoreBeyondOrdinary.MODID + ":" + textureBase + "_" + i);
        }
        this.blockIcon = icons[0]; // по умолчанию стадия 0
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icons[MathHelper.clamp_int(meta, 0, maxGrowthStage)];
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        if (!world.isRemote) {
            int meta = world.getBlockMetadata(x, y, z);
            if (meta < maxGrowthStage && rand.nextInt(1) == 0) { // шанс роста
                world.setBlockMetadataWithNotify(x, y, z, meta + 1, 2);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
                                    EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            int meta = world.getBlockMetadata(x, y, z);
            if (meta == maxGrowthStage && player.getCurrentEquippedItem() != null &&
                    player.getCurrentEquippedItem().getItem() instanceof ItemShears) {

                // дроп урожая
                world.spawnEntityInWorld(new net.minecraft.entity.item.EntityItem(
                        world, x + 0.5, y + 1.0, z + 0.5,
                        new net.minecraft.item.ItemStack(dropItem)
                ));

                // обнуляем стадию роста
                world.setBlockMetadataWithNotify(x, y, z, 0, 2);

                // урон ножницам
                player.getCurrentEquippedItem().damageItem(1, player);

                return true;
            }
        }
        return false;
    }
}
