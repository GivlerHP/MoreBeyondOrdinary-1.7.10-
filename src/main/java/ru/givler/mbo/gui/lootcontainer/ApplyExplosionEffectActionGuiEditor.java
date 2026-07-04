package ru.givler.mbo.gui.lootcontainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.potion.Potion;
import ru.givler.mbo.lootcontainer.action.ApplyExplosionEffectAction;
import ru.givler.mbo.lootcontainer.action.LootContainerActionRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ApplyExplosionEffectActionGuiEditor extends AbstractLootActionGuiEditor<ApplyExplosionEffectAction> {
    public static final ActionEditorType EDITOR_TYPE = new ActionEditorType(
            LootContainerActionRegistry.TYPE_APPLY_EXPLOSION_EFFECT,
            "Explosion Effect",
            ApplyExplosionEffectAction.class,
            ApplyExplosionEffectActionGuiEditor::newDefault,
            action -> new ApplyExplosionEffectActionGuiEditor((ApplyExplosionEffectAction) action)
    );

//    private static final int VALUE_FIELD_WIDTH = 106;
//    private static final int EXTRA_FIELD_X_OFFSET = 270;
//    private static final int EXTRA_FIELD_WIDTH = 52;
    private static final int SECOND_ROW_Y_OFFSET = 20;
    private static final int BUTTON_GAP = 6;
    private static final int PARTICLES_BUTTON_WIDTH = 86;
    private static final int KNOCKBACK_BUTTON_WIDTH = 90;
    private static final int AMPLIFIER_FIELD_X_OFFSET = 282;
    private static final int AMPLIFIER_FIELD_WIDTH = 22;

    private String potionIdInput;
    private String durationSecondsInput;
    private String amplifierLevelInput;
    private String radiusInput;
    private boolean particlesEnabled;
    private boolean knockbackEnabled;
    private boolean dropdownOpen;
    private int dropdownScroll;
    private GuiTextField potionIdField;
    private GuiTextField durationField;
    private GuiTextField amplifierField;
    private GuiTextField radiusField;
    private GuiButton particlesButton;
    private GuiButton knockbackButton;

    public ApplyExplosionEffectActionGuiEditor(ApplyExplosionEffectAction action) {
        super(action);
        potionIdInput = action.potionId == null ? "" : action.potionId;
        durationSecondsInput = String.valueOf(Math.max(1, action.durationSec));
        amplifierLevelInput = String.valueOf(Math.max(0, action.amplifier));
        radiusInput = String.valueOf(Math.max(0.1F, action.radius));
        particlesEnabled = action.particles;
        knockbackEnabled = action.knockback;
        dropdownOpen = false;
        dropdownScroll = 0;
    }

    private static AbstractLootActionGuiEditor<?> newDefault() {
        return new ApplyExplosionEffectActionGuiEditor(new ApplyExplosionEffectAction());
    }

    @Override
    protected void initInternal(ActionEditorContext context) {
        potionIdField = newTextField(VALUE_FIELD_WIDTH, 96);
        durationField = newTextField(EXTRA_FIELD_WIDTH, 32);
        amplifierField = newTextField(AMPLIFIER_FIELD_WIDTH, 6);
        radiusField = newTextField(EXTRA_FIELD_WIDTH, 16);
        particlesButton = new GuiButton(9000 + context.actionIndex, 0, 0, PARTICLES_BUTTON_WIDTH, 14, "");
        knockbackButton = new GuiButton(9100 + context.actionIndex, 0, 0, KNOCKBACK_BUTTON_WIDTH, 14, "");
        context.buttonList.add(particlesButton);
        context.buttonList.add(knockbackButton);
        syncFieldsFromState();
    }

    @Override
    public int getHeight() {
        return 40;
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

        radiusField.xPosition = x + EXTRA_FIELD_X_OFFSET;
        radiusField.yPosition = y + SECOND_ROW_Y_OFFSET;
        radiusField.width = EXTRA_FIELD_WIDTH;

        particlesButton.xPosition = contentX;
        particlesButton.yPosition = y + SECOND_ROW_Y_OFFSET;
        particlesButton.displayString = particlesButtonLabel();
        particlesButton.visible = true;
        knockbackButton.xPosition = contentX + PARTICLES_BUTTON_WIDTH + BUTTON_GAP;
        knockbackButton.yPosition = y + SECOND_ROW_Y_OFFSET;
        knockbackButton.displayString = knockbackButtonLabel();
        knockbackButton.visible = true;
    }

    @Override
    protected void renderInternal(int mouseX, int mouseY) {
        drawLabeledField(potionIdField, "pot");
        drawLabeledField(durationField, "sec");
        drawLabeledField(amplifierField, "amp");
        drawLabeledField(radiusField, "rad");
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
        boolean changedRadius = radiusField.textboxKeyTyped(c, code);
        boolean changed = changedPotion || changedDuration || changedAmplifier || changedRadius;
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
        radiusField.mouseClicked(mouseX, mouseY, button);
        readStateFromFields();
        dropdownOpen = potionIdField.isFocused();
    }

    @Override
    protected void writeStateToAction() {
        readStateFromFields();
        action.potionId = potionIdInput;
        action.durationSec = Math.max(1, parseIntOrDefault(durationSecondsInput, 10));
        action.amplifier = Math.max(0, parseIntOrDefault(amplifierLevelInput, 0));
        action.radius = Math.max(0.1F, parseFloatOrDefault(radiusInput, 4.0F));
        action.particles = particlesEnabled;
        action.knockback = knockbackEnabled;
    }

    @Override
    public boolean actionPerformed(GuiButton button) {
        if (button == particlesButton) {
            particlesEnabled = !particlesEnabled;
            particlesButton.displayString = particlesButtonLabel();
            return true;
        }
        if (button == knockbackButton) {
            knockbackEnabled = !knockbackEnabled;
            knockbackButton.displayString = knockbackButtonLabel();
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        if (particlesButton != null) {
            context.buttonList.remove(particlesButton);
            particlesButton = null;
        }
        if (knockbackButton != null) {
            context.buttonList.remove(knockbackButton);
            knockbackButton = null;
        }
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
            return;
        }
        if (!isValidPositiveNumber(radiusInput)) {
            errors.add(prefix + "radius must be > 0.");
        }
    }

    private void syncFieldsFromState() {
        if (potionIdField != null) potionIdField.setText(potionIdInput == null ? "" : potionIdInput);
        if (durationField != null) durationField.setText(durationSecondsInput == null ? "" : durationSecondsInput);
        if (amplifierField != null) amplifierField.setText(amplifierLevelInput == null ? "" : amplifierLevelInput);
        if (radiusField != null) radiusField.setText(radiusInput == null ? "" : radiusInput);
        if (particlesButton != null) particlesButton.displayString = particlesButtonLabel();
        if (knockbackButton != null) knockbackButton.displayString = knockbackButtonLabel();
    }

    private void readStateFromFields() {
        potionIdInput = potionIdField == null ? "" : (potionIdField.getText() == null ? "" : potionIdField.getText());
        durationSecondsInput = durationField == null ? "" : (durationField.getText() == null ? "" : durationField.getText());
        amplifierLevelInput = amplifierField == null ? "" : (amplifierField.getText() == null ? "" : amplifierField.getText());
        radiusInput = radiusField == null ? "" : (radiusField.getText() == null ? "" : radiusField.getText());
    }

    private String particlesButtonLabel() {
        return (particlesEnabled ? "[x] " : "[ ] ") + "particles";
    }

    private String knockbackButtonLabel() {
        return (knockbackEnabled ? "[x] " : "[ ] ") + "knockback";
    }
}
