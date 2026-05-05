package xfacthd.itemmodeltransformer.client.util;

import net.minecraft.client.resources.model.cuboid.ItemTransform;
import org.joml.Vector3fc;

import java.util.function.Function;

public enum Attribute {
    ROTATION(
            "rotation",
            "rotation",
            ItemTransform::rotation,
            ItemTransform.Deserializer.DEFAULT_ROTATION,
            360F,
            1F,
            true
    ),
    TRANSLATION(
            "translation",
            "translation",
            ItemTransform::translation,
            ItemTransform.Deserializer.DEFAULT_TRANSLATION,
            ItemTransform.Deserializer.MAX_TRANSLATION,
            .0625F, // Translation is a special snowflake and gets divided by 16, see ItemTransform.Deserializer
            false
    ),
    SCALE(
            "scale",
            "scale",
            ItemTransform::scale,
            ItemTransform.Deserializer.DEFAULT_SCALE,
            ItemTransform.Deserializer.MAX_SCALE,
            1F,
            false
    ),
    RIGHT_ROTATION(
            "right_rotation",
            "rightRotation",
            ItemTransform::rightRotation,
            ItemTransform.Deserializer.DEFAULT_ROTATION,
            360F,
            1F,
            true
    );

    private final String jsonKey;
    private final String datagenMethod;
    private final Function<ItemTransform, Vector3fc> lookup;
    private final Vector3fc defaultValue;
    private final float range;
    private final float multiplier;
    private final boolean wrap;

    Attribute(String jsonKey, String datagenMethod, Function<ItemTransform, Vector3fc> lookup, Vector3fc defaultValue, float range, float multiplier, boolean wrap) {
        this.jsonKey = jsonKey;
        this.datagenMethod = datagenMethod;
        this.lookup = lookup;
        this.defaultValue = defaultValue;
        this.range = range;
        this.multiplier = multiplier;
        this.wrap = wrap;
    }

    public String getJsonKey() {
        return jsonKey;
    }

    public String getDatagenMethod() {
        return datagenMethod;
    }

    public Vector3fc lookup(ItemTransform transform) {
        return lookup.apply(transform);
    }

    public Vector3fc getDefaultValue() {
        return defaultValue;
    }

    public float getRange() {
        return range;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public boolean isWrap() {
        return wrap;
    }
}
