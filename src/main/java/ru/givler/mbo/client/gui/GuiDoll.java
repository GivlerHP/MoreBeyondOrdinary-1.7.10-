package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiButton;
import cpw.mods.fml.client.config.GuiButtonExt;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import ru.givler.mbo.client.render.DollPlayer;
import ru.givler.mbo.container.ContainerDoll;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketDollPose;
import ru.givler.mbo.tileentity.TileDoll;

public final class GuiDoll extends GuiContainer {
    private static final int VIEW_X = 151, VIEW_Y = 21, VIEW_W = 163, VIEW_H = 78;
    private static final String[] PARTS = {"head", "body", "right_arm", "left_arm", "right_leg", "left_leg", "offset"};
    private static final String[] SLOTS = {"head", "body", "legs", "feet", "right_hand", "left_hand"};
    private static final int TRACK_X = 54, TRACK_WIDTH = 48, AXIS_Y = 45, AXIS_STEP = 18;
    private static final int[][] BODY_PARTS = {
        {19, 45, 12, 12}, {19, 58, 12, 13}, {12, 58, 6, 13}, {32, 58, 6, 13}, {19, 72, 6, 15}, {26, 72, 5, 15}
    };
    private final TileDoll doll;
    private final GuiButton[] decrease = new GuiButton[3], increase = new GuiButton[3];
    private final GuiTextField[] values = new GuiTextField[3];
    private DollPlayer model;
    private int part, previousX, previousY;
    private float yaw = 24, pitch = -7;
    private boolean dragging;
    private int dragAxis = -1, dragValue;

    public GuiDoll(EntityPlayer player, TileDoll doll) {
        super(new ContainerDoll(player, doll));
        this.doll = doll;
        xSize = 320;
        ySize = 185;
    }

    @Override @SuppressWarnings("unchecked") public void initGui() {
        super.initGui();
        buttonList.clear();
        buttonList.add(new GuiButtonExt(0, guiLeft + 8, guiTop + 23, 15, 16, "<"));
        buttonList.add(new GuiButtonExt(1, guiLeft + 131, guiTop + 23, 15, 16, ">"));
        for (int i = 0; i < 3; i++) {
            int y = AXIS_Y + i * AXIS_STEP;
            decrease[i] = new GuiButtonExt(2 + i * 2, guiLeft + 128, guiTop + y, 9, 16, "-");
            increase[i] = new GuiButtonExt(3 + i * 2, guiLeft + 138, guiTop + y, 9, 16, "+");
            values[i] = new GuiTextField(fontRendererObj, guiLeft + 104, guiTop + y + 4, 22, 12);
            values[i].setMaxStringLength(5);
            values[i].setEnableBackgroundDrawing(false);
            buttonList.add(decrease[i]);
            buttonList.add(increase[i]);
        }
        buttonList.add(new GuiButtonExt(8, guiLeft + 8, guiTop + 103, 68, 17, I18n.format("mbo.doll.reset")));
        buttonList.add(new GuiButtonExt(9, guiLeft + 78, guiTop + 103, 68, 17, I18n.format("mbo.doll.rotate")));
    }

    @Override protected void actionPerformed(GuiButton button) {
        if (button.id < 2) {
            part = (part + (button.id == 0 ? PARTS.length - 1 : 1)) % PARTS.length;
            return;
        }
        if (button.id < 8) {
            int axis = (button.id - 2) / 2;
            sendValue(axis, displayedValue(axis) + ((button.id & 1) == 0 ? -1 : 1));
        } else {
            if (button.id == 8) doll.resetPose();
            mc.playerController.sendEnchantPacket(inventorySlots.windowId, button.id == 8 ? ContainerDoll.RESET : ContainerDoll.ROTATE);
        }
    }

    @Override protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY) {
        DescriptionWorkbenchSkin.background(guiLeft, guiTop, xSize, ySize);
        DescriptionWorkbenchSkin.panel(guiLeft + 5, guiTop + 21, 144, 103);
        DescriptionWorkbenchSkin.panel(guiLeft + VIEW_X, guiTop + VIEW_Y, VIEW_W, VIEW_H);
        DescriptionWorkbenchSkin.inventory(guiLeft + 152, guiTop + 104);
        for (int i = 0; i < TileDoll.SIZE; i++) DescriptionWorkbenchSkin.inputSlot(guiLeft + 7 + i * 24, guiTop + 137);
        if (dragAxis >= 0) doll.setPose(part * 3 + dragAxis, dragValue);
        model = DollPlayer.update(model, doll, mc.theWorld);
        MannequinPreview.draw(mc, model, guiLeft + VIEW_X, guiTop + VIEW_Y, VIEW_W, VIEW_H, 28,
            width, height, yaw, pitch);
    }

    @Override protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(I18n.format("mbo.doll.title"), 8, 8, 0xE4D2AC);
        String section = I18n.format("mbo.doll.part." + PARTS[part]);
        fontRendererObj.drawString(section, 77 - fontRendererObj.getStringWidth(section) / 2, 28, 0xE4D2AC);
        for (int i = 0; i < BODY_PARTS.length; i++) {
            int[] bounds = BODY_PARTS[i];
            int color = part == i ? 0xFFE0BE88 : 0xFF82684C;
            drawRect(bounds[0], bounds[1], bounds[0] + bounds[2], bounds[1] + bounds[3], color);
        }
        fontRendererObj.drawString("XYZ", 15, 93, part == TileDoll.PARTS ? 0xFFF0C6 : 0xB9A687);
        for (int axis = 0; axis < 3; axis++) {
            int value = displayedValue(axis), limit = limit();
            decrease[axis].enabled = value > -limit;
            increase[axis].enabled = value < limit;
            int y = AXIS_Y + axis * AXIS_STEP;
            fontRendererObj.drawString(String.valueOf("XYZ".charAt(axis)), 45, y + 4, 0xB9A687);
            drawRect(TRACK_X, y + 7, TRACK_X + TRACK_WIDTH, y + 9, 0xFF725940);
            int thumb = TRACK_X + Math.round((TRACK_WIDTH - 1F) * (value + limit) / (2 * limit));
            drawRect(thumb - 1, y + 3, thumb + 2, y + 13, dragAxis == axis ? 0xFFFFD78C : 0xFFD6BC91);
        }
        fontRendererObj.drawString(I18n.format("mbo.doll.equipment"), 8, 126, 0xE4D2AC);
    }

    @Override public void drawScreen(int mouseX, int mouseY, float partial) {
        if (dragAxis >= 0) updateSlider(mouseX);
        if (dragging) {
            yaw = (yaw + (mouseX - previousX) * 2.5F) % 360F;
            pitch = Math.max(-55, Math.min(55, pitch + (mouseY - previousY) * 2F));
            previousX = mouseX;
            previousY = mouseY;
        }
        super.drawScreen(mouseX, mouseY, partial);
        for (int axis = 0; axis < values.length; axis++) {
            GuiTextField field = values[axis];
            if (!field.isFocused()) {
                String text = part < TileDoll.PARTS ? Integer.toString(displayedValue(axis))
                    : String.format(java.util.Locale.ROOT, "%.2f", displayedValue(axis) * .05);
                if (!text.equals(field.getText())) field.setText(text);
            }
            DescriptionWorkbenchSkin.field(guiLeft + 103, guiTop + AXIS_Y + axis * AXIS_STEP + 2, 24, 12, field.isFocused());
            field.drawTextBox();
        }
        if (mc.thePlayer.inventory.getItemStack() != null) return;
        for (int i = 0; i < TileDoll.SIZE; i++) {
            if (doll.getStackInSlot(i) == null && contains(8 + i * 24, 138, 16, 16, mouseX, mouseY)) {
                drawHoveringText(java.util.Collections.singletonList(I18n.format("mbo.doll.part." + SLOTS[i])),
                    mouseX, mouseY, fontRendererObj);
                break;
            }
        }
    }

    private boolean contains(int x, int y, int w, int h, int mouseX, int mouseY) {
        return mouseX >= guiLeft + x && mouseX < guiLeft + x + w && mouseY >= guiTop + y && mouseY < guiTop + y + h;
    }

    @Override protected void mouseClicked(int x, int y, int button) {
        if (button == 0) {
            for (int axis = 0; axis < values.length; axis++) {
                if (contains(103, AXIS_Y + axis * AXIS_STEP + 2, 24, 12, x, y)) {
                    if (!values[axis].isFocused()) finishValue();
                    values[axis].mouseClicked(x, y, button);
                    values[axis].setFocused(true);
                    return;
                }
            }
            finishValue();
            for (int index = 0; index < BODY_PARTS.length; index++) {
                int[] bounds = BODY_PARTS[index];
                if (contains(bounds[0], bounds[1], bounds[2], bounds[3], x, y)) { part = index; return; }
            }
            if (contains(12, 89, 25, 13, x, y)) { part = TileDoll.PARTS; return; }
            for (int axis = 0; axis < 3; axis++) {
                if (contains(TRACK_X, AXIS_Y + axis * AXIS_STEP, TRACK_WIDTH, 16, x, y)) {
                    dragAxis = axis;
                    updateSlider(x);
                    return;
                }
            }
        }
        if (button == 0 && contains(VIEW_X, VIEW_Y, VIEW_W, VIEW_H, x, y)) {
            dragging = true;
            previousX = x;
            previousY = y;
            return;
        }
        super.mouseClicked(x, y, button);
    }

    @Override protected void mouseMovedOrUp(int x, int y, int button) {
        if (button == 0 && dragAxis >= 0) {
            updateSlider(x);
            int axis = dragAxis;
            dragAxis = -1;
            sendValue(axis, dragValue);
            return;
        }
        if (button == 0 && dragging) { dragging = false; return; }
        super.mouseMovedOrUp(x, y, button);
    }

    @Override public void updateScreen() {
        super.updateScreen();
        for (GuiTextField value : values) value.updateCursorCounter();
    }

    @Override protected void keyTyped(char character, int key) {
        for (GuiTextField value : values) {
            if (!value.isFocused()) continue;
            if (key == org.lwjgl.input.Keyboard.KEY_RETURN || key == org.lwjgl.input.Keyboard.KEY_NUMPADENTER) finishValue();
            else if (key == org.lwjgl.input.Keyboard.KEY_ESCAPE) value.setFocused(false);
            else value.textboxKeyTyped(character, key);
            return;
        }
        super.keyTyped(character, key);
    }

    private void finishValue() {
        for (int axis = 0; axis < values.length; axis++) {
            GuiTextField field = values[axis];
            if (!field.isFocused()) continue;
            field.setFocused(false);
            try {
                int value = part < TileDoll.PARTS ? Integer.parseInt(field.getText())
                    : (int) Math.round(Double.parseDouble(field.getText().replace(',', '.')) * 20);
                sendValue(axis, value);
            } catch (NumberFormatException ignored) {
                // The text box returns to the current pose value next frame.
            }
        }
    }

    private int limit() { return part < TileDoll.PARTS ? 180 : 10; }

    private int displayedValue(int axis) { return dragAxis == axis ? dragValue : doll.pose(part * 3 + axis); }

    private void updateSlider(int mouseX) {
        float fraction = Math.max(0, Math.min(1, (mouseX - guiLeft - TRACK_X) / (TRACK_WIDTH - 1F)));
        dragValue = Math.round(fraction * 2 * limit()) - limit();
    }

    private void sendValue(int axis, int value) {
        int index = part * 3 + axis;
        int bounded = Math.max(-limit(), Math.min(limit(), value));
        doll.setPose(index, bounded);
        PacketManager.INSTANCE.sendToServer(new PacketDollPose(inventorySlots.windowId, index, bounded));
    }
}
