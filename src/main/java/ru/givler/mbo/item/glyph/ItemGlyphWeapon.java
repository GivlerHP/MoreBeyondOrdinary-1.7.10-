package ru.givler.mbo.item.glyph;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.network.packet.PacketSpawnParticle;
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


        world.playSoundAtEntity(player, "mbo:blind", 1.0F, 1.0F);
        player.swingItem();

        if (!world.isRemote) {
            PacketSpawnParticle.send(EnumParticleType.DARK_MAGIC, world, player, 30, 2.0, 2.0, 2.0, 0.0, 0.0, 0.0, 0.0);
        }
        return itemStack;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {}
}