package ru.givler.mbo.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.minecraft.potion.Potion;

public final class PotionArrayExpander {
    private PotionArrayExpander() {}

    /**
     * Расширяет любой статический массив Potion[] в классе Potion до newSize.
     * Подходит для обфусцированных сборок, где имя поля может быть любым.
     */
    public static void expand(final int newSize) {
        try {
            // 1) Найти в Potion единственное статическое поле Potion[]
            Field targetField = null;
            for (Field f : Potion.class.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())
                        && f.getType().isArray()
                        && f.getType().getComponentType() == Potion.class) {
                    targetField = f;
                    break;
                }
            }
            if (targetField == null) {
                throw new RuntimeException("Не найдено поле Potion[] в классе Potion");
            }

            // 2) Делаем поле доступным и снимаем final
            targetField.setAccessible(true);
            Field mod = Field.class.getDeclaredField("modifiers");
            mod.setAccessible(true);
            mod.setInt(targetField, targetField.getModifiers() & ~Modifier.FINAL);

            // 3) Расширяем массив
            Potion[] oldArray = (Potion[]) targetField.get(null);
            if (oldArray.length >= newSize) return; // уже достаточно велик
            Potion[] newArray = new Potion[newSize];
            System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);

            // 4) Перезаписываем поле
            targetField.set(null, newArray);

            System.out.println("[MBO] Expanded Potion array from "
                    + oldArray.length + " to " + newSize);
        } catch (Exception e) {
            throw new RuntimeException("Failed to expand Potion array", e);
        }
    }
}
