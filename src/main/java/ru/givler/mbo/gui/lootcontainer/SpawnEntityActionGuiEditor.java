package ru.givler.mbo.gui.lootcontainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.EntityList;
import ru.givler.mbo.lootcontainer.action.LootContainerActionRegistry;
import ru.givler.mbo.lootcontainer.action.SpawnEntityAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class SpawnEntityActionGuiEditor extends AbstractLootActionGuiEditor<SpawnEntityAction> {
    public static final ActionEditorType EDITOR_TYPE = new ActionEditorType(
            LootContainerActionRegistry.TYPE_SPAWN_ENTITY,
            "Spawn",
            SpawnEntityAction.class,
            SpawnEntityActionGuiEditor::newDefault,
            action -> new SpawnEntityActionGuiEditor((SpawnEntityAction) action)
    );

//    private static final int VALUE_FIELD_WIDTH = 132;
//    private static final int EXTRA_FIELD_X_OFFSET = 270;
//    private static final int EXTRA_FIELD_WIDTH = 44;

    private String entityIdInput;
    private String spawnCountInput;
    private boolean dropdownOpen;
    private int dropdownScroll;
    private GuiTextField entityIdField;
    private GuiTextField spawnCountField;

    public SpawnEntityActionGuiEditor(SpawnEntityAction action) {
        super(action);
        entityIdInput = action.entityId == null ? "" : action.entityId;
        spawnCountInput = action.countExpr == null || action.countExpr.trim().isEmpty()
                ? String.valueOf(Math.max(1, action.spawnCount))
                : action.countExpr;
        dropdownOpen = false;
        dropdownScroll = 0;
    }

    private static AbstractLootActionGuiEditor<?> newDefault() {
        return new SpawnEntityActionGuiEditor(new SpawnEntityAction());
    }

    @Override
    protected void initInternal(ActionEditorContext context) {
        entityIdField = newTextField(VALUE_FIELD_WIDTH, 96);
        spawnCountField = newTextField(EXTRA_FIELD_WIDTH, 32);
        syncFieldsFromState();
    }

//    @Override
//    protected int getChanceFieldXOffset() {
//        return 346;
//    }

    @Override
    protected void setBoundsInternal(int contentX, int y, int contentWidth) {
        entityIdField.xPosition = contentX;
        entityIdField.yPosition = y + 3;
        entityIdField.width = VALUE_FIELD_WIDTH;
        spawnCountField.xPosition = x + EXTRA_FIELD_X_OFFSET;
        spawnCountField.yPosition = y + 3;
        spawnCountField.width = EXTRA_FIELD_WIDTH;
    }

    @Override
    protected void renderInternal(int mouseX, int mouseY) {
        drawLabeledField(entityIdField, "ent");
        drawLabeledField(spawnCountField, "cnt");
    }

    @Override
    protected void renderPostInternal(int mouseX, int mouseY) {
        drawDropdown(getDropdownState(), mouseX, mouseY);
    }

    @Override
    protected boolean keyTypedInternal(char c, int code) {
        boolean changedEntity = entityIdField.textboxKeyTyped(c, code);
        boolean changedCount = spawnCountField.textboxKeyTyped(c, code);
        boolean changed = changedEntity || changedCount;
        if (changed) {
            readStateFromFields();
            if (changedEntity) {
                dropdownScroll = 0;
                dropdownOpen = entityIdField.isFocused();
            }
        }
        return changed;
    }

    @Override
    protected void mouseClickedInternal(int mouseX, int mouseY, int button) {
        entityIdField.mouseClicked(mouseX, mouseY, button);
        spawnCountField.mouseClicked(mouseX, mouseY, button);
        readStateFromFields();
        dropdownOpen = entityIdField.isFocused();
    }

    @Override
    protected void writeStateToAction() {
        readStateFromFields();
        action.entityId = entityIdInput;
        String normalizedExpr = spawnCountInput == null ? "" : spawnCountInput.trim();
        if (normalizedExpr.isEmpty()) normalizedExpr = "1";
        action.countExpr = normalizedExpr;
        action.spawnCount = resolveSpawnCount(normalizedExpr);
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
        entityIdInput = option.value;
        dropdownOpen = false;
        syncFieldsFromState();
        return true;
    }

    private LootActionDropdownState getDropdownState() {
        if (entityIdField == null || !entityIdField.isFocused() || !dropdownOpen) return null;
        String query = entityIdInput == null ? "" : entityIdInput.toLowerCase();

        List<LootActionDropdownOption> matches = new ArrayList<LootActionDropdownOption>();
        for (Object keyObj : EntityList.stringToClassMapping.keySet()) {
            String key = String.valueOf(keyObj);
            if (query.isEmpty() || key.toLowerCase().contains(query)) {
                matches.add(new LootActionDropdownOption(key, key));
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
                entityIdField.xPosition,
                entityIdField.yPosition + DROPDOWN_VERTICAL_OFFSET,
                entityIdField.width,
                boxHeight
        );
    }

    @Override
    protected void collectValidationErrorsInternal(List<String> errors, String prefix) {
        readStateFromFields();
        if (!isValidPositiveCountExpression(spawnCountInput)) {
            errors.add(prefix + "spawn count must be N or N-M with positive integers.");
        }
    }

    private void syncFieldsFromState() {
        if (entityIdField != null) entityIdField.setText(entityIdInput == null ? "" : entityIdInput);
        if (spawnCountField != null) spawnCountField.setText(spawnCountInput == null ? "1" : spawnCountInput);
    }

    private void readStateFromFields() {
        entityIdInput = entityIdField == null ? "" : (entityIdField.getText() == null ? "" : entityIdField.getText());
        spawnCountInput = spawnCountField == null ? "" : (spawnCountField.getText() == null ? "" : spawnCountField.getText());
    }

    private static int resolveSpawnCount(String expression) {
        if (expression == null) return 1;
        String value = expression.trim();
        if (value.isEmpty()) return 1;
        int dash = value.indexOf('-');
        if (dash < 0) {
            return Math.max(1, parseIntOrDefault(value, 1));
        }
        if (dash == 0 || dash == value.length() - 1 || value.indexOf('-', dash + 1) >= 0) return 1;
        int left = parseIntOrDefault(value.substring(0, dash).trim(), 1);
        int right = parseIntOrDefault(value.substring(dash + 1).trim(), left);
        return Math.max(1, Math.min(left, right));
    }
}
