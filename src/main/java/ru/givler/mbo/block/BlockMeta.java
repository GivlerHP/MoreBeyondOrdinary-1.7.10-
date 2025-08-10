package ru.givler.mbo.block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import ru.givler.mbo.ItemBlockMetadata;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;

//класс необходимых для блоков с метаданными. Если есть много блоков одного типа, их можно сделать через этот класс, тогда это потребует лишь 1 id.
public class BlockMeta extends Block {

    private int count;
    @SideOnly(Side.CLIENT)
    private IIcon[] icon;

    public BlockMeta(Material material, String name, String texture, int count) {
        super(material);
        this.count=count;
        this.setBlockName(name);  // Устанавливаем внутреннее (регистрационное) имя блока
        this.setLightLevel(0.0F);  // Устанавливаем уровень освещения блока (0.0F — не светится, 1.0F — максимальная яркость)
        this.setLightOpacity(0);     // Устанавливаем прозрачность блока (0 — полностью прозрачный, 255 — полностью непрозрачный)
        this.setHardness(1.0F);         // Устанавливаем твёрдость блока (1.0F — обычная твёрдость камня, 0.0F — моментально разрушается)
        this.setCreativeTab(CreativeTabRegistry.tabMBOblocks);     // Добавляем блок в пользовательскую креативную вкладку
        this.setResistance(10.0F);         // Устанавливаем сопротивление взрывам
        this.setHarvestLevel("pick_axe", 0);   // Устанавливаем инструмент, необходимый для добычи блока
        this.setStepSound(soundTypeStone);                  // Устанавливаем звук при размещении/разрушении блока
        this.setBlockTextureName(MoreBeyondOrdinary.MODID + ":" + texture);       // Задаём текстуру блока
        GameRegistry.registerBlock(this, ItemBlockMetadata.class, name);   // Регистрируем блок в системе Minecraft, используя уникальное имя
    }

    //какой блока выпадет при разрушение
    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    //добавляет блоки во вкладку в инвенторе с различными метаданными
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List subItems) {
        for (int n=0; n<this.count; ++n) {
            subItems.add(new ItemStack(this,1,n));
        }
    }

    //определяет какую текстуру будет использовать блок
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.icon[meta];
    }

    //регестрирует текстуры для блока.  Для каждого возможного варианта метаданных регистрируется отдельная текстура.
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister icon) {
        this.icon = new IIcon[this.count];
        for (int i = 0; i < this.count; ++i) {
            this.icon[i] = icon.registerIcon(this.getTextureName() + "_" + i);
        }
    }
}