package ru.givler.mbo.gui.lootcontainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.potion.Potion;
import ru.givler.mbo.lootcontainer.action.ApplyEffectAction;
import ru.givler.mbo.lootcontainer.action.LootContainerActionRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ApplyEffectActionGuiEditor extends AbstractLootActionGuiEditor<ApplyEffectAction> {
    public static final ActionEditorType EDITOR_TYPE = new ActionEditorType(
            LootContainerActionRegistry.TYPE_APPLY_EFFECT,
            "Apply Effect",
            ApplyEffectAction.class,
            ApplyEffectActionGuiEditor::newDefault,
            action -> new ApplyEffectActionGuiEditor((ApplyEffectAction) action)
    );

//    private static final int VALUE_FIELD_WIDTH = 106;
//    private static final int EXTRA_FIELD_X_OFFSET = 270;
//    private static final int EXTRA_FIELD_WIDTH = 52;
    private static final int AMPLIFIER_FIELD_X_OFFSET = 282;
    private static final int AMPLIFIER_FIELD_WIDTH = 22;

    private String potionIdInput;
    private String durationSecondsInput;
    private String amplifierLevelInput;
    private boolean dropdownOpen;
    private int dropdownScroll;
    private GuiTextField potionIdField;
    private GuiTextField durationField;
    private GuiTextField amplifierField;

    public ApplyEffectActionGuiEditor(ApplyEffectAction action) {
        super(action);
        potionIdInput = action.potionId == null ? "" : action.potionId;
        durationSecondsInput = String.valueOf(Math.max(1, action.durationSec));
        amplifierLevelInput = String.valueOf(Math.max(0, action.amplifier));
        dropdownOpen = false;
        dropdownScroll = 0;
    }

    private static AbstractLootActionGuiEditor<?> newDefault() {
        return new ApplyEffectActionGuiEditor(new ApplyEffectAction());
    }

    @Override
    protected void initInternal(ActionEditorContext context) {
        potionIdField = newTextField(VALUE_FIELD_WIDTH, 96);
        durationField = newTextField(EXTRA_FIELD_WIDTH, 32);
        amplifierField = newTextField(AMPLIFIER_FIELD_WIDTH, 6);
        syncFieldsFromState();
    }

//    @Override
//    protected int getChanceFieldXOffset() {
//        return 364;
//    }

    @Override
    protected void setBoundsInternal(int contentX, int y, int contentWidth) {
        potionIdField.xPosition = contentX;
        potionIdField.yPosition = y + 3;
        potionIdField.width = VALUE_FIELD_WIDTH;
        durationField.xPosition = x + EXTRA_FIELD_X_OFFSET;
        durationField.yPosition = y + 3;
        durationField.width = EXTRA_FIELD_WIDTH;
        amplifierField.xPosition = x + AMPLIFIER_FIELD_X_OFFSET;
        amplifierField.yPosition = y + 3;
        amplifierField.width = AMPLIFIER_FIELD_WIDTH;
    }

    @Override
    protected void renderInternal(int mouseX, int mouseY) {
        drawLabeledField(potionIdField, "pot");
        drawLabeledField(durationField, "sec");
        drawLabeledField(amplifierField, "amp");
    }

    @Override
    protected void renderPostInternal(int mouseX, int mouseY) {
        drawDropdown(getDropdownState(), mouseX, mouseY);
    }

    @Override
    protected boolean keyTypedInternal(char c, int code) {
        boolean changedPotion = potionIdField.textboxKeyTyped(c, code);
        boolean changedDuration = durationField.textboxKeyTyped(c, code);
        boolean changedAmplifier = amplifierField.textboxKeyTyped(c, code);
        boolean changed = changedPotion || changedDuration || changedAmplifier;
        if (changed) {
            readStateFromFields();
            if (changedPotion) {
                dropdownScroll = 0;
                dropdownOpen = potionIdField.isFocused();
            }
        }
        return changed;
    }

    @Override
    protected void mouseClickedInternal(int mouseX, int mouseY, int button) {
        potionIdField.mouseClicked(mouseX, mouseY, button);
        durationField.mouseClicked(mouseX, mouseY, button);
        amplifierField.mouseClicked(mouseX, mouseY, button);
        readStateFromFields();
        dropdownOpen = potionIdField.isFocused();
    }

    @Override
    protected void writeStateToAction() {
        readStateFromFields();
        action.potionId = potionIdInput;
        action.durationSec = Math.max(1, parseIntOrDefault(durationSecondsInput, 10));
        action.amplifier = Math.max(0, parseIntOrDefault(amplifierLevelInput, 0));
    }

    @Override
    public boolean handleMouseInput(int mouseX, int mouseY, int wheel) {
        LootActionDropdownState state = getDropdownState();
        if (!isMouseOverDropdown(state, mouseX, mouseY)) return false;
        int next = state.scroll;
        if (wheel < 0) next++;
        if (wheel > 0) next--;
        dropdownScroll = clampInt(next, 0, state.maxScroll);
        return true;
    }

    @Override
    public boolean tryClickOverlay(int mouseX, int mouseY, int button) {
        if (button != 0) return false;
        LootActionDropdownOption option = getClickedDropdownOption(getDropdownState(), mouseX, mouseY);
        if (option == null) return false;
        potionIdInput = option.value;
        dropdownOpen = false;
        syncFieldsFromState();
        return true;
    }

    private LootActionDropdownState getDropdownState() {
        if (potionIdField == null || !potionIdField.isFocused() || !dropdownOpen) return null;
        String query = potionIdInput == null ? "" : potionIdInput.toLowerCase();

        List<LootActionDropdownOption> matches = new ArrayList<LootActionDropdownOption>();
        for (int id = 0; id < Potion.potionTypes.length; id++) {
            Potion potion = Potion.potionTypes[id];
            if (potion == null) continue;
            String name = potion.getName();
            if (name == null || name.trim().isEmpty()) continue;
            String label = id + ": " + name;
            if (query.isEmpty() || label.toLowerCase().contains(query)) {
                matches.add(new LootActionDropdownOption(label, name));
            }
        }
        Collections.sort(matches);

        int shown = Math.min(DROPDOWN_VISIBLE_ROWS, matches.size());
        if (shown <= 0) return null;
        int maxScroll = Math.max(0, matches.size() - shown);
        dropdownScroll = clampInt(dropdownScroll, 0, maxScroll);
        int boxHeight = shown * DROPDOWN_ROW_HEIGHT + DROPDOWN_BOX_PADDING;
        return new LootActionDropdownState(
                matches,
                shown,
                dropdownScroll,
                maxScroll,
                potionIdField.xPosition,
                potionIdField.yPosition + DROPDOWN_VERTICAL_OFFSET,
                potionIdField.width,
                boxHeight
        );
    }

    @Override
    protected void collectValidationErrorsInternal(List<String> errors, String prefix) {
        readStateFromFields();
        if (!isValidPositiveInt(durationSecondsInput)) {
            errors.add(prefix + "duration must be a positive integer.");
            return;
        }
        if (!isValidNonNegativeInt(amplifierLevelInput)) {
            errors.add(prefix + "amplifier must be a non-negative integer.");
        }
    }

    private void syncFieldsFromState() {
        if (potionIdField != null) potionIdField.setText(potionIdInput == null ? "" : potionIdInput);
        if (durationField != null) durationField.setText(durationSecondsInput == null ? "" : durationSecondsInput);
        if (amplifierField != null) amplifierField.setText(amplifierLevelInput == null ? "" : amplifierLevelInput);
    }

    private void readStateFromFields() {
        potionIdInput = potionIdField == null ? "" : (potionIdField.getText() == null ? "" : potionIdField.getText());
        durationSecondsInput = durationField == null ? "" : (durationField.getText() == null ? "" : durationField.getText());
        amplifierLevelInput = amplifierField == null ? "" : (amplifierField.getText() == null ? "" : amplifierField.getText());
    }
}
