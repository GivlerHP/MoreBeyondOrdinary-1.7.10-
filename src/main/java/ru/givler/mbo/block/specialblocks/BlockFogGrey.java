package ru.givler.mbo.block.specialblocks;

import net.minecraft.util.*;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockFogGrey extends Block {

    public BlockFogGrey(Material material, String name, String texture) {
        super(material);

        this.setBlockName(name);   // Устанавливаем внутреннее (регистрационное) имя блока
        this.setLightLevel(0.5F);   // Устанавливаем уровень освещения блока (0.0F — не светится, 1.0F — максимальная яркость)
        this.setLightOpacity(0);   // Устанавливаем прозрачность блока (0 — полностью прозрачный, 255 — полностью непрозрачный)
        this.setHardness(1.0F);      // Устанавливаем твёрдость блока (1.0F — обычная твёрдость камня, 0.0F — моментально разрушается)
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);   // Добавляем блок в пользовательскую креативную вкладку
        this.setResistance(10.0F);         // Устанавливаем сопротивление взрывам
        this.setHarvestLevel("pick_axe", 1);  // Устанавливаем инструмент, необходимый для добычи блока
        this.setStepSound(soundTypeStone);              // Устанавливаем звук при размещении/разрушении блока
        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texture); // Задаём текстуру блока
        this.setBlockUnbreakable();
        GameRegistry.registerBlock(this, name);        // Регистрируем блок в системе Minecraft, используя уникальное имя
    }


    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            player.addChatMessage(new ChatComponentText(StatCollector.translateToLocal("message.fog.closed"))
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_GRAY)));
        }
        return true;
    }

}
