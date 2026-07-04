package ru.givler.mbo.gui.lootcontainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ActionEditorContext {
    public final int actionIndex;
    public final FontRenderer fontRenderer;
    public final List buttonList;
    public final RenderItem itemRenderer;
    public final ActionEditorHost host;

    public ActionEditorContext(int actionIndex,
                               FontRenderer fontRenderer,
                               List buttonList,
                               RenderItem itemRenderer,
                               ActionEditorHost host) {
        this.actionIndex = actionIndex;
        this.fontRenderer = fontRenderer;
        this.buttonList = buttonList;
        this.itemRenderer = itemRenderer;
        this.host = host;
    }
}
