package ru.givler.mbo.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.givler.mbo.container.ContainerLockConfig;
import ru.givler.mbo.lockable.LockDifficulty;
import ru.givler.mbo.lockable.RefillMode;
import ru.givler.mbo.network.PacketManager;
import ru.givler.mbo.network.packet.PacketLockLootSettings;
import ru.givler.mbo.network.packet.PacketSetLockDifficulty;
import ru.givler.mbo.tileentity.TileEntityLockableChest;

public class GuiLockChestConfig extends GuiContainer {
    private final TileEntityLockableChest chest;
    private GuiTextField group, fillField, cooldownField, intervalField, countField, radiusField, weightField;
    private int difficulty, mode, fill, cooldown, interval, count, radius, selected;
    private boolean weighted;
    private final int[] weights = new int[14];
    private final boolean[] guaranteed = new boolean[14];

    public GuiLockChestConfig(InventoryPlayer player, TileEntityLockableChest chest) {
        super(new ContainerLockConfig(player, chest)); this.chest = chest; xSize = 340; ySize = 276;
        difficulty = chest.getLockData().getDifficulty().ordinal(); mode = chest.getRefillMode().ordinal();
        fill = chest.getFillSlots(); cooldown = chest.getCooldownSeconds(); interval = chest.getGradualIntervalSeconds();
        count = chest.getGradualCount(); radius = chest.getLockData().getPlayerRadius(); weighted = chest.isWeightedSelection();
        for (int i = 0; i < 14; i++) { weights[i] = chest.getLootWeight(i); guaranteed[i] = chest.isGuaranteed(i); }
    }

    @Override public void initGui() {
        super.initGui(); Keyboard.enableRepeatEvents(true);
        int x = guiLeft + 188, y = guiTop + 24;
        buttonList.add(new GuiButton(1, x, y, 144, 20, difficultyLabel()));
        buttonList.add(new GuiButton(2, x, y + 23, 144, 20, modeLabel()));
        buttonList.add(new GuiButton(3, x, y + 46, 144, 20, selectionLabel()));
        fillField = numberField(x, y + 81, 70, fill); cooldownField = numberField(x + 74, y + 81, 70, cooldown);
        intervalField = numberField(x, y + 111, 70, interval); countField = numberField(x + 74, y + 111, 70, count);
        radiusField = numberField(x, y + 141, 70, radius);
        buttonList.add(new GuiButton(9, x + 74, y + 140, 70, 20, guaranteeLabel()));
        weightField = numberField(x, y + 171, 144, weights[selected]);
        group = new GuiTextField(fontRendererObj, x, y + 201, 144, 18);
        group.setText(chest.getGroupId()); group.setMaxStringLength(64);
        buttonList.add(new GuiButton(12, x, y + 224, 70, 20, I18n.format("mbo.lock.template.open")));
        buttonList.add(new GuiButton(11, x + 74, y + 224, 70, 20, I18n.format("gui.done")));
    }

    private GuiTextField numberField(int x, int y, int width, int value) {
        GuiTextField field = new GuiTextField(fontRendererObj, x, y, width, 18);
        field.setText(Integer.toString(value)); field.setMaxStringLength(10); return field;
    }

    private String difficultyLabel() { return I18n.format("mbo.lock.gui.difficulty.value", I18n.format("mbo.lock.difficulty." + LockDifficulty.byOrdinal(difficulty).name().toLowerCase())); }
    private String modeLabel() { return I18n.format("mbo.lock.gui.refill.value", I18n.format("mbo.lock.refill." + RefillMode.byOrdinal(mode).name().toLowerCase())); }
    private String selectionLabel() { return I18n.format("mbo.lock.gui.selection.value", I18n.format(weighted ? "mbo.lock.selection.weighted" : "mbo.lock.selection.equal")); }
    private String guaranteeLabel() { return I18n.format("mbo.lock.gui.guaranteed.short", I18n.format(guaranteed[selected] ? "mbo.lock.state.on" : "mbo.lock.state.off")); }

    @Override protected void actionPerformed(GuiButton button) {
        if (button.id == 1) { difficulty = (difficulty + 1) % 4; button.displayString = difficultyLabel(); }
        else if (button.id == 2) { mode = (mode + 1) % 3; button.displayString = modeLabel(); }
        else if (button.id == 3) { weighted = !weighted; button.displayString = selectionLabel(); }
        else if (button.id == 9) { guaranteed[selected] = !guaranteed[selected]; button.displayString = guaranteeLabel(); }
        else if (button.id == 11) { save(); mc.displayGuiScreen(null); }
        else if (button.id == 12) { mc.displayGuiScreen(new GuiLockTemplates(chest.xCoord,chest.yCoord,chest.zCoord,buildTemplate())); }
    }

    private void save() {
        readFields();
        PacketManager.INSTANCE.sendToServer(new PacketSetLockDifficulty(chest.xCoord, chest.yCoord, chest.zCoord, difficulty));
        NBTTagCompound tag = new NBTTagCompound(); tag.setInteger("RefillMode", mode); tag.setBoolean("Weighted", weighted);
        tag.setInteger("FillSlots", fill); tag.setInteger("Cooldown", cooldown); tag.setInteger("GradualInterval", interval);
        tag.setInteger("GradualCount", count); tag.setInteger("PlayerRadius", radius); tag.setString("GroupId", group.getText());
        tag.setIntArray("Weights", weights); for (int i = 0; i < 14; i++) tag.setBoolean("Guaranteed" + i, guaranteed[i]);
        PacketManager.INSTANCE.sendToServer(new PacketLockLootSettings(chest.xCoord, chest.yCoord, chest.zCoord, tag));
    }

    private void readFields() {
        fill = value(fillField, fill, 1, 27); cooldown = value(cooldownField, cooldown, 1, 31536000);
        interval = value(intervalField, interval, 1, 31536000); count = value(countField, count, 1, 27);
        radius = value(radiusField, radius, 0, 128); weights[selected] = value(weightField, weights[selected], 0, 1000000);
    }

    private NBTTagCompound buildTemplate() {
        readFields();
        NBTTagCompound result=new NBTTagCompound(),settings=new NBTTagCompound();
        result.setInteger("Difficulty",difficulty);result.setInteger("RelockDelay",chest.getLockData().getRelockDelaySec());
        settings.setInteger("RefillMode",mode);settings.setBoolean("Weighted",weighted);settings.setInteger("FillSlots",fill);
        settings.setInteger("Cooldown",cooldown);settings.setInteger("GradualInterval",interval);settings.setInteger("GradualCount",count);
        settings.setInteger("PlayerRadius",radius);settings.setString("GroupId",group.getText());settings.setIntArray("Weights",weights);
        for(int i=0;i<14;i++)settings.setBoolean("Guaranteed"+i,guaranteed[i]);result.setTag("Settings",settings);
        NBTTagList items=new NBTTagList();ContainerLockConfig container=(ContainerLockConfig)inventorySlots;
        for(int i=0;i<14;i++){ItemStack stack=container.getTemplate(i);if(stack!=null){NBTTagCompound item=new NBTTagCompound();item.setByte("Slot",(byte)i);stack.writeToNBT(item);items.appendTag(item);}}
        result.setTag("Items",items);return result;
    }

    private int value(GuiTextField field, int fallback, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(field.getText()))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    @Override protected void mouseClicked(int mx, int my, int button) {
        super.mouseClicked(mx, my, button);
        for (GuiTextField field : fields()) field.mouseClicked(mx, my, button); group.mouseClicked(mx, my, button);
        int sx = mx - (guiLeft + 17), sy = my - (guiTop + 32);
        if (sx >= 0 && sx < 126 && sy >= 0 && sy < 36) {
            weights[selected] = value(weightField, weights[selected], 0, 1000000);
            selected = (sy / 18) * 7 + sx / 18; weightField.setText(Integer.toString(weights[selected]));
            for (Object object : buttonList) { GuiButton b = (GuiButton) object; if (b.id == 9) b.displayString = guaranteeLabel(); }
        }
    }

    private GuiTextField[] fields() { return new GuiTextField[]{fillField, cooldownField, intervalField, countField, radiusField, weightField}; }

    @Override protected void keyTyped(char ch, int key) {
        if (group.isFocused() && group.textboxKeyTyped(ch, key)) return;
        for (GuiTextField field : fields()) if (numericKey(field, ch, key)) return;
        super.keyTyped(ch, key);
    }

    private boolean numericKey(GuiTextField field, char ch, int key) {
        if (!field.isFocused()) return false;
        if (key == Keyboard.KEY_ESCAPE) return false;
        if (Character.isDigit(ch) || key == Keyboard.KEY_BACK || key == Keyboard.KEY_DELETE
                || key == Keyboard.KEY_LEFT || key == Keyboard.KEY_RIGHT || key == Keyboard.KEY_HOME || key == Keyboard.KEY_END
                || (isCtrlKeyDown() && (key == Keyboard.KEY_A || key == Keyboard.KEY_C || key == Keyboard.KEY_V || key == Keyboard.KEY_X)))
            field.textboxKeyTyped(ch, key);
        return true;
    }

    @Override public void updateScreen() { super.updateScreen(); group.updateCursorCounter(); for (GuiTextField field : fields()) field.updateCursorCounter(); }
    @Override protected void drawGuiContainerBackgroundLayer(float p, int mx, int my) {
        GL11.glDisable(GL11.GL_TEXTURE_2D); drawGradientRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xff202020, 0xff101010);
        for (int i = 0; i < 14; i++) { int x = guiLeft + 16 + (i % 7) * 18, y = guiTop + 31 + (i / 7) * 18; drawRect(x, y, x + 18, y + 18, i == selected ? 0xffb08030 : 0xff555555); }
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
    @Override protected void drawGuiContainerForegroundLayer(int mx, int my) {
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.settings"), 8, 8, 0xffffff);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.lootTemplates", 14), 17, 21, 0xdddddd);
        int x = 188, y = 95;
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.slots.label"), x, y, 0xdddddd);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.cooldown.label"), x + 74, y, 0xdddddd);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.interval.label"), x, y + 30, 0xdddddd);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.count.label"), x + 74, y + 30, 0xdddddd);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.radius.label"), x, y + 60, 0xdddddd);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.weight.label"), x, y + 90, 0xdddddd);
        fontRendererObj.drawString(I18n.format("mbo.lock.gui.groupId"), x, y + 120, 0xdddddd);
    }
    @Override public void drawScreen(int mx, int my, float partial) { super.drawScreen(mx, my, partial); for (GuiTextField field : fields()) field.drawTextBox(); group.drawTextBox(); }
    @Override public void onGuiClosed() { Keyboard.enableRepeatEvents(false); super.onGuiClosed(); }
}
