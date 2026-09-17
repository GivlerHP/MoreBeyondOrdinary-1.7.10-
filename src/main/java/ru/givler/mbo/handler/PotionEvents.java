package ru.givler.mbo.handler;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import ru.givler.mbo.potion.PotionEnum;
import ru.givler.mbo.potion.Fear;
import ru.givler.mbo.potion.MindControl;
import ru.givler.mbo.registry.PotionRegistry;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.particles.EnumParticleType;
import ru.givler.mbo.particles.ParticleSettings;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.magic.PacketStaticAuraImpact;
import ru.givler.mbo.potion.SyncedPotionEffects;
import cpw.mods.fml.common.network.NetworkRegistry;
import ru.givler.mbo.item.magic.ItemSpellScroll;
import ru.givler.mbo.magic.registry.MagicSpells;

import java.util.Random;
import java.util.List;

public class PotionEvents {

    /**
    Vulnerability и Vampirism
    */
    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase target = event.entityLiving;
        if (target.worldObj.isRemote) return;
        float amount = event.ammount;

        if (target instanceof EntityPlayer && ((EntityPlayer) target).isUsingItem()) {
            ItemStack used = ((EntityPlayer) target).getItemInUse();
            if (used != null && used.getItem() instanceof ItemSpellScroll
                    && ((ItemSpellScroll) used.getItem()).getSpell(used) == MagicSpells.SHADOW_WARD) {
                Entity attacker = event.source.getEntity();
                if (attacker instanceof EntityLivingBase && attacker != target) {
                    ((EntityLivingBase) attacker).attackEntityFrom(DamageSource.magic, amount * 0.5F);
                }
                amount *= 0.5F;
            }
        }

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
        if (event.entityLiving.worldObj.isRemote) return;
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
        if (event.entityLiving.worldObj.isRemote) return;
        if (!(event.source.getEntity() instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.source.getEntity();
        EntityLivingBase target = event.entityLiving;

        if (player.isPotionActive(PotionEnum.APPLYSTUN)) {
            PotionEffect effect = player.getActivePotionEffect(PotionEnum.APPLYSTUN);
            if (effect != null) {
                int level = effect.getAmplifier() + 1;
                double chance = Math.min(1.0D, 0.1D * level);

                if (rand.nextDouble() <= chance) {
                    target.addPotionEffect(new PotionEffect(PotionRegistry.BashStun.id, 10, 0));
                }
            }
        }
        if (player.isPotionActive(PotionEnum.HEX)) {
            PotionEffect effect = player.getActivePotionEffect(PotionEnum.HEX);
            int level = effect.getAmplifier() + 1;

            double chance = Math.min(1.0D, 0.15D * level);
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
        if (event.entityLiving.worldObj.isRemote) return;
        if (event.entityLiving.isPotionActive(PotionRegistry.Transience)
                && event.source != null && !event.source.isUnblockable()) {
            event.setCanceled(true);
            return;
        }
        if (event.source != null && event.source.getEntity() instanceof EntityLivingBase) {
            EntityLivingBase attacker = (EntityLivingBase) event.source.getEntity();
            EntityLivingBase victim = event.entityLiving;
            if (victim.isPotionActive(PotionRegistry.MindTrick)) {
                victim.removePotionEffect(PotionRegistry.MindTrick.id);
            }
            if (!event.source.isProjectile() && victim.isPotionActive(PotionRegistry.Fireskin)) {
                attacker.setFire(5);
            }
            if (!event.source.isProjectile() && victim.isPotionActive(PotionRegistry.IceShroud)) {
                SyncedPotionEffects.apply(attacker,
                        new PotionEffect(PotionRegistry.Frost.id, 100, 0, true));
            }
            if (!event.source.isProjectile() && victim.isPotionActive(PotionRegistry.StaticAura)
                    && !"mbo.staticAura".equals(event.source.getDamageType())) {
                attacker.attackEntityFrom(new EntityDamageSource("mbo.staticAura", victim).setMagicDamage(), 4.0F);
                PacketManager.INSTANCE.sendToAllAround(new PacketStaticAuraImpact(attacker),
                        new NetworkRegistry.TargetPoint(attacker.dimension,
                                attacker.posX, attacker.posY, attacker.posZ, 64.0D));
                victim.worldObj.playSoundAtEntity(attacker, "mbo:arc", 1.0F,
                        victim.worldObj.rand.nextFloat() * 0.4F + 1.5F);
            }
            if (attacker.isPotionActive(PotionRegistry.Transience)) {
                event.setCanceled(true);
                return;
            }
        }
        if (!(event.entityLiving instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.entityLiving;
        if (!player.isPotionActive(PotionEnum.DODGE)) return;

        DamageSource source = event.source;

        boolean isPotionDamage = (source == DamageSource.magic) &&
                (player.isPotionActive(Potion.poison) ||
                        player.isPotionActive(Potion.wither));

        if (source == DamageSource.fall ||
                source == DamageSource.inFire ||
                source == DamageSource.onFire ||
                source == DamageSource.lava ||
                source == DamageSource.drown ||
                source == DamageSource.starve ||
                source == DamageSource.wither ||
                source == DamageSource.inWall ||
                source == DamageSource.outOfWorld ||
                source.isDamageAbsolute() ||
                isPotionDamage ||
                player.isPotionActive(PotionRegistry.DodgeHit)) {
            return;
        }

        PotionEffect effect = player.getActivePotionEffect(PotionEnum.DODGE);
        int level = effect.getAmplifier() + 1;
        double chance = Math.min(1.0D, 0.1D * level);

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
        updateMagicEffects(event.entityLiving);
        if (!(event.entityLiving instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.entityLiving;
        if (!player.isPotionActive(PotionEnum.BASH_STUN)) {
            return;
        }
        // --- Начало «жёсткого» сброса передвижения ---
        // 3) Запомним, где игрок находился до перерасчёта движения:
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
        if (event.entityLiving.worldObj.isRemote || "thorns".equals(event.source.getDamageType())) return;
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
        if (world.isRemote) return;
        Block block = event.block;
        EntityPlayer player = event.harvester;
        if (player != null) {

            if (player.isPotionActive(PotionEnum.LUCK)) {
                int scale = player.getActivePotionEffect(PotionEnum.LUCK).getAmplifier() + 1;

                if (world.rand.nextInt(100) < Math.min(100, 20 * scale)) {
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
                    if (Loader.isModLoaded("minefantasy2")) {
                        bonus = getMineFantasyBonus(block, scale);
                    }
                    if (bonus != null)
                        event.drops.add(bonus);
                }
            }
        }
    }

    private static void updateMagicEffects(EntityLivingBase entity) {
        World world = entity.worldObj;
        if (world.isRemote) {
            double x = entity.posX + (world.rand.nextDouble() - 0.5D) * entity.width;
            double y = entity.boundingBox.minY + world.rand.nextDouble() * entity.height;
            double z = entity.posZ + (world.rand.nextDouble() - 0.5D) * entity.width;
            if (entity.isPotionActive(PotionRegistry.Frost)) MoreBeyondOrdinary.proxy.spawnParticle(
                    EnumParticleType.SNOW, world, x, y, z, 0, -0.02D, 0,
                    new ParticleSettings(15 + world.rand.nextInt(5), 1, 1, 1, 0.6F, false));
            if (entity.isPotionActive(PotionRegistry.Fireskin)) MoreBeyondOrdinary.proxy.spawnParticle(
                    EnumParticleType.VANILLA_FLAME, world, x, y, z, 0, 0, 0);
            if (entity.isPotionActive(PotionRegistry.IceShroud)) {
                float brightness = 0.5F + world.rand.nextFloat() * 0.5F;
                MoreBeyondOrdinary.proxy.spawnParticle(EnumParticleType.SNOW,
                        world, x, y, z, 0, -0.02D, 0,
                        new ParticleSettings(40 + world.rand.nextInt(10), 1, 1, 1, 0.6F, false));
                MoreBeyondOrdinary.proxy.spawnParticle(EnumParticleType.SPARKLE,
                        world, x, y, z, 0, 0, 0,
                        new ParticleSettings(48 + world.rand.nextInt(12), brightness,
                                brightness + 0.1F, 1.0F, 0.75F, true));
            }
            if (entity.isPotionActive(PotionRegistry.StaticAura)) MoreBeyondOrdinary.proxy.spawnParticle(
                    EnumParticleType.SPARK, world, x, y, z, 0, 0, 0,
                    new ParticleSettings(3, 1, 1, 1, 1.4F, false));
            if (entity.isPotionActive(PotionRegistry.Transience)) MoreBeyondOrdinary.proxy.spawnParticle(
                    EnumParticleType.SPARKLE, world, x, y, z, 0, 0, 0,
                    new ParticleSettings(20, 0.8F, 0.8F, 1.0F, 0.6F, false));
            return;
        }
        if (entity.isPotionActive(PotionRegistry.Decay) && entity.onGround
                && entity.ticksExisted % 20 == 0) {
            boolean hasPatch = false;
            List nearby = world.getEntitiesWithinAABB(
                    ru.givler.mbo.entity.magic.EntityGroundMagicEffect.class,
                    entity.boundingBox.expand(0.25D, 0.25D, 0.25D));
            for (Object object : nearby) {
                if (((ru.givler.mbo.entity.magic.EntityGroundMagicEffect) object).kind()
                        == ru.givler.mbo.entity.magic.EntityGroundMagicEffect.Kind.DECAY) {
                    hasPatch = true;
                    break;
                }
            }
            if (!hasPatch) world.spawnEntityInWorld(
                    new ru.givler.mbo.entity.magic.EntityGroundMagicEffect(world,
                            entity.posX, entity.posY + 0.01D, entity.posZ, entity,
                            ru.givler.mbo.entity.magic.EntityGroundMagicEffect.Kind.DECAY,
                            400, 1.0F));
        }
        if (entity instanceof EntityLiving && entity.isPotionActive(PotionRegistry.MindTrick)) {
            ((EntityLiving) entity).setAttackTarget(null);
            if (entity instanceof EntityCreature) ((EntityCreature) entity).setTarget(null);
        }
        if (entity instanceof EntityLiving && entity.isPotionActive(PotionRegistry.MindControl)) {
            controlMind((EntityLiving) entity);
        }
        if (entity instanceof EntityCreature && entity.isPotionActive(PotionRegistry.Fear)) {
            runAway((EntityCreature) entity, Fear.getSource(entity));
        }
    }

    @SubscribeEvent
    public void onMagicBreakSpeed(BreakSpeed event) {
        PotionEffect frost = event.entityPlayer.getActivePotionEffect(PotionRegistry.Frost);
        if (frost != null) {
            event.newSpeed = Math.max(0.0F,
                    event.originalSpeed * (1.0F - 0.5F * (frost.getAmplifier() + 1)));
        }
    }

    @SubscribeEvent
    public void onMagicBlockPlace(BlockEvent.PlaceEvent event) {
        if (event.player.isPotionActive(PotionRegistry.Transience)) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onMagicBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer().isPotionActive(PotionRegistry.Transience)) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onMagicTarget(LivingSetAttackTargetEvent event) {
        if (!(event.entityLiving instanceof EntityLiving)) return;
        if ((event.entityLiving.isPotionActive(PotionRegistry.MindTrick)
                || event.entityLiving.isPotionActive(PotionRegistry.Fear)) && event.target != null) {
            ((EntityLiving) event.entityLiving).setAttackTarget(null);
        } else if (event.entityLiving.isPotionActive(PotionRegistry.MindControl)) {
            controlMind((EntityLiving) event.entityLiving);
        }
    }

    private static void controlMind(EntityLiving controlled) {
        EntityLivingBase owner = MindControl.getController(controlled);
        if (owner == null) {
            controlled.setAttackTarget(null);
            return;
        }
        double range = controlled.getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue();
        List nearby = controlled.worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
                controlled.boundingBox.expand(range, range, range));
        EntityLivingBase closest = null;
        for (Object object : nearby) {
            EntityLivingBase candidate = (EntityLivingBase) object;
            if (candidate == controlled || candidate == owner || !candidate.isEntityAlive()) continue;
            if (closest == null || controlled.getDistanceSqToEntity(candidate)
                    < controlled.getDistanceSqToEntity(closest)) closest = candidate;
        }
        controlled.setAttackTarget(closest);
        if (controlled instanceof EntityCreature) ((EntityCreature) controlled).setTarget(closest);
    }

    private static void runAway(EntityCreature creature, EntityLivingBase feared) {
        if (feared == null || creature.getDistanceToEntity(feared) >= 16.0F) return;
        Vec3 position = RandomPositionGenerator.findRandomTargetBlockAwayFrom(
                creature, 16, 7, Vec3.createVectorHelper(feared.posX, feared.posY, feared.posZ));
        creature.setAttackTarget(null);
        creature.setTarget(null);
        if (position != null && creature.getNavigator().noPath()) {
            creature.getNavigator().tryMoveToXYZ(position.xCoord, position.yCoord, position.zCoord, 1.25D);
        }
    }

    private static ItemStack getMineFantasyBonus(Block block, int amount) {
        try {
            Class<?> bridge = Class.forName(
                    "ru.givler.mbo.integration.minefantasy2.MineFantasyLuckDrops");
            return (ItemStack) bridge.getMethod("getBonus", Block.class, int.class)
                    .invoke(null, block, amount);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to query optional MineFantasy drops", e);
        }
    }

}
