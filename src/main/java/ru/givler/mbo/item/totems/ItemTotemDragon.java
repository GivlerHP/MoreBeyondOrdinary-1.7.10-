package ru.givler.mbo.item.totems;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.main;
import ru.givler.mbo.registry.CreativeTabRegistry;

//класс для создания предметов
public class ItemTotemDragon extends Item {

    public ItemTotemDragon(String name, String texture, int maxStackSize) {
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setTextureName(main.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(360);
        this.maxStackSize = maxStackSize;
        this.setFull3D();
        GameRegistry.registerItem(this, name);
    }

    //при ударе этим предметом по существу, предмет потеряет прочность 1 ед.
    public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase player, EntityLivingBase Entity) {
        par1ItemStack.damageItem(1, Entity);
        return false;
    }

    //что будет происходить при ударе по существу
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return false;
    }

    //что будет происходить при нажатие правой кнопки.
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        // Применяем эффекты как на клиенте, так и на сервере
        player.addPotionEffect(new PotionEffect(Potion.weakness.id, 300, 2)); // Слабость (10 сек, уровень 1)
        player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 400, 0)); // Скорость (10 сек, уровень 2)


        // Создаём эффекты только на сервере
        if (world.isRemote) {
            for (int i = 0; i < 30; i++) {
                world.spawnParticle("flame",
                        player.posX + (world.rand.nextDouble() - 0.5) * 2.0,
                        player.posY + (world.rand.nextDouble() * 0.5) * -1.5,
                        player.posZ + (world.rand.nextDouble() - 0.5) * 2.0,
                        0.0, 0.1, 0.0);
            }

        }
        // Проигрываем звук НА СЕРВЕРЕ, чтобы он был слышен всем
        world.playSoundAtEntity(player, "mbo:bkdrattk", 1.0F, 1.0F);
        player.swingItem();
        itemStack.damageItem(50, player);

        return itemStack;
    }

    //что происходит если нажать правой кнопкой по существу. Например включается, отключается урон
    public boolean itemInteractionForEntity(ItemStack itemStack, EntityPlayer player, EntityLivingBase Entity) {
        return false;
    }

    //если у игрока в инвенторе этот предмет, тогда он получает бафф.

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        super.onUpdate(stack, world, entity, par4, par5);

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack equipped = player.getCurrentEquippedItem();

            if (equipped == stack) {
                player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 2, 0, true));

                // Используем уникальное время для каждого предмета
                if (!stack.hasTagCompound()) {
                    stack.setTagCompound(new NBTTagCompound());
                }

                long lastTimeChecked = stack.getTagCompound().getLong("lastTimeChecked");

                // Уменьшаем прочность каждую секунду
                if (!world.isRemote) {
                    long currentTimeMillis = System.currentTimeMillis();

                    // Если прошло больше 1000 миллисекунд (1 секунда)
                    if (currentTimeMillis - lastTimeChecked >= 1000) {
                        stack.getTagCompound().setLong("lastTimeChecked", currentTimeMillis); // Обновляем время последней проверки

                        if (stack.getItemDamage() < stack.getMaxDamage()) {
                            stack.setItemDamage(stack.getItemDamage() + 1); // Уменьшаем прочность на 1
                        }

                        // Если прочность предмета достигла максимального значения (предмет ломается)
                        if (stack.getItemDamage() >= stack.getMaxDamage() - 1) {
                            player.setCurrentItemOrArmor(0, null);  // Убирает сломанный предмет
                        }
                    }
                }
            }
        }
    }


    //если предмет скрафчен. Например пишет в чат.
    public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {

    }

    //редкость предмета
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.epic;
    }
}