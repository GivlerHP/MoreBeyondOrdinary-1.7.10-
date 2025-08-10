package ru.givler.mbo.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import ru.givler.mbo.registry.ItemRegistry;

public class EntityStoneGolem extends EntityIronGolem {

    public EntityStoneGolem(World world) {
        super(world);

        this.isImmuneToFire = true;
        this.setSize(1.4F, 2.9F);
        this.experienceValue = 20;

        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(180.0D);
        if (this.getEntityAttribute(SharedMonsterAttributes.attackDamage) == null) {
            this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        }
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(15.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
        this.getEntityAttribute(SharedMonsterAttributes.knockbackResistance).setBaseValue(0.8D);

        // === Поведение AI ===
        this.tasks.taskEntries.clear();
        this.targetTasks.taskEntries.clear();

        // Основное поведение
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(2, new EntityAIAttackOnCollide(this, EntityPlayer.class, 1.0D, false));
        this.tasks.addTask(5, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(7, new EntityAILookIdle(this));

        // Цели для атаки
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, true));

    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
    }

    @Override
    protected String getLivingSound() {
        return "mob.irongolem.say";
    }

    @Override
    protected String getHurtSound() {
        return "mbo:stonegolem_hit";
    }

    @Override
    protected String getDeathSound() {
        return "mbo:stonegolem_death";
    }

    @Override
    protected void func_145780_a(int x, int y, int z, Block blockIn) {
        this.playSound("mbo:stonegolem_walk", 1.0F, 1.0F);
    }

    @Override
    public boolean getCanSpawnHere() {
        return this.posY < 30 && super.getCanSpawnHere();
    }

    @Override
    public boolean isPlayerCreated() {
        return false;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int lootingLevel) {
        int count = 1 + this.rand.nextInt(2);
        this.dropItem(ItemRegistry.SapphireEye, count);
        this.dropItem(Item.getItemFromBlock(Blocks.stone), 10);
        this.dropItem(Item.getItemFromBlock(Blocks.cobblestone), 5);

        if (this.rand.nextFloat() < 0.10F) {
            this.dropItem(ItemRegistry.SapphireHeart, 1);
        }
    }
}
