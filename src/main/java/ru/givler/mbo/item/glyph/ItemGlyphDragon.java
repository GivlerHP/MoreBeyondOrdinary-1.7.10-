package ru.givler.mbo.item.glyph;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.network.packet.PacketSpawnParticle;
import ru.givler.mbo.registry.CreativeTabRegistry;


public class ItemGlyphDragon extends ItemGlyphBasic {

    public ItemGlyphDragon(String name, String texture, int maxStackSize) {
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
        player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 200, 0));

        world.playSoundAtEntity(player, "mbo:bkdrattk", 1.0F, 1.0F);
        player.swingItem();
        itemStack.damageItem(50, player);

        PacketSpawnParticle.send(EnumParticleType.VANILLA_FLAME, world, player, 30, 2.0, 2.0, 2.0, 0.0,   0.0, 0.0, 0.0);

        return itemStack;
    }


    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        super.onUpdate(stack, world, entity, par4, par5);

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack equipped = player.getCurrentEquippedItem();

            if (equipped == stack) {
                player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 2, 0, true));

            }
        }
    }
}