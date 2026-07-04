package ru.givler.mbo.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.block.BlockModels;
import ru.givler.mbo.gui.lootcontainer.ActionEditorHost;
import ru.givler.mbo.item.ItemBlockLootContainer;
import ru.givler.mbo.lootcontainer.LootContainerConfigValidator;
import ru.givler.mbo.lootcontainer.LootContainerData;
import ru.givler.mbo.lootcontainer.action.ApplyEffectAction;
import ru.givler.mbo.lootcontainer.action.ApplyExplosionEffectAction;
import ru.givler.mbo.lootcontainer.action.BurningAction;
import ru.givler.mbo.lootcontainer.action.ExplosionBurningAction;
import ru.givler.mbo.lootcontainer.action.ExplosionInstantDamageAction;
import ru.givler.mbo.lootcontainer.action.InstantDamageAction;
import ru.givler.mbo.lootcontainer.action.ItemDropAction;
import ru.givler.mbo.lootcontainer.action.LootContainerAction;
import ru.givler.mbo.lootcontainer.action.SpawnEntityAction;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketLootContainerGiveItem;
import ru.givler.mbo.network.packet.PacketLootContainerConfig;
import ru.givler.mbo.network.packet.PacketLootContainerRestore;
import ru.givler.mbo.proxy.ClientProxy;
import ru.givler.mbo.tileentity.TileEntityLootContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SideOnly(Side.CLIENT)
public class GuiLootContainerConfig extends GuiScreen implements ActionEditorHost {
    private final EntityPlayer player;
    private final TileEntityLootContainer tile;
    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final boolean editingBlock;
    private final RenderItem itemRenderer = new RenderItem();

    private EditorState state;
    private final LootContainerData initialData;

    private GuiTextField customNameField;
    private GuiTextField recoveryField;
    private final List<ModelVariationEditor> variationEditors = new ArrayList<>();
    private final List<LootContainerActionEditor> actionEditors = new ArrayList<>();

    private GuiButton doneButton;
    private GuiButton getItemButton;
    private GuiButton restoreButton;
    private String warningText = "";
    private String errorText = "";
    private boolean confirmInvalidEntity = false;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private boolean draggingScroll = false;
    private int dragScrollStartY = 0;
    private int dragScrollStartOffset = 0;

    private static final int GUI_LEFT_PAD = 210;
    private static final int CONTENT_TOP = 24;
    private static final int CONTENT_BOTTOM_MARGIN = 32;

    public GuiLootContainerConfig(EntityPlayer player, TileEntityLootContainer tile) {
        this.player = player;
        this.tile = tile;
        this.editingBlock = tile != null;
        LootContainerData data;
        if (tile != null) {
            this.blockX = tile.xCoord;
            this.blockY = tile.yCoord;
            this.blockZ = tile.zCoord;
            data = tile.exportConfig();
        } else {
            this.blockX = 0;
            this.blockY = 0;
            this.blockZ = 0;
            ItemStack held = player == null ? null : player.getCurrentEquippedItem();
            data = ItemBlockLootContainer.readData(held);
        }

        if (data == null) {
            data = new LootContainerData();
        }
        this.initialData = copyData(data);
        this.state = EditorState.fromData(initialData);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        if (customNameField != null && recoveryField != null && !variationEditors.isEmpty()) {
            syncStateFromControls();
        }
        buttonList.clear();
        variationEditors.clear();
        actionEditors.clear();

        int left = width / 2 - GUI_LEFT_PAD;
        customNameField = new GuiTextField(fontRendererObj, left + 8, CONTENT_TOP + 14, 220, 16);
        customNameField.setMaxStringLength(LootContainerData.MAX_CUSTOM_NAME_LENGTH);
        customNameField.setText(state.customNameInput == null ? "" : state.customNameInput);

        recoveryField = new GuiTextField(fontRendererObj, left + 8, CONTENT_TOP + 348, 100, 16);
        recoveryField.setMaxStringLength(16);
        recoveryField.setText(state.recoveryInput == null ? "" : state.recoveryInput);

        int varTop = CONTENT_TOP + 52;
        for (int i = 0; i < LootContainerData.MAX_VARIATIONS; i++) {
            LootContainerData.ModelVariation variation = i < state.modelVariations.size()
                    ? state.modelVariations.get(i)
                    : new LootContainerData.ModelVariation();
            ModelVariationEditor editor = new ModelVariationEditor(i, variation);
            editor.init(left + 8, varTop + i * 54, 480);
            variationEditors.add(editor);
        }

        int actionsTop = CONTENT_TOP + 54 + LootContainerData.MAX_VARIATIONS * 54 + 18;
        List<LootContainerAction> actions = state.actions;
        int actionY = actionsTop;
        for (int i = 0; i < LootContainerData.MAX_ACTIONS; i++) {
            LootContainerAction action;
            if (i < actions.size()) {
                action = actions.get(i);
            } else {
                action = null;
            }
            LootContainerActionEditor editor = new LootContainerActionEditor(i, action, fontRendererObj, buttonList, this);
            editor.init(left + 8, actionY, 480);
            actionEditors.add(editor);
            actionY += editor.getHeight();
        }

        buttonList.add(new GuiButton(200, left + 8, CONTENT_TOP + 372, 150, 20, toggleLabel("Allow Multiaction", state.allowMultiaction)));
        buttonList.add(new GuiButton(201, left + 164, CONTENT_TOP + 372, 150, 20, toggleLabel("Destroy on Player Collide", state.destroyPlayerCollide)));
        buttonList.add(new GuiButton(202, left + 320, CONTENT_TOP + 372, 150, 20, toggleLabel("Destroy on Entity Collide", state.destroyEntityCollide)));
        buttonList.add(new GuiButton(203, left + 8, CONTENT_TOP + 396, 150, 20, toggleLabel("Destroy on Explosion", state.destroyExplosion)));
        buttonList.add(new GuiButton(204, left + 164, CONTENT_TOP + 396, 150, 20, toggleLabel("Destroy on Projectile Hit", state.destroyProjectile)));

        doneButton = new GuiButton(1, left + 320, CONTENT_TOP + 420, 70, 20, "Done");
        buttonList.add(doneButton);
        buttonList.add(new GuiButton(2, left + 396, CONTENT_TOP + 420, 70, 20, "Cancel"));
        getItemButton = new GuiButton(205, left + 236, CONTENT_TOP + 14, 90, 20, "Get Item");
        restoreButton = new GuiButton(206, left + 332, CONTENT_TOP + 14, 70, 20, "Restore");
        buttonList.add(getItemButton);
        buttonList.add(restoreButton);

        refreshDynamicButtons();
        updateLayout();
        validateState();
    }

    private void refreshDynamicButtons() {
        // no-op: fixed tile count for models/actions
    }

    private void updateLayout() {
        int left = width / 2 - GUI_LEFT_PAD;
        int contentHeight = 220 + variationEditors.size() * 54 + getActionsHeight();
        int viewportHeight = height - CONTENT_BOTTOM_MARGIN - CONTENT_TOP;
        maxScroll = Math.max(0, contentHeight - viewportHeight);
        scrollOffset = MathHelper.clamp_int(scrollOffset, 0, maxScroll);
        int top = CONTENT_TOP - scrollOffset;

        customNameField.xPosition = left + 8;
        customNameField.yPosition = top + 14;
        recoveryField.xPosition = left + 8;

        int varTop = top + 52;
        for (int i = 0; i < variationEditors.size(); i++) {
            variationEditors.get(i).setPosition(left + 8, varTop + i * 54, 430);
        }

        int actionsTop = varTop + variationEditors.size() * 54 + 18;
        int actionY = actionsTop;
        for (int i = 0; i < actionEditors.size(); i++) {
            LootContainerActionEditor editor = actionEditors.get(i);
            editor.setPosition(left + 8, actionY, 430);
            actionY += editor.getHeight();
        }
        int checkboxesTop = actionY + 30;
        int recoveryTop = checkboxesTop + 62;
        int doneTop = recoveryTop + 34;
        recoveryField.yPosition = recoveryTop;

        for (Object obj : buttonList) {
            GuiButton button = (GuiButton) obj;
            if (button.id == 200) {
                button.xPosition = left + 8;
                button.yPosition = checkboxesTop;
            } else if (button.id == 201) {
                button.xPosition = left + 164;
                button.yPosition = checkboxesTop;
            } else if (button.id == 202) {
                button.xPosition = left + 320;
                button.yPosition = checkboxesTop;
            } else if (button.id == 203) {
                button.xPosition = left + 8;
                button.yPosition = checkboxesTop + 24;
            } else if (button.id == 204) {
                button.xPosition = left + 164;
                button.yPosition = checkboxesTop + 24;
            } else if (button.id == 1) {
                button.xPosition = left + 320;
                button.yPosition = doneTop;
            } else if (button.id == 2) {
                button.xPosition = left + 396;
                button.yPosition = doneTop;
            } else if (button.id == 205) {
                button.xPosition = left + 236;
                button.yPosition = top + 12;
            } else if (button.id == 206) {
                button.xPosition = left + 332;
                button.yPosition = top + 12;
            }
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;

        if (button.id == 1) {
            if (validateState()) {
                sendAndClose();
            } else if (confirmInvalidEntity) {
                sendAndClose();
            } else {
                confirmInvalidEntity = true;
            }
            return;
        }
        if (button.id == 2) {
            mc.displayGuiScreen(null);
            return;
        }
        if (button.id == 205) {
            handleGetItem();
            return;
        }
        if (button.id == 206) {
            handleRestore();
            return;
        }
        if (button.id >= 5000 && button.id < 6000) {
            int idx = button.id - 5000;
            if (idx >= 0 && idx < actionEditors.size()) {
                actionEditors.get(idx).cycleType();
            }
            return;
        }
        for (LootContainerActionEditor editor : actionEditors) {
            if (editor.actionPerformed(button)) {
                return;
            }
        }
        if (button.id >= 7000 && button.id < 8000) {
            int raw = button.id - 7000;
            int idx = raw / 2;
            boolean normal = (raw % 2) == 0;
            if (idx >= 0 && idx < variationEditors.size()) {
                ModelVariationEditor editor = variationEditors.get(idx);
                if (normal) {
                    editor.normalField.setFocused(true);
                    editor.destroyedField.setFocused(false);
                } else {
                    editor.destroyedField.setFocused(true);
                    editor.normalField.setFocused(false);
                }
                editor.toggleDropdown(normal);
            }
            return;
        }
        if (button.id >= 200 && button.id <= 204) {
            switch (button.id) {
                case 200: state.allowMultiaction = !state.allowMultiaction; break;
                case 201: state.destroyPlayerCollide = !state.destroyPlayerCollide; break;
                case 202: state.destroyEntityCollide = !state.destroyEntityCollide; break;
                case 203: state.destroyExplosion = !state.destroyExplosion; break;
                case 204: state.destroyProjectile = !state.destroyProjectile; break;
                default: break;
            }
            button.displayString = toggleLabel(button.displayString.replace("[x] ", "").replace("[ ] ", ""), getToggle(button.id));
        }
    }

    public void onItemPicked(int actionIndex, ItemStack selected) {
        if (selected == null || actionIndex < 0 || actionIndex >= actionEditors.size()) return;
        actionEditors.get(actionIndex).setPickedItem(selected);
        syncStateFromControls();
        validateState();
    }

    @Override
    public void openItemPicker(int actionIndex) {
        if (actionIndex < 0 || actionIndex >= actionEditors.size()) return;
        GuiLootContainerItemPicker picker = new GuiLootContainerItemPicker(this, actionIndex, player);
        mc.displayGuiScreen(picker);
    }

    private boolean getToggle(int id) {
        if (id == 200) return state.allowMultiaction;
        if (id == 201) return state.destroyPlayerCollide;
        if (id == 202) return state.destroyEntityCollide;
        if (id == 203) return state.destroyExplosion;
        if (id == 204) return state.destroyProjectile;
        return false;
    }

    private String toggleLabel(String name, boolean state) {
        return (state ? "[x] " : "[ ] ") + name;
    }

    private void sendAndClose() {
        syncStateFromControls();
        LootContainerData payload = state.toData(true);
        NBTTagCompound tag = new NBTTagCompound();
        payload.writeToItemStackNbt(tag);
        if (editingBlock) {
            PacketManager.INSTANCE.sendToServer(PacketLootContainerConfig.forBlock(blockX, blockY, blockZ, tag));
        } else {
            PacketManager.INSTANCE.sendToServer(PacketLootContainerConfig.forHeldItem(tag));
        }
        mc.displayGuiScreen(null);
    }

    private void handleGetItem() {
        syncStateFromControls();
        LootContainerData payload = state.toData(true);
        NBTTagCompound tag = new NBTTagCompound();
        payload.writeToItemStackNbt(tag);
        PacketManager.INSTANCE.sendToServer(PacketLootContainerGiveItem.of(tag));
    }

    private void handleRestore() {
        this.state = EditorState.fromData(initialData);
        this.customNameField = null;
        this.recoveryField = null;
        this.scrollOffset = 0;
        initGui();

        NBTTagCompound tag = new NBTTagCompound();
        initialData.writeToItemStackNbt(tag);
        if (editingBlock) {
            PacketManager.INSTANCE.sendToServer(PacketLootContainerRestore.at(blockX, blockY, blockZ));
            PacketManager.INSTANCE.sendToServer(PacketLootContainerConfig.forBlock(blockX, blockY, blockZ, tag));
        } else {
            PacketManager.INSTANCE.sendToServer(PacketLootContainerConfig.forHeldItem(tag));
        }
    }

    private boolean isActionFilled(LootContainerAction action) {
        return isActionFilledStatic(action);
    }

    private static boolean isActionFilledStatic(LootContainerAction action) {
        if (action == null) return false;
        if (action instanceof ItemDropAction) {
            ItemDropAction itemDrop = (ItemDropAction) action;
            return itemDrop.itemId != null && !itemDrop.itemId.trim().isEmpty();
        }
        if (action instanceof SpawnEntityAction) {
            SpawnEntityAction spawn = (SpawnEntityAction) action;
            return spawn.entityId != null && !spawn.entityId.trim().isEmpty() && spawn.spawnCount > 0;
        }
        if (action instanceof ApplyEffectAction) {
            ApplyEffectAction effect = (ApplyEffectAction) action;
            return effect.potionId != null && !effect.potionId.trim().isEmpty();
        }
        if (action instanceof ApplyExplosionEffectAction) {
            ApplyExplosionEffectAction effect = (ApplyExplosionEffectAction) action;
            return effect.potionId != null && !effect.potionId.trim().isEmpty();
        }
        if (action instanceof InstantDamageAction) {
            InstantDamageAction damage = (InstantDamageAction) action;
            return damage.damage > 0.0F;
        }
        if (action instanceof ExplosionInstantDamageAction) {
            ExplosionInstantDamageAction damage = (ExplosionInstantDamageAction) action;
            return damage.damage > 0.0F && damage.radius > 0.0F;
        }
        if (action instanceof BurningAction) {
            BurningAction burning = (BurningAction) action;
            return burning.durationSec > 0;
        }
        if (action instanceof ExplosionBurningAction) {
            ExplosionBurningAction burning = (ExplosionBurningAction) action;
            return burning.durationSec > 0 && burning.radius > 0.0F;
        }
        return false;
    }

    private boolean validateState() {
        errorText = "";
        warningText = "";
        confirmInvalidEntity = false;

        syncStateFromControls();
        LootContainerData collected = state.toData(true);
        List<String> clientErrors = new ArrayList<String>();

        String recoveryRaw = recoveryField == null ? "" : recoveryField.getText();
        RecoveryParseResult recoveryResult = parseRecoverySecondsResult(recoveryRaw);
        if (!recoveryResult.valid) {
            clientErrors.add(recoveryResult.error);
        }

        for (int i = 0; i < actionEditors.size(); i++) {
            actionEditors.get(i).collectValidationErrors(clientErrors, i);
        }

        List<String> schemaErrors = LootContainerConfigValidator.validate(collected);
        if (schemaErrors != null && !schemaErrors.isEmpty()) {
            clientErrors.addAll(schemaErrors);
        }

        if (!clientErrors.isEmpty()) {
            errorText = "Error: " + clientErrors.get(0);
            doneButton.enabled = false;
            return false;
        }

        boolean hasAnyNormalModel = false;
        for (LootContainerData.ModelVariation variation : collected.modelVariations) {
            if (variation != null && variation.normalModel != null && !variation.normalModel.trim().isEmpty()) {
                hasAnyNormalModel = true;
                break;
            }
        }
        if (!hasAnyNormalModel) {
            errorText = "Error: no model variation configured.";
            doneButton.enabled = false;
            return false;
        }

        boolean hasDestroyed = false;
        for (LootContainerData.ModelVariation variation : collected.modelVariations) {
            if (variation.destroyedModel != null && !variation.destroyedModel.trim().isEmpty()) {
                hasDestroyed = true;
                break;
            }
        }
        if (!hasDestroyed) {
            warningText = "Warn: destroyed state fallback is jugs.geo.json + missing texture (creative-only).";
        }

        if (collected.getActions().isEmpty()) {
            warningText = appendWarning(warningText, "Warn: no actions configured.");
        }

        for (LootContainerActionEditor editor : actionEditors) {
            LootContainerAction action = editor.getAction();
            if (action instanceof SpawnEntityAction && !isEntityValid(((SpawnEntityAction) action).entityId)) {
                String entityId = ((SpawnEntityAction) action).entityId;
                warningText = appendWarning(warningText, "Warn: unknown entity id \"" + entityId + "\". Done again to confirm.");
                doneButton.enabled = true;
                return false;
            }
        }

        doneButton.enabled = true;
        return true;
    }

    private void syncStateFromControls() {
        if (state == null) {
            state = new EditorState();
        }
        state.customNameInput = customNameField == null ? "" : customNameField.getText();
        state.recoveryInput = recoveryField == null ? "" : recoveryField.getText();

        state.modelVariations.clear();
        for (ModelVariationEditor editor : variationEditors) {
            LootContainerData.ModelVariation variation = editor.toData();
            state.modelVariations.add(EditorState.copyVariation(variation));
        }

        state.actions.clear();
        for (LootContainerActionEditor editor : actionEditors) {
            LootContainerAction action = editor.toData();
            state.actions.add(action);
        }
    }

    private static String appendWarning(String base, String next) {
        if (base == null || base.isEmpty()) return next;
        return base + " " + next;
    }

    private static boolean isEntityValid(String id) {
        if (id == null || id.trim().isEmpty()) return false;
        return EntityList.stringToClassMapping.containsKey(id);
    }

    private int parseRecoverySeconds(String value) {
        return parseRecoverySecondsResult(value).seconds;
    }

    private static int parseRecoverySecondsStatic(String value) {
        return parseRecoverySecondsResult(value).seconds;
    }

    private static RecoveryParseResult parseRecoverySecondsResult(String value) {
        if (value == null) {
            return RecoveryParseResult.error("Recovery time must be a non-negative number with optional m/h/d suffix.");
        }
        String raw = value.trim();
        if (raw.isEmpty()) {
            return RecoveryParseResult.error("Recovery time must be a non-negative number with optional m/h/d suffix.");
        }

        long multiplier = 1L;
        String numberPart = raw;
        char last = raw.charAt(raw.length() - 1);
        if (Character.isLetter(last)) {
            char suffix = Character.toLowerCase(last);
            numberPart = raw.substring(0, raw.length() - 1).trim();
            if (numberPart.isEmpty()) {
                return RecoveryParseResult.error("Recovery time must start with a number.");
            }
            if (suffix == 'm') {
                multiplier = 60L;
            } else if (suffix == 'h') {
                multiplier = 60L * 60L;
            } else if (suffix == 'd') {
                multiplier = 24L * 60L * 60L;
            } else {
                return RecoveryParseResult.error("Recovery time supports only m, h or d suffix.");
            }
        }

        long base;
        try {
            base = Long.parseLong(numberPart);
        } catch (NumberFormatException ignored) {
            return RecoveryParseResult.error("Recovery time must be a non-negative whole number.");
        }
        if (base < 0L) {
            return RecoveryParseResult.error("Recovery time must be non-negative.");
        }
        if (base > (Long.MAX_VALUE / Math.max(1L, multiplier))) {
            return RecoveryParseResult.error("Recovery time is too large.");
        }

        long seconds = base * multiplier;
        if (seconds > Integer.MAX_VALUE) {
            return RecoveryParseResult.error("Recovery time is too large.");
        }
        return RecoveryParseResult.success((int) seconds);
    }

    private static LootContainerData copyData(LootContainerData source) {
        if (source == null) return new LootContainerData();
        NBTTagCompound tag = new NBTTagCompound();
        source.writeToItemStackNbt(tag);
        return LootContainerData.fromItemStackNbt(tag);
    }

    private static class RecoveryParseResult {
        final boolean valid;
        final int seconds;
        final String error;

        private RecoveryParseResult(boolean valid, int seconds, String error) {
            this.valid = valid;
            this.seconds = seconds;
            this.error = error == null ? "" : error;
        }

        static RecoveryParseResult success(int seconds) {
            return new RecoveryParseResult(true, Math.max(0, seconds), "");
        }

        static RecoveryParseResult error(String message) {
            return new RecoveryParseResult(false, 0, message);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (customNameField.textboxKeyTyped(typedChar, keyCode) ||
                recoveryField.textboxKeyTyped(typedChar, keyCode)) {
            validateState();
            return;
        }
        for (ModelVariationEditor editor : variationEditors) {
            if (editor.keyTyped(typedChar, keyCode)) {
                validateState();
                return;
            }
        }
        for (LootContainerActionEditor editor : actionEditors) {
            if (editor.keyTyped(typedChar, keyCode)) {
                validateState();
                return;
            }
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        updateLayout();
        if (button == 0 && isOverScrollTrack(mouseX, mouseY)) {
            draggingScroll = true;
            dragScrollStartY = mouseY;
            dragScrollStartOffset = scrollOffset;
        }
        for (ModelVariationEditor editor : variationEditors) {
            if (editor.tryClickDropdown(mouseX, mouseY, button)) {
                validateState();
                return;
            }
        }
        for (LootContainerActionEditor editor : actionEditors) {
            if (editor.tryClickDropdown(mouseX, mouseY, button)) {
                validateState();
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
        customNameField.mouseClicked(mouseX, mouseY, button);
        recoveryField.mouseClicked(mouseX, mouseY, button);
        for (ModelVariationEditor editor : variationEditors) {
            if (editor.mouseClicked(mouseX, mouseY, button)) {
                validateState();
                return;
            }
        }
        for (LootContainerActionEditor editor : actionEditors) {
            editor.mouseClicked(mouseX, mouseY, button);
        }
        validateState();
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int button) {
        super.mouseMovedOrUp(mouseX, mouseY, button);
        if (button == 0) {
            draggingScroll = false;
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0) return;

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        for (ModelVariationEditor editor : variationEditors) {
            if (editor.handleDropdownScroll(mouseX, mouseY, wheel)) {
                return;
            }
        }
        for (LootContainerActionEditor editor : actionEditors) {
            if (editor.handleDropdownScroll(mouseX, mouseY, wheel)) {
                return;
            }
        }

        if (wheel > 0) scrollOffset -= 20;
        if (wheel < 0) scrollOffset += 20;
        scrollOffset = MathHelper.clamp_int(scrollOffset, 0, maxScroll);
        updateLayout();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (draggingScroll && Mouse.isButtonDown(0) && maxScroll > 0) {
            int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;
            int delta = mouseY - dragScrollStartY;
            int trackHeight = (height - CONTENT_BOTTOM_MARGIN) - CONTENT_TOP - 20;
            if (trackHeight > 0) {
                int contentDelta = (int) ((delta / (float) trackHeight) * maxScroll);
                scrollOffset = MathHelper.clamp_int(dragScrollStartOffset + contentDelta, 0, maxScroll);
                updateLayout();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        updateLayout();
        int left = width / 2 - GUI_LEFT_PAD;
        int top = CONTENT_TOP - scrollOffset;
        int varTop = top + 52;
        int actionsTop = varTop + variationEditors.size() * 54 + 18;

        drawCenteredString(fontRendererObj, "LootContainer Configuration", width / 2, top - 14, 0xFFFFFF);
        drawString(fontRendererObj, "Custom name", left + 8, top + 2, 0xCFCFCF);
        drawString(fontRendererObj, "Models (max 5)", left + 8, top + 36, 0xFFD070);
        drawString(fontRendererObj, "Actions (max 8)", left + 8, actionsTop - 14, 0xFFD070);
        RecoveryParseResult recoveryView = parseRecoverySecondsResult(recoveryField.getText());
        drawString(fontRendererObj, "Recovery time (no suffix=sec, m=minutes, h=hours, d=days)", left + 8, recoveryField.yPosition - 12, 0xCFCFCF);
        if (recoveryView.valid) {
            drawString(fontRendererObj, "Value will be set: " + recoveryView.seconds + " sec",
                    left + 116, recoveryField.yPosition + 4, 0x9E9E9E);
        } else {
            drawString(fontRendererObj, recoveryView.error,
                    left + 116, recoveryField.yPosition + 4, 0xFF5555);
        }

        customNameField.drawTextBox();
        recoveryField.drawTextBox();

        for (ModelVariationEditor editor : variationEditors) {
            editor.draw(mouseX, mouseY);
        }
        for (LootContainerActionEditor editor : actionEditors) {
            editor.renderPre(mouseX, mouseY);
        }
        for (LootContainerActionEditor editor : actionEditors) {
            editor.render(mouseX, mouseY);
        }

        if (!errorText.isEmpty()) {
            drawString(fontRendererObj, EnumChatFormatting.RED + errorText, left + 8, doneButton.yPosition + 6, 0xFF5555);
        } else if (!warningText.isEmpty()) {
            drawString(fontRendererObj, EnumChatFormatting.GOLD + warningText, left + 8, doneButton.yPosition + 6, 0xFFAA00);
        }

        int barX1 = getScrollBarX();
        int barY1 = CONTENT_TOP;
        int barY2 = height - CONTENT_BOTTOM_MARGIN;
        drawRect(barX1, barY1, barX1 + 6, barY2, 0x66101010);
        if (maxScroll > 0) {
            int track = barY2 - barY1 - 20;
            int thumbY = barY1 + (int) ((scrollOffset / (float) maxScroll) * track);
            drawRect(barX1 + 1, thumbY, barX1 + 5, thumbY + 20, 0xFF7D7D7D);
            drawRect(barX1 + 1, thumbY, barX1 + 4, thumbY + 19, 0xFFC3C3C3);
        } else {
            drawRect(barX1 + 1, barY1 + 1, barX1 + 5, barY1 + 21, 0xFF808080);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);

        for (ModelVariationEditor editor : variationEditors) {
            editor.drawSearchOverlay(mouseX, mouseY);
        }
        for (LootContainerActionEditor editor : actionEditors) {
            editor.renderPost(mouseX, mouseY);
        }
    }

    private int getActionsHeight() {
        int total = 0;
        for (LootContainerActionEditor editor : actionEditors) {
            total += editor.getHeight();
        }
        return total;
    }

    private int getScrollBarX() {
        return width - 6;
    }

    private boolean isOverScrollTrack(int mouseX, int mouseY) {
        int x = getScrollBarX();
        int y1 = CONTENT_TOP;
        int y2 = height - CONTENT_BOTTOM_MARGIN;
        return mouseX >= x && mouseX <= x + 6 && mouseY >= y1 && mouseY <= y2;
    }

    private static class EditorState {
        String customNameInput = "";
        String recoveryInput = "5";
        boolean allowMultiaction = false;
        boolean destroyPlayerCollide = true;
        boolean destroyEntityCollide = false;
        boolean destroyExplosion = false;
        boolean destroyProjectile = false;
        final List<LootContainerData.ModelVariation> modelVariations = new ArrayList<LootContainerData.ModelVariation>();
        final List<LootContainerAction> actions = new ArrayList<LootContainerAction>();

        static EditorState fromData(LootContainerData data) {
            EditorState state = new EditorState();
            if (data == null) return state;
            state.customNameInput = data.customName == null ? "" : data.customName;
            state.recoveryInput = String.valueOf(Math.max(0, data.recoveryTimeSec));
            state.allowMultiaction = data.allowMultiaction;
            state.destroyPlayerCollide = data.destroyOnPlayerCollide;
            state.destroyEntityCollide = data.destroyOnEntityCollide;
            state.destroyExplosion = data.destroyOnExplosion;
            state.destroyProjectile = data.destroyOnProjectileHit;

                        if (data.modelVariations != null) {
                            for (LootContainerData.ModelVariation variation : data.modelVariations) {
                                state.modelVariations.add(copyVariation(variation));
                                if (state.modelVariations.size() >= LootContainerData.MAX_VARIATIONS) break;
                            }
                        }
                        List<LootContainerAction> parsedActions = data.getActions();
            if (parsedActions != null) {
                for (LootContainerAction action : parsedActions) {
                    state.actions.add(action);
                    if (state.actions.size() >= LootContainerData.MAX_ACTIONS) break;
                }
            }
            return state;
        }

        LootContainerData toData(boolean trimIncompleteActions) {
            LootContainerData data = new LootContainerData();
            data.customName = LootContainerData.limitName(customNameInput);
            data.recoveryTimeSec = parseRecoverySecondsStatic(recoveryInput);
            data.allowMultiaction = allowMultiaction;
            data.destroyOnPlayerCollide = destroyPlayerCollide;
            data.destroyOnEntityCollide = destroyEntityCollide;
            data.destroyOnExplosion = destroyExplosion;
            data.destroyOnProjectileHit = destroyProjectile;

            data.modelVariations.clear();
            for (LootContainerData.ModelVariation variation : modelVariations) {
                LootContainerData.ModelVariation copy = copyVariation(variation);
                if (copy.normalModel != null && !copy.normalModel.trim().isEmpty()) {
                    data.modelVariations.add(copy);
                }
                if (data.modelVariations.size() >= LootContainerData.MAX_VARIATIONS) break;
            }

            List<LootContainerAction> outActions = new ArrayList<LootContainerAction>();
            for (LootContainerAction action : actions) {
                if (action == null) continue;
                if (trimIncompleteActions && !isActionFilledStatic(action)) continue;
                outActions.add(action);
                if (outActions.size() >= LootContainerData.MAX_ACTIONS) break;
            }
            data.setActions(outActions);
            return data;
        }

        static LootContainerData.ModelVariation copyVariation(LootContainerData.ModelVariation src) {
            LootContainerData.ModelVariation copy = new LootContainerData.ModelVariation();
            if (src == null) return copy;
            copy.normalModel = src.normalModel == null ? "" : src.normalModel;
            copy.normalTexture = src.normalTexture == null ? "" : src.normalTexture;
            copy.destroyedModel = src.destroyedModel == null ? "" : src.destroyedModel;
            copy.destroyedTexture = src.destroyedTexture == null ? "" : src.destroyedTexture;
            return copy;
        }
    }

    private class ModelVariationEditor {
        final int index;
        final LootContainerData.ModelVariation variation;
        GuiTextField normalField;
        GuiTextField destroyedField;
        GuiButton normalDropdownButton;
        GuiButton destroyedDropdownButton;
        int normalDropScroll = 0;
        int destroyedDropScroll = 0;
        boolean normalDropdownPinned = false;
        boolean destroyedDropdownPinned = false;
        boolean normalSearchByInput = false;
        boolean destroyedSearchByInput = false;
        boolean normalFocusDropdownOpen = false;
        boolean destroyedFocusDropdownOpen = false;
        int x;
        int y;
        int w;

        ModelVariationEditor(int index, LootContainerData.ModelVariation variation) {
            this.index = index;
            this.variation = variation == null ? new LootContainerData.ModelVariation() : variation;
        }

        void init(int x, int y, int w) {
            this.w = w;
            normalField = new GuiTextField(fontRendererObj, x + 2, y + 12, 120, 16);
            normalField.setMaxStringLength(64);
            normalField.setText(variation.normalModel == null ? "" : variation.normalModel);

            destroyedField = new GuiTextField(fontRendererObj, x + 206, y + 12, 120, 16);
            destroyedField.setMaxStringLength(64);
            destroyedField.setText(variation.destroyedModel == null ? "" : variation.destroyedModel);

            normalDropdownButton = new GuiButton(7000 + index * 2, x + 124, y + 16, 18, 16, "v");
            destroyedDropdownButton = new GuiButton(7001 + index * 2, x + 328, y + 16, 18, 16, "v");
            buttonList.add(normalDropdownButton);
            buttonList.add(destroyedDropdownButton);
            setPosition(x, y, w);
        }

        void setPosition(int x, int y, int w) {
            this.x = x;
            this.y = y;
            this.w = w;
            normalField.xPosition = x + 2;
            normalField.yPosition = y + 16;
            destroyedField.xPosition = x + 206;
            destroyedField.yPosition = y + 16;
            if (normalDropdownButton != null) {
                normalDropdownButton.xPosition = x + 124;
                normalDropdownButton.yPosition = y + 16;
            }
            if (destroyedDropdownButton != null) {
                destroyedDropdownButton.xPosition = x + 328;
                destroyedDropdownButton.yPosition = y + 16;
            }
        }

        boolean keyTyped(char c, int code) {
            boolean normalFocused = normalField.isFocused();
            boolean destroyedFocused = destroyedField.isFocused();
            boolean normalChanged = normalField.textboxKeyTyped(c, code);
            boolean destroyedChanged = destroyedField.textboxKeyTyped(c, code);
            boolean changed = normalChanged || destroyedChanged;
            if (normalChanged) {
                normalSearchByInput = normalFocused;
                normalFocusDropdownOpen = normalFocused;
            }
            if (destroyedChanged) {
                destroyedSearchByInput = destroyedFocused;
                destroyedFocusDropdownOpen = destroyedFocused;
            }
            if (changed) {
                variation.normalModel = normalField.getText();
                variation.destroyedModel = destroyedField.getText();
                normalDropScroll = 0;
                destroyedDropScroll = 0;
                BlockModels normalBlock = ClientProxy.MODEL_REGISTRY.get(variation.normalModel);
                variation.normalTexture = normalBlock == null ? "" : normalBlock.getTextureName();
                BlockModels destroyedBlock = ClientProxy.MODEL_REGISTRY.get(variation.destroyedModel);
                variation.destroyedTexture = destroyedBlock == null ? "" : destroyedBlock.getTextureName();
            }
            return changed;
        }

        void toggleDropdown(boolean normal) {
            if (normal) {
                normalDropdownPinned = !normalDropdownPinned;
                if (normalDropdownPinned) {
                    normalDropScroll = 0;
                    normalSearchByInput = false;
                    normalFocusDropdownOpen = false;
                }
            } else {
                destroyedDropdownPinned = !destroyedDropdownPinned;
                if (destroyedDropdownPinned) {
                    destroyedDropScroll = 0;
                    destroyedSearchByInput = false;
                    destroyedFocusDropdownOpen = false;
                }
            }
        }

        boolean mouseClicked(int mouseX, int mouseY, int button) {
            normalField.mouseClicked(mouseX, mouseY, button);
            destroyedField.mouseClicked(mouseX, mouseY, button);
            if (normalField.isFocused()) {
                normalFocusDropdownOpen = true;
            } else {
                normalFocusDropdownOpen = false;
            }
            if (destroyedField.isFocused()) {
                destroyedFocusDropdownOpen = true;
            } else {
                destroyedFocusDropdownOpen = false;
            }
            return false;
        }

        void draw(int mouseX, int mouseY) {
            drawString(fontRendererObj, "Model Variation #" + (index + 1), x + 2, y + 4, 0xBFBFBF);
            drawString(fontRendererObj, "Destroyed Model", x + 206, y + 4, 0xBFBFBF);
            normalField.drawTextBox();
            destroyedField.drawTextBox();

            drawModelPreview(variation.normalModel, x + 146, y + 12, mouseX, mouseY);
            drawModelPreview(variation.destroyedModel, x + 350, y + 12, mouseX, mouseY);
        }

        void drawSearchOverlay(int mouseX, int mouseY) {
            drawSearchOverlay(normalField, true, mouseX, mouseY);
            drawSearchOverlay(destroyedField, false, mouseX, mouseY);
        }

        private void drawStoneBackground(int x, int y) {
            mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            drawTexturedModelRectFromIcon(x, y, Blocks.stone.getIcon(0, 0), 18, 18);
            drawRect(x, y, x + 18, y + 18, 0x22000000);
        }

        private void drawModelPreview(String modelName, int x, int y, int mouseX, int mouseY) {
            drawStoneBackground(x, y);
            BlockModels model = ClientProxy.MODEL_REGISTRY.get(modelName);
            if (model == null) return;
            Item item = Item.getItemFromBlock(model);
            if (item == null) return;
            RenderHelper.enableGUIStandardItemLighting();
            itemRenderer.renderItemAndEffectIntoGUI(fontRendererObj, mc.getTextureManager(), new ItemStack(item, 1, 0), x + 1, y + 1);
            RenderHelper.disableStandardItemLighting();
            if (mouseX >= x && mouseX <= x + 18 && mouseY >= y && mouseY <= y + 18) {
                List lines = new ArrayList();
                lines.add(modelName);
                lines.add("texture: " + model.getTextureName());
                drawHoveringText(lines, mouseX, mouseY, fontRendererObj);
            }
        }

        private void drawSearchOverlay(GuiTextField field, boolean normal, int mouseX, int mouseY) {
            ModelDropdownState state = getDropdownState(field, normal);
            if (state == null) return;
            drawRect(state.bx, state.by, state.bx + state.bw, state.by + state.bh, 0xE0101010);
            for (int i = 0; i < state.shown; i++) {
                int idx = i + state.scroll;
                if (idx >= state.matches.size()) break;
                String value = state.matches.get(idx);
                int ly = state.by + 2 + i * 12;
                int color = 0xE0E0E0;
                if (mouseX >= state.bx + 2 && mouseX <= state.bx + state.bw - 2 && mouseY >= ly && mouseY <= ly + 10) {
                    color = 0xFFFFFF;
                }
                drawString(fontRendererObj, value, state.bx + 4, ly + 1, color);
            }
        }

        boolean tryClickDropdown(int mouseX, int mouseY, int button) {
            if (button != 0) return false;
            if (tryClickDropdown(normalField, true, mouseX, mouseY)) return true;
            return tryClickDropdown(destroyedField, false, mouseX, mouseY);
        }

        private String getModelQuery(GuiTextField field, boolean normal) {
            boolean searchByInput = normal ? normalSearchByInput : destroyedSearchByInput;
            if (!field.isFocused() || !searchByInput) {
                return "";
            }
            String text = field.getText();
            return text == null ? "" : text.toLowerCase();
        }

        private List<String> getModelMatches(String query) {
            List<String> matches = new ArrayList<String>();
            for (String key : ClientProxy.MODEL_REGISTRY.keySet()) {
                if (query.isEmpty() || key.toLowerCase().contains(query)) {
                    matches.add(key);
                }
            }
            return matches;
        }

        boolean handleDropdownScroll(int mouseX, int mouseY, int wheel) {
            if (handleOneDropdownScroll(normalField, true, mouseX, mouseY, wheel)) return true;
            return handleOneDropdownScroll(destroyedField, false, mouseX, mouseY, wheel);
        }

        private boolean handleOneDropdownScroll(GuiTextField field, boolean normal, int mouseX, int mouseY, int wheel) {
            ModelDropdownState state = getDropdownState(field, normal);
            if (state == null) return false;
            if (mouseX < state.bx || mouseX > state.bx + state.bw || mouseY < state.by || mouseY > state.by + state.bh) return false;
            int scroll = state.scroll;
            if (wheel < 0) scroll++;
            if (wheel > 0) scroll--;
            scroll = MathHelper.clamp_int(scroll, 0, state.maxScroll);
            if (normal) normalDropScroll = scroll; else destroyedDropScroll = scroll;
            return true;
        }

        private boolean tryClickDropdown(GuiTextField field, boolean normal, int mouseX, int mouseY) {
            ModelDropdownState state = getDropdownState(field, normal);
            if (state == null) return false;
            for (int i = 0; i < state.shown; i++) {
                int idx = i + state.scroll;
                if (idx >= state.matches.size()) break;
                int ly = state.by + 2 + i * 12;
                if (mouseX >= state.bx + 2 && mouseX <= state.bx + state.bw - 2 && mouseY >= ly && mouseY <= ly + 10) {
                    String value = state.matches.get(idx);
                    field.setText(value);
                    if (normal) {
                        variation.normalModel = value;
                        BlockModels block = ClientProxy.MODEL_REGISTRY.get(value);
                        variation.normalTexture = block == null ? "" : block.getTextureName();
                        normalSearchByInput = false;
                        normalFocusDropdownOpen = false;
                        normalDropdownPinned = false;
                    } else {
                        variation.destroyedModel = value;
                        BlockModels block = ClientProxy.MODEL_REGISTRY.get(value);
                        variation.destroyedTexture = block == null ? "" : block.getTextureName();
                        destroyedSearchByInput = false;
                        destroyedFocusDropdownOpen = false;
                        destroyedDropdownPinned = false;
                    }
                    return true;
                }
            }
            return false;
        }

        private ModelDropdownState getDropdownState(GuiTextField field, boolean normal) {
            boolean pinned = normal ? normalDropdownPinned : destroyedDropdownPinned;
            boolean focusOpen = normal ? normalFocusDropdownOpen : destroyedFocusDropdownOpen;
            if (!pinned && (!field.isFocused() || !focusOpen)) return null;
            String query = getModelQuery(field, normal);
            List<String> matches = getModelMatches(query);
            Collections.sort(matches);
            int shown = Math.min(10, matches.size());
            if (shown <= 0) return null;
            int max = Math.max(0, matches.size() - shown);
            int scroll = normal ? normalDropScroll : destroyedDropScroll;
            scroll = MathHelper.clamp_int(scroll, 0, max);
            if (normal) {
                normalDropScroll = scroll;
            } else {
                destroyedDropScroll = scroll;
            }
            return new ModelDropdownState(matches, shown, scroll, max, field.xPosition, field.yPosition + 18, field.width);
        }

        private class ModelDropdownState {
            final List<String> matches;
            final int shown;
            final int scroll;
            final int maxScroll;
            final int bx;
            final int by;
            final int bw;
            final int bh;

            ModelDropdownState(List<String> matches, int shown, int scroll, int maxScroll, int bx, int by, int bw) {
                this.matches = matches;
                this.shown = shown;
                this.scroll = scroll;
                this.maxScroll = maxScroll;
                this.bx = bx;
                this.by = by;
                this.bw = bw;
                this.bh = shown * 12 + 6;
            }
        }

        LootContainerData.ModelVariation toData() {
            variation.normalModel = normalField.getText();
            variation.destroyedModel = destroyedField.getText();
            BlockModels normalBlock = ClientProxy.MODEL_REGISTRY.get(variation.normalModel);
            variation.normalTexture = normalBlock == null ? "" : normalBlock.getTextureName();
            BlockModels destroyedBlock = ClientProxy.MODEL_REGISTRY.get(variation.destroyedModel);
            variation.destroyedTexture = destroyedBlock == null ? "" : destroyedBlock.getTextureName();
            return variation;
        }
    }
}
