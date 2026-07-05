package ru.givler.mbo.integration.thaumcraft.item.focus;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketSpawnParticle;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.registry.PotionRegistry;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.items.wands.WandManager;

import java.util.List;

import static thaumcraft.api.wands.FocusUpgradeType.enlarge;

public class ItemFocusMHealing extends ItemFocusPartyBasic {

    public ItemFocusMHealing() {
        this.maxStackSize = 1;
        this.canRepair = false;
        this.setMaxDamage(0);
        this.setUnlocalizedName("focusMHealing");
    }

    long soundDelay = 0L;

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        AspectList al = new AspectList();
        al.add(Aspect.EARTH, 10);
        al.add(Aspect.WATER, 10);
        al.add(Aspect.ORDER, 5);
        return al;
    }

    @Override
    public ItemStack onFocusRightClick(ItemStack wandstack, World world, EntityPlayer player, MovingObjectPosition mop) {
        if (!hasThaumcraftArmor(player)) {
            if (!world.isRemote) {
                player.addChatMessage(new ChatComponentText(
                        StatCollector.translateToLocal("focus.mhealing.no_armor")
                ));
            }
            return wandstack;
        }

        player.setItemInUse(wandstack, Integer.MAX_VALUE);
        WandManager.setCooldown(player, -1);
        return wandstack;
    }

    @Override
    public boolean isVisCostPerTick(ItemStack focusstack) {
        return true;
    }

    @Override
    public WandFocusAnimation getAnimation(ItemStack focusstack) {
        return WandFocusAnimation.CHARGE;
    }

    @Override
    public int getFocusColor(ItemStack focusstack) {
        return 0xFF44FF;
    }

    @Override
    public void onUsingFocusTick(ItemStack wandstack, EntityPlayer player, int count) {
        World world = player.worldObj;

        ItemWandCasting wand = (ItemWandCasting) wandstack.getItem();
        if (!wand.consumeAllVis(wandstack, player, this.getVisCost(wandstack), true, false)) {
            player.stopUsingItem();
            return;
        }

        if (world.isRemote) return;

        if (soundDelay < System.currentTimeMillis()) {
            world.playSoundAtEntity(player, "mbo:heal", 0.33F, 1.0F);
            soundDelay = System.currentTimeMillis() + 2000L;
        }

        int slowLevel = isUpgradedWith(wand.getFocusItem(wandstack), TMFocusUpgrades.spirit) ? 0 : 1;
        int weakLevel = isUpgradedWith(wand.getFocusItem(wandstack), TMFocusUpgrades.spirit) ? 1 : 2;
        player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, slowLevel));
        player.addPotionEffect(new PotionEffect(Potion.weakness.id, 200, weakLevel));

        List<EntityPlayer> targets = getPartyTargets(world, player, wandstack);

        for (EntityPlayer target : targets) {
            int regenLevel = isUpgradedWith(wand.getFocusItem(wandstack), TMFocusUpgrades.vitality) ? 1 : 0;
            target.addPotionEffect(new PotionEffect(Potion.regeneration.id, 60, regenLevel, true));

            if (world.getTotalWorldTime() % 60 == 0 && target.getHealth() < target.getMaxHealth()) {
                float healAmount = isUpgradedWith(wand.getFocusItem(wandstack), TMFocusUpgrades.vitality) ? 2.0F : 1.0F;
                target.heal(healAmount);
            }

            if (isUpgradedWith(wand.getFocusItem(wandstack), TMFocusUpgrades.inspiration)) {
                target.addPotionEffect(new PotionEffect(PotionRegistry.Stamina.id, 20, 0, true));
            }
        }

        if (world.rand.nextInt(6) == 0) {
            NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(
                    player.dimension,
                    player.posX, player.posY, player.posZ, 64.0
            );

            for (int i = 0; i < 2; i++) {
                double angle = world.rand.nextDouble() * Math.PI * 2;
                double r = getPartyRadius(wand.getFocusItem(wandstack));
                double xOffset = Math.cos(angle) * r * (world.rand.nextDouble() - 0.5) * 2;
                double yOffset = world.rand.nextDouble() * 1.0;
                double zOffset = Math.sin(angle) * r * (world.rand.nextDouble() - 0.5) * 2;

                PacketSpawnParticle pkt = new PacketSpawnParticle(
                        EnumParticleType.VANILLA_HEART,
                        player.posX + xOffset,
                        player.posY + yOffset,
                        player.posZ + zOffset,
                        0.0, 0.1, 0.0
                );
                PacketManager.INSTANCE.sendToAllAround(pkt, point);
                PacketManager.INSTANCE.sendTo(pkt, (EntityPlayerMP) player);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register) {
        this.icon = register.registerIcon("mbo:focus_mhealing");
    }

    @Override
    public void onPlayerStoppedUsingFocus(ItemStack wandstack, World world, EntityPlayer player, int count) {
        if (world.isRemote) return;

        List<EntityPlayer> targets = getPartyTargets(world, player, wandstack);
        for (EntityPlayer target : targets) {
            target.removePotionEffect(Potion.regeneration.id);
        }
    }

    @Override
    protected double getPartyRadius(ItemStack focusstack) {
        double radius = 6.0D;
        radius += getUpgradeLevel(focusstack, enlarge) * 2.0D;
        return radius;
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        switch (rank) {
            case 1: return new FocusUpgradeType[]{FocusUpgradeType.frugal};
            case 2: return new FocusUpgradeType[]{FocusUpgradeType.frugal, enlarge};
            case 3: return new FocusUpgradeType[]{FocusUpgradeType.frugal, enlarge};
            case 4: return new FocusUpgradeType[]{FocusUpgradeType.frugal, TMFocusUpgrades.spirit};
            case 5: return new FocusUpgradeType[]{FocusUpgradeType.frugal, TMFocusUpgrades.vitality, TMFocusUpgrades.inspiration};
            default: return null;
        }
    }
}