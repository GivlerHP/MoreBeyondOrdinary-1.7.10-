package ru.givler.mbo.item.glyph;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.EnumParticleType;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.network.packet.PacketSpawnParticle;
import ru.givler.mbo.registry.CreativeTabRegistry;

//класс для создания предметов
public class ItemGlyphHawk extends ItemGlyphBasic {

    public ItemGlyphHawk(String name, String texture, int maxStackSize) {
        super(name, texture, maxStackSize);
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setCreativeTab(CreativeTabRegistry.tabMBOitems);
        this.setMaxDamage(1200);
        this.maxStackSize = maxStackSize;
        GameRegistry.registerItem(this, name);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        player.addPotionEffect(new PotionEffect(Potion.weakness.id, 300, 2));
        player.addPotionEffect(new PotionEffect(Potion.hunger.id, 400, 1));
        player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 300, 1));
        player.addPotionEffect(new PotionEffect(Potion.jump.id, 300, 1));

        world.playSoundAtEntity(player, "mbo:tailwind", 1.0F, 1.0F);
        player.swingItem();
        itemStack.damageItem(50, player);

        PacketSpawnParticle.send(EnumParticleType.VANILLA_MAGICCRIT, world, player, 30, 2.0, 2.0, 2.0, 0.0,   0.0, 0.0, 0.0);

        return itemStack;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        super.onUpdate(stack, world, entity, par4, par5);

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack equipped = player.getCurrentEquippedItem();

            if (equipped == stack) {
                player.addPotionEffect(new PotionEffect(Potion.jump.id, 2, 0, true));
                player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, 2, 0, true));
            }

        }
    }
}