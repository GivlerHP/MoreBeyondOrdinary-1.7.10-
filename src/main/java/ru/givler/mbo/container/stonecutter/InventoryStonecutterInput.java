package ru.givler.mbo.container.stonecutter;

import net.minecraft.inventory.InventoryBasic;

final class InventoryStonecutterInput extends InventoryBasic {
    private final ContainerStonecutter container;

    InventoryStonecutterInput(ContainerStonecutter container) {
        super("stonecutter.input", false, 1);
        this.container = container;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        container.onInputChanged();
    }
}
