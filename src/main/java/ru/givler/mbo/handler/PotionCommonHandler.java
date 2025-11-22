package ru.givler.mbo.handler;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import minefantasy.mf2.block.list.BlockListMF;
import minefantasy.mf2.item.list.ComponentListMF;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import ru.givler.mbo.potion.PotionEnum;
import ru.givler.mbo.registry.PotionRegistry;

import java.util.Random;

public class PotionCommonHandler {

    /**
    Vulnerability и Vampirism
    */
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase target = event.entityLiving;
        float amount = event.ammount;

        if (target.isPotionActive(PotionEnum.VULNERABILITY)) {
            int amplifier = target.getActivePotionEffect(PotionEnum.VULNERABILITY).getAmplifier();
            float multiplier = 1.0F + 0.10F * (amplifier + 1);
            amount *= multiplier;
        }

        if (event.source.getEntity() instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) event.source.getEntity();

            if (attacker.isPotionActive(PotionEnum.VAMPIRISM)) {
                int level = attacker.getActivePotionEffect(PotionEnum.VAMPIRISM).getAmplifier();
                float vampirismPercent = 0.03F + (level * 0.015F);
                float healAmount = amount * vampirismPercent;

                if (attacker instanceof EntityPlayer) {
                    ((EntityPlayer) attacker).heal(healAmount);
                } else {
                    float currentHealth = attacker.getHealth();
                    attacker.setHealth(Math.min(currentHealth + healAmount, attacker.getMaxHealth()));
                }
            }
        }
        event.ammount = amount;
    }
    /**
    Phoenix
    */
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.entityLiving;

        if (!player.isPotionActive(PotionEnum.PHOENIX)) return;
        if (event.source == DamageSource.outOfWorld) return;

        event.setCanceled(true);

        PotionEffect pe = player.getActivePotionEffect(PotionEnum.PHOENIX);
        int level = pe.getAmplifier() + 1;

        float healAmount = Math.min(player.getMaxHealth(), 4.0F * level);

        player.addPotionEffect(new PotionEffect(Potion.resistance.id, 60, 4));
        player.setHealth(healAmount);
        player.removePotionEffect(PotionEnum.PHOENIX.id);
        player.worldObj.playSoundAtEntity(player, "mbo:resurect", 1.0F, 1.0F);
    }
    /**
     *Bashstun и hex
    */
    private static final Random rand = new Random();
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onEntityAttacked(LivingHurtEvent event) {
        if (!(event.source.getEntity() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.source.getEntity();
        EntityLivingBase target = event.entityLiving;

        if (player.isPotionActive(PotionEnum.APPLYSTUN)) {
            PotionEffect effect = player.getActivePotionEffect(PotionEnum.APPLYSTUN);
            if (effect != null) {
                int level = effect.getAmplifier() + 1;
                double chance = 0.1 * level;

                if (rand.nextDouble() <= chance) {
                    target.addPotionEffect(new PotionEffect(PotionRegistry.BashStun.id, 10, 0));
                }
            }
        }
        if (player.isPotionActive(PotionEnum.HEX)) {
            PotionEffect effect = player.getActivePotionEffect(PotionEnum.HEX);
            int level = effect.getAmplifier() + 1;

            double chance = 0.15 * level;
            if (rand.nextDouble() <= chance) {
                int randomEffect = rand.nextInt(6); // Выбираем случайный эффект

                switch (randomEffect) {
                    case 0:
                        target.addPotionEffect(new PotionEffect(Potion.weakness.id, 100, 0)); // Слабость
                        break;
                    case 1:
                        target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 100, 0)); // Замедление
                        break;
                    case 2:
                        target.addPotionEffect(new PotionEffect(Potion.blindness.id, 100, 0)); // Слепота
                        break;
                    case 3:
                        target.addPotionEffect(new PotionEffect(Potion.hunger.id, 100, 0)); // Голод
                        break;
                    case 4:
                        target.addPotionEffect(new PotionEffect(Potion.poison.id, 100, 0)); //Яд
                        break;
                    case 5:
                        target.addPotionEffect(new PotionEffect(PotionRegistry.DodgeHit.id, 100, 0)); //Отмена уклонения
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Dodge
     */
    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.entityLiving;

        if (!player.isPotionActive(PotionEnum.DODGE)) return;

        DamageSource source = event.source;
        if (
                source == DamageSource.fall ||
                        source == DamageSource.inFire ||
                        source == DamageSource.onFire ||
                        source == DamageSource.lava ||
                        source == DamageSource.magic ||
                        source == DamageSource.drown ||
                        source == DamageSource.starve  ||
                        player.isPotionActive(PotionRegistry.DodgeHit)
        ) {
            return;
        }

        PotionEffect effect = player.getActivePotionEffect(PotionEnum.DODGE);
        int level = effect.getAmplifier() + 1;
        double chance = 0.1 * level;

        if (player.worldObj.rand.nextDouble() < chance) {
            event.setCanceled(true);
            player.worldObj.playSoundAtEntity(player, "mbo:coldring", 1.0F, 1.0F);
        }
    }

    /**
     * Bushstun (блокирование движения)
     */
    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.entityLiving;
        World world = player.getEntityWorld();

        if (!player.isPotionActive(PotionEnum.BASH_STUN)) {
            return;
        }
        // --- Начало «жёсткого» сброса передвижения ---
        // 3) Запомним, где игрок находился до перерасчёта движения:
        double prevX = player.posX;
        double prevY = player.posY;
        double prevZ = player.posZ;

        player.moveForward = 0.0F;
        player.moveStrafing = 0.0F;

        player.motionX = 0.0;
        player.motionZ = 0.0;

        player.setSprinting(false);
        player.setJumping(false);

        player.velocityChanged = true;

        // 8) После всех внутриигровых расчётов (в том же тике),
        //    возвращаем игрока в исходную точку:
        //    поскольку LivingUpdateEvent вызывается до непосредственного
        //    применения движения, нам нужно «телепортировать» обратно.
        //    Однако если мы вызывать setPositionAndUpdate здесь,
        //    это пересинхронизирует позицию на клиенте/сервере
        if (player instanceof EntityPlayerMP) {
            ((EntityPlayerMP) player).playerNetServerHandler.setPlayerLocation(prevX, prevY, prevZ, player.rotationYaw, player.rotationPitch);
        }
        // --- Конец жёсткого сброса ---
    }

    /**
     * Bushstun и Disarm (Блокирование ЛКМ)
     */
    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player.isPotionActive(PotionEnum.BASH_STUN)) {
            event.setCanceled(true);
        }
        if (player.isPotionActive(PotionEnum.DISARM)) {
            event.setCanceled(true);
        }
    }

    /**
     * Bushstun (блокирование ПКМ)
     */
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        PlayerInteractEvent.Action action = event.action;

        if (player.isPotionActive(PotionEnum.BASH_STUN)) {
            if (action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * Thorns
     */
    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityLivingBase)) return;

        EntityLivingBase target = (EntityLivingBase) event.entityLiving;
        PotionEffect effect = target.getActivePotionEffect(PotionEnum.THORNS);

        if (effect != null && event.source.getEntity() instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) event.source.getEntity();

            float damageTaken = event.ammount;
            int level = effect.getAmplifier() + 1;
            float thornsDamage = damageTaken * 0.1F * level;
            if (thornsDamage > 0.1F) {
                attacker.attackEntityFrom(DamageSource.causeThornsDamage(target),thornsDamage );
            }
        }
    }
    /**
     * Luck
     */
    @SubscribeEvent
    public void additionalDropWithLuck(BlockEvent.HarvestDropsEvent event) {
        World world = event.world;
        Block block = event.block;
        EntityPlayer player = event.harvester;
        if (player != null) {

            if (player.isPotionActive(PotionEnum.LUCK)) {
                int scale = player.getActivePotionEffect(PotionEnum.LUCK).getAmplifier() == 0 ? 1 : 2;

                if (world.rand.nextInt(100) < 20 * scale) {
                    ItemStack bonus = null;

                    if (block == Blocks.coal_ore) {
                        bonus = new ItemStack(Items.coal, scale);
                    }
                    if (block == Blocks.lapis_ore) {
                        bonus = new ItemStack(Items.dye, scale, 4);
                    }
                    if (block == Blocks.diamond_ore) {
                        bonus = new ItemStack(Items.diamond, scale);
                    }
                    if (block == Blocks.emerald_ore) {
                        bonus = new ItemStack(Items.emerald, scale);
                    }
                    if (block == Blocks.redstone_ore) {
                        bonus = new ItemStack(Items.redstone, scale);
                    }
                    if(Loader.isModLoaded("minefantasy2")) {
                    if (block == BlockListMF.oreBorax) {
                        bonus = new ItemStack(ComponentListMF.flux_strong, scale);
                    }
                    if (block == BlockListMF.oreClay) {
                        bonus = new ItemStack(Items.clay_ball, scale);
                    }
                    if (block == BlockListMF.oreCoalRich) {
                        bonus = new ItemStack(Items.coal, 1 + scale);
                    }
                    if (block == BlockListMF.oreKaolinite) {
                        bonus = new ItemStack(ComponentListMF.kaolinite, scale);
                    }
                    if (block == BlockListMF.oreNitre) {
                        bonus = new ItemStack(ComponentListMF.nitre, scale);
                    }
                    if (block == BlockListMF.oreSulfur) {
                        bonus = new ItemStack(ComponentListMF.sulfur, scale);
                    }
                        if (block == BlockListMF.oreInferno) {
                            bonus = new ItemStack(ComponentListMF.inferno_crystal, scale);
                        }
                        if (block == BlockListMF.oreVoid) {
                            bonus = new ItemStack(ComponentListMF.void_crystal, scale);
                        }
                        if (block == BlockListMF.oreTear) {
                            bonus = new ItemStack(ComponentListMF.tear_crystal, scale);
                        }
                    }
                    if (bonus != null)
                        event.drops.add(bonus);
                }
            }
        }
    }
}