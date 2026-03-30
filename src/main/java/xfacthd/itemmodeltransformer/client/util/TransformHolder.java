package xfacthd.itemmodeltransformer.client.util;

import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

public final class TransformHolder {
    private final ItemDisplayContext perspective;
    private final ItemTransform transform = new ItemTransform(
            new Vector3f(ItemTransform.Deserializer.DEFAULT_ROTATION),
            new Vector3f(ItemTransform.Deserializer.DEFAULT_TRANSLATION),
            new Vector3f(ItemTransform.Deserializer.DEFAULT_SCALE),
            new Vector3f(ItemTransform.Deserializer.DEFAULT_ROTATION)
    );
    @Nullable
    private ItemTransform srcTransform = null;
    private boolean modified = false;

    public TransformHolder(ItemDisplayContext perspective) {
        this.perspective = perspective;
    }

    public ItemTransform getTransform() {
        return transform;
    }

    public void modify(Attribute attrib, int element, float dir) {
        Vector3fc vec = attrib.lookup.apply(transform);
        float component = vec.get(element) + (dir * attrib.multiplier);
        if (attrib.wrap) {
            component = Mth.positiveModulo(component, attrib.range);
        } else {
            component = Mth.clamp(component, -attrib.range, attrib.range);
        }
        ((Vector3f) vec).setComponent(element, component);

        ItemTransform testTransform = Objects.requireNonNullElse(srcTransform, ItemTransform.NO_TRANSFORM);
        modified = !Utils.equals(transform, testTransform);
    }

    public boolean load(ItemTransform source, boolean overwrite) {
        if (modified && !overwrite) {
            return false;
        }

        setVector(transform.rotation(), source.rotation());
        setVector(transform.translation(), source.translation());
        setVector(transform.scale(), source.scale());
        setVector(transform.rightRotation(), source.rightRotation());
        srcTransform = source;
        modified = false;
        return true;
    }

    public void clear() {
        setVector(transform.rotation(), ItemTransform.Deserializer.DEFAULT_ROTATION);
        setVector(transform.translation(), ItemTransform.Deserializer.DEFAULT_TRANSLATION);
        setVector(transform.scale(), ItemTransform.Deserializer.DEFAULT_SCALE);
        setVector(transform.rightRotation(), ItemTransform.Deserializer.DEFAULT_ROTATION);
        srcTransform = null;
        modified = false;
    }

    private static void setVector(Vector3fc target, Vector3fc source) {
        ((Vector3f) target).set(source);
    }

    public String printType() {
        String type = perspective.getSerializedName();
        if (modified) {
            type += " *";
        }
        return type;
    }

    public enum Attribute {
        ROTATION(
                ItemTransform::rotation,
                360F,
                1F,
                true
        ),
        TRANSLATION(
                ItemTransform::translation,
                ItemTransform.Deserializer.MAX_TRANSLATION,
                .0625F, // Translation is a special snowflake and gets divided by 16, see ItemTransform.Deserializer
                false
        ),
        SCALE(
                ItemTransform::scale,
                ItemTransform.Deserializer.MAX_SCALE,
                1F,
                false
        ),
        RIGHT_ROTATION(
                ItemTransform::rightRotation,
                360F,
                1F,
                true
        );

        private final Function<ItemTransform, Vector3fc> lookup;
        private final float range;
        private final float multiplier;
        private final boolean wrap;

        Attribute(Function<ItemTransform, Vector3fc> lookup, float range, float multiplier, boolean wrap) {
            this.lookup = lookup;
            this.range = range;
            this.multiplier = multiplier;
            this.wrap = wrap;
        }
    }
}
