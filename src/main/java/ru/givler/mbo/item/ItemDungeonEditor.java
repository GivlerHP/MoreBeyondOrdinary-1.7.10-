package ru.givler.mbo.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.editor.AreaSelection;

public final class ItemDungeonEditor extends ItemAreaEditor {
    public static final int MAX_VOLUME = 32768;

    public ItemDungeonEditor() {
        super("DungeonEditor");
    }

    public void setPoint(ItemStack stack, EntityPlayer player, String key, int x, int y, int z) {
        selectPoint(stack,player,key,x,y,z,MAX_VOLUME,"Pos1".equals(key)?"mbo.dungeon.pos1":"mbo.dungeon.pos2");
    }

    public static int[] getPoint(ItemStack stack, String key, int dimension) {
        return AreaSelection.getPoint(stack,key,dimension);
    }
    public static boolean hasCompleteSelection(ItemStack stack,int dimension){return getPoint(stack,"Pos1",dimension)!=null&&getPoint(stack,"Pos2",dimension)!=null;}
    @Override protected String tooLargeMessageKey(){return "mbo.dungeon.selection.tooLarge";}
    @Override public int selectionColor(){return 0xB840FFF2;}
}
