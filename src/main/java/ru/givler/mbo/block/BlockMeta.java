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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.givler.mbo.item.ItemBlockMetadata;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;
import java.util.Random;

//класс необходимых для блоков с метаданными. Если есть много блоков одного типа, их можно сделать через этот класс, тогда это потребует лишь 1 id.
public class BlockMeta extends Block {

    private int count;
    private String[] textureNames;
    private boolean copper;
    private boolean copperGrate;
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
    public BlockMeta(Material material, String name, String[] textures) {
        this(material, name, textures[0], textures.length);
        this.textureNames = textures;
    }

    public BlockMeta withCopper(boolean grate) {
        copper = true;
        copperGrate = grate;
        setHardness(3.0F);
        setResistance(6.0F);
        setHarvestLevel("pickaxe", 1);
        setLightOpacity(grate ? 0 : 255);
        setTickRandomly(true);
        final String sound = grate ? "copper.grate" : "copper";
        setStepSound(new SoundType("copper", 1.0F, 1.0F) {
            @Override public String getBreakSound() { return "mbo:" + sound + ".break"; }
            @Override public String getStepResourcePath() { return "mbo:" + sound + ".step"; }
            @Override public String func_150496_b() { return "mbo:" + sound + ".break"; }
        });
        return this;
    }

    public boolean isCopper() { return copper; }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    //добавляет блоки во вкладку в инвенторе с различными метаданными
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List subItems) {
        for (int n=0; n<this.count; ++n) {
            if (copper && n >= 4 && n < 8) continue;
            subItems.add(new ItemStack(this,1,n));
        }
    }

    @Override
    public boolean isOpaqueCube() { return !copperGrate; }

    @Override
    public boolean renderAsNormalBlock() { return !copperGrate; }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        // RenderBlocks passes the coordinates of the neighbouring block here.
        // All oxidation and wax states share this block ID, so their touching
        // grate faces are internal even when their metadata differs.
        if (copperGrate && world.getBlock(x, y, z) == this) return false;
        return super.shouldSideBeRendered(world, x, y, z, side);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random random) {
        if (copper && !world.isRemote) CopperOxidation.tick(world, x, y, z, random);
        else if (!copper) super.updateTick(world, x, y, z, random);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
                                    int side, float hitX, float hitY, float hitZ) {
        return copper && CopperOxidation.interact(world, x, y, z, player);
    }

    //определяет какую текстуру будет использовать блок
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (meta < 0 || meta >= this.icon.length) {
            return this.icon[0]; // fallback-текстура вместо краша
        }
        return this.icon[meta];
    }
    //регестрирует текстуры для блока.  Для каждого возможного варианта метаданных регистрируется отдельная текстура.
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister icon) {
        this.icon = new IIcon[this.count];
        for (int i = 0; i < this.count; ++i) {
            String texture = this.textureNames == null
                    ? this.getTextureName() + "_" + i
                    : MoreBeyondOrdinary.MODID + ":" + this.textureNames[i];
            this.icon[i] = icon.registerIcon(texture);
        }
    }
}
