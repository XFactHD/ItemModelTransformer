package io.github.xfacthd.itemmodeltransformer.util;

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
            attributePrinted |= printAttributeJson(builder, Attribute.ROTATION, transform, attributePrinted);
            attributePrinted |= printAttributeJson(builder, Attribute.TRANSLATION, transform, attributePrinted);
            attributePrinted |= printAttributeJson(builder, Attribute.SCALE, transform, attributePrinted);
            attributePrinted |= printAttributeJson(builder, Attribute.RIGHT_ROTATION, transform, attributePrinted);
            builder.append("\n").append(JSON_PERSPECTIVE_INDENT).append("}");

            perspectivePrinted = true;
        }
        builder.append("\n}");
        return builder.toString();
    }

    private static boolean printAttributeJson(StringBuilder builder, Attribute attribute, ItemTransform transform, boolean prevPrinted) {
        Vector3fc value = attribute.lookup(transform);
        if (Utils.equals(value, attribute.getDefaultValue())) {
            return false;
        }

        float multiplier = 1F / attribute.getMultiplier();

        if (prevPrinted) {
            builder.append(",\n");
        }
        builder.append(JSON_ATTRIBUTE_INDENT)
                .append("\"")
                .append(attribute.getJsonKey())
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
            printAttributeDatagen(builder, Attribute.ROTATION, transform);
            printAttributeDatagen(builder, Attribute.TRANSLATION, transform);
            printAttributeDatagen(builder, Attribute.SCALE, transform);
            printAttributeDatagen(builder, Attribute.RIGHT_ROTATION, transform);
            builder.append("\n")
                    .append(DATAGEN_PERSPECTIVE_INDENT)
                    .append(")");
        }
        return builder.append("\n")
                .append(DATAGEN_PERSPECTIVE_INDENT)
                .append(".build();")
                .toString();
    }

    private static void printAttributeDatagen(StringBuilder builder, Attribute attribute, ItemTransform transform) {
        Vector3fc value = attribute.lookup(transform);
        if (Utils.equals(value, attribute.getDefaultValue())) {
            return;
        }

        float multiplier = 1F / attribute.getMultiplier();

        builder.append("\n")
                .append(DATAGEN_ATTRIBUTE_INDENT)
                .append(".")
                .append(attribute.getDatagenMethod())
                .append("(");
        printFloat(builder, value.x(), multiplier);
        builder.append("F, ");
        printFloat(builder, value.y(), multiplier);
        builder.append("F, ");
        printFloat(builder, value.z(), multiplier);
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
