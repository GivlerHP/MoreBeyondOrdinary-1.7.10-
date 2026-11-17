package ru.givler.mbo.client.gui;

import java.util.*;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.client.font.ModernFontRenderer;
import ru.givler.mbo.client.font.TooltipElements;
import ru.givler.mbo.container.ContainerDescriptionWorkbench;
import ru.givler.mbo.item.ItemDescription;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketItemDescription;
import ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench;

/** Plain editable text plus per-character colour data, with an independent clipped live preview. */
public final class GuiDescriptionWorkbench extends GuiContainer {
    private static final int[] COLORS = {0x000000, 0x0000aa, 0x00aa00, 0x00aaaa,
            0xaa0000, 0xaa00aa, 0xffaa00, 0xaaaaaa, 0x555555, 0x5555ff, 0x55ff55, 0x55ffff,
            0xff5555, 0xff55ff, 0xffff55, 0xffffff};
    private final TileEntityDescriptionWorkbench tile;
    private final List<GuiTextField> fields = new ArrayList<GuiTextField>();
    private final List<int[]> styles = new ArrayList<int[]>();
    private final Deque<Draft> undo = new ArrayDeque<Draft>();
    private GuiTextField hex;
    private ItemStack editing;
    private int editorWidth, previewX, activeLine, selectionStart, selectionEnd, previewScroll;
    private boolean previewDirty = true;
    private List<String> previewRows = new ArrayList<String>();
    private final java.nio.IntBuffer oldScissor = org.lwjgl.BufferUtils.createIntBuffer(16);
    private int dragLine = -1, dragAnchor, lastClickLine = -1;
    private long lastTextClick;
    private boolean weaponTab;
    private final WeaponWorkbenchPanel weapons = new WeaponWorkbenchPanel();
    private boolean weaponAccess() { return ((ContainerDescriptionWorkbench)inventorySlots).weaponAccess; }
    private boolean historyAccess() { return ((ContainerDescriptionWorkbench)inventorySlots).historyAccess; }

    private static final class Draft {
        final List<String> text = new ArrayList<String>();
        final List<int[]> styles = new ArrayList<int[]>();
    }
    public GuiDescriptionWorkbench(InventoryPlayer inventory, TileEntityDescriptionWorkbench tile) {
        super(new ContainerDescriptionWorkbench(inventory, tile));
        this.tile = tile; xSize = 420; ySize = 286;
    }
    @Override public void initGui() {
        Draft pending = fields.isEmpty() ? null : snapshot();
        ItemStack previousItem = editing;
        String brush = hex == null ? null : hex.getText();
        List<Draft> history = new ArrayList<Draft>(undo);
        xSize = Math.min(420, width - 64);
        super.initGui(); Keyboard.enableRepeatEvents(true); fields.clear(); buttonList.clear();
        editorWidth = (xSize - 24) / 2; previewX = editorWidth + 16;
        for (int i = 0; i < ItemDescription.MAX_LINES; i++) {
            GuiTextField field = new GuiTextField(fontRendererObj, guiLeft + 16, guiTop + 32 + i * 15, editorWidth - 16, 10);
            field.setMaxStringLength(ItemDescription.MAX_LINE_LENGTH); field.setEnableBackgroundDrawing(false); fields.add(field);
        }
        hex = new GuiTextField(fontRendererObj, guiLeft + 16, guiTop + 144, 57, 10);
        hex.setMaxStringLength(6); hex.setEnableBackgroundDrawing(false);
        buttonList.add(new GuiButton(0, guiLeft + 12, guiTop + 182, (editorWidth - 12) / 2, 20, I18n.format("mbo.description.save")));
        buttonList.add(new GuiButton(1, guiLeft + 16 + (editorWidth - 12) / 2, guiTop + 182, (editorWidth - 12) / 2, 20, I18n.format("mbo.description.clear_short")));
        buttonList.add(new GuiButton(2, guiLeft + 81, guiTop + 139, editorWidth - 77, 20, I18n.format("mbo.description.selection")));
        buttonList.add(new GuiButton(3, guiLeft + 12, guiTop + 160, (editorWidth - 12) / 2, 20, I18n.format("mbo.description.line_color")));
        buttonList.add(new GuiButton(4, guiLeft + 16 + (editorWidth - 12) / 2, guiTop + 160, (editorWidth - 12) / 2, 20, I18n.format("mbo.description.all_color")));
        net.minecraft.inventory.Slot input = (net.minecraft.inventory.Slot) inventorySlots.inventorySlots.get(0);
        input.xDisplayPosition = previewX + 7; input.yDisplayPosition = 33;
        int inventoryX = (xSize - 162) / 2;
        for (int i = 1; i < inventorySlots.inventorySlots.size(); i++) {
            net.minecraft.inventory.Slot slot = (net.minecraft.inventory.Slot) inventorySlots.inventorySlots.get(i);
            slot.xDisplayPosition = inventoryX + (i - 1) % 9 * 18;
        }
        weapons.init(fontRendererObj,guiLeft,guiTop,editorWidth+12,previewX);
        loadItem();
        if (pending != null && ItemStack.areItemStacksEqual(previousItem, editing)) {
            styles.clear();
            for (int i = 0; i < fields.size(); i++) {
                fields.get(i).setText(pending.text.get(i)); styles.add(pending.styles.get(i));
            }
            if (brush != null) hex.setText(brush);
            undo.addAll(history);
        }
    }
    private void loadItem() {
        ItemStack stack = tile.getStackInSlot(0);
        editing = stack == null ? null : stack.copy();
        List<String> lines = ItemDescription.read(stack); List<int[]> colors = ItemDescription.colors(stack);
        styles.clear(); undo.clear(); previewScroll = 0;
        for (int i = 0; i < fields.size(); i++) {
            fields.get(i).setText(i < lines.size() ? lines.get(i) : "");
            styles.add(i < colors.size() ? colors.get(i).clone() : new int[0]);
        }
        hex.setText(String.format(Locale.ROOT, "%06X", ItemDescription.color(stack)));
        selectionStart = selectionEnd = 0; activeLine = 0;
        weapons.load(stack);
        previewDirty = true;
    }
    @Override public void updateScreen() {
        super.updateScreen();
        if (!ItemStack.areItemStacksEqual(editing, tile.getStackInSlot(0))) loadItem();
        for (GuiTextField field : fields) field.updateCursorCounter();
        hex.updateCursorCounter();
        if(weaponTab && !weaponAccess()) weaponTab=false;
        for (Object object : buttonList) { ((GuiButton)object).enabled = editing != null && historyAccess(); ((GuiButton)object).visible=!weaponTab; }
        ((GuiButton) buttonList.get(0)).enabled = editing != null && historyAccess() && hex.getText().matches("[0-9a-fA-F]{6}");
    }
    private int color() {
        try { return Integer.parseInt(hex.getText(), 16) & 0xffffff; }
        catch (NumberFormatException invalid) { return 0xffffff; }
    }
    private List<String> text() {
        List<String> result = new ArrayList<String>();
        for (GuiTextField field : fields) result.add(field.getText());
        return result;
    }
    private Draft snapshot() {
        Draft draft = new Draft(); draft.text.addAll(text());
        for (int[] style : styles) draft.styles.add(style.clone());
        return draft;
    }
    private void remember(Draft draft) { undo.push(draft); while (undo.size() > 40) undo.removeLast(); }
    private void undo() {
        if (undo.isEmpty()) return;
        Draft draft = undo.pop(); styles.clear();
        for (int i = 0; i < fields.size(); i++) { fields.get(i).setText(draft.text.get(i)); styles.add(draft.styles.get(i)); }
        selectionStart = selectionEnd = 0;
        previewDirty = true;
    }
    private void captureSelection() {
        for (int i = 0; i < fields.size(); i++) if (fields.get(i).isFocused()) {
            activeLine = i;
            selectionStart = Math.min(fields.get(i).getCursorPosition(), fields.get(i).getSelectionEnd());
            selectionEnd = Math.max(fields.get(i).getCursorPosition(), fields.get(i).getSelectionEnd());
        }
    }
    private void applyColor(int scope) {
        if (scope == 2 && selectionStart == selectionEnd) return;
        previewDirty = true;
        remember(snapshot());
        for (int i = 0; i < fields.size(); i++) {
            if (scope != 4 && i != activeLine) continue;
            int start = scope == 2 ? selectionStart : 0;
            int end = scope == 2 ? selectionEnd : styles.get(i).length;
            if (scope == 2 && start == end) return;
            for (int j = start; j < Math.min(end, styles.get(i).length); j++) styles.get(i)[j] = color();
        }
    }
    @Override protected void actionPerformed(GuiButton button) {
        if (editing == null || !historyAccess()) return;
        if (button.id >= 2) { applyColor(button.id); return; }
        if (button.id == 1) {
            previewDirty = true;
            remember(snapshot());
            for (int i = 0; i < fields.size(); i++) { fields.get(i).setText(""); styles.set(i, new int[0]); }
            return;
        }
        PacketManager.INSTANCE.sendToServer(new PacketItemDescription(inventorySlots.windowId,
                editing.copy(), text(), styles, color()));
    }
    @Override protected void mouseClicked(int x, int y, int button) {
        if(button==0 && x>=guiLeft-27 && x<guiLeft+1) {
            if(y>=guiTop+28 && y<guiTop+50 && historyAccess()) weaponTab=false;
            if(y>=guiTop+54 && y<guiTop+76 && weaponAccess()) weaponTab=true;
            for(Object object:buttonList)((GuiButton)object).visible=!weaponTab;
            return;
        }
        if(weaponTab) { if(weaponAccess() && weapons.click(x,y,button,inventorySlots.windowId))return; super.mouseClicked(x,y,button);return; }
        if(!historyAccess()) {super.mouseClicked(x,y,button);return;}
        captureSelection();
        if (button == 0 && y >= guiTop + 125 && y < guiTop + 135 && x >= guiLeft + 12) {
            int index = (x - guiLeft - 12) / 8;
            if (index < COLORS.length) { hex.setText(String.format(Locale.ROOT, "%06X", COLORS[index])); if (selectionStart != selectionEnd) applyColor(2); return; }
        }
        super.mouseClicked(x, y, button);
        for (GuiTextField field : fields) field.mouseClicked(x, y, button);
        hex.mouseClicked(x, y, button); captureSelection();
        if (button == 0) for (int i = 0; i < fields.size(); i++) if (fields.get(i).isFocused()) {
            GuiTextField field = fields.get(i);
            long now = System.currentTimeMillis();
            dragLine = i; dragAnchor = field.getCursorPosition();
            if (lastClickLine == i && now - lastTextClick < 300) {
                String text = field.getText(); int start = dragAnchor, end = dragAnchor;
                while (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) start--;
                while (end < text.length() && Character.isLetterOrDigit(text.charAt(end))) end++;
                field.setCursorPosition(start); field.setSelectionPos(end); dragLine = -1;
                captureSelection();
            }
            lastClickLine = i; lastTextClick = now;
        }
    }
    @Override protected void mouseClickMove(int x, int y, int button, long elapsed) {
        if (button == 0 && dragLine >= 0) {
            GuiTextField field = fields.get(dragLine);
            int clampedX = Math.max(guiLeft + 16, Math.min(guiLeft + editorWidth, x));
            field.mouseClicked(clampedX, guiTop + 33 + dragLine * 15, 0);
            field.setSelectionPos(dragAnchor); captureSelection();
        } else super.mouseClickMove(x, y, button, elapsed);
    }
    @Override protected void mouseMovedOrUp(int x, int y, int button) {
        if (button == 0) dragLine = -1;
        super.mouseMovedOrUp(x, y, button);
    }
    @Override protected void keyTyped(char c, int key) {
        if(weaponTab) {
            if(key!=Keyboard.KEY_ESCAPE && weaponAccess() && weapons.key(c,key)) return;
            super.keyTyped(c,key);return;
        }
        if(!historyAccess()) {super.keyTyped(c,key);return;}
        if (isCtrlKeyDown() && key == Keyboard.KEY_Z) { undo(); return; }
        if (key == Keyboard.KEY_ESCAPE) { super.keyTyped(c, key); return; }
        for (int i = 0; i < fields.size(); i++) if (fields.get(i).isFocused()) {
            if (key == Keyboard.KEY_TAB || key == Keyboard.KEY_RETURN) {
                fields.get(i).setFocused(false); fields.get((i + 1) % fields.size()).setFocused(true);
            } else {
                Draft before = snapshot(); String old = fields.get(i).getText(); fields.get(i).textboxKeyTyped(c, key);
                if (!old.equals(fields.get(i).getText())) {
                    previewDirty = true;
                    remember(before); styles.set(i, ItemDescription.editColors(old, fields.get(i).getText(), styles.get(i), color()));
                }
            }
            captureSelection(); return;
        }
        if (hex.isFocused()) { hex.textboxKeyTyped(c, key); return; }
        super.keyTyped(c, key);
    }
    @Override public void handleMouseInput() {
        super.handleMouseInput(); int wheel = Mouse.getEventDWheel();
        int x = Mouse.getEventX() * width / mc.displayWidth;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        if (x >= guiLeft + previewX && x < guiLeft + xSize - 8 && y >= guiTop + 55 && y < guiTop + 195)
            previewScroll = Math.max(0, previewScroll - Integer.signum(wheel) * 20);
    }
    @Override protected void drawGuiContainerBackgroundLayer(float partial, int x, int y) {
        DescriptionWorkbenchSkin.background(guiLeft,guiTop,xSize,ySize);
        if(historyAccess())DescriptionWorkbenchSkin.tab(guiLeft-27,guiTop+28,!weaponTab);
        if(weaponAccess())DescriptionWorkbenchSkin.tab(guiLeft-27,guiTop+54,weaponTab);
        DescriptionWorkbenchSkin.panel(guiLeft+previewX,guiTop+28,xSize-previewX-8,171);
        net.minecraft.inventory.Slot input = (net.minecraft.inventory.Slot)inventorySlots.inventorySlots.get(0);
        DescriptionWorkbenchSkin.inputSlot(guiLeft+input.xDisplayPosition-1,guiTop+input.yDisplayPosition-1);
        DescriptionWorkbenchSkin.inventory(guiLeft+(xSize-162)/2-1,guiTop+204);
        if(!weaponTab)for (int i=0;i<COLORS.length;i++) DescriptionWorkbenchSkin.swatch(guiLeft+12+i*8,guiTop+125,i);
    }
    @Override protected void drawGuiContainerForegroundLayer(int x, int y) {
        fontRendererObj.drawString(I18n.format("container.mbo.description_workbench"), 12, 10, 0xf1dfac);
        fontRendererObj.drawString(I18n.format("mbo.description.preview"), previewX, 17, 0xd0c2a6);
        if (editing != null) fontRendererObj.drawString(fontRendererObj.trimStringToWidth(editing.getDisplayName(), xSize - previewX - 35), previewX + 29, 37, 0xd0c2a6);
        // GuiContainer renders this layer before its hovered-item tooltip.
        GL11.glPushMatrix(); GL11.glTranslatef(-guiLeft,-guiTop,0);
        try { drawEditorWidgets(x,y); } finally { GL11.glPopMatrix(); }
    }
    private void drawPreview() {
        if (editing == null) return;
        if (previewDirty) {
            ItemStack draft = editing.copy(); ItemDescription.write(draft, text(), styles, color());
            previewRows = ItemDescriptionTooltips.preview(draft, fontRendererObj, xSize - previewX - 32);
            if (previewRows.isEmpty()) previewRows.add(I18n.format("mbo.description.preview_empty"));
            else { previewRows.set(0, "<mbo:statline><mbo:scroll>" + previewRows.get(0)); TooltipElements.expandBlocks(previewRows); }
            previewDirty = false;
        }
        List<String> rows = previewRows;
        previewScroll = Math.min(previewScroll, Math.max(0, rows.size() * 10 - 130));
        double scaleX = mc.displayWidth / (double)width, scaleY = mc.displayHeight / (double)height;
        boolean scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        oldScissor.clear(); GL11.glGetInteger(GL11.GL_SCISSOR_BOX, oldScissor);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)((guiLeft + previewX + 3) * scaleX), (int)(mc.displayHeight - (guiTop + 195) * scaleY),
                (int)((xSize - previewX - 14) * scaleX), (int)(136 * scaleY));
        for (int i = 0; i < rows.size(); i++) {
            String row = rows.get(i);
            if (!(fontRendererObj instanceof ModernFontRenderer)) row = row.replaceAll("<mbo:[^>]+>", "");
            fontRendererObj.drawStringWithShadow(row, guiLeft + previewX + 7, guiTop + 59 + i * 10 - previewScroll, 0xffd4c4a5);
        }
        GL11.glScissor(oldScissor.get(0), oldScissor.get(1), oldScissor.get(2), oldScissor.get(3));
        if (!scissor) GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }
    private void drawEditorWidgets(int mouseX,int mouseY) {
        if(historyAccess())fontRendererObj.drawString("<mbo:scroll>",guiLeft-20,guiTop+33,0xffffff);
        if(weaponAccess())fontRendererObj.drawString("<mbo:weapon>",guiLeft-20,guiTop+59,0xffffff);
        if(weaponTab) { weapons.draw(mouseX,mouseY); return; }
        if(!historyAccess()) return;
        for (int i=0;i<fields.size();i++) {
            DescriptionWorkbenchSkin.field(guiLeft+12,guiTop+29+i*15,editorWidth-8,13,i==activeLine);
            fields.get(i).setTextColor(0xe0d5bf); fields.get(i).drawTextBox();
        }
        DescriptionWorkbenchSkin.field(guiLeft+12,guiTop+141,65,14,hex.isFocused());
        hex.drawTextBox();
        drawPreview();
    }
    @Override public void onGuiClosed() { super.onGuiClosed(); Keyboard.enableRepeatEvents(false); }
}
