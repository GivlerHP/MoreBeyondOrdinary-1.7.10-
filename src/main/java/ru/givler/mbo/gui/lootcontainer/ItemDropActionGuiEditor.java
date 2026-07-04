package ru.givler.mbo.gui.lootcontainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.lootcontainer.action.ItemDropAction;
import ru.givler.mbo.lootcontainer.action.LootContainerAction;
import ru.givler.mbo.lootcontainer.action.LootContainerActionRegistry;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ItemDropActionGuiEditor extends AbstractLootActionGuiEditor<ItemDropAction> {
    public static final ActionEditorType EDITOR_TYPE = new ActionEditorType(
            LootContainerActionRegistry.TYPE_ITEM_DROP,
            "Item Drop",
            ItemDropAction.class,
            ItemDropActionGuiEditor::newDefault,
            action -> new ItemDropActionGuiEditor((ItemDropAction) action)
    );

//    private static final int VALUE_FIELD_WIDTH = 130;
    private static final int USE_ITEM_BUTTON_X_OFFSET = 2;
    private static final int USE_ITEM_BUTTON_Y_OFFSET = 20;
//    private static final int EXTRA_FIELD_X_OFFSET = 270;
//    private static final int EXTRA_FIELD_WIDTH = 44;
    private static final int PREVIEW_X_OFFSET = 260;
    private static final int PREVIEW_Y_OFFSET = 3;
    private static final int PREVIEW_SIZE = 16;

    private String itemIdInput;
    private String countExpressionInput;
    private GuiTextField itemIdField;
    private GuiTextField countField;
    private GuiButton useItemButton;

    public ItemDropActionGuiEditor(ItemDropAction action) {
        super(action);
        itemIdInput = action.itemId == null ? "" : action.itemId;
        countExpressionInput = action.countExpr == null || action.countExpr.trim().isEmpty() ? "1" : action.countExpr;
    }

    private static AbstractLootActionGuiEditor<?> newDefault() {
        return new ItemDropActionGuiEditor(new ItemDropAction());
    }

    @Override
    protected void initInternal(ActionEditorContext context) {
        itemIdField = newTextField(VALUE_FIELD_WIDTH, 96);
        countField = newTextField(EXTRA_FIELD_WIDTH, 32);
        useItemButton = new GuiButton(6000 + context.actionIndex, 0, 0, 100, 14, "Use item...");
        context.buttonList.add(useItemButton);
        syncFieldsFromState();
    }

    @Override
    public int getHeight() {
        return 40;
    }

//    @Override
//    protected int getChanceFieldXOffset() {
//        return 346;
//    }

    @Override
    protected void setBoundsInternal(int contentX, int y, int contentWidth) {
        itemIdField.xPosition = contentX;
        itemIdField.yPosition = y + 3;
        itemIdField.width = VALUE_FIELD_WIDTH;
        countField.xPosition = x + EXTRA_FIELD_X_OFFSET;
        countField.yPosition = y + 3;
        countField.width = EXTRA_FIELD_WIDTH;
        useItemButton.xPosition = x + USE_ITEM_BUTTON_X_OFFSET;
        useItemButton.yPosition = y + USE_ITEM_BUTTON_Y_OFFSET;
        useItemButton.visible = true;
    }

    @Override
    protected void renderInternal(int mouseX, int mouseY) {
        drawLabeledField(itemIdField, "id");
        drawLabeledField(countField, "qty");
        drawItemPreview();
    }

    @Override
    protected void renderPostInternal(int mouseX, int mouseY) {
        ItemStack stack = getPreviewStack();
        if (stack == null || !isOverPreview(mouseX, mouseY)) return;
        List<String> lines = new ArrayList<String>();
        lines.add(stack.getDisplayName());
        appendPreviewTooltip(lines);
        drawHoverText(lines, mouseX, mouseY);
    }

    @Override
    protected boolean keyTypedInternal(char c, int code) {
        boolean changed = itemIdField.textboxKeyTyped(c, code);
        changed |= countField.textboxKeyTyped(c, code);
        if (changed) readStateFromFields();
        return changed;
    }

    @Override
    protected void mouseClickedInternal(int mouseX, int mouseY, int button) {
        itemIdField.mouseClicked(mouseX, mouseY, button);
        countField.mouseClicked(mouseX, mouseY, button);
        readStateFromFields();
    }

    @Override
    protected void writeStateToAction() {
        readStateFromFields();
        action.itemId = itemIdInput;
        action.countExpr = countExpressionInput;
    }

    @Override
    public void onItemPicked(ItemStack selected) {
        if (selected == null) return;
        action.itemId = String.valueOf(Item.itemRegistry.getNameForObject(selected.getItem()));
        action.itemMeta = selected.getItemDamage();
        action.itemNbt = selected.stackTagCompound == null ? "" : selected.stackTagCompound.toString();
        if (countExpressionInput == null || countExpressionInput.trim().isEmpty()) {
            countExpressionInput = "1";
        }
        itemIdInput = action.itemId;
        syncFieldsFromState();
    }

    @Override
    public boolean actionPerformed(GuiButton button) {
        if (button != useItemButton) return false;
        if (context.host != null) {
            context.host.openItemPicker(context.actionIndex);
        }
        return true;
    }

    @Override
    public void dispose() {
        if (useItemButton != null) {
            context.buttonList.remove(useItemButton);
            useItemButton = null;
        }
    }

    public ItemStack getPreviewStack() {
        if (itemIdInput == null || itemIdInput.trim().isEmpty()) return null;
        Item item = (Item) Item.itemRegistry.getObject(itemIdInput);
        if (item == null) return null;
        return new ItemStack(item, 1, Math.max(0, action.itemMeta));
    }

    public void appendPreviewTooltip(List<String> lines) {
        if (lines == null) return;
        if (itemIdInput != null && !itemIdInput.trim().isEmpty()) {
            lines.add(itemIdInput);
        }
    }

    @Override
    protected void collectValidationErrorsInternal(List<String> errors, String prefix) {
        readStateFromFields();
        String itemId = itemIdInput == null ? "" : itemIdInput.trim();
        String count = countExpressionInput == null ? "" : countExpressionInput.trim();
        if (!itemId.isEmpty() && !isValidCountExpression(count)) {
            errors.add(prefix + "count must be N or N-M with non-negative integers.");
        }
    }

    private void syncFieldsFromState() {
        if (itemIdField != null) itemIdField.setText(itemIdInput == null ? "" : itemIdInput);
        if (countField != null) countField.setText(countExpressionInput == null ? "1" : countExpressionInput);
    }

    private void readStateFromFields() {
        itemIdInput = itemIdField == null ? "" : (itemIdField.getText() == null ? "" : itemIdField.getText());
        countExpressionInput = countField == null ? "" : (countField.getText() == null ? "" : countField.getText());
    }

    private void drawItemPreview() {
        int previewX = x + PREVIEW_X_OFFSET;
        int previewY = y + PREVIEW_Y_OFFSET;
        drawRect(previewX - 1, previewY - 1, previewX + PREVIEW_SIZE + 1, previewY + PREVIEW_SIZE + 1, 0xFF202020);
        drawRect(previewX, previewY, previewX + PREVIEW_SIZE, previewY + PREVIEW_SIZE, 0xFF5B5B5B);
        ItemStack stack = getPreviewStack();
        if (stack == null) return;
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        context.itemRenderer.renderItemAndEffectIntoGUI(fontRenderer, Minecraft.getMinecraft().getTextureManager(), stack, previewX, previewY);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        RenderHelper.disableStandardItemLighting();
    }

    private boolean isOverPreview(int mouseX, int mouseY) {
        int previewX = x + PREVIEW_X_OFFSET;
        int previewY = y + PREVIEW_Y_OFFSET;
        return mouseX >= previewX && mouseX <= previewX + PREVIEW_SIZE
                && mouseY >= previewY && mouseY <= previewY + PREVIEW_SIZE;
    }
}
