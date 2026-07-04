package ru.givler.mbo.gui.lootcontainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import ru.givler.mbo.lootcontainer.action.ExplosionInstantDamageAction;
import ru.givler.mbo.lootcontainer.action.LootContainerActionRegistry;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ExplosionInstantDamageActionGuiEditor extends AbstractLootActionGuiEditor<ExplosionInstantDamageAction> {
    public static final ActionEditorType EDITOR_TYPE = new ActionEditorType(
            LootContainerActionRegistry.TYPE_EXPLOSION_INSTANT_DAMAGE,
            "Explosion Damage",
            ExplosionInstantDamageAction.class,
            ExplosionInstantDamageActionGuiEditor::newDefault,
            action -> new ExplosionInstantDamageActionGuiEditor((ExplosionInstantDamageAction) action)
    );

    private static final int VALUE_FIELD_WIDTH = 90;
    private static final int SECOND_ROW_Y_OFFSET = 20;
    private static final int BUTTON_GAP = 6;
    private static final int PARTICLES_BUTTON_WIDTH = 86;
    private static final int KNOCKBACK_BUTTON_WIDTH = 90;

    private String damageInput;
    private String radiusInput;
    private boolean particlesEnabled;
    private boolean knockbackEnabled;
    private GuiTextField damageField;
    private GuiTextField radiusField;
    private GuiButton particlesButton;
    private GuiButton knockbackButton;

    public ExplosionInstantDamageActionGuiEditor(ExplosionInstantDamageAction action) {
        super(action);
        damageInput = String.valueOf(Math.max(0F, action.damage));
        radiusInput = String.valueOf(Math.max(0.1F, action.radius));
        particlesEnabled = action.particles;
        knockbackEnabled = action.knockback;
    }

    private static AbstractLootActionGuiEditor<?> newDefault() {
        return new ExplosionInstantDamageActionGuiEditor(new ExplosionInstantDamageAction());
    }

    @Override
    protected void initInternal(ActionEditorContext context) {
        damageField = newTextField(VALUE_FIELD_WIDTH, 32);
        radiusField = newTextField(EXTRA_FIELD_WIDTH, 32);
        particlesButton = new GuiButton(9200 + context.actionIndex, 0, 0, PARTICLES_BUTTON_WIDTH, 14, "");
        knockbackButton = new GuiButton(9250 + context.actionIndex, 0, 0, KNOCKBACK_BUTTON_WIDTH, 14, "");
        context.buttonList.add(particlesButton);
        context.buttonList.add(knockbackButton);
        syncFieldsFromState();
    }

    @Override
    public int getHeight() {
        return 40;
    }

    @Override
    protected void setBoundsInternal(int contentX, int y, int contentWidth) {
        damageField.xPosition = contentX;
        damageField.yPosition = y + 3;
        damageField.width = VALUE_FIELD_WIDTH;
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
        drawLabeledField(damageField, "dmg");
        drawLabeledField(radiusField, "rad");
    }

    @Override
    protected boolean keyTypedInternal(char c, int code) {
        boolean changed = damageField.textboxKeyTyped(c, code);
        changed |= radiusField.textboxKeyTyped(c, code);
        if (changed) readStateFromFields();
        return changed;
    }

    @Override
    protected void mouseClickedInternal(int mouseX, int mouseY, int button) {
        damageField.mouseClicked(mouseX, mouseY, button);
        radiusField.mouseClicked(mouseX, mouseY, button);
        readStateFromFields();
    }

    @Override
    protected void writeStateToAction() {
        readStateFromFields();
        action.damage = Math.max(0.0F, parseFloatOrDefault(damageInput, 4.0F));
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
    protected void collectValidationErrorsInternal(List<String> errors, String prefix) {
        readStateFromFields();
        if (!isValidPositiveNumber(damageInput)) {
            errors.add(prefix + "damage must be > 0.");
            return;
        }
        if (!isValidPositiveNumber(radiusInput)) {
            errors.add(prefix + "radius must be > 0.");
        }
    }

    private void syncFieldsFromState() {
        if (damageField != null) damageField.setText(damageInput == null ? "" : damageInput);
        if (radiusField != null) radiusField.setText(radiusInput == null ? "" : radiusInput);
        if (particlesButton != null) particlesButton.displayString = particlesButtonLabel();
        if (knockbackButton != null) knockbackButton.displayString = knockbackButtonLabel();
    }

    private void readStateFromFields() {
        damageInput = damageField == null ? "" : (damageField.getText() == null ? "" : damageField.getText());
        radiusInput = radiusField == null ? "" : (radiusField.getText() == null ? "" : radiusField.getText());
    }

    private String particlesButtonLabel() {
        return (particlesEnabled ? "[x] " : "[ ] ") + "particles";
    }

    private String knockbackButtonLabel() {
        return (knockbackEnabled ? "[x] " : "[ ] ") + "knockback";
    }
}
