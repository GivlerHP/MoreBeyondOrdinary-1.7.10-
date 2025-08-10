package ru.givler.mbo.item;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;

public class ItemMeta extends Item {
    private final int count;
    private final String textureBase;

    @SideOnly(Side.CLIENT)
    private IIcon[] icon;

    public ItemMeta(String name, String texture, int maxStackSize, int count) {
        this.count = count;
        this.textureBase = texture;

        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(maxStackSize);
        this.setHasSubtypes(true); //говорит о том что предметы будут с методаттой
        this.setMaxDamage(0);
        this.canRepair = false;

        GameRegistry.registerItem(this, name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        this.icon = new IIcon[this.count];
        for (int i = 0; i < this.count; ++i) {
            this.icon[i] = register.registerIcon(MoreBeyondOrdinary.MODID + ":" + this.textureBase + "_" + i);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return meta < icon.length ? icon[meta] : icon[0];
    }

    @Override
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < this.count; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack.getItemDamage();
        return super.getUnlocalizedName() + "." + meta;
    }
}
