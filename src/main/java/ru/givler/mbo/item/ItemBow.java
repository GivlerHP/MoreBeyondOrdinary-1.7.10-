package ru.givler.mbo.item;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemBow extends net.minecraft.item.ItemBow {
    private final float drawSpeed;
    private final float damageMultiplier;
    public IIcon[] icons;
    private final String texture;

    public ItemBow(String name, String texture, int maxDamage, float drawSpeed, float damageMultiplier) {
        this.setUnlocalizedName(name);
        this.texture = texture;
        this.setMaxDamage(maxDamage);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.drawSpeed = drawSpeed;
        this.damageMultiplier = damageMultiplier;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        icons = new IIcon[4];
        icons[0] = iconRegister.registerIcon(MoreBeyondOrdinary.MODID + ":weapon/" + texture + "_standart");
        for (int i = 1; i < 4; i++) {
            icons[i] = iconRegister.registerIcon(MoreBeyondOrdinary.MODID + ":weapon/" + texture + "_pulling_" + (i-1));
        }
    }

    public void register() {
        GameRegistry.registerItem(this, this.getUnlocalizedName().substring(5));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconIndex(ItemStack stack) {
        return icons[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining) {
        if (usingItem == null || !(usingItem.getItem() instanceof ItemBow)) {
            return icons[0];
        }

        int drawTime = usingItem.getMaxItemUseDuration() - useRemaining;
        float charge = drawTime / 20.0F;
        charge = (charge * charge + charge * 2.0F) / 3.0F;
        charge *= this.drawSpeed;

        // Ограничим максимум, как в onPlayerStoppedUsing
        if (charge > 1.0F) charge = 1.0F;

        // Анимационные пороги можно задать как доли натяжения
        if (charge > 0.9F) return icons[3];
        if (charge > 0.65F) return icons[2];
        if (charge > 0.25F) return icons[1];

        return icons[0];
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityPlayer player, int timeLeft) {
        boolean hasInfinite = player.capabilities.isCreativeMode ||
                EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, stack) > 0;

        // Проверяем наличие стрел
        if (hasInfinite || player.inventory.hasItem(Items.arrow)) {
            // Вычисляем силу натяжения
            int drawTime = this.getMaxItemUseDuration(stack) - timeLeft;
            float charge = drawTime / 20.0F;
            charge = (charge * charge + charge * 2.0F) / 3.0F;

            // Применяем скорость натяжения
            charge *= drawSpeed;

            if (charge < 0.1F) {
                return; // Слишком слабое натяжение
            }

            if (charge > 1.0F) {
                charge = 1.0F;
            }

            if (!world.isRemote) {
                EntityArrow arrow = new EntityArrow(world, player, charge * 2.0F);
                arrow.setDamage(arrow.getDamage() * damageMultiplier);

                if (charge >= 1.0F) {
                    arrow.setIsCritical(true);
                }

                int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
                if (powerLevel > 0) {
                    arrow.setDamage(arrow.getDamage() + powerLevel * 0.5D + 0.5D);
                }

                int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
                if (punchLevel > 0) {
                    arrow.setKnockbackStrength(punchLevel);
                }

                if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) > 0) {
                    arrow.setFire(100);
                }

                stack.damageItem(1, player);

                world.playSoundAtEntity(player, "random.bow", 1.0F,
                        1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + charge * 0.5F);

                world.spawnEntityInWorld(arrow);
            }

            if (!hasInfinite) {
                player.inventory.consumeInventoryItem(Items.arrow);
            }
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    public float getDrawSpeed() {
        return drawSpeed;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }
}