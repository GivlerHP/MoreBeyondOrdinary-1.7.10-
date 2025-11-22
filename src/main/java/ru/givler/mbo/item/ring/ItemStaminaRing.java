package ru.givler.mbo.item.ring;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import minefantasy.mf2.api.stamina.StaminaBar;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.item.ItemRingBase;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.List;

public class ItemStaminaRing extends ItemRingBase {
    private final float value;
    private final String level;

    public ItemStaminaRing(String name, String texture, float value, String level) {
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxStackSize(1);
        GameRegistry.registerItem(this, name);

        this.value = value;
        this.level = level;
    }


    @Override
    public void onWornTick(ItemStack itemstack, EntityLivingBase player) {
        if (!itemstack.hasTagCompound()) {
            itemstack.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
        }

        if (!itemstack.getTagCompound().getBoolean("StaminaAdded")) {
            if (Loader.isModLoaded("minefantasy2")) {
                StaminaBar.buffStaminaInfinite(player, this.value);
                itemstack.getTagCompound().setBoolean("StaminaAdded", true);
            }
        }
    }

    @Override
    public void onUnequipped(ItemStack itemstack, EntityLivingBase player) {
        if (itemstack.hasTagCompound() && itemstack.getTagCompound().getBoolean("StaminaAdded")) {
            if (Loader.isModLoaded("minefantasy2")) {
                StaminaBar.removeBuffStaminaInfinite(player, this.value);
                itemstack.getTagCompound().setBoolean("StaminaAdded", false);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        String key = "description.ring." + this.level + "." + this.getUnlocalizedName().substring(5);
        if (StatCollector.canTranslate(key)) {
            list.add(StatCollector.translateToLocal(key));
        }
    }
}
