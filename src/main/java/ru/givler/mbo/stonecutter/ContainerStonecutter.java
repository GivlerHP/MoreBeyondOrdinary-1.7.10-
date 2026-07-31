package ru.givler.mbo.stonecutter;

import java.util.Collections;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import ru.givler.mbo.registry.StonecutterRegistry;

public class ContainerStonecutter extends Container {
    private final InventoryStonecutterInput input;
    private final InventoryBasic output = new InventoryBasic("stonecutter.result", false, 1);
    private List<StonecutterRecipe> recipes = Collections.emptyList();
    private int selected = -1;
    private final World world;
    private final int x, y, z;

    public ContainerStonecutter(InventoryPlayer player, World world, int x, int y, int z) {
        this.world=world; this.x=x; this.y=y; this.z=z;
        input = new InventoryStonecutterInput(this);
        addSlotToContainer(new Slot(input, 0, 20, 33));
        addSlotToContainer(new SlotStonecutterResult(this, output, 0, 142, 33));
        for (int row=0; row<3; row++) for (int col=0; col<9; col++)
            addSlotToContainer(new Slot(player, col+row*9+9, 8+col*18, 84+row*18));
        for (int col=0; col<9; col++) addSlotToContainer(new Slot(player, col, 8+col*18, 142));
    }

    void onInputChanged() {
        recipes = StonecutterRecipes.getRecipes(input.getStackInSlot(0));
        if (selected >= recipes.size()) selected = -1;
        updateOutput();
    }
    void onResultTaken() {
        input.decrStackSize(0, 1);
        if (!world.isRemote) {
            world.playSoundEffect(x+.5D,y+.5D,z+.5D,
                    "mbo:stonecutter_use"+(world.rand.nextBoolean()?1:2),1F,1F);
        }
        updateOutput();
    }
    private void updateOutput() {
        output.setInventorySlotContents(0,
                selected >= 0 && selected < recipes.size() && input.getStackInSlot(0) != null
                        ? recipes.get(selected).getOutput() : null);
    }
    public List<StonecutterRecipe> getRecipes() { return recipes; }
    public int getSelected() { return selected; }
    @Override public boolean enchantItem(EntityPlayer player, int id) {
        if (id < 0 || id >= recipes.size()) return false;
        selected=id; updateOutput(); return true;
    }
    @Override public boolean canInteractWith(EntityPlayer player) {
        return world.getBlock(x,y,z)==StonecutterRegistry.stonecutter
                && player.getDistanceSq(x+.5,y+.5,z+.5)<=64;
    }
    @Override public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!player.worldObj.isRemote) {
            ItemStack stack=input.getStackInSlotOnClosing(0);
            if(stack!=null) player.dropPlayerItemWithRandomChoice(stack,false);
        }
    }
    @Override public ItemStack transferStackInSlot(EntityPlayer player,int index) {
        Slot slot=(Slot)inventorySlots.get(index);
        if(slot==null||!slot.getHasStack()) return null;
        ItemStack stack=slot.getStack(), copy=stack.copy();
        if(index==1) {
            if(!mergeItemStack(stack,2,38,true)) return null;
            slot.onSlotChange(stack,copy);
        } else if(index>=2) {
            if(!mergeItemStack(stack,0,1,false)) return null;
        } else if(!mergeItemStack(stack,2,38,false)) return null;
        if(stack.stackSize==0) slot.putStack(null); else slot.onSlotChanged();
        if(stack.stackSize==copy.stackSize) return null;
        slot.onPickupFromSlot(player,stack);
        return copy;
    }
}
