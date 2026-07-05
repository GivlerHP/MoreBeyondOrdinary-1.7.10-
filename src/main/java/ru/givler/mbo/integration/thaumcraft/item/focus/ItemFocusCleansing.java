package ru.givler.mbo.integration.thaumcraft.item.focus;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
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

import java.util.ArrayList;
import java.util.List;

public class ItemFocusCleansing extends ItemFocusPartyBasic {

    private static int[] REMOVABLE_EFFECTS = null;

    private static int[] getRemovableEffects() {
        if (REMOVABLE_EFFECTS == null) {
            REMOVABLE_EFFECTS = new int[]{
                    Potion.invisibility.getId(),
                    Potion.poison.getId(),
                    Potion.wither.getId(),
                    Potion.confusion.getId(),
                    Potion.blindness.getId(),
                    PotionRegistry.Disarm.getId(),
                    PotionRegistry.DodgeHit.getId()
            };
        }
        return REMOVABLE_EFFECTS;
    }

    private boolean isRemovable(int potionID) {
        for (int id : getRemovableEffects()) {
            if (id == potionID) return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    private IIcon depthIcon;

    public ItemFocusCleansing() {
        this.maxStackSize = 1;
        this.canRepair = false;
        this.setMaxDamage(0);
        this.setUnlocalizedName("focusCleansing");
    }

    @Override
    public AspectList getVisCost(ItemStack focusstack) {
        return new AspectList()
                .add(Aspect.WATER, 250)
                .add(Aspect.AIR, 300)
                .add(Aspect.ORDER, 400);
    }

    @Override
    protected double getPartyRadius(ItemStack focusstack) {
        double radius = 6.0D;
        radius += getUpgradeLevel(focusstack, FocusUpgradeType.enlarge) * 2.0D;
        return radius;
    }

    @Override
    public int getActivationCooldown(ItemStack focusstack) {
        int cd = 40000;
        cd -= getUpgradeLevel(focusstack, TMFocusUpgrades.cooldown) * 10000;
        return Math.max(cd, 10000);
    }

    public IIcon getFocusDepthLayerIcon(ItemStack itemstack) {
        return this.depthIcon;
    }

    @Override
    public int getFocusColor(ItemStack focusstack) {
        return 0xF0E68C;
    }

    @Override
    public WandFocusAnimation getAnimation(ItemStack focusstack) {
        return WandFocusAnimation.WAVE;
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

        ItemWandCasting wand = (ItemWandCasting) wandstack.getItem();
        if (!wand.consumeAllVis(wandstack, player, this.getVisCost(wandstack), !world.isRemote, false)) {
            return wandstack;
        }

        if (!world.isRemote) {
            List<EntityPlayer> targets = getPartyTargets(world, player, wandstack);

            for (EntityPlayer target : targets) {
                List<PotionEffect> toRemove = new ArrayList<>();

                for (Object obj : target.getActivePotionEffects()) {
                    PotionEffect effect = (PotionEffect) obj;
                    if (isRemovable(effect.getPotionID())) {
                        toRemove.add(effect);
                    }
                }

                for (PotionEffect effect : toRemove) {
                    target.removePotionEffect(effect.getPotionID());
                }

                if (isUpgradedWith(wand.getFocusItem(wandstack), TMFocusUpgrades.divineProtection)) {
                    target.addPotionEffect(new PotionEffect(Potion.resistance.id, 100, 0));
                }

                PacketSpawnParticle.send(EnumParticleType.SACRED, world, target, 30, 2.0, 2.0, 2.0, 1.0, 0.0, 0.0, 0.0);
            }

            world.playSoundAtEntity(player, "mbo:temple", 1.0F, 1.0F);
            player.swingItem();
        }

        return wandstack;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister register) {
        this.icon = register.registerIcon("mbo:focus_cleasing");
        this.depthIcon = register.registerIcon("thaumcraft:focus_warding_depth");
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank(ItemStack focusstack, int rank) {
        switch (rank) {
            case 1: return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
            case 2: return new FocusUpgradeType[]{FocusUpgradeType.frugal, TMFocusUpgrades.cooldown};
            case 3: return new FocusUpgradeType[]{FocusUpgradeType.frugal, FocusUpgradeType.enlarge};
            case 4: return new FocusUpgradeType[]{FocusUpgradeType.frugal, TMFocusUpgrades.cooldown};
            case 5: return new FocusUpgradeType[]{FocusUpgradeType.frugal, TMFocusUpgrades.cooldown, TMFocusUpgrades.divineProtection};
            default: return null;
        }
    }
}