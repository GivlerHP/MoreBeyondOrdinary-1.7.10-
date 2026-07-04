package ru.givler.mbo.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import ru.givler.mbo.gui.lootcontainer.AbstractLootActionGuiEditor;
import ru.givler.mbo.gui.lootcontainer.ActionEditorContext;
import ru.givler.mbo.gui.lootcontainer.ActionEditorHost;
import ru.givler.mbo.gui.lootcontainer.ActionEditorType;
import ru.givler.mbo.gui.lootcontainer.ApplyEffectActionGuiEditor;
import ru.givler.mbo.gui.lootcontainer.ApplyExplosionEffectActionGuiEditor;
import ru.givler.mbo.gui.lootcontainer.BurningActionGuiEditor;
import ru.givler.mbo.gui.lootcontainer.ExplosionBurningActionGuiEditor;
import ru.givler.mbo.gui.lootcontainer.ExplosionInstantDamageActionGuiEditor;
import ru.givler.mbo.gui.lootcontainer.InstantDamageActionGuiEditor;
import ru.givler.mbo.gui.lootcontainer.ItemDropActionGuiEditor;
import ru.givler.mbo.gui.lootcontainer.SpawnEntityActionGuiEditor;
import ru.givler.mbo.lootcontainer.action.LootContainerAction;

import java.util.Arrays;
import java.util.List;

@SideOnly(Side.CLIENT)
public class LootContainerActionEditor extends Gui {
    private static final int TYPE_NONE_INDEX = -1;
    private static final String NONE_LABEL = "<none>";
    private static final int NONE_HEIGHT = 24;

    private static final int ROW_BACKGROUND_TOP_OFFSET = 1;
    private static final int ROW_BACKGROUND_BOTTOM_OFFSET = 3;
    private static final int TYPE_BUTTON_X_OFFSET = 2;
    private static final int TYPE_BUTTON_Y_OFFSET = 3;
    private static final List<ActionEditorType> ACTION_EDITOR_TYPES = Arrays.asList(
            ItemDropActionGuiEditor.EDITOR_TYPE,
            SpawnEntityActionGuiEditor.EDITOR_TYPE,
            ApplyEffectActionGuiEditor.EDITOR_TYPE,
            ApplyExplosionEffectActionGuiEditor.EDITOR_TYPE,
            InstantDamageActionGuiEditor.EDITOR_TYPE,
            ExplosionInstantDamageActionGuiEditor.EDITOR_TYPE,
            BurningActionGuiEditor.EDITOR_TYPE,
            ExplosionBurningActionGuiEditor.EDITOR_TYPE
    );

    private LootContainerAction action;
    private AbstractLootActionGuiEditor<?> actionEditor;
    private int actionEditorTypeIndex = TYPE_NONE_INDEX;

    private final List buttonList;
    private final GuiButton typeButton;
    private final ActionEditorContext context;

    private int x;
    private int y;
    private int w;

    public LootContainerActionEditor(int index,
                                     LootContainerAction action,
                                     FontRenderer fontRenderer,
                                     List buttonList,
                                     ActionEditorHost host) {
        this.action = action;
        this.buttonList = buttonList;
        this.context = new ActionEditorContext(index, fontRenderer, buttonList, new RenderItem(), host);

        this.typeButton = new GuiButton(5000 + index, 0, 0, 100, 16, NONE_LABEL);
        this.buttonList.add(typeButton);
    }

    public void init(int x, int y, int w) {
        setPosition(x, y, w);
        refreshFromData();
    }

    public int getHeight() {
        return actionEditor == null ? NONE_HEIGHT : actionEditor.getHeight();
    }

    public LootContainerAction getAction() {
        return action;
    }

    public void setPickedItem(ItemStack selected) {
        if (selected == null || actionEditor == null) return;
        actionEditor.onItemPicked(selected);
        action = actionEditor.toAction();
    }

    public void setPosition(int x, int y, int w) {
        this.x = x;
        this.y = y;
        this.w = w;
        applyLayout();
    }

    public void cycleType() {
        float preservedChance = action == null ? 100.0F : action.getChance();
        int nextIndex = actionEditorTypeIndex + 1;
        if (nextIndex >= ACTION_EDITOR_TYPES.size()) {
            nextIndex = TYPE_NONE_INDEX;
        }
        setEditorType(nextIndex, preservedChance);
    }

    public boolean keyTyped(char c, int code) {
        if (actionEditor == null) return false;
        boolean changed = actionEditor.keyTyped(c, code);
        if (changed) {
            action = actionEditor.toAction();
        }
        return changed;
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (actionEditor == null) return;
        actionEditor.mouseClicked(mouseX, mouseY, button);
    }

    public void draw(int mouseX, int mouseY) {
        renderPre(mouseX, mouseY);
        render(mouseX, mouseY);
    }

    public void renderPre(int mouseX, int mouseY) {
        if (actionEditor != null) {
            actionEditor.renderPre(mouseX, mouseY);
        }
    }

    public void render(int mouseX, int mouseY) {
        drawRect(x, y + ROW_BACKGROUND_TOP_OFFSET, x + w, y + getHeight() - ROW_BACKGROUND_BOTTOM_OFFSET, 0xAA101010);
        if (actionEditor != null) {
            actionEditor.render(mouseX, mouseY);
        }
    }

    public void drawOverlay(int mouseX, int mouseY) {
        renderPost(mouseX, mouseY);
    }

    public void renderPost(int mouseX, int mouseY) {
        if (actionEditor != null) {
            actionEditor.renderPost(mouseX, mouseY);
        }
    }

    public boolean handleDropdownScroll(int mouseX, int mouseY, int wheel) {
        return actionEditor != null && actionEditor.handleMouseInput(mouseX, mouseY, wheel);
    }

    public boolean tryClickDropdown(int mouseX, int mouseY, int button) {
        if (actionEditor == null) return false;
        boolean handled = actionEditor.tryClickOverlay(mouseX, mouseY, button);
        if (handled) {
            action = actionEditor.toAction();
        }
        return handled;
    }

    public boolean actionPerformed(GuiButton button) {
        return actionEditor != null && actionEditor.actionPerformed(button);
    }

    public LootContainerAction toData() {
        if (actionEditor == null) {
            action = null;
        } else {
            action = actionEditor.toAction();
        }
        return action;
    }

    public void collectValidationErrors(List<String> errors, int actionIndex) {
        if (errors == null || actionEditor == null) return;
        String prefix = "Action #" + (actionIndex + 1) + ": ";
        actionEditor.collectValidationErrors(errors, prefix);
    }

    private void refreshFromData() {
        if (actionEditor != null) {
            actionEditor.dispose();
        }
        int typeIndex = findTypeIndexForAction(action);
        if (typeIndex < 0) {
            actionEditorTypeIndex = TYPE_NONE_INDEX;
            actionEditor = null;
            action = null;
        } else {
            actionEditorTypeIndex = typeIndex;
            actionEditor = ACTION_EDITOR_TYPES.get(typeIndex).createFromAction(action);
            actionEditor.init(context);
        }
        applyLayout();
    }

    private void setEditorType(int nextTypeIndex, float chance) {
        if (actionEditor != null) {
            actionEditor.dispose();
        }
        actionEditorTypeIndex = nextTypeIndex;
        if (nextTypeIndex == TYPE_NONE_INDEX) {
            actionEditor = null;
            action = null;
        } else {
            actionEditor = ACTION_EDITOR_TYPES.get(nextTypeIndex).createDefault();
            actionEditor.init(context);
            actionEditor.setChance(chance);
            action = actionEditor.toAction();
        }
        applyLayout();
    }

    private void applyLayout() {
        typeButton.displayString = actionEditorTypeIndex == TYPE_NONE_INDEX ? NONE_LABEL : ACTION_EDITOR_TYPES.get(actionEditorTypeIndex).label;

        typeButton.xPosition = x + TYPE_BUTTON_X_OFFSET;
        typeButton.yPosition = y + TYPE_BUTTON_Y_OFFSET;

        if (actionEditor != null) {
            actionEditor.setBounds(x, y, w);
        }
    }

    private int findTypeIndexForAction(LootContainerAction candidate) {
        if (candidate == null) return TYPE_NONE_INDEX;
        for (int i = 0; i < ACTION_EDITOR_TYPES.size(); i++) {
            if (ACTION_EDITOR_TYPES.get(i).supports(candidate)) {
                return i;
            }
        }
        return TYPE_NONE_INDEX;
    }
}
