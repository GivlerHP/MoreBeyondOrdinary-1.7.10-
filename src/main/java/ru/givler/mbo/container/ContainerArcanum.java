package ru.givler.mbo.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import ru.givler.mbo.tileentity.TileEntityArcanum;

public class ContainerArcanum extends Container {
    private final TileEntityArcanum tile;
    private int lastProgress,lastMaxProgress,lastBurnTime,lastItemBurnTime;
    private boolean lastActive;

    public ContainerArcanum(InventoryPlayer playerInventory,TileEntityArcanum tile){
        this.tile=tile;
        for(int row=0;row<3;row++)for(int col=0;col<3;col++)
            addSlotToContainer(new Slot(tile,col+row*3,36+col*20,8+row*20));
        addSlotToContainer(new SlotArcanumFuel(tile,9,132,61));
        addSlotToContainer(new SlotArcanumResult(tile,10,132,28));
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)
            addSlotToContainer(new Slot(playerInventory,col+row*9+9,8+col*18,106+row*18));
        for(int col=0;col<9;col++)addSlotToContainer(new Slot(playerInventory,col,8+col*18,164));
    }

    @Override public boolean canInteractWith(EntityPlayer player){return tile.isUseableByPlayer(player);}
    @Override public void addCraftingToCrafters(ICrafting crafter){
        super.addCraftingToCrafters(crafter);
        sendAll(crafter);
    }
    private void sendAll(ICrafting c){
        c.sendProgressBarUpdate(this,0,tile.getProgress());
        c.sendProgressBarUpdate(this,1,tile.getMaxProgress());
        c.sendProgressBarUpdate(this,2,tile.getBurnTime());
        c.sendProgressBarUpdate(this,3,tile.getCurrentItemBurnTime());
        c.sendProgressBarUpdate(this,4,tile.isActive()?1:0);
    }
    @Override public void detectAndSendChanges(){
        super.detectAndSendChanges();
        for(Object object:crafters){
            ICrafting c=(ICrafting)object;
            if(lastProgress!=tile.getProgress())c.sendProgressBarUpdate(this,0,tile.getProgress());
            if(lastMaxProgress!=tile.getMaxProgress())c.sendProgressBarUpdate(this,1,tile.getMaxProgress());
            if(lastBurnTime!=tile.getBurnTime())c.sendProgressBarUpdate(this,2,tile.getBurnTime());
            if(lastItemBurnTime!=tile.getCurrentItemBurnTime())c.sendProgressBarUpdate(this,3,tile.getCurrentItemBurnTime());
            if(lastActive!=tile.isActive())c.sendProgressBarUpdate(this,4,tile.isActive()?1:0);
        }
        lastProgress=tile.getProgress();lastMaxProgress=tile.getMaxProgress();
        lastBurnTime=tile.getBurnTime();lastItemBurnTime=tile.getCurrentItemBurnTime();
        lastActive=tile.isActive();
    }
    @Override public void updateProgressBar(int id,int value){
        if(id==0)tile.setProgress(value);
        else if(id==1)tile.setMaxProgress(value);
        else if(id==2)tile.setBurnTime(value);
        else if(id==3)tile.setCurrentItemBurnTime(value);
        else if(id==4)tile.setActiveFromServer(value!=0);
    }

    @Override public ItemStack transferStackInSlot(EntityPlayer player,int index){
        Slot slot=(Slot)inventorySlots.get(index);
        if(slot==null||!slot.getHasStack())return null;
        ItemStack stack=slot.getStack(),copy=stack.copy();
        if(index==10){
            if(!mergeItemStack(stack,11,47,true))return null;
            slot.onSlotChange(stack,copy);
        }else if(index>=11){
            if(TileEntityFurnace.getItemBurnTime(stack)>0){
                if(!mergeItemStack(stack,9,10,false))return null;
            }else if(!mergeItemStack(stack,0,9,false))return null;
        }else if(!mergeItemStack(stack,11,47,false))return null;
        if(stack.stackSize==0)slot.putStack(null);else slot.onSlotChanged();
        if(stack.stackSize==copy.stackSize)return null;
        slot.onPickupFromSlot(player,stack);
        return copy;
    }
}
