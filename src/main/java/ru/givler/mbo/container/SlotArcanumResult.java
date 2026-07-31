package ru.givler.mbo.container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
final class SlotArcanumResult extends Slot {
    SlotArcanumResult(IInventory inventory,int index,int x,int y){super(inventory,index,x,y);}
    @Override public boolean isItemValid(ItemStack stack){return false;}
}
