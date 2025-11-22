package ru.givler.mbo.item.glyph;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.MoreBeyondOrdinary;
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

        if (world.isRemote) {
            for (int i = 0; i < 20; i++) {
                world.spawnParticle("heart",
                        player.posX + (world.rand.nextDouble() - 0.5) * 2.0,
                        player.posY + (world.rand.nextDouble() * 0.5) * -1.5,
                        player.posZ + (world.rand.nextDouble() - 0.5) * 2.0,
                        0.0, 0.1, 0.0);
            }
        }

        world.playSoundAtEntity(player, "mbo:heal", 1.0F, 1.0F);
        player.swingItem();
        itemStack.damageItem(1, player);

        return itemStack;
    }

    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.rare;
    }
}
