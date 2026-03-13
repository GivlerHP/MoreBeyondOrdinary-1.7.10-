package ru.givler.mbo.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import ru.givler.mbo.registry.BlockRegistry;

public class ItemChalk extends ItemBase {
    public ItemChalk(String name, String texture, int maxStackSize) {
        super(name, texture, maxStackSize);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if(world.isRemote) return true;

        // Проверяем, что клик по верхней стороне блока
        if(side != 1) return false;

        // Проверяем, что над блоком есть свободное место 3x3
        for (int ix = -1; ix <= 1; ix++) {
            for (int iz = -1; iz <= 1; iz++) {
                Block b = world.getBlock(x + ix, y + 1, z + iz);
                if (b != Blocks.air) {
                    player.addChatMessage(new ChatComponentText("§cНедостаточно места для круга изгнания!"));
                    return false;
                }
            }
        }

        // ← ИЗМЕНЕНО: размещаем ТОЛЬКО ОДИН блок в центре
        world.setBlock(x, y + 1, z, BlockRegistry.BlockExorcism);

        // Сохраняем координаты центра
       // TileExorcismCircle.markCenter(world, x, y + 1, z, x, y + 1, z);

        player.addChatMessage(new ChatComponentText("§aКруг изгнания начерчен."));
        return true;
    }
}