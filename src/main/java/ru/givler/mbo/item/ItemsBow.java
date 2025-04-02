package ru.givler.mbo.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import cpw.mods.fml.common.registry.GameRegistry;
import ru.givler.mbo.main;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemsBow extends ItemBow {
    private final float drawSpeed;
    private final float damageMultiplier;
    public IIcon[] icons;
    private final String texture;

    public ItemsBow(String name, String texture, int maxDamage, float drawSpeed, float damageMultiplier) {
        this.setUnlocalizedName(name);
        this.texture = texture;
        this.setMaxDamage(maxDamage);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.drawSpeed = drawSpeed;
        this.damageMultiplier = damageMultiplier;
    }



    @Override
    public void registerIcons(IIconRegister iconRegister) {
        icons = new IIcon[4];
        // Убедитесь, что текстура правильно загружается и имеет правильный путь
        icons[0] = iconRegister.registerIcon(main.MODID + ":" + texture + "_standart");
        for (int i = 1; i < 4; i++) {
            icons[i] = iconRegister.registerIcon(main.MODID + ":" + texture + "_pulling_" + (i-1));
        }
    }

    public void register() {
        GameRegistry.registerItem(this, this.getUnlocalizedName().substring(5));
    }

    @Override
    public IIcon getIconIndex(ItemStack stack) {
        return icons[0];  // Верните иконку для инвентаря (можно изменить логику, если нужно)
    }

    @Override
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining ) {
        // Если лук не используется, возвращаем иконку по умолчанию
        if (usingItem == null || !(usingItem.getItem() instanceof ItemsBow))
        {
            return icons[0];
        }

        // Логика для отображения иконки при натягивании лука
        int drawTime = usingItem.getMaxItemUseDuration() - useRemaining;
        if (drawTime >= 35) return icons[3];  // Лук сильно натянут
        if (drawTime >= 20) return icons[2];  // Лук средне натянут
        if (drawTime >= 0) return icons[1];   // Лук немного натянут


        return icons[0]; // Иконка по умолчанию

    }


    // Получение скорости натяжения тетивы
    public float getDrawSpeed() {
        return drawSpeed;
    }

    // Получение множителя урона
    public float getDamageMultiplier() {
        return damageMultiplier;
    }
}
