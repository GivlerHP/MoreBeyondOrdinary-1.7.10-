package ru.givler.mbo.gui.lootcontainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiTextField;
import ru.givler.mbo.lootcontainer.action.BurningAction;
import ru.givler.mbo.lootcontainer.action.LootContainerActionRegistry;

import java.util.List;

@SideOnly(Side.CLIENT)
public class BurningActionGuiEditor extends AbstractLootActionGuiEditor<BurningAction> {
    public static final ActionEditorType EDITOR_TYPE = new ActionEditorType(
            LootContainerActionRegistry.TYPE_BURNING,
            "Burning",
            BurningAction.class,
            BurningActionGuiEditor::newDefault,
            action -> new BurningActionGuiEditor((BurningAction) action)
    );

    private static final int VALUE_FIELD_WIDTH = 90;

    private String durationSecondsInput;
    private GuiTextField durationField;

    public BurningActionGuiEditor(BurningAction action) {
        super(action);
        durationSecondsInput = String.valueOf(Math.max(1, action.durationSec));
    }

    private static AbstractLootActionGuiEditor<?> newDefault() {
        return new BurningActionGuiEditor(new BurningAction());
    }

    @Override
    protected void initInternal(ActionEditorContext context) {
        durationField = newTextField(VALUE_FIELD_WIDTH, 32);
        syncFieldsFromState();
    }

    @Override
    protected void setBoundsInternal(int contentX, int y, int contentWidth) {
        durationField.xPosition = contentX;
        durationField.yPosition = y + 3;
        durationField.width = VALUE_FIELD_WIDTH;
    }

    @Override
    protected void renderInternal(int mouseX, int mouseY) {
        drawLabeledField(durationField, "sec");
    }

    @Override
    protected boolean keyTypedInternal(char c, int code) {
        boolean changed = durationField.textboxKeyTyped(c, code);
        if (changed) readStateFromFields();
        return changed;
    }

    @Override
    protected void mouseClickedInternal(int mouseX, int mouseY, int button) {
        durationField.mouseClicked(mouseX, mouseY, button);
        readStateFromFields();
    }

    @Override
    protected void writeStateToAction() {
        readStateFromFields();
        action.durationSec = Math.max(1, parseIntOrDefault(durationSecondsInput, 5));
    }

    @Override
    protected void collectValidationErrorsInternal(List<String> errors, String prefix) {
        readStateFromFields();
        if (!isValidPositiveInt(durationSecondsInput)) {
            errors.add(prefix + "duration must be a positive integer.");
        }
    }

    private void syncFieldsFromState() {
        if (durationField != null) durationField.setText(durationSecondsInput == null ? "" : durationSecondsInput);
    }

    private void readStateFromFields() {
        durationSecondsInput = durationField == null ? "" : (durationField.getText() == null ? "" : durationField.getText());
    }
}
