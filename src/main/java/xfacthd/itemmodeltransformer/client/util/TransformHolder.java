package xfacthd.itemmodeltransformer.client.util;

import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

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
        Vector3fc vec = attrib.lookup(transform);
        float component = vec.get(element) + (dir * attrib.getMultiplier());
        if (attrib.isWrap()) {
            component = Mth.positiveModulo(component, attrib.getRange());
        } else {
            component = Mth.clamp(component, -attrib.getRange(), attrib.getRange());
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
}
