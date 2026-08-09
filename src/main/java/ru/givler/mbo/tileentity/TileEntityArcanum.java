package ru.givler.mbo.tileentity;

import java.util.Arrays;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.EnumSkyBlock;
import ru.givler.mbo.recipes.ArcanumRecipes;
import ru.givler.mbo.recipes.ArcanumRecipesManager;

public class TileEntityArcanum extends TileEntity implements ISidedInventory {
    public static final int INPUT_END=9, FUEL_SLOT=9, OUTPUT_SLOT=10;
    private static final int[] INPUT_SLOTS={0,1,2,3,4,5,6,7,8};
    private static final int[] FUEL_SLOTS={9};
    private static final int[] OUTPUT_SLOTS={10,9};
    private ItemStack[] inventory=new ItemStack[11];
    private int progress;
    private int maxProgress=200;
    private int burnTime;
    private int currentItemBurnTime;
    private int soundTickCounter;
    private ArcanumRecipes currentRecipe;
    private boolean active;

    @Override public void updateEntity() {
        if(worldObj==null||worldObj.isRemote)return;
        boolean changed=false;
        if(burnTime>0){burnTime--;changed=true;}

        currentRecipe=ArcanumRecipesManager.getInstance().getMatchingRecipe(getCraftingMatrix());
        boolean craftable=currentRecipe!=null&&canAccept(currentRecipe.getOutput());
        if(craftable) maxProgress=Math.max(1,currentRecipe.getCookTime());

        if(craftable&&burnTime<=0&&consumeFuel()) changed=true;
        boolean nowActive=craftable&&burnTime>0;
        setActive(nowActive);

        if(nowActive){
            progress++; soundTickCounter++; changed=true;
            if(soundTickCounter>=30){
                worldObj.playSoundEffect(xCoord+.5,yCoord+.5,zCoord+.5,"fire.fire",.5F,1F);
                soundTickCounter=0;
            }
            if(progress>=maxProgress){
                craftItem(currentRecipe);
                worldObj.playSoundEffect(xCoord+.5,yCoord+.5,zCoord+.5,"random.fizz",1F,1F);
                progress=0;
            }
        }else if(progress!=0){
            progress=0; changed=true;
        }
        if(changed)markDirty();
    }

    private boolean canAccept(ItemStack result){
        if(result==null)return false;
        ItemStack current=inventory[OUTPUT_SLOT];
        if(current==null)return true;
        if(!current.isItemEqual(result)||!ItemStack.areItemStackTagsEqual(current,result))return false;
        int limit=Math.min(getInventoryStackLimit(),current.getMaxStackSize());
        return current.stackSize+result.stackSize<=limit;
    }

    private boolean consumeFuel(){
        ItemStack fuel=inventory[FUEL_SLOT];
        int value=TileEntityFurnace.getItemBurnTime(fuel);
        if(fuel==null||value<=0)return false;
        burnTime=currentItemBurnTime=value;
        ItemStack consumed=fuel.copy();
        fuel.stackSize--;
        if(fuel.stackSize<=0){
            inventory[FUEL_SLOT]=consumed.getItem().hasContainerItem(consumed)
                    ?consumed.getItem().getContainerItem(consumed):null;
        }
        return true;
    }

    private void craftItem(ArcanumRecipes recipe){
        if(recipe==null||!canAccept(recipe.getOutput()))return;
        ItemStack result=recipe.getOutput().copy();
        if(inventory[OUTPUT_SLOT]==null)inventory[OUTPUT_SLOT]=result;
        else inventory[OUTPUT_SLOT].stackSize+=result.stackSize;

        for(int i=0;i<INPUT_END;i++){
            ItemStack ingredient=inventory[i];
            if(ingredient==null)continue;
            ItemStack consumed=ingredient.copy();
            ingredient.stackSize--;
            ItemStack remainder=consumed.getItem().hasContainerItem(consumed)
                    ?consumed.getItem().getContainerItem(consumed):null;
            if(ingredient.stackSize<=0)inventory[i]=remainder;
            else if(remainder!=null)dropRemainder(remainder);
        }
        markDirty();
    }

    private void dropRemainder(ItemStack stack){
        worldObj.spawnEntityInWorld(new EntityItem(worldObj,xCoord+.5,yCoord+.75,zCoord+.5,stack));
    }

    public boolean isActive(){return active;}
    public boolean isCrafting(){return progress>0;}
    public void setActive(boolean value){
        if(active==value)return;
        active=value;
        if(worldObj!=null){
            worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);
            worldObj.updateLightByType(EnumSkyBlock.Block,xCoord,yCoord,zCoord);
        }
    }
    public int getProgress(){return progress;}
    public int getMaxProgress(){return Math.max(1,maxProgress);}
    public int getBurnTime(){return burnTime;}
    public int getCurrentItemBurnTime(){return currentItemBurnTime;}
    public void setProgress(int value){progress=value;}
    public void setMaxProgress(int value){maxProgress=Math.max(1,value);}
    public void setBurnTime(int value){burnTime=value;}
    public void setCurrentItemBurnTime(int value){currentItemBurnTime=value;}
    public void setActiveFromServer(boolean value){
        active=value;
        if(worldObj!=null)worldObj.updateLightByType(EnumSkyBlock.Block,xCoord,yCoord,zCoord);
    }
    public ItemStack[] getCraftingMatrix(){return Arrays.copyOfRange(inventory,0,INPUT_END);}

    @Override public int getSizeInventory(){return inventory.length;}
    @Override public ItemStack getStackInSlot(int index){return inventory[index];}
    @Override public ItemStack decrStackSize(int index,int count){
        ItemStack stack=inventory[index];
        if(stack==null)return null;
        ItemStack result;
        if(stack.stackSize<=count){result=stack;inventory[index]=null;}
        else{result=stack.splitStack(count);if(stack.stackSize<=0)inventory[index]=null;}
        markDirty(); return result;
    }
    @Override public ItemStack getStackInSlotOnClosing(int index){
        ItemStack result=inventory[index];inventory[index]=null;return result;
    }
    @Override public void setInventorySlotContents(int index,ItemStack stack){
        inventory[index]=stack;
        if(stack!=null)stack.stackSize=Math.min(stack.stackSize,
                Math.min(getInventoryStackLimit(),stack.getMaxStackSize()));
        markDirty();
    }
    @Override public int getInventoryStackLimit(){return 64;}
    @Override public boolean isUseableByPlayer(EntityPlayer player){
        return worldObj.getTileEntity(xCoord,yCoord,zCoord)==this
                &&player.getDistanceSq(xCoord+.5,yCoord+.5,zCoord+.5)<=64;
    }
    @Override public boolean isItemValidForSlot(int index,ItemStack stack){
        if(index==OUTPUT_SLOT)return false;
        return index!=FUEL_SLOT||TileEntityFurnace.getItemBurnTime(stack)>0;
    }
    @Override public int[] getAccessibleSlotsFromSide(int side){
        return side==0?OUTPUT_SLOTS:side==1?INPUT_SLOTS:FUEL_SLOTS;
    }
    @Override public boolean canInsertItem(int slot,ItemStack stack,int side){
        return isItemValidForSlot(slot,stack);
    }
    @Override public boolean canExtractItem(int slot,ItemStack stack,int side){
        if(slot==OUTPUT_SLOT)return true;
        return slot==FUEL_SLOT&&TileEntityFurnace.getItemBurnTime(stack)<=0;
    }
    @Override public String getInventoryName(){return "container.magic_furnace";}
    @Override public boolean hasCustomInventoryName(){return false;}
    @Override public void openInventory(){}
    @Override public void closeInventory(){}

    @Override public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        NBTTagList items=new NBTTagList();
        for(int i=0;i<inventory.length;i++)if(inventory[i]!=null){
            NBTTagCompound item=new NBTTagCompound();
            item.setByte("Slot",(byte)i);inventory[i].writeToNBT(item);items.appendTag(item);
        }
        tag.setTag("Items",items);
        tag.setInteger("Progress",progress);
        tag.setInteger("MaxProgress",maxProgress);
        tag.setInteger("BurnTime",burnTime);
        tag.setInteger("CurrentItemBurnTime",currentItemBurnTime);
        tag.setBoolean("IsActive",active);
    }
    @Override public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        inventory=new ItemStack[11];
        NBTTagList items=tag.getTagList("Items",10);
        for(int i=0;i<items.tagCount();i++){
            NBTTagCompound item=items.getCompoundTagAt(i);
            int slot=item.getByte("Slot")&255;
            if(slot<inventory.length)inventory[slot]=ItemStack.loadItemStackFromNBT(item);
        }
        progress=tag.getInteger("Progress");
        maxProgress=tag.hasKey("MaxProgress")?Math.max(1,tag.getInteger("MaxProgress")):200;
        burnTime=tag.getInteger("BurnTime");
        currentItemBurnTime=tag.getInteger("CurrentItemBurnTime");
        active=tag.getBoolean("IsActive");
    }
    @Override public Packet getDescriptionPacket(){
        NBTTagCompound tag=new NBTTagCompound();writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord,yCoord,zCoord,1,tag);
    }
    @Override public void onDataPacket(NetworkManager network,S35PacketUpdateTileEntity packet){
        readFromNBT(packet.func_148857_g());
        worldObj.markBlockForUpdate(xCoord,yCoord,zCoord);
        worldObj.updateLightByType(EnumSkyBlock.Block,xCoord,yCoord,zCoord);
    }
}
