package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketOpenLockConfig;
import ru.givler.mbo.registry.ItemRegistry;
import ru.givler.mbo.tileentity.TileEntityLockableChest;

public class GuiLockableChest extends GuiChest {
    private final TileEntityLockableChest chest;
    public GuiLockableChest(InventoryPlayer inventory,TileEntityLockableChest chest){super(inventory,chest);this.chest=chest;}
    @Override public void initGui(){
        super.initGui();
        ItemStack held=mc.thePlayer.getCurrentEquippedItem();
        if(mc.thePlayer.capabilities.isCreativeMode && held!=null && held.getItem()==ItemRegistry.AdminKey)
            buttonList.add(new GuiButton(901,guiLeft+xSize-82,guiTop-24,76,20,StatCollector.translateToLocal("mbo.lock.gui.configure")));
    }
    @Override protected void actionPerformed(GuiButton button){
        if(button.id==901) PacketManager.INSTANCE.sendToServer(new PacketOpenLockConfig(chest.xCoord,chest.yCoord,chest.zCoord));
    }
}
