package ru.givler.mbo.gui.lootcontainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.lootcontainer.action.LootContainerAction;

import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class AbstractLootActionGuiEditor<T extends LootContainerAction> extends Gui {
    protected static final int DROPDOWN_VISIBLE_ROWS = 8;
    protected static final int DROPDOWN_ROW_HEIGHT = 12;
    protected static final int DROPDOWN_VERTICAL_OFFSET = 18;
    protected static final int DROPDOWN_BOX_PADDING = 4;

    public static final int VALUE_FIELD_WIDTH = 132;
    public static final int EXTRA_FIELD_X_OFFSET = 334;
    public static final int EXTRA_FIELD_WIDTH = 40;

    private static final int DEFAULT_HEIGHT = 24;
    private static final int DEFAULT_CONTENT_X_OFFSET = 124;
    private static final int CHANCE_FIELD_Y_OFFSET = 3;
    private static final int CHANCE_FIELD_WIDTH = 30;
    private static final int CHANCE_FIELD_X_OFFSET = 397;
    private static final int LABEL_GAP = 4;
    private static final int LABEL_Y_OFFSET = 4;
    private static final int DROPDOWN_ENTRY_HEIGHT = 12;
    private static final int DROPDOWN_ENTRY_INSET = 2;
    private static final int DROPDOWN_TEXT_X_OFFSET = 4;
    private static final int DROPDOWN_TEXT_Y_OFFSET = 2;

    protected final T action;
    protected ActionEditorContext context;
    protected FontRenderer fontRenderer;
    protected int x;
    protected int y;
    protected int width;
    protected int contentX;
    protected int contentWidth;

    private GuiTextField chanceField;

    protected AbstractLootActionGuiEditor(T action) {
        this.action = action;
    }

    public final void init(ActionEditorContext context) {
        this.context = context;
        this.fontRenderer = context.fontRenderer;
        this.chanceField = new GuiTextField(fontRenderer, 0, 0, CHANCE_FIELD_WIDTH, 16);
        this.chanceField.setMaxStringLength(6);
        setChance(action == null ? 100.0F : action.getChance());
        initInternal(context);
    }

    protected void initInternal(ActionEditorContext context) {
        // no-op by default
    }

    public int getHeight() {
        return DEFAULT_HEIGHT;
    }

    public final void setBounds(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.contentX = x + getContentXOffset();
        this.contentWidth = Math.max(0, width - getContentXOffset());

        chanceField.xPosition = x + CHANCE_FIELD_X_OFFSET;
        chanceField.yPosition = y + CHANCE_FIELD_Y_OFFSET;
        chanceField.width = CHANCE_FIELD_WIDTH;

        setBoundsInternal(contentX, y, contentWidth);
    }

    protected void setBoundsInternal(int contentX, int y, int contentWidth) {
        // no-op by default
    }

    protected int getContentXOffset() {
        return DEFAULT_CONTENT_X_OFFSET;
    }

    public final void renderPre(int mouseX, int mouseY) {
        renderPreInternal(mouseX, mouseY);
    }

    protected void renderPreInternal(int mouseX, int mouseY) {
        // no-op by default
    }

    public final void render(int mouseX, int mouseY) {
        renderInternal(mouseX, mouseY);
        chanceField.drawTextBox();
        drawFieldLabel(chanceField, "ch");
    }

    protected void renderInternal(int mouseX, int mouseY) {
        // no-op by default
    }

    public final void renderPost(int mouseX, int mouseY) {
        renderPostInternal(mouseX, mouseY);
    }

    protected void renderPostInternal(int mouseX, int mouseY) {
        // no-op by default
    }

    public final boolean keyTyped(char c, int code) {
        boolean changed = chanceField.textboxKeyTyped(c, code);
        changed |= keyTypedInternal(c, code);
        return changed;
    }

    protected boolean keyTypedInternal(char c, int code) {
        return false;
    }

    public final void mouseClicked(int mouseX, int mouseY, int button) {
        chanceField.mouseClicked(mouseX, mouseY, button);
        mouseClickedInternal(mouseX, mouseY, button);
    }

    protected void mouseClickedInternal(int mouseX, int mouseY, int button) {
        // no-op by default
    }

    public boolean handleMouseInput(int mouseX, int mouseY, int wheel) {
        return false;
    }

    public boolean tryClickOverlay(int mouseX, int mouseY, int button) {
        return false;
    }

    public boolean actionPerformed(GuiButton button) {
        return false;
    }

    public void dispose() {
        // no-op by default
    }

    public void collectValidationErrors(List<String> errors, String prefix) {
        if (errors == null) return;
        if (!isValidNonNegativeNumber(chanceField.getText())) {
            errors.add(prefix + "chance must be a non-negative number.");
            return;
        }
        collectValidationErrorsInternal(errors, prefix);
    }

    protected void collectValidationErrorsInternal(List<String> errors, String prefix) {
        // no-op by default
    }

    public void onItemPicked(ItemStack selected) {
        // no-op by default
    }

    public final LootContainerAction toAction() {
        writeStateToAction();
        action.setChance(resolveChance());
        return action;
    }

    protected abstract void writeStateToAction();

    public final void setChance(float chance) {
        if (chanceField != null) {
            chanceField.setText(formatChanceInput(chance));
        }
    }

    protected final float resolveChance() {
        return parseChance(chanceField == null ? "" : chanceField.getText());
    }

    protected final GuiTextField newTextField(int width, int maxLength) {
        GuiTextField field = new GuiTextField(fontRenderer, 0, 0, width, 16);
        field.setMaxStringLength(maxLength);
        return field;
    }

    protected final void drawLabeledField(GuiTextField field, String label) {
        if (field == null) return;
        field.drawTextBox();
        drawFieldLabel(field, label);
    }

    protected final void drawFieldLabel(GuiTextField field, String label) {
        if (field == null || label == null || label.isEmpty()) return;
        int labelX = field.xPosition - fontRenderer.getStringWidth(label) - LABEL_GAP;
        drawString(fontRenderer, label, labelX, field.yPosition + LABEL_Y_OFFSET, 0xBFBFBF);
    }

    protected final void drawDropdown(LootActionDropdownState state, int mouseX, int mouseY) {
        if (state == null) return;
        drawRect(state.boxX, state.boxY, state.boxX + state.boxWidth, state.boxY + state.boxHeight, 0xE0101010);
        for (int i = 0; i < state.shown; i++) {
            int idx = i + state.scroll;
            if (idx >= state.matches.size()) break;
            LootActionDropdownOption value = state.matches.get(idx);
            int lineY = state.boxY + 2 + i * DROPDOWN_ENTRY_HEIGHT;
            boolean hovered = mouseX >= state.boxX + DROPDOWN_ENTRY_INSET
                    && mouseX <= state.boxX + state.boxWidth - DROPDOWN_ENTRY_INSET
                    && mouseY >= lineY
                    && mouseY <= lineY + 10;
            int color = hovered ? 0xFFFFFF : 0xE0E0E0;
            drawString(fontRenderer, value.label, state.boxX + DROPDOWN_TEXT_X_OFFSET, lineY + DROPDOWN_TEXT_Y_OFFSET, color);
        }
    }

    protected final boolean isMouseOverDropdown(LootActionDropdownState state, int mouseX, int mouseY) {
        return state != null
                && mouseX >= state.boxX
                && mouseX <= state.boxX + state.boxWidth
                && mouseY >= state.boxY
                && mouseY <= state.boxY + state.boxHeight;
    }

    protected final LootActionDropdownOption getClickedDropdownOption(LootActionDropdownState state, int mouseX, int mouseY) {
        if (state == null) return null;
        for (int i = 0; i < state.shown; i++) {
            int idx = i + state.scroll;
            if (idx >= state.matches.size()) break;
            int lineY = state.boxY + 2 + i * DROPDOWN_ENTRY_HEIGHT;
            boolean clicked = mouseX >= state.boxX + DROPDOWN_ENTRY_INSET
                    && mouseX <= state.boxX + state.boxWidth - DROPDOWN_ENTRY_INSET
                    && mouseY >= lineY
                    && mouseY <= lineY + 10;
            if (clicked) return state.matches.get(idx);
        }
        return null;
    }

    protected final void drawHoverText(List<String> lines, int mouseX, int mouseY) {
        if (lines == null || lines.isEmpty()) return;
        int maxWidth = 0;
        for (String line : lines) {
            maxWidth = Math.max(maxWidth, fontRenderer.getStringWidth(line));
        }
        int tx = mouseX + 8;
        int ty = mouseY - 10;
        int height = 8 + (lines.size() - 1) * 10;

        this.zLevel = 300.0F;
        drawGradientRect(tx - 3, ty - 4, tx + maxWidth + 3, ty + height + 4, 0xF0100010, 0xF0100010);
        for (int i = 0; i < lines.size(); i++) {
            fontRenderer.drawStringWithShadow(lines.get(i), tx, ty + i * 10, 0xFFFFFF);
        }
        this.zLevel = 0.0F;
    }

    protected static int clampInt(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    protected static int parseIntOrDefault(String value, int fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    protected static float parseFloatOrDefault(String value, float fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    protected static boolean isValidPositiveInt(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        try {
            return Integer.parseInt(text.trim()) > 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    protected static boolean isValidNonNegativeInt(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        try {
            return Integer.parseInt(text.trim()) >= 0;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    protected static boolean isValidPositiveNumber(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        try {
            return Double.parseDouble(text.trim()) > 0.0D;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    protected static boolean isValidNonNegativeNumber(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        try {
            return Double.parseDouble(text.trim()) >= 0.0D;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    protected static boolean isValidCountExpression(String text) {
        if (text == null) return false;
        String value = text.trim();
        if (value.isEmpty()) return false;
        int dash = value.indexOf('-');
        if (dash < 0) {
            return isValidNonNegativeInt(value);
        }
        if (dash == 0 || dash == value.length() - 1 || value.indexOf('-', dash + 1) >= 0) return false;
        String left = value.substring(0, dash).trim();
        String right = value.substring(dash + 1).trim();
        if (!isValidNonNegativeInt(left) || !isValidNonNegativeInt(right)) return false;
        return Integer.parseInt(right) >= Integer.parseInt(left);
    }

    protected static boolean isValidPositiveCountExpression(String text) {
        if (text == null) return false;
        String value = text.trim();
        if (value.isEmpty()) return false;
        int dash = value.indexOf('-');
        if (dash < 0) {
            return isValidPositiveInt(value);
        }
        if (dash == 0 || dash == value.length() - 1 || value.indexOf('-', dash + 1) >= 0) return false;
        String left = value.substring(0, dash).trim();
        String right = value.substring(dash + 1).trim();
        if (!isValidPositiveInt(left) || !isValidPositiveInt(right)) return false;
        return Integer.parseInt(right) >= Integer.parseInt(left);
    }

    private static String formatChanceInput(float chance) {
        return String.valueOf(Math.max(0, Math.round(chance)));
    }

    private static float parseChance(String value) {
        if (value == null || value.trim().isEmpty()) return 100.0F;
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException ignored) {
            try {
                return Math.max(0, Math.round(Float.parseFloat(value.trim())));
            } catch (NumberFormatException ignored2) {
                return 100.0F;
            }
        }
    }
}
