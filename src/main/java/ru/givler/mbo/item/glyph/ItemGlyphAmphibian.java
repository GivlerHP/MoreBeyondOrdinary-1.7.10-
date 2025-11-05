package ru.givler.mbo.item.glyph;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

//класс для создания предметов
public class ItemGlyphAmphibian extends ItemGlyphBasic {

    public ItemGlyphAmphibian(String name, String texture, int maxStackSize) {
        super(name, texture, maxStackSize);
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(360);
        this.maxStackSize = maxStackSize;
        GameRegistry.registerItem(this, name);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(Potion.weakness.id, 300, 2));
        player.addPotionEffect(new PotionEffect(Potion.waterBreathing.id, 400, 0));

        if (world.isRemote) {
            for (int i = 0; i < 30; i++) {
                world.spawnParticle("splash",
                        player.posX + (world.rand.nextDouble() - 0.5) * 2.0,
                        player.posY + (world.rand.nextDouble() * 0.5) * -1.5,
                        player.posZ + (world.rand.nextDouble() - 0.5) * 2.0,
                        0.0, 0.0, 0.0);
            }
            for (int i = 0; i < 30; i++) {
                world.spawnParticle("bubble",
                        player.posX + (world.rand.nextDouble() - 0.5) * 2.0,
                        player.posY + (world.rand.nextDouble() * 0.5) * -1.5,
                        player.posZ + (world.rand.nextDouble() - 0.5) * 2.0,
                        0.0, 0.0, 0.0);
            }


        }
        world.playSoundAtEntity(player, "mbo:bubble", 1.0F, 1.0F);
        player.swingItem();
        itemStack.damageItem(50, player);

        return itemStack;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        super.onUpdate(stack, world, entity, par4, par5);

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack equipped = player.getCurrentEquippedItem();

            if (equipped == stack) {
                player.addPotionEffect(new PotionEffect(Potion.waterBreathing.id, 2, 0, true));

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
}