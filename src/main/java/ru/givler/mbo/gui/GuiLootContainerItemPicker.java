package ru.givler.mbo.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiLootContainerItemPicker extends GuiScreen {
    private final GuiLootContainerConfig parent;
    private final int actionIndex;
    private final EntityPlayer player;
    private final RenderItem itemRenderer = new RenderItem();

    public GuiLootContainerItemPicker(GuiLootContainerConfig parent, int actionIndex, EntityPlayer player) {
        this.parent = parent;
        this.actionIndex = actionIndex;
        this.player = player;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(1, width / 2 - 40, height - 28, 80, 20, "Cancel"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (player == null || player.inventory == null) return;

        int left = width / 2 - 94;
        int top = height / 2 - 89;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = row == 3 ? col : (row * 9 + col + 9);
                int sx = left + col * 18;
                int sy = top + row * 18;
                if (mouseX >= sx && mouseX <= sx + 16 && mouseY >= sy && mouseY <= sy + 16) {
                    ItemStack stack = player.inventory.getStackInSlot(slot);
                    if (stack != null) {
                        parent.onItemPicked(actionIndex, stack.copy());
                        mc.displayGuiScreen(parent);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRect(0, 0, width, height, 0xCC050505);
        drawCenteredString(fontRendererObj, "Select the item whose name and nbt will be used", width / 2, 18, 0xFFFFFF);

        int left = width / 2 - 94;
        int top = height / 2 - 89;
        drawRect(left - 6, top - 6, left + 9 * 18 + 6, top + 4 * 18 + 6, 0x99303030);

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                int slot = row == 3 ? col : (row * 9 + col + 9);
                int x = left + col * 18;
                int y = top + row * 18;
                drawRect(x - 1, y - 1, x + 17, y + 17, 0x88202020);
                ItemStack stack = player.inventory.getStackInSlot(slot);
                if (stack != null) {
                    RenderHelper.enableGUIStandardItemLighting();
                    itemRenderer.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), stack, x, y);
                    RenderHelper.disableStandardItemLighting();

                    if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16) {
                        List lines = new ArrayList();
                        lines.add(stack.getDisplayName());
                        if (stack.stackTagCompound != null) {
                            lines.add(stack.stackTagCompound.toString());
                        }
                        drawHoveringText(lines, mouseX, mouseY, fontRendererObj);
                    }
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
