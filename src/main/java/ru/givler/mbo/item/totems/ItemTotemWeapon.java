package ru.givler.mbo.item.totems;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.EnumParticleType;
import ru.givler.mbo.main;
import ru.givler.mbo.registry.CreativeTabRegistry;

import static ru.givler.mbo.registry.ItemRegistry.WeaponRapier;

public class ItemTotemWeapon extends Item {

    public ItemTotemWeapon(String name, String texture, int maxStackSize) {
        this.setUnlocalizedName(name);
        this.setTextureName(main.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
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
                main.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world,
                        player.posX + (world.rand.nextDouble() - 0.5) * 2.0,
                        player.posY + (world.rand.nextDouble() * 0.5) * -1.5,
                        player.posZ + (world.rand.nextDouble() - 0.5) * 2.0,
                        0.0, 0.1, 0.0);
            }
        }
        // Проигрываем звук НА СЕРВЕРЕ, чтобы он был слышен всем
        world.playSoundAtEntity(player, "mbo:blind", 1.0F, 1.0F);

        return itemStack;
    }

    @Override
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.epic;
    }
}