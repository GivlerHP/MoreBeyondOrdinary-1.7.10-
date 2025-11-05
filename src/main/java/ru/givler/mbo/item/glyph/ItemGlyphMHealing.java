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

import java.util.List;

public class ItemGlyphMHealing extends ItemGlyphBasic {

    public ItemGlyphMHealing(String name, String texture, int maxStackSize) {
        super(name, texture, maxStackSize);
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(240);
        this.maxStackSize = maxStackSize;
        GameRegistry.registerItem(this, name);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        super.onUpdate(stack, world, entity, par4, par5);

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack equipped = player.getCurrentEquippedItem();

            if (equipped == stack) {
                // Регенерация для игроков в радиусе
                if (!world.isRemote) {
                    double radius = 8.0D;
                    List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class,
                            player.boundingBox.expand(radius, radius, radius));

                    for (EntityPlayer target : players) {

                        if (player.getDistanceToEntity(target) <= radius && target.getHealth() < target.getMaxHealth() && player.worldObj.getTotalWorldTime() % 60 == 0) {
                            target.heal(1.0F);
                        }
                        if (player.getDistanceToEntity(target) <= radius) {
                            target.addPotionEffect(new PotionEffect(Potion.regeneration.id, 2,0));
                        }
                    }

                    // Применяем ослабление (статусы) игроку
                    player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 0));
                    player.addPotionEffect(new PotionEffect(Potion.weakness.id, 200, 2));
                }

                // Частицы только на клиенте
                if (world.isRemote && world.rand.nextInt(6) == 0) { // 1 из 6 шанса каждый тик (~3 раза в секунду)
                    for (int i = 0; i < 2; i++) { // Генерируем 2 частицы
                        double angle = world.rand.nextDouble() * Math.PI * 2; // Угол по оси Y
                        double radius = 8.0;
                        double xOffset = Math.cos(angle) * radius * (world.rand.nextDouble() - 0.5) * 2; // Распределение по оси X
                        double yOffset = world.rand.nextDouble() * 1.0 * -1.5; // Распределение по оси Y
                        double zOffset = Math.sin(angle) * radius * (world.rand.nextDouble() - 0.5) * 2; // Распределение по оси Z

                        world.spawnParticle("heart",
                                player.posX + xOffset,
                                player.posY + yOffset,
                                player.posZ + zOffset,
                                0.0, 0.1, 0.0);
                    }
                }

                // Уникальное время для таймера из NBT
                if (!stack.hasTagCompound()) {
                    stack.setTagCompound(new NBTTagCompound());
                }

                long lastTimeChecked = stack.getTagCompound().getLong("lastTimeChecked");

                if (!world.isRemote) {
                    long currentTimeMillis = System.currentTimeMillis();

                    // Уменьшаем интервал только для урона предмета
                    if (currentTimeMillis - lastTimeChecked >= 1000) {
                        stack.getTagCompound().setLong("lastTimeChecked", currentTimeMillis);

                        if (stack.getItemDamage() < stack.getMaxDamage()) {
                            stack.setItemDamage(stack.getItemDamage() + 1);
                        }

                        if (stack.getItemDamage() >= stack.getMaxDamage() - 1) {
                            player.setCurrentItemOrArmor(0, null); // Удаляет предмет из руки
                        }
                    }
                }
            }
        }
    }
}
