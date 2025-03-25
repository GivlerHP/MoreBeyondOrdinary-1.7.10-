package ru.givler.mbo.item.totems;

import java.util.List;

import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import ru.givler.mbo.main;

//класс для создания предметов
public class ItemTotemMiner extends Item {

    boolean mode = false;

    public ItemTotemMiner(String name, String texture, int maxStackSize) {
        this.canRepair = false;
        this.setUnlocalizedName(name);
        this.setTextureName(main.MODID + ":" + texture);
        this.setCreativeTab(main.tabMBOitems);
        this.setMaxDamage(1200);
        this.maxStackSize = maxStackSize;
        this.setFull3D();
        GameRegistry.registerItem(this, name);
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
    }

    //при ударе этим предметом по существу, предмет потеряет прочность 1 ед.
    public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase player, EntityLivingBase Entity) {
        par1ItemStack.damageItem(1, Entity);
        return false;
    }

    //что будет происходить при ударе по существу
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        return false;
    }

    //что будет происходить при нажатие правой кнопки. Например включается полёт
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        // Применяем эффекты как на клиенте, так и на сервере
        player.addPotionEffect(new PotionEffect(Potion.digSpeed.id, 400, 0));
        player.addPotionEffect(new PotionEffect(Potion.hunger.id, 400, 2));


        // Создаём эффекты только на сервере
        if (world.isRemote) {
            for (int i = 0; i < 20; i++) {
                world.spawnParticle("witchMagic",
                        player.posX + (world.rand.nextDouble() - 0.5) * 2.0,
                        player.posY + (world.rand.nextDouble() * 0.5) * -1.5,
                        player.posZ + (world.rand.nextDouble() - 0.5) * 2.0,
                        0.0, 0.1, 0.0);
            }

        }
        // Проигрываем звук НА СЕРВЕРЕ, чтобы он был слышен всем
        world.playSoundAtEntity(player, "mbo:aura", 1.0F, 1.0F);

        itemStack.damageItem(50, player);

        return itemStack;
    }

    //что происходит если нажать правой кнопкой по существу. Например включается, отключается урон
    public boolean itemInteractionForEntity(ItemStack itemStack, EntityPlayer player, EntityLivingBase Entity) {
        return false;
    }


    //если предмет скрафчен. Например пишет в чат.
    public void onCreated(ItemStack itemStack, World world, EntityPlayer player) {

    }

    //редкость предмета
    public EnumRarity getRarity(ItemStack itemStack) {
        return EnumRarity.epic;
    }
}