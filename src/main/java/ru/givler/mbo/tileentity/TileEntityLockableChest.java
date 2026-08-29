package ru.givler.mbo.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import ru.givler.mbo.lockable.ILockableTile;
import ru.givler.mbo.lockable.LockData;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.handler.MboGui;
import ru.givler.mbo.lockable.RefillMode;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TileEntityLockableChest extends TileEntityChest implements ILockableTile {
    private final LockData lock = new LockData();
    private final ItemStack[] lootTemplates = new ItemStack[14];
    private final int[] lootWeights = new int[14];
    private final boolean[] guaranteed = new boolean[14];
    private RefillMode refillMode = RefillMode.BATCH;
    private boolean weightedSelection;
    private int fillSlots = 8, cooldownSeconds = 3600, gradualIntervalSeconds = 60, gradualCount = 1;
    private String groupId = "";
    private long nextRefillEpoch;
    private int refillTicker;
    private boolean changingLoot;

    public TileEntityLockableChest() { for (int i=0;i<lootWeights.length;i++) lootWeights[i]=1; }
    @Override public int getSizeInventory(){return lock.getDifficulty().inventorySlots;}
    public boolean canChangeDifficulty(ru.givler.mbo.lockable.LockDifficulty value){for(int i=value.inventorySlots;i<27;i++)if(super.getStackInSlot(i)!=null)return false;return true;}

    @Override public LockData getLockData() { return lock; }
    @Override public TileEntity asTileEntity() { return this; }
    @Override public void onUnlocked(EntityPlayer player) { openFor(player); }

    public void openFor(EntityPlayer player) {
        if (player != null && !worldObj.isRemote) {
            // A refill cycle belongs to a particular opening. Do not let an old
            // deadline expire while the player is looking through the chest.
            nextRefillEpoch = now() + cooldownSeconds;
            markDirty();
            player.openGui(MoreBeyondOrdinary.instance,
                    MboGui.LOCKABLE_CHEST.id, worldObj, xCoord, yCoord, zCoord);
        }
    }

    public ItemStack getLootTemplate(int slot){return slot>=0&&slot<14?lootTemplates[slot]:null;}
    public void setLootTemplate(int slot,ItemStack stack){if(slot>=0&&slot<14)lootTemplates[slot]=stack==null?null:stack.copy();}
    public int getLootWeight(int slot){return slot>=0&&slot<14?lootWeights[slot]:1;}
    public boolean isGuaranteed(int slot){return slot>=0&&slot<14&&guaranteed[slot];}
    public RefillMode getRefillMode(){return refillMode;} public boolean isWeightedSelection(){return weightedSelection;}
    public int getFillSlots(){return fillSlots;} public int getCooldownSeconds(){return cooldownSeconds;}
    public int getGradualIntervalSeconds(){return gradualIntervalSeconds;} public int getGradualCount(){return gradualCount;}
    public String getGroupId(){return groupId;}
    public void applyLootSettings(NBTTagCompound tag){
        refillMode=RefillMode.byOrdinal(tag.getInteger("RefillMode"));weightedSelection=tag.getBoolean("Weighted");
        fillSlots=Math.max(1,Math.min(getSizeInventory(),tag.getInteger("FillSlots")));cooldownSeconds=Math.max(1,tag.getInteger("Cooldown"));
        gradualIntervalSeconds=Math.max(1,tag.getInteger("GradualInterval"));gradualCount=Math.max(1,Math.min(27,tag.getInteger("GradualCount")));
        lock.setPlayerRadius(tag.getInteger("PlayerRadius"));groupId=tag.getString("GroupId");
        int[] weights=tag.getIntArray("Weights");for(int i=0;i<14;i++){lootWeights[i]=weights.length>i?Math.max(0,weights[i]):1;guaranteed[i]=tag.getBoolean("Guaranteed"+i);}
        if(worldObj!=null)nextRefillEpoch=now()+cooldownSeconds;markDirty();
    }
    public NBTTagCompound createLootSettings(){NBTTagCompound tag=new NBTTagCompound();tag.setInteger("RefillMode",refillMode.ordinal());tag.setBoolean("Weighted",weightedSelection);tag.setInteger("FillSlots",fillSlots);tag.setInteger("Cooldown",cooldownSeconds);tag.setInteger("GradualInterval",gradualIntervalSeconds);tag.setInteger("GradualCount",gradualCount);tag.setInteger("PlayerRadius",lock.getPlayerRadius());tag.setString("GroupId",groupId);tag.setIntArray("Weights",lootWeights);for(int i=0;i<14;i++)tag.setBoolean("Guaranteed"+i,guaranteed[i]);return tag;}
    @Override public ItemStack decrStackSize(int slot,int amount){ItemStack result=super.decrStackSize(slot,amount);if(result!=null&&!changingLoot)scheduleAfterTake();return result;}
    @Override public ItemStack getStackInSlotOnClosing(int slot){ItemStack result=super.getStackInSlotOnClosing(slot);if(result!=null&&!changingLoot)scheduleAfterTake();return result;}
    private void scheduleAfterTake(){nextRefillEpoch=refillMode==RefillMode.IMMEDIATE?now():now()+cooldownSeconds;markDirty();}
    @Override public void updateEntity(){super.updateEntity();if(worldObj==null||worldObj.isRemote||++refillTicker<20)return;refillTicker=0;if(nextRefillEpoch==0)nextRefillEpoch=now()+cooldownSeconds;if(now()<nextRefillEpoch||numPlayersUsing>0||lock.hasActiveAttempt()||lock.hasPlayerNearby(this)||!hasTemplates())return;int missing=Math.max(0,fillSlots-countOccupied());if(refillMode==RefillMode.GRADUAL){addGenerated(Math.min(gradualCount,missing));nextRefillEpoch=now()+(countOccupied()>=fillSlots?cooldownSeconds:gradualIntervalSeconds);}else{addGenerated(missing);nextRefillEpoch=now()+cooldownSeconds;lock.lock(worldObj.rand);worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);}markDirty();}
    private boolean hasTemplates(){for(ItemStack stack:lootTemplates)if(stack!=null)return true;return false;}
    private int countOccupied(){int count=0;for(int i=0;i<getSizeInventory();i++)if(getStackInSlot(i)!=null)count++;return count;}
    private void addGenerated(int amount){if(amount<=0)return;List<Integer> chosen=chooseTemplates(amount),empty=new ArrayList<>();for(int i=0;i<getSizeInventory();i++)if(getStackInSlot(i)==null)empty.add(i);Collections.shuffle(empty,worldObj.rand);changingLoot=true;for(int i=0;i<chosen.size()&&i<empty.size();i++)super.setInventorySlotContents(empty.get(i),lootTemplates[chosen.get(i)].copy());changingLoot=false;}
    private List<Integer> chooseTemplates(int amount){List<Integer> pool=new ArrayList<>(),result=new ArrayList<>();for(int i=0;i<14;i++)if(lootTemplates[i]!=null){if(guaranteed[i]&&containsTemplate(lootTemplates[i]))continue;pool.add(i);if(guaranteed[i]&&result.size()<amount)result.add(i);}pool.removeAll(result);while(result.size()<amount&&!pool.isEmpty()){int pick;if(!weightedSelection)pick=worldObj.rand.nextInt(pool.size());else{int total=0;for(int index:pool)total+=Math.max(0,lootWeights[index]);if(total<=0)break;int roll=worldObj.rand.nextInt(total),sum=0;pick=0;for(int i=0;i<pool.size();i++){sum+=Math.max(0,lootWeights[pool.get(i)]);if(roll<sum){pick=i;break;}}}result.add(pool.remove(pick));}return result;}
    private boolean containsTemplate(ItemStack template){for(int i=0;i<getSizeInventory();i++){ItemStack existing=getStackInSlot(i);if(existing!=null&&existing.isItemEqual(template)&&ItemStack.areItemStackTagsEqual(existing,template))return true;}return false;}
    private static long now(){return System.currentTimeMillis()/1000L;}
    @Override public void writeToNBT(NBTTagCompound tag) { super.writeToNBT(tag); lock.writeToNBT(tag);tag.setLong("MboNextRefill",nextRefillEpoch);tag.setTag("MboLootSettings",createLootSettings());NBTTagList list=new NBTTagList();for(int i=0;i<14;i++)if(lootTemplates[i]!=null){NBTTagCompound item=new NBTTagCompound();item.setByte("Slot",(byte)i);lootTemplates[i].writeToNBT(item);list.appendTag(item);}tag.setTag("MboLootTemplates",list); }
    @Override public void readFromNBT(NBTTagCompound tag) { super.readFromNBT(tag); lock.readFromNBT(tag);if(tag.hasKey("MboLootSettings"))applyLootSettings(tag.getCompoundTag("MboLootSettings"));nextRefillEpoch=tag.getLong("MboNextRefill");for(int i=0;i<14;i++)lootTemplates[i]=null;NBTTagList list=tag.getTagList("MboLootTemplates",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound item=list.getCompoundTagAt(i);int slot=item.getByte("Slot")&255;if(slot<14)lootTemplates[slot]=ItemStack.loadItemStackFromNBT(item);} }
    @Override public Packet getDescriptionPacket() { NBTTagCompound tag = new NBTTagCompound(); writeToNBT(tag); return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag); }
    @Override public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) { readFromNBT(packet.func_148857_g()); }
}
