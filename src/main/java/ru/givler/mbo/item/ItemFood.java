package ru.givler.mbo.item;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import java.util.ArrayList;
import java.util.List;

// Класс для создания еды с несколькими эффектами.
public class ItemFood extends net.minecraft.item.ItemFood {
    private List<PotionEffect> potionEffects;  // Список эффектов

    public ItemFood(String name, String texture, int healAmount, float saturation, int maxStackSize, boolean isWolfFood) {
        super(healAmount, saturation, isWolfFood);
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
       // this.setCreativeTab(CreativeTabRegistry.tabMBOfoods);
        this.maxStackSize = maxStackSize;
        potionEffects = new ArrayList<>();  // Инициализация списка эффектов
        GameRegistry.registerItem(this, name);
    }

    // Метод для добавления эффекта
    public ItemFood addPotionEffect(int potionId, int duration, int amplifier) {
        PotionEffect effect = new PotionEffect(potionId, duration, amplifier);
        potionEffects.add(effect);  // Добавляем эффект в список
        return this;
    }

    // Применение эффектов при съедании пищи
    @Override
    public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
        super.onEaten(stack, world, player);

        if (!world.isRemote) {
            // Применяем все эффекты из списка
            for (PotionEffect effect : potionEffects) {
                player.addPotionEffect(effect);
            }
        }
        return stack;
    }
}
