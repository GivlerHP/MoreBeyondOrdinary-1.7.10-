package ru.givler.mbo.handler;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import ru.givler.mbo.item.belt.ItemFertilityBelt;
import ru.givler.mbo.registry.ItemRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BeltEventHandler {

    private static final Set<Block> CROPS = new HashSet<Block>() {{
        add(Blocks.wheat);
        add(Blocks.carrots);
        add(Blocks.potatoes);
        add(Blocks.nether_wart);
    }};

    @SubscribeEvent
    public void onHarvest(BlockEvent.HarvestDropsEvent event) {
        if (!(event.harvester instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.harvester;

        if (!isWearingFertilityBelt(player)) {
            return;
        }

        Block block = event.block;

        if (!CROPS.contains(block)) {
            return;
        }

        boolean isPotatoOrCarrot = block == Blocks.potatoes || block == Blocks.carrots;
        boolean dropIsSmall = false;

        if (isPotatoOrCarrot) {
            int totalCount = 0;
            for (ItemStack drop : event.drops) {
                if (drop != null) {
                    totalCount += drop.stackSize;
                }
            }
            if (totalCount <= 1) {
                dropIsSmall = true;
            }
        }

        if (dropIsSmall) {
            return;
        }

        List<ItemStack> extraDrops = new ArrayList<>();
        for (ItemStack drop : event.drops) {
            extraDrops.add(drop.copy());
        }
        event.drops.addAll(extraDrops);
    }


    private boolean isWearingFertilityBelt(EntityPlayer player) {
        for (int i = 0; i < BaublesApi.getBaubles(player).getSizeInventory(); i++) {
            ItemStack stack = BaublesApi.getBaubles(player).getStackInSlot(i);
            if (stack != null && stack.getItem() == ItemRegistry.FertilityBelt) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onFallDamage(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) return;
        if (!"fall".equals(event.source.damageType)) return;

        EntityPlayer player = (EntityPlayer) event.entityLiving;

        if (!hasFallBelt(player)) {
            return;
        } else {
            float original = event.ammount;
            System.out.println("Fall damage reduced: " + original + " -> " + event.ammount);
            event.ammount = original * 0.5F;

        }
    }

    private boolean hasFallBelt(EntityPlayer player) {
        for (int i = 0; i < BaublesApi.getBaubles(player).getSizeInventory(); i++) {
            ItemStack stack = BaublesApi.getBaubles(player).getStackInSlot(i);
            if (stack != null && stack.getItem() == ItemRegistry.FallBelt) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.entityPlayer;

        if (hasEfficiencyBelt(player)) {
            float originalSpeed = event.newSpeed;
            event.newSpeed = originalSpeed * 1.1F;
        }

        if (hasWaterMiningBelt(player)) {
            if (player.isInsideOfMaterial(Material.water)) {
                if (!player.onGround) {
                    event.newSpeed *= 5.0F;
                } else {
                    event.newSpeed *= 2.0F;
                }
            }
        }
    }

    private boolean hasEfficiencyBelt(EntityPlayer player) {
        for (int i = 0; i < BaublesApi.getBaubles(player).getSizeInventory(); i++) {
            ItemStack stack = BaublesApi.getBaubles(player).getStackInSlot(i);
            if (stack != null && stack.getItem() == ItemRegistry.MinerBelt) {
                return true;
            }
        }
        return false;
    }

    private boolean hasWaterMiningBelt(EntityPlayer player) {
        for (int i = 0; i < BaublesApi.getBaubles(player).getSizeInventory(); i++) {
            ItemStack stack = BaublesApi.getBaubles(player).getStackInSlot(i);
            if (stack != null && stack.getItem() == ItemRegistry.WaterminerBelt) {
                return true;
            }
        }
        return false;
    }


    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (event.player.worldObj.isRemote) return;

        EntityPlayer player = event.player;

        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();

        if (currentHealth >= maxHealth * 0.5F) return;


        if (player.ticksExisted % 20 != 0) return;

        for (int i = 0; i < BaublesApi.getBaubles(player).getSizeInventory(); i++) {
            ItemStack stack = BaublesApi.getBaubles(player).getStackInSlot(i);
            if (stack != null && stack.getItem() == ItemRegistry.KnightBelt) {
                player.addPotionEffect(new PotionEffect(Potion.resistance.id, 200, 0));
                player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0));
                player.worldObj.playSoundAtEntity(player, "random.break", 1.0F, 1.0F);
                BaublesApi.getBaubles(player).setInventorySlotContents(i, null);
                return;
            }
        }
    }
}