package ru.givler.mbo.tileentity;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/** Stores one section of a petrified creature. The bottom section owns its release timer. */
public final class TileEntityPetrifiedStatue extends TileEntity {
  private EntityLiving creature;
  private NBTTagCompound creatureData;
  private String creatureId = "";
  private int section = 1;
  private int sections = 1;
  private int age;
  private int lifetime = 900;

  public EntityLiving creature() {
    if (creature == null && worldObj != null && !creatureId.isEmpty() && creatureData != null) {
      net.minecraft.entity.Entity restored = EntityList.createEntityByName(creatureId, worldObj);
      if (restored instanceof EntityLiving) {
        creature = (EntityLiving) restored;
        creature.readFromNBT(creatureData);
      }
    }
    return creature;
  }

  public void configure(EntityLiving entity, int section, int sections, int lifetime) {
    this.creature = entity;
    this.creatureData = new NBTTagCompound();
    entity.writeToNBT(this.creatureData);
    this.creatureId = EntityList.getEntityString(entity);
    this.section = section;
    this.sections = sections;
    this.lifetime = lifetime;
    markDirty();
    if (worldObj != null) worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
  }

  public int section() { return section; }
  public int sections() { return sections; }

  @Override public void updateEntity() {
    age++;
    creature();
    boolean ice=worldObj.getBlock(xCoord,yCoord,zCoord) instanceof ru.givler.mbo.block.magic.BlockPetrifiedStatue
        && ((ru.givler.mbo.block.magic.BlockPetrifiedStatue)worldObj.getBlock(xCoord,yCoord,zCoord)).isIce();
    if (!worldObj.isRemote && section == 1 && ((ice && age>lifetime) || (!ice && age > lifetime && age % 200 == 0
        && worldObj.getBlockLightValue(xCoord, yCoord, zCoord) < worldObj.rand.nextInt(12) - 3))) {
      worldObj.func_147480_a(xCoord, yCoord, zCoord, false);
    }
  }

  @Override public void writeToNBT(NBTTagCompound tag) {
    super.writeToNBT(tag);
    if (creature != null) {
      creatureData = new NBTTagCompound();
      creature.writeToNBT(creatureData);
      creatureId = EntityList.getEntityString(creature);
    }
    if (creatureData != null) tag.setTag("Creature", creatureData);
    tag.setString("CreatureId", creatureId);
    tag.setInteger("Section", section);
    tag.setInteger("Sections", sections);
    tag.setInteger("Age", age);
    tag.setInteger("Lifetime", lifetime);
  }

  @Override public void readFromNBT(NBTTagCompound tag) {
    super.readFromNBT(tag);
    creatureData = tag.getCompoundTag("Creature");
    creatureId = tag.getString("CreatureId");
    section = Math.max(1, tag.getInteger("Section"));
    sections = Math.max(1, tag.getInteger("Sections"));
    age = tag.getInteger("Age");
    lifetime = Math.max(1, tag.getInteger("Lifetime"));
    creature = null;
  }

  @Override public Packet getDescriptionPacket() {
    NBTTagCompound tag = new NBTTagCompound();
    writeToNBT(tag);
    return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
  }

  @Override public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) {
    readFromNBT(packet.func_148857_g());
  }
}
