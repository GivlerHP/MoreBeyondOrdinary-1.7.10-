package ru.givler.mbo.client.gui.lootcontainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public interface ActionEditorHost {
    void openItemPicker(int actionIndex);
}
