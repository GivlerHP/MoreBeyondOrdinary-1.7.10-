package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class BlockBasic extends Block {

        public BlockBasic(Material material, String name, String texture) {
            super(material);

            this.setBlockName(name);   // Устанавливаем внутреннее (регистрационное) имя блока
            this.setLightLevel(0.0F);   // Устанавливаем уровень освещения блока (0.0F — не светится, 1.0F — максимальная яркость)
            this.setLightOpacity(0);   // Устанавливаем прозрачность блока (0 — полностью прозрачный, 255 — полностью непрозрачный)
            this.setHardness(1.0F);      // Устанавливаем твёрдость блока (1.0F — обычная твёрдость камня, 0.0F — моментально разрушается)
            this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);   // Добавляем блок в пользовательскую креативную вкладку
            this.setResistance(10.0F);         // Устанавливаем сопротивление взрывам
            this.setHarvestLevel("pick_axe", 1);  // Устанавливаем инструмент, необходимый для добычи блока
            this.setStepSound(soundTypeStone);              // Устанавливаем звук при размещении/разрушении блока
            this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texture); // Задаём текстуру блока
            GameRegistry.registerBlock(this, name);        // Регистрируем блок в системе Minecraft, используя уникальное имя
        }

    }

