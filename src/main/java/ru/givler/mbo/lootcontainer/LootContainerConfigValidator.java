package ru.givler.mbo.lootcontainer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import ru.givler.mbo.lootcontainer.action.LootContainerActionRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LootContainerConfigValidator {
    private LootContainerConfigValidator() {
    }

    public static List<String> validate(LootContainerData data) {
        if (data == null) return Collections.singletonList("Config is null.");
        List<String> errors = new ArrayList<String>();

        if (data.recoveryTimeSec < 0 || data.recoveryTimeSec > LootContainerData.MAX_RECOVERY_TIME_SEC) {
            errors.add("Recovery time is outside the allowed range.");
        }
        if (tooLong(data.customName, LootContainerData.MAX_CUSTOM_NAME_LENGTH)
                || tooLong(data.destroySound, LootContainerData.MAX_CONFIG_STRING_LENGTH)) {
            errors.add("A config string is too long.");
        }
        if (tooLong(data.actionsJson, LootContainerData.MAX_ACTIONS_JSON_LENGTH)) {
            errors.add("Actions json is too large.");
            return errors;
        }
        if (!validCollision(data)) {
            errors.add("Collision bounds must be finite, inside the block and ordered.");
        }
        if (data.modelVariations == null || data.modelVariations.isEmpty()) {
            errors.add("At least one model variation is required.");
        }
        if (data.modelVariations != null) {
            for (int i = 0; i < data.modelVariations.size(); i++) {
                LootContainerData.ModelVariation variation = data.modelVariations.get(i);
                if (variation == null || variation.normalModel == null || variation.normalModel.trim().isEmpty()) {
                    errors.add("Model variation #" + (i + 1) + " has empty normal model.");
                    break;
                }
                if (tooLong(variation.normalModel, LootContainerData.MAX_CONFIG_STRING_LENGTH)
                        || tooLong(variation.normalTexture, LootContainerData.MAX_CONFIG_STRING_LENGTH)
                        || tooLong(variation.destroyedModel, LootContainerData.MAX_CONFIG_STRING_LENGTH)
                        || tooLong(variation.destroyedTexture, LootContainerData.MAX_CONFIG_STRING_LENGTH)) {
                    errors.add("Model variation #" + (i + 1) + " contains an overlong resource name.");
                    break;
                }
            }
        }
        validateActionsJson(data.actionsJson, errors);
        return errors;
    }

    private static void validateActionsJson(String json, List<String> errors) {
        if (json == null || json.trim().isEmpty()) return;
        JsonElement root;
        try {
            root = new JsonParser().parse(json);
        } catch (JsonParseException ex) {
            errors.add("Actions json is invalid.");
            return;
        }
        if (root == null || !root.isJsonArray()) {
            errors.add("Actions json must be an array.");
            return;
        }
        JsonArray array = root.getAsJsonArray();
        if (array.size() > LootContainerData.MAX_ACTIONS) {
            errors.add("Too many actions: " + array.size() + " > " + LootContainerData.MAX_ACTIONS + ".");
            return;
        }
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            if (element == null || !element.isJsonObject()) {
                errors.add("Action #" + (i + 1) + " must be an object.");
                return;
            }
            JsonObject action = element.getAsJsonObject();
            String type = getRequiredString(action, "type");
            if (type == null) {
                errors.add("Action #" + (i + 1) + " has empty type.");
                return;
            }
            Double chance = getRequiredNumber(action, "chance");
            if (!isFiniteInRange(chance, 0.0D, 100.0D)) {
                errors.add("Action #" + (i + 1) + " has invalid chance.");
                return;
            }
            String err = validateTypeSpecific(i + 1, type, action);
            if (err != null) {
                errors.add(err);
                return;
            }
        }
    }

    private static String validateTypeSpecific(int index, String type, JsonObject action) {
        if (LootContainerActionRegistry.TYPE_ITEM_DROP.equals(type)) {
            String itemId = getRequiredString(action, "itemId");
            if (itemId == null || tooLong(itemId, LootContainerData.MAX_CONFIG_STRING_LENGTH)) return "Action #" + index + " item_drop has invalid itemId.";
            Double meta = getOptionalNumber(action, "itemMeta");
            if (meta != null && (meta.doubleValue() < 0.0D || meta.doubleValue() != Math.floor(meta.doubleValue()))) {
                return "Action #" + index + " item_drop has invalid itemMeta.";
            }
            String count = getRequiredString(action, "countExpr");
            if (count == null || !isValidCountExpression(count)) {
                return "Action #" + index + " item_drop has invalid count expression.";
            }
            return null;
        }
        if (LootContainerActionRegistry.TYPE_SPAWN_ENTITY.equals(type)) {
            String entityId = getRequiredString(action, "entityId");
            if (entityId == null || tooLong(entityId, LootContainerData.MAX_CONFIG_STRING_LENGTH)) {
                return "Action #" + index + " spawn_entity has empty entityId.";
            }
            String spawnCountExpr = getRequiredString(action, "countExpr");
            if (spawnCountExpr != null) {
                return !isValidPositiveCountExpression(spawnCountExpr)
                        ? "Action #" + index + " spawn_entity has invalid count expression." : null;
            }
            Double spawnCount = getOptionalNumber(action, "spawnCount");
            if (spawnCount == null) spawnCount = getOptionalNumber(action, "count");
            if (spawnCount == null) return null;
            return !isWholeInRange(spawnCount, 1, LootContainerData.MAX_ACTION_COUNT)
                    ? "Action #" + index + " spawn_entity has invalid spawnCount." : null;
        }
        if (LootContainerActionRegistry.TYPE_APPLY_EFFECT.equals(type)
                || LootContainerActionRegistry.TYPE_APPLY_EXPLOSION_EFFECT.equals(type)) {
            String potionId = getRequiredString(action, "potionId");
            if (potionId == null || tooLong(potionId, LootContainerData.MAX_CONFIG_STRING_LENGTH)) {
                return "Action #" + index + " has empty potionId.";
            }
            Double duration = getOptionalNumber(action, "duration");
            if (duration == null) duration = getOptionalNumber(action, "durationSec");
            Double amplifier = getOptionalNumber(action, "amplifier");
            if (!isWholeInRange(duration, 1, LootContainerData.MAX_EFFECT_DURATION_SEC)
                    || !isWholeInRange(amplifier, 0, LootContainerData.MAX_EFFECT_AMPLIFIER)) {
                return "Action #" + index + " has invalid duration/amplifier.";
            }
            if (LootContainerActionRegistry.TYPE_APPLY_EXPLOSION_EFFECT.equals(type)) {
                Double radius = getOptionalNumber(action, "radius");
                if (radius == null) radius = 4.0D;
                if (!isFiniteInRange(radius, 0.1D, LootContainerData.MAX_ACTION_RADIUS)) {
                    return "Action #" + index + " explosion effect has invalid radius.";
                }
            }
            return null;
        }
        if (LootContainerActionRegistry.TYPE_INSTANT_DAMAGE.equals(type)) {
            Double damage = getRequiredNumber(action, "damage");
            return !isFiniteInRange(damage, 0.1D, LootContainerData.MAX_ACTION_DAMAGE)
                    ? "Action #" + index + " instant_damage has invalid damage." : null;
        }
        if (LootContainerActionRegistry.TYPE_EXPLOSION_INSTANT_DAMAGE.equals(type)) {
            Double damage = getRequiredNumber(action, "damage");
            Double radius = getRequiredNumber(action, "radius");
            return !isFiniteInRange(damage, 0.1D, LootContainerData.MAX_ACTION_DAMAGE)
                    || !isFiniteInRange(radius, 0.1D, LootContainerData.MAX_ACTION_RADIUS)
                    ? "Action #" + index + " explosion_instant_damage has invalid damage/radius." : null;
        }
        if (LootContainerActionRegistry.TYPE_BURNING.equals(type)) {
            Double duration = getOptionalNumber(action, "duration");
            if (duration == null) duration = getOptionalNumber(action, "durationSec");
            return !isWholeInRange(duration, 1, LootContainerData.MAX_EFFECT_DURATION_SEC)
                    ? "Action #" + index + " burning has invalid duration." : null;
        }
        if (LootContainerActionRegistry.TYPE_EXPLOSION_BURNING.equals(type)) {
            Double duration = getOptionalNumber(action, "duration");
            if (duration == null) duration = getOptionalNumber(action, "durationSec");
            Double radius = getRequiredNumber(action, "radius");
            return !isWholeInRange(duration, 1, LootContainerData.MAX_EFFECT_DURATION_SEC)
                    || !isFiniteInRange(radius, 0.1D, LootContainerData.MAX_ACTION_RADIUS)
                    ? "Action #" + index + " explosion_burning has invalid duration/radius." : null;
        }
        return "Action #" + index + " has unknown type \"" + type + "\".";
    }

    public static boolean isValidCountExpression(String expr) {
        if (expr == null) return false;
        String v = expr.trim();
        if (v.isEmpty()) return false;
        int dash = v.indexOf('-');
        if (dash < 0) {
            return isWholeInRange(parseNumber(v), 0, LootContainerData.MAX_ACTION_COUNT);
        }
        if (dash == 0 || dash == v.length() - 1 || v.indexOf('-', dash + 1) >= 0) return false;
        Double min = parseNumber(v.substring(0, dash).trim());
        Double max = parseNumber(v.substring(dash + 1).trim());
        if (!isWholeInRange(min, 0, LootContainerData.MAX_ACTION_COUNT)
                || !isWholeInRange(max, 0, LootContainerData.MAX_ACTION_COUNT)) return false;
        return max.doubleValue() >= min.doubleValue();
    }

    private static boolean isValidPositiveCountExpression(String expr) {
        if (expr == null) return false;
        String v = expr.trim();
        if (v.isEmpty()) return false;
        int dash = v.indexOf('-');
        if (dash < 0) {
            return isWholeInRange(parseNumber(v), 1, LootContainerData.MAX_ACTION_COUNT);
        }
        if (dash == 0 || dash == v.length() - 1 || v.indexOf('-', dash + 1) >= 0) return false;
        Double min = parseNumber(v.substring(0, dash).trim());
        Double max = parseNumber(v.substring(dash + 1).trim());
        if (!isWholeInRange(min, 1, LootContainerData.MAX_ACTION_COUNT)
                || !isWholeInRange(max, 1, LootContainerData.MAX_ACTION_COUNT)) return false;
        return max.doubleValue() >= min.doubleValue();
    }

    private static String getRequiredString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) return null;
        JsonElement el = obj.get(key);
        if (el == null || !el.isJsonPrimitive() || !el.getAsJsonPrimitive().isString()) return null;
        String v = el.getAsString();
        return v == null || v.trim().isEmpty() ? null : v.trim();
    }

    private static Double getRequiredNumber(JsonObject obj, String key) {
        Double n = getOptionalNumber(obj, key);
        return n == null ? null : n;
    }

    private static Double getOptionalNumber(JsonObject obj, String key) {
        if (obj == null || !obj.has(key)) return null;
        return parseNumber(obj.get(key));
    }

    private static Double parseNumber(String value) {
        if (value == null) return null;
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private static Double parseNumber(JsonElement element) {
        if (element == null || !element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) return null;
        try {
            return element.getAsDouble();
        } catch (Exception ex) {
            return null;
        }
    }

    private static boolean isPositiveWhole(Double value) {
        return value != null && value.doubleValue() > 0.0D && value.doubleValue() == Math.floor(value.doubleValue());
    }

    private static boolean isNonNegativeWhole(Double value) {
        return value != null && value.doubleValue() >= 0.0D && value.doubleValue() == Math.floor(value.doubleValue());
    }

    private static boolean isWholeInRange(Double value, int min, int max) {
        return value != null && Double.isFinite(value.doubleValue())
                && value.doubleValue() >= min && value.doubleValue() <= max
                && value.doubleValue() == Math.floor(value.doubleValue());
    }

    private static boolean isFiniteInRange(Double value, double min, double max) {
        return value != null && Double.isFinite(value.doubleValue())
                && value.doubleValue() >= min && value.doubleValue() <= max;
    }

    private static boolean tooLong(String value, int maxLength) {
        return value != null && value.length() > maxLength;
    }

    private static boolean validCollision(LootContainerData data) {
        return finiteUnit(data.collisionMinX) && finiteUnit(data.collisionMinY) && finiteUnit(data.collisionMinZ)
                && finiteUnit(data.collisionMaxX) && finiteUnit(data.collisionMaxY) && finiteUnit(data.collisionMaxZ)
                && data.collisionMinX <= data.collisionMaxX
                && data.collisionMinY <= data.collisionMaxY
                && data.collisionMinZ <= data.collisionMaxZ;
    }

    private static boolean finiteUnit(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value) && value >= 0.0F && value <= 1.0F;
    }
}
