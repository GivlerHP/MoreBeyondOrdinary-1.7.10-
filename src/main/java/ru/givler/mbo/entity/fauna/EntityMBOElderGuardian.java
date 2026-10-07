package ru.givler.mbo.entity.fauna;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketElderGuardianCurse;

/** Persistent elder guardian; its curse seals boat controls after the apparition finishes. */
public final class EntityMBOElderGuardian extends EntityMBOGuardian {
  public static final int CURSE_RADIUS = 50;
  public static final int APPARITION_TICKS = 30;
  private final Map<Integer, Integer> nextCurse = new HashMap<Integer, Integer>();
  private int sealCountdown = -1;
  private boolean boatSealActive;

  public EntityMBOElderGuardian(World world) {
    super(world);
    setSize(.85F * 2.35F, .85F * 2.35F);
    func_110163_bv();
  }

  @Override
  protected void applyEntityAttributes() {
    super.applyEntityAttributes();
    getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(80);
    getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(.3);
    getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(8);
  }

  @Override
  protected int getWanderInterval() {
    return 400;
  }

  @Override
  protected double getSwimAcceleration() {
    return .015;
  }

  @Override
  protected boolean avoidsCloseTargets() {
    return false;
  }

  @Override
  public int getAttackDuration() {
    return 60;
  }

  @Override
  protected boolean canDespawn() {
    return false;
  }

  public boolean blocksBoat(Entity boat) {
    return boatSealActive
        && isEntityAlive()
        && boat.worldObj == worldObj
        && getDistanceSqToEntity(boat) <= CURSE_RADIUS * CURSE_RADIUS;
  }

  @Override
  public void onLivingUpdate() {
    super.onLivingUpdate();
    if (worldObj.isRemote || !isEntityAlive()) return;
    advanceBoatSeal();
    if (ticksExisted % 10 != 0) return;
    Set<Integer> present = new HashSet<Integer>();
    for (Object entry : worldObj.playerEntities) {
      if (!(entry instanceof EntityPlayerMP)) continue;
      EntityPlayerMP player = (EntityPlayerMP) entry;
      if (!player.isEntityAlive()
          || player.capabilities.isCreativeMode
          || getDistanceSqToEntity(player) > CURSE_RADIUS * CURSE_RADIUS) continue;
      int id = player.getEntityId();
      present.add(id);
      Integer due = nextCurse.get(id);
      if (due != null && ticksExisted < due) continue;
      nextCurse.put(id, ticksExisted + 1200);
      PotionEffect fatigue = player.getActivePotionEffect(Potion.digSlowdown);
      boolean show =
          due == null
              || fatigue == null
              || fatigue.getAmplifier() < 2
              || fatigue.getAmplifier() == 2 && fatigue.getDuration() < 1200;
      if (show) {
        player.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 6000, 2));
        PacketManager.INSTANCE.sendTo(new PacketElderGuardianCurse(), player);
        if (!boatSealActive && sealCountdown < 0) sealCountdown = APPARITION_TICKS;
      }
    }
    nextCurse.keySet().retainAll(present);
  }

  private void advanceBoatSeal() {
    if (sealCountdown > 0 && --sealCountdown == 0) boatSealActive = true;
  }

  @Override
  public void writeEntityToNBT(NBTTagCompound tag) {
    super.writeEntityToNBT(tag);
    tag.setBoolean("BoatSealActive", boatSealActive);
    tag.setInteger("BoatSealCountdown", sealCountdown);
  }

  @Override
  public void readEntityFromNBT(NBTTagCompound tag) {
    super.readEntityFromNBT(tag);
    boatSealActive = tag.getBoolean("BoatSealActive");
    sealCountdown = tag.hasKey("BoatSealCountdown") ? tag.getInteger("BoatSealCountdown") : -1;
  }

  @Override
  public boolean getCanSpawnHere() {
    return false;
  }

  @Override
  protected String getLivingSound() {
    return "mbo:entity.elder_guardian.ambient" + (isInWater() ? "" : "_land");
  }

  @Override
  protected String getHurtSound() {
    return "mbo:entity.elder_guardian.hurt" + (isInWater() ? "" : "_land");
  }

  @Override
  protected String getDeathSound() {
    return "mbo:entity.elder_guardian.death" + (isInWater() ? "" : "_land");
  }

  @Override
  protected String getFlopSound() {
    return "mbo:entity.elder_guardian.flop";
  }

  @Override
  protected void dropFewItems(boolean hit, int looting) {
    super.dropFewItems(hit, looting);
    // 1.7.10 has no wet sponge variant; use its vanilla sponge counterpart.
    entityDropItem(new ItemStack(Blocks.sponge), 0);
  }
}
