package ru.givler.mbo.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import ru.givler.mbo.MoreBeyondOrdinary;
import ru.givler.mbo.editor.BuilderAccess;
import ru.givler.mbo.editor.AreaSelection;
import ru.givler.mbo.registry.CreativeTabRegistry;

public abstract class ItemAreaEditor extends Item {
    protected ItemAreaEditor(String name){setUnlocalizedName(name);setTextureName(MoreBeyondOrdinary.MODID+":admin_key");setCreativeTab(CreativeTabRegistry.tabMBOitems);setMaxStackSize(1);}
    protected final boolean selectPoint(ItemStack stack,EntityPlayer player,String key,int x,int y,int z,long maxVolume,String messageKey){
        if(!BuilderAccess.canUseTool(player,this))return false;
        if(AreaSelection.SECOND.equals(key)&&maxVolume>0){int[] first=AreaSelection.getPoint(stack,AreaSelection.FIRST,player.dimension);if(first!=null&&AreaSelection.prospectiveVolume(first,x,y,z)>maxVolume){if(!player.worldObj.isRemote)player.addChatMessage(new ChatComponentTranslation(tooLargeMessageKey(),maxVolume));return false;}}
        beforePointStored(stack,key);AreaSelection.setPoint(stack,key,player.dimension,x,y,z);
        if(!player.worldObj.isRemote)player.addChatMessage(new ChatComponentTranslation(messageKey,x,y,z));return true;
    }
    protected void beforePointStored(ItemStack stack,String key){}
    protected abstract String tooLargeMessageKey();
    public abstract int selectionColor();
    public final AreaSelection selection(ItemStack stack,int dimension){return AreaSelection.read(stack,dimension);}
}
