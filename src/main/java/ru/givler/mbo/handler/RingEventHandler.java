package ru.givler.mbo.handler;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import ru.givler.mbo.registry.ItemRegistry;

public class RingEventHandler {

    private static final int MUSHROOM_REGEN_DURATION_TICKS = 6 * 20;
    private static final int MUSHROOM_REGEN_AMPLIFIER = 0;

    private static Item mushroomSaladItem;
    private static boolean lookedUp = false;

    private static Item getMushroomSaladItem() {
        if (!lookedUp) {
            lookedUp = true;
            mushroomSaladItem = GameRegistry.findItem("BiomesOPlenty", "food");
        }
        return mushroomSaladItem;
    }

    @SubscribeEvent
    public void onItemUseFinish(PlayerUseItemEvent.Finish event) {
        EntityPlayer player = event.entityPlayer;
        if (player == null || player.worldObj.isRemote) return;

        ItemStack stack = event.item;
        if (stack == null || stack.getItem() == null) return;

        boolean isMushroomStew = stack.getItem() == Items.mushroom_stew;

        boolean isMushroomSalad = getMushroomSaladItem() != null
                && stack.getItem() == getMushroomSaladItem()
                && stack.getItemDamage() == 6;

        if (!isMushroomStew && !isMushroomSalad) return;

        if (hasMushroomRing(player)) {
            player.addPotionEffect(new PotionEffect(Potion.regeneration.id, MUSHROOM_REGEN_DURATION_TICKS, MUSHROOM_REGEN_AMPLIFIER));
        }
    }

    private boolean hasMushroomRing(EntityPlayer player) {
        for (int i = 0; i < BaublesApi.getBaubles(player).getSizeInventory(); i++) {
            ItemStack stack = BaublesApi.getBaubles(player).getStackInSlot(i);
            if (stack != null && stack.getItem() == ItemRegistry.MushroomRing) {
                return true;
            }
        }
        return false;
    }
}