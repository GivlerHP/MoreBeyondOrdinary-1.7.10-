package ru.givler.mbo.lootcontainer.action;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LootContainerActionRegistry {
    public static final String TYPE_ITEM_DROP = "item_drop";
    public static final String TYPE_SPAWN_ENTITY = "spawn_entity";
    public static final String TYPE_APPLY_EFFECT = "apply_effect";
    public static final String TYPE_APPLY_EXPLOSION_EFFECT = "apply_explosion_effect";
    public static final String TYPE_INSTANT_DAMAGE = "instant_damage";
    public static final String TYPE_EXPLOSION_INSTANT_DAMAGE = "explosion_instant_damage";
    public static final String TYPE_BURNING = "burning";
    public static final String TYPE_EXPLOSION_BURNING = "explosion_burning";

    private static final Map<String, Class<? extends LootContainerAction>> REGISTRY =
            new LinkedHashMap<String, Class<? extends LootContainerAction>>();

    static {
        register(TYPE_ITEM_DROP, ItemDropAction.class);
        register(TYPE_SPAWN_ENTITY, SpawnEntityAction.class);
        register(TYPE_APPLY_EFFECT, ApplyEffectAction.class);
        register(TYPE_APPLY_EXPLOSION_EFFECT, ApplyExplosionEffectAction.class);
        register(TYPE_INSTANT_DAMAGE, InstantDamageAction.class);
        register(TYPE_EXPLOSION_INSTANT_DAMAGE, ExplosionInstantDamageAction.class);
        register(TYPE_BURNING, BurningAction.class);
        register(TYPE_EXPLOSION_BURNING, ExplosionBurningAction.class);
    }

    private LootContainerActionRegistry() {
    }

    public static void register(String type, Class<? extends LootContainerAction> clazz) {
        if (type == null || type.trim().isEmpty() || clazz == null) return;
        REGISTRY.put(type, clazz);
    }

    public static LootContainerAction create(String type) {
        Class<? extends LootContainerAction> clazz = REGISTRY.get(type);
        if (clazz == null) return null;
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Map<String, Class<? extends LootContainerAction>> types() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}
