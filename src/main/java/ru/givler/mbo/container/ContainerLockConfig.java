package ru.givler.mbo.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.tileentity.TileEntityLockableChest;

public class ContainerLockConfig extends Container {
    private final TileEntityLockableChest chest;
    private final InventoryBasic templates=new InventoryBasic("mbo.lock.templates",false,14);
    public ContainerLockConfig(InventoryPlayer player,TileEntityLockableChest chest){this.chest=chest;for(int i=0;i<14;i++)templates.setInventorySlotContents(i,chest.getLootTemplate(i));for(int i=0;i<14;i++)addSlotToContainer(new Slot(templates,i,17+(i%7)*18,32+(i/7)*18));for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlotToContainer(new Slot(player,col+row*9+9,17+col*18,151+row*18));for(int col=0;col<9;col++)addSlotToContainer(new Slot(player,col,17+col*18,209));}
    public ItemStack getTemplate(int slot){return slot>=0&&slot<14?templates.getStackInSlot(slot):null;}
    @Override public ItemStack slotClick(int slot,int button,int mode,EntityPlayer player){if(slot>=0&&slot<14&&player.capabilities.isCreativeMode){ItemStack cursor=player.inventory.getItemStack();templates.setInventorySlotContents(slot,cursor==null?null:cursor.copy());detectAndSendChanges();return cursor;}return super.slotClick(slot,button,mode,player);}
    @Override public void onContainerClosed(EntityPlayer player){super.onContainerClosed(player);if(!player.worldObj.isRemote){for(int i=0;i<14;i++)chest.setLootTemplate(i,templates.getStackInSlot(i));chest.markDirty();}}
    @Override public boolean canInteractWith(EntityPlayer player){return chest.isUseableByPlayer(player);}
}
