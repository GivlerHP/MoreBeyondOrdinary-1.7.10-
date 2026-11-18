package ru.givler.mbo.integration.thaumcraft.item.focus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
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
    private static final double TARGET_CONE_COS = Math.cos(Math.toRadians(25.0D));
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

    @Override
    public short[] getAppliedUpgrades(ItemStack stack) {
        short[] upgrades = super.getAppliedUpgrades(stack);
        // Older configs used the Triple Eye ID for the rank-three Corrosive upgrade too.
        if (TMFocusUpgrades.tripleEye != null && TMFocusUpgrades.corrosive_darklight != null
                && upgrades[2] == TMFocusUpgrades.tripleEye.id) {
            upgrades[2] = TMFocusUpgrades.corrosive_darklight.id;
        }
        return upgrades;
    }

    private boolean canApplyDebuff (final ItemStack stack) {
        return isUpgradedWith(stack, TMFocusUpgrades.corrosive_darklight);
    }

    @Override
    public AspectList getVisCost (final ItemStack stack) {
        final boolean corrosive = canApplyDebuff(stack);
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
        final ItemWandCasting wand = (ItemWandCasting) stack.getItem();
        final ItemStack focusstack = wand.getFocusItem(stack);
        if (!canActivateFocus(focusstack, world, player, false)) {
            return stack;
        }
        player.setItemInUse(stack, 2147483647);
        return stack;
    }

    @Override
    public String getRequiredResearch(final ItemStack focusstack) {
        return "FOCUSSHOCK";
    }

    @Override
    public void applyFocusBacklash(final ItemStack focusstack, final World world, final EntityPlayer player) {
        if (world.isRemote) return;
        player.attackEntityFrom(DamageSource.magic, 6.0F);
        player.addPotionEffect(new PotionEffect(Potion.weakness.id, 160, 1));
    }

    @Override
    public void onUsingFocusTick (final ItemStack stack, final EntityPlayer player, final int count) {
        if (player.worldObj.isRemote || !(player instanceof EntityPlayerMP)) {
            return;
        }

        final EntityPlayerMP playerMP = (EntityPlayerMP) player;
        final ItemWandCasting wand = (ItemWandCasting) stack.getItem();
        final ItemStack focusItem = wand.getFocusItem(stack);

        if (focusItem == null || count % STRIKE_INTERVAL_TICKS != 0) {
            return;
        }

        final boolean applyDebuff = canApplyDebuff(focusItem);
        final int maxTargets = getMaxTargets(focusItem);
        final boolean trace = Boolean.TRUE.equals(Launch.blackboard.get("fml.deobfuscatedEnvironment"));

        final List<EntityLivingBase> targets = findValidTargets(playerMP, RANGE);
        if (trace) {
            org.apache.logging.log4j.LogManager.getLogger("MBO").info(
                    "[Triple Eye] upgrades={}, limit={}, eligible={}",
                    java.util.Arrays.toString(getAppliedUpgrades(focusItem)), maxTargets, targets.size());
        }
        if (targets.isEmpty()) {
            return;
        }
        if (!wand.consumeAllVis(stack, player, getVisCost(focusItem), true, false)) {
            player.stopUsingItem();
            return;
        }

        final float damage = BASE_DAMAGE + wand.getFocusPotency(stack);
        int hits = 0;
        for (final EntityLivingBase target : targets) {
            float before = trace ? target.getHealth() : 0F;
            boolean landed = GazeEffectHandler.dealDamage(playerMP, target, damage, true);
            if (trace) {
                org.apache.logging.log4j.LogManager.getLogger("MBO").info(
                        "[Triple Eye] target={} type={} accepted={} health={}->{}",
                        target.getEntityId(), target.getClass().getSimpleName(), landed, before, target.getHealth());
            }
            if (!landed) continue;
            if (applyDebuff) {
                GazeEffectHandler.applyDebuffEffect(target, player.worldObj.rand);
            }
            if (++hits == maxTargets) break;
        }

        if (hits > 0) {
            player.worldObj.playSoundAtEntity(player, "thaumcraft:brain", 0.4F,
                    0.8F + player.worldObj.rand.nextFloat() * 0.2F);
        }
    }

    @SuppressWarnings ("unchecked")
    private List<EntityLivingBase> findValidTargets (final EntityPlayerMP player, final double range) {
        final AxisAlignedBB box = player.boundingBox.expand(range, range, range);
        final List<EntityLivingBase> candidates = player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);
        final Vec3 look = player.getLookVec().normalize();

        final List<EntityLivingBase> valid = new ArrayList<EntityLivingBase>();
        for (final EntityLivingBase candidate : candidates) {
            if (angleCos(player, look, candidate) >= TARGET_CONE_COS
                    && GazeEffectHandler.isValidTargetIgnoringLookCone(player, candidate, range)) {
                valid.add(candidate);
            }
        }

        valid.sort(Comparator.<EntityLivingBase>comparingDouble(target -> angleCos(player, look, target))
                .reversed().thenComparingDouble(player::getDistanceSqToEntity));

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
