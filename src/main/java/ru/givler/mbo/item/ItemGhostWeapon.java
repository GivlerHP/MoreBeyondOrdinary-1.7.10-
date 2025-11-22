package ru.givler.mbo.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemGhostWeapon extends ItemSword {
    private float scale;

    public ItemGhostWeapon(String name, String texture, ToolMaterial material, int maxDamage, int maxStackSize, float scale) {
        super(material);
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":weapon/" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(maxDamage);
        this.maxStackSize = maxStackSize;
        this.scale = scale;
        GameRegistry.registerItem(this, name);
    }

    // Методы для создания материалов
    public static ToolMaterial createMaterial(String name, int harvestLevel, int durability, float damage, float efficiency, int enchantability) {
        return EnumHelper.addToolMaterial(name, harvestLevel, durability, damage, efficiency, enchantability);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        super.onUpdate(stack, world, entity, par4, par5);

        if (!world.isRemote && entity instanceof EntityPlayer) { // Только на сервере и только для игроков
            EntityPlayer player = (EntityPlayer) entity;

                if (world.getWorldTime() % 20 == 0) { // Каждую секунду
                    if (stack.getItemDamage() < stack.getMaxDamage()) {
                        stack.setItemDamage(stack.getItemDamage() + 1); // Уменьшаем прочность
                    }

                    if (stack.getItemDamage() >= stack.getMaxDamage() - 1) {
                        // Убираем предмет из руки, если это тот предмет, который износился
                        if (player.getHeldItem() == stack) {
                            player.setCurrentItemOrArmor(0, null); // Убираем предмет из руки
                        }

                        // Убираем предмет из инвентаря, если он полностью износился
                        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                            if (player.inventory.getStackInSlot(i) == stack) {
                                player.inventory.setInventorySlotContents(i, null); // Убираем предмет из инвентаря
                                break; // Прерываем цикл, чтобы не искать больше
                            }
                        }
                    }
                }
            }
        }


    // Метод для получения масштаба
    public float getScale() {
        return scale;
    }
}
