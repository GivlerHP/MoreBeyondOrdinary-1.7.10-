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

public class ItemGlyphMiner extends ItemGlyphBasic {

    public ItemGlyphMiner(String name, String texture, int maxStackSize) {
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
        player.addPotionEffect(new PotionEffect(Potion.digSpeed.id, 400, 0));
        player.addPotionEffect(new PotionEffect(Potion.hunger.id, 400, 1));

        world.playSoundAtEntity(player, "mbo:aura", 1.0F, 1.0F);
        player.swingItem();
        itemStack.damageItem(50, player);

        if (!world.isRemote) {
            PacketSpawnParticle.send(EnumParticleType.VANILLA_WITCH_MAGIC, world, player, 30, 2.0, 2.0, 2.0, 0.0, 0.0, 0.0, 0.0);
        }
        return itemStack;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) { }
}