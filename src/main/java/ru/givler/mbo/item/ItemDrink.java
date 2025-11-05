package ru.givler.mbo.item;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.ArrayList;
import java.util.List;

public class ItemDrink extends Item {
    private List<PotionEffect> potionEffects;  // Список эффектов

    public ItemDrink(String name, String texture) {
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
       // this.setCreativeTab(CreativeTabRegistry.tabMBOfoods);
        this.setMaxStackSize(1);
        GameRegistry.registerItem(this, name);

        potionEffects = new ArrayList<>();
    }

    // Метод для добавления эффектов
    public ItemDrink addPotionEffect(int potionId, int duration, int amplifier) {
        potionEffects.add(new PotionEffect(potionId, duration, amplifier));
        return this;
    }

    // Делаем предмет питьевым
    @Override
    public EnumAction getItemUseAction(ItemStack item) {
        return EnumAction.drink;
    }

    // Время анимации питья
    @Override
    public int getMaxItemUseDuration(ItemStack item) {
        return 42;
    }

    // Что происходит при использовании зелья
    @Override
    public ItemStack onEaten(ItemStack item, World world, EntityPlayer player) {
        if (!world.isRemote) {
            for (PotionEffect effect : potionEffects) {
                player.addPotionEffect(new PotionEffect(effect));
            }
        }
        return null;
    }

    // Запускаем процесс питья
    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player) {
        player.setItemInUse(item, this.getMaxItemUseDuration(item));
        return item;
    }
}
