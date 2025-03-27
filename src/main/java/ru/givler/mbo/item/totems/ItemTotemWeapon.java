package ru.givler.mbo.item.totems;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import ru.givler.mbo.main;
import ru.givler.mbo.particles.ParticleDarkMagic;

import java.util.List;

import static ru.givler.mbo.registry.ItemRegistry.WeaponRapier;

public class ItemTotemWeapon extends Item {

    public ItemTotemWeapon(String name, String texture, int maxStackSize) {
        this.setUnlocalizedName(name);
        this.setTextureName(main.MODID + ":" + texture);
        this.setCreativeTab(main.tabMBOitems);
        this.setMaxDamage(100);
        this.maxStackSize = maxStackSize;
        this.setFull3D();
        GameRegistry.registerItem(this, name);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!world.isRemote) { // Делаем проверку, чтобы действие выполнялось только на сервере
            ItemStack summonedItem = new ItemStack(WeaponRapier, 1); // Призываем алмаз
            summonedItem.addEnchantment(Enchantment.smite, 3);

            if (!player.inventory.addItemStackToInventory(summonedItem)) {
                player.dropPlayerItemWithRandomChoice(summonedItem, false); // Если инвентарь полон, дропаем предмет
            } else {
                player.inventory.markDirty(); // Обновляем инвентарь
            }



            itemStack.damageItem(10, player); // Уменьшаем прочность предмета
        }

        if (world.isRemote) {
            for (int i = 0; i < 30; i++) {
                double offsetX = (world.rand.nextDouble() - 0.5) * 2.0;
                double offsetY = (world.rand.nextDouble() * 0.5) * -1.5;
                double offsetZ = (world.rand.nextDouble() - 0.5) * 2.0;

                // Добавляем кастомную частицу в эффекты
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDarkMagic(
                        world,
                        player.posX + offsetX,
                        player.posY + offsetY,
                        player.posZ + offsetZ,
                        0.0, 0.1, 0.0, 1.0f, 0.0f, 0.0f // Цвет и начальная скорость
                ));
            }

        }
        // Проигрываем звук НА СЕРВЕРЕ, чтобы он был слышен всем
        world.playSoundAtEntity(player, "mbo:shield", 1.0F, 1.0F);

        return itemStack;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        list.add(EnumChatFormatting.AQUA + "При нажатии ПКМ призывает предмет!");
    }

    @Override
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.rare;
    }
}