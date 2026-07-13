package ru.givler.mbo.integration.thaumcraft.item.focus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import ru.givler.mbo.registry.CreativeTabRegistry;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.wands.FocusUpgradeType;
import thaumcraft.api.wands.ItemFocusBasic;
import thaumcraft.common.items.wands.ItemWandCasting;

import thaumicdyes.common.lib.GazeEffectHandler;

public class ItemFocusDarkLightning extends ItemFocusPartyBasic {

    private static final float BASE_DAMAGE = 5.0F;
    private static final double RANGE = GazeEffectHandler.DEFAULT_RANGE;
    private static final int STRIKE_INTERVAL_TICKS = 10;

    private static final AspectList COST = new AspectList().add(Aspect.ENTROPY, 25);
    private static final AspectList COST_CORROSIVE = COST.copy().add(Aspect.WATER, 50);
    private static final AspectList COST_TRIPLE = COST.copy().add(Aspect.ENTROPY, 60);
    private static final AspectList COST_CORROSIVE_TRIPLE = COST_CORROSIVE.copy().add(Aspect.AIR    , 60);

    public ItemFocusDarkLightning () {
        setCreativeTab(CreativeTabRegistry.tabMBOitems);
        setUnlocalizedName("focusDarkLightning");
    }

    @Override
    @SideOnly (Side.CLIENT)
    public void registerIcons (final IIconRegister ir) {
        icon = ir.registerIcon("mbo:focus_darklightning");
    }

    @Override
    public String getSortingHelper (final ItemStack stack) {
        return "ELDRITCH" + super.getSortingHelper(stack);
    }

    @Override
    public int getFocusColor (final ItemStack stack) {
        return 0x1a002b;
    }

    private int getMaxTargets (final ItemStack stack) {
        return isUpgradedWith(stack, TMFocusUpgrades.tripleEye) ? 3 : 1;
    }

    private boolean canApplyDebuff (final ItemStack stack) {
        return isUpgradedWith(stack, TMFocusUpgrades.corrosive);
    }

    @Override
    public AspectList getVisCost (final ItemStack stack) {
        final boolean corrosive = isUpgradedWith(stack, TMFocusUpgrades.corrosive);
        final boolean triple = isUpgradedWith(stack, TMFocusUpgrades.tripleEye);

        if (corrosive && triple) {
            return COST_CORROSIVE_TRIPLE;
        }
        if (corrosive) {
            return COST_CORROSIVE;
        }
        if (triple) {
            return COST_TRIPLE;
        }
        return COST;
    }

    @Override
    public boolean isVisCostPerTick (final ItemStack stack) {
        return true;
    }

    @Override
    public int getActivationCooldown (final ItemStack stack) {
        return -1;
    }

    @Override
    public ItemFocusBasic.WandFocusAnimation getAnimation (final ItemStack stack) {
        return ItemFocusBasic.WandFocusAnimation.CHARGE;
    }

    @Override
    public ItemStack onFocusRightClick (final ItemStack stack, final World world, final EntityPlayer player,
                                        final MovingObjectPosition mop) {
        player.setItemInUse(stack, 2147483647);
        return stack;
    }

    @Override
    public void onUsingFocusTick (final ItemStack stack, final EntityPlayer player, final int count) {
        if (player.worldObj.isRemote || !(player instanceof EntityPlayerMP)) {
            return;
        }

        final EntityPlayerMP playerMP = (EntityPlayerMP) player;
        final ItemWandCasting wand = (ItemWandCasting) stack.getItem();
        final ItemStack focusItem = wand.getFocusItem(stack);

        if (count % STRIKE_INTERVAL_TICKS != 0) {
            return;
        }

        if (!wand.consumeAllVis(stack, player, getVisCost(focusItem), true, false)) {
            player.stopUsingItem();
            return;
        }

        final boolean applyDebuff = canApplyDebuff(focusItem);
        final int maxTargets = getMaxTargets(focusItem);

        final List<EntityLivingBase> targets = findValidTargets(playerMP, RANGE, maxTargets);
        if (targets.isEmpty()) {
            return;
        }

        final float damage = BASE_DAMAGE + wand.getFocusPotency(stack);
        for (final EntityLivingBase target : targets) {
            final boolean hitLanded = GazeEffectHandler.dealDamage(playerMP, target, damage);
            if (hitLanded && applyDebuff) {
                GazeEffectHandler.applyDebuffEffect(target, player.worldObj.rand);
            }
        }

        player.worldObj.playSoundAtEntity(player, "thaumcraft:brain", 0.4F,
                0.8F + player.worldObj.rand.nextFloat() * 0.2F);
    }

    @SuppressWarnings ("unchecked")
    private List<EntityLivingBase> findValidTargets (final EntityPlayerMP player, final double range, final int maxTargets) {
        final AxisAlignedBB box = player.boundingBox.expand(range, range, range);
        final List<Entity> candidates = player.worldObj.getEntitiesWithinAABB(Entity.class, box);

        final List<EntityLivingBase> valid = new ArrayList<EntityLivingBase>();
        for (final Entity candidate : candidates) {
            if (GazeEffectHandler.isValidTargetIgnoringLookCone(player, candidate, range)) {
                valid.add((EntityLivingBase) candidate);
            }
        }

        if (valid.size() > 1) {
            final Vec3 look = player.getLookVec().normalize();
            Collections.sort(valid, new Comparator<EntityLivingBase>() {
                @Override
                public int compare (final EntityLivingBase a, final EntityLivingBase b) {
                    return Double.compare(angleCos(player, look, b), angleCos(player, look, a));
                }
            });
        }

        if (valid.size() > maxTargets) {
            return valid.subList(0, maxTargets);
        }
        return valid;
    }

    private static double angleCos (final EntityPlayerMP player, final Vec3 look, final Entity target) {
        double dx = target.posX - player.posX;
        double dy = (target.boundingBox.minY + target.height / 2.0D) - (player.posY + player.getEyeHeight());
        double dz = target.posZ - player.posZ;

        final double lengthSq = dx * dx + dy * dy + dz * dz;
        if (lengthSq < 1.0E-4D) {
            return 1.0D;
        }

        final double length = Math.sqrt(lengthSq);
        dx /= length;
        dy /= length;
        dz /= length;

        return look.xCoord * dx + look.yCoord * dy + look.zCoord * dz;
    }

    @Override
    public FocusUpgradeType[] getPossibleUpgradesByRank (final ItemStack stack, final int rank) {
        switch (rank) {
            case 1 :
                return new FocusUpgradeType[]{ FocusUpgradeType.frugal, FocusUpgradeType.potency };
            case 2 :
                return new FocusUpgradeType[]{ FocusUpgradeType.frugal, FocusUpgradeType.potency };
            case 3 :
                return new FocusUpgradeType[]{ FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        TMFocusUpgrades.corrosive_darklight };
            case 4 :
                return new FocusUpgradeType[]{ FocusUpgradeType.frugal, FocusUpgradeType.potency };
            case 5 :
                return new FocusUpgradeType[]{ FocusUpgradeType.frugal, FocusUpgradeType.potency,
                        TMFocusUpgrades.tripleEye };
        }
        return null;
    }
}