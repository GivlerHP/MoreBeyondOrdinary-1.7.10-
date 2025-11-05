package ru.givler.mbo.item.glyph;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.EnumParticleType;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.registry.CreativeTabRegistry;

import static ru.givler.mbo.registry.ItemRegistry.WeaponRapier;

public class ItemGlyphWeapon extends ItemGlyphBasic {

    public ItemGlyphWeapon(String name, String texture, int maxStackSize) {
        super(name, texture, maxStackSize);
        this.setUnlocalizedName(name);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(100);
        this.maxStackSize = maxStackSize;
        GameRegistry.registerItem(this, name);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            ItemStack summonedItem = new ItemStack(WeaponRapier, 1);
            summonedItem.addEnchantment(Enchantment.smite, 3);

            if (!player.inventory.addItemStackToInventory(summonedItem)) {
                player.dropPlayerItemWithRandomChoice(summonedItem, false);
            } else {
                player.inventory.markDirty();
            }
            itemStack.damageItem(10, player);
        }

        if (world.isRemote) {
            for (int i = 0; i < 30; i++) {
                MoreBeyondOrdinary.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world,
                        player.posX + (world.rand.nextDouble() - 0.5) * 2.0,
                        player.posY + (world.rand.nextDouble() * 0.5) * -1.5,
                        player.posZ + (world.rand.nextDouble() - 0.5) * 2.0,
                        0.0, 0.1, 0.0);
            }
        }
        world.playSoundAtEntity(player, "mbo:blind", 1.0F, 1.0F);
        player.swingItem();
        return itemStack;
    }
}