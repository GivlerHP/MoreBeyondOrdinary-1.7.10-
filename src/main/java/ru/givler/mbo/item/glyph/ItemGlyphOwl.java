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

public class ItemGlyphOwl extends ItemGlyphBasic {

    public ItemGlyphOwl(String name, String texture, int maxStackSize) {
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
        player.addPotionEffect(new PotionEffect(Potion.nightVision.id, 400, 1));


        if (world.isRemote) {
            for (int i = 0; i < 20; i++) {
                world.spawnParticle("witchMagic",
                        player.posX + (world.rand.nextDouble() - 0.5) * 2.0,
                        player.posY + (world.rand.nextDouble() * 0.5) * -1.5,
                        player.posZ + (world.rand.nextDouble() - 0.5) * 2.0,
                        0.0, 0.1, 0.0);
            }
        }
        world.playSoundAtEntity(player, "mbo:darkaura", 1.0F, 1.0F);
        player.swingItem();
        itemStack.damageItem(50, player);

        return itemStack;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int par4, boolean par5) {
        super.onUpdate(stack, world, entity, par4, par5);

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack equipped = player.getCurrentEquippedItem();

            if (equipped == stack) {
                player.addPotionEffect(new PotionEffect(Potion.nightVision.id, 2, 0, true));
                if (!stack.hasTagCompound()) {
                    stack.setTagCompound(new NBTTagCompound());
                }
                long lastTimeChecked = stack.getTagCompound().getLong("lastTimeChecked");
                if (!world.isRemote) {
                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - lastTimeChecked >= 1000) {
                        stack.getTagCompound().setLong("lastTimeChecked", currentTimeMillis);

                        if (stack.getItemDamage() < stack.getMaxDamage()) {
                            stack.setItemDamage(stack.getItemDamage() + 1);
                        }
                        if (stack.getItemDamage() >= stack.getMaxDamage() - 1) {
                            player.setCurrentItemOrArmor(0, null);
                        }
                    }
                }
            }
        }
    }
}