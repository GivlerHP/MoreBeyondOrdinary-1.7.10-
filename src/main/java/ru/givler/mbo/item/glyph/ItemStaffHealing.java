package ru.givler.mbo.item.glyph;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.packet.PacketSpawnParticle;
import ru.givler.mbo.registry.CreativeTabRegistry;

public class ItemStaffHealing extends ItemGlyphBasic {
    public ItemStaffHealing(String name, String texture, int maxStackSize) {
        super(name, texture, maxStackSize);
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setTextureName(MoreBeyondOrdinary.MODID + ":" + texture);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(2);
        this.maxStackSize = maxStackSize;
        GameRegistry.registerItem(this, name);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        player.heal(4.0F);

        world.playSoundAtEntity(player, "mbo:heal", 1.0F, 1.0F);
        player.swingItem();
        itemStack.damageItem(1, player);

        PacketSpawnParticle.send(EnumParticleType.VANILLA_HEART, world, player, 30, 2.0, 2.0, 2.0,1.0,   0.0, 0.0, 0.0);

        return itemStack;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) { }

    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.rare;
    }
}
