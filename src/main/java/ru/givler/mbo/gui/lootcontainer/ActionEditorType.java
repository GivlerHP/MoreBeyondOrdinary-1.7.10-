package ru.givler.mbo.gui.lootcontainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.givler.mbo.lootcontainer.action.LootContainerAction;

import java.util.function.Function;
import java.util.function.Supplier;

@SideOnly(Side.CLIENT)
public class ActionEditorType {
    public final String typeKey;
    public final String label;
    private final Class<? extends LootContainerAction> actionClass;
    private final Supplier<AbstractLootActionGuiEditor<?>> defaultFactory;
    private final Function<LootContainerAction, AbstractLootActionGuiEditor<?>> actionFactory;

    public ActionEditorType(String typeKey,
                            String label,
                            Class<? extends LootContainerAction> actionClass,
                            Supplier<AbstractLootActionGuiEditor<?>> defaultFactory,
                            Function<LootContainerAction, AbstractLootActionGuiEditor<?>> actionFactory) {
        this.typeKey = typeKey;
        this.label = label;
        this.actionClass = actionClass;
        this.defaultFactory = defaultFactory;
        this.actionFactory = actionFactory;
    }

    public boolean supports(LootContainerAction action) {
        return actionClass.isInstance(action);
    }

    public AbstractLootActionGuiEditor<?> createDefault() {
        return defaultFactory.get();
    }

    public AbstractLootActionGuiEditor<?> createFromAction(LootContainerAction action) {
        return actionFactory.apply(action);
    }
}
