package ru.givler.mbo.gui.lootcontainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiTextField;
import ru.givler.mbo.lootcontainer.action.InstantDamageAction;
import ru.givler.mbo.lootcontainer.action.LootContainerActionRegistry;

import java.util.List;

@SideOnly(Side.CLIENT)
public class InstantDamageActionGuiEditor extends AbstractLootActionGuiEditor<InstantDamageAction> {
    public static final ActionEditorType EDITOR_TYPE = new ActionEditorType(
            LootContainerActionRegistry.TYPE_INSTANT_DAMAGE,
            "Instant Damage",
            InstantDamageAction.class,
            InstantDamageActionGuiEditor::newDefault,
            action -> new InstantDamageActionGuiEditor((InstantDamageAction) action)
    );

    private static final int VALUE_FIELD_WIDTH = 90;

    private String damageInput;
    private GuiTextField damageField;

    public InstantDamageActionGuiEditor(InstantDamageAction action) {
        super(action);
        damageInput = String.valueOf(Math.max(0F, action.damage));
    }

    private static AbstractLootActionGuiEditor<?> newDefault() {
        return new InstantDamageActionGuiEditor(new InstantDamageAction());
    }

    @Override
    protected void initInternal(ActionEditorContext context) {
        damageField = newTextField(VALUE_FIELD_WIDTH, 32);
        syncFieldsFromState();
    }

    @Override
    protected void setBoundsInternal(int contentX, int y, int contentWidth) {
        damageField.xPosition = contentX;
        damageField.yPosition = y + 3;
        damageField.width = VALUE_FIELD_WIDTH;
    }

    @Override
    protected void renderInternal(int mouseX, int mouseY) {
        drawLabeledField(damageField, "dmg");
    }

    @Override
    protected boolean keyTypedInternal(char c, int code) {
        boolean changed = damageField.textboxKeyTyped(c, code);
        if (changed) readStateFromFields();
        return changed;
    }

    @Override
    protected void mouseClickedInternal(int mouseX, int mouseY, int button) {
        damageField.mouseClicked(mouseX, mouseY, button);
        readStateFromFields();
    }

    @Override
    protected void writeStateToAction() {
        readStateFromFields();
        action.damage = Math.max(0.0F, parseFloatOrDefault(damageInput, 4.0F));
    }

    @Override
    protected void collectValidationErrorsInternal(List<String> errors, String prefix) {
        readStateFromFields();
        if (!isValidPositiveNumber(damageInput)) {
            errors.add(prefix + "damage must be > 0.");
        }
    }

    private void syncFieldsFromState() {
        if (damageField != null) damageField.setText(damageInput == null ? "" : damageInput);
    }

    private void readStateFromFields() {
        damageInput = damageField == null ? "" : (damageField.getText() == null ? "" : damageField.getText());
    }
}
