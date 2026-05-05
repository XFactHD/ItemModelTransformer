package xfacthd.itemmodeltransformer.client.util;

import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3fc;

public final class TransformPrinter {
    private static final ItemDisplayContext[] PERSPECTIVES = ItemDisplayContext.values();
    private static final String INDENT = " ".repeat(4);
    private static final String JSON_PERSPECTIVE_INDENT = INDENT;
    private static final String JSON_ATTRIBUTE_INDENT = INDENT.repeat(2);
    private static final String DATAGEN_PERSPECTIVE_INDENT = INDENT.repeat(2);
    private static final String DATAGEN_ATTRIBUTE_INDENT = INDENT.repeat(4);

    @SuppressWarnings("UnusedAssignment")
    public static String printJson(TransformHolder[] transforms) {
        StringBuilder builder = new StringBuilder("\"display\": {\n");
        boolean perspectivePrinted = false;
        for (ItemDisplayContext perspective : PERSPECTIVES) {
            if (perspective == ItemDisplayContext.NONE) {
                continue;
            }
            ItemTransform transform = transforms[perspective.ordinal() - 1].getTransform();
            if (isEmptyTransform(transform)) {
                continue;
            }

            if (perspectivePrinted) {
                builder.append(",\n");
            }
            boolean attributePrinted = false;
            builder.append(JSON_PERSPECTIVE_INDENT).append("\"").append(perspective.getSerializedName()).append("\": {\n");
            attributePrinted |= printAttributeJson(builder, "rotation", transform.rotation(), ItemTransform.Deserializer.DEFAULT_ROTATION, attributePrinted, 1F);
            attributePrinted |= printAttributeJson(builder, "translation", transform.translation(), ItemTransform.Deserializer.DEFAULT_TRANSLATION, attributePrinted, 16F);
            attributePrinted |= printAttributeJson(builder, "scale", transform.scale(), ItemTransform.Deserializer.DEFAULT_SCALE, attributePrinted, 1F);
            attributePrinted |= printAttributeJson(builder, "right_rotation", transform.rightRotation(), ItemTransform.Deserializer.DEFAULT_ROTATION, attributePrinted, 1F);
            builder.append("\n").append(JSON_PERSPECTIVE_INDENT).append("}");

            perspectivePrinted = true;
        }
        builder.append("\n}");
        return builder.toString();
    }

    private static boolean printAttributeJson(StringBuilder builder, String type, Vector3fc value, Vector3fc defaultValue, boolean prevPrinted, float multiplier) {
        if (Utils.equals(value, defaultValue)) {
            return false;
        }

        if (prevPrinted) {
            builder.append(",\n");
        }
        builder.append(JSON_ATTRIBUTE_INDENT)
                .append("\"")
                .append(type)
                .append("\": [ ");
        printFloat(builder, value.x(), multiplier);
        builder.append(", ");
        printFloat(builder, value.y(), multiplier);
        builder.append(", ");
        printFloat(builder, value.z(), multiplier);
        builder.append(" ]");
        return true;
    }

    public static String printDatagen(TransformHolder[] transforms) {
        StringBuilder builder = new StringBuilder("ExtendedModelTemplateBuilder.builder()");
        for (ItemDisplayContext perspective : PERSPECTIVES) {
            if (perspective == ItemDisplayContext.NONE) {
                continue;
            }
            ItemTransform transform = transforms[perspective.ordinal() - 1].getTransform();
            if (isEmptyTransform(transform)) {
                continue;
            }

            builder.append("\n")
                    .append(DATAGEN_PERSPECTIVE_INDENT)
                    .append(".transform(ItemDisplayContext.").append(perspective).append(", builder -> builder");
            printAttributeDatagen(builder, "rotation", transform.rotation(), ItemTransform.Deserializer.DEFAULT_ROTATION, 1F);
            printAttributeDatagen(builder, "translation", transform.translation(), ItemTransform.Deserializer.DEFAULT_TRANSLATION, 16F);
            printAttributeDatagen(builder, "scale", transform.scale(), ItemTransform.Deserializer.DEFAULT_SCALE, 1F);
            printAttributeDatagen(builder, "rightRotation", transform.rightRotation(), ItemTransform.Deserializer.DEFAULT_ROTATION, 1F);
            builder.append("\n")
                    .append(DATAGEN_PERSPECTIVE_INDENT)
                    .append(")");
        }
        return builder.append("\n")
                .append(DATAGEN_PERSPECTIVE_INDENT)
                .append(".build();")
                .toString();
    }

    private static void printAttributeDatagen(StringBuilder builder, String type, Vector3fc value, Vector3fc defaultValue, float multiplier) {
        if (Utils.equals(value, defaultValue)) {
            return;
        }

        builder.append("\n")
                .append(DATAGEN_ATTRIBUTE_INDENT)
                .append(".")
                .append(type)
                .append("(");
        printFloat(builder, value.x(), multiplier);
        builder.append("F, ");
        printFloat(builder, value.x(), multiplier);
        builder.append("F, ");
        printFloat(builder, value.x(), multiplier);
        builder.append("F)");
    }

    private static void printFloat(StringBuilder builder, float value, float multiplier) {
        float scaledValue = value * multiplier;
        float filteredValue = Math.round(scaledValue * 1000F) / 1000F;
        if (Mth.equal(filteredValue, 0F)) {
            filteredValue = 0F;
        }
        if (filteredValue == (int) filteredValue) {
            builder.append((int) filteredValue);
        } else {
            builder.append(filteredValue);
        }
    }

    private static boolean isEmptyTransform(ItemTransform transform) {
        return Utils.equals(transform, ItemTransform.NO_TRANSFORM);
    }

    private TransformPrinter() { }
}
