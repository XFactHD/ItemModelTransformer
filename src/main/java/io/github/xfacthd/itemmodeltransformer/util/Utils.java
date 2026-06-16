package io.github.xfacthd.itemmodeltransformer.util;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.xfacthd.itemmodeltransformer.ItemModelTransformer;
import io.github.xfacthd.itemmodeltransformer.mixin.AccessorKeyMapping;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;
import net.neoforged.neoforge.common.util.Lazy;
import org.joml.Vector3fc;

public final class Utils {
    private static final Style FULL_SPACE_FONT = Style.EMPTY.withFont(new FontDescription.Resource(Utils.rl("full_space")));
    private static final Style STYLE_DEFAULT = Style.EMPTY.applyFormat(ChatFormatting.WHITE);
    private static final Style STYLE_SELECTED = Style.EMPTY.withColor(0xFF6666);

    public static Component printVector(Vector3fc vec, boolean selected, int element) {
        return printVector(vec, selected, element, 1F);
    }

    public static Component printVector(Vector3fc vec, boolean selected, int element, float mult) {
        return Component.literal("[ ").setStyle(STYLE_DEFAULT)
                .append(printComponent(vec.x() * mult, selected && element == 0))
                .append(Component.literal(" | ").setStyle(STYLE_DEFAULT))
                .append(printComponent(vec.y() * mult, selected && element == 1))
                .append(Component.literal(" | ").setStyle(STYLE_DEFAULT))
                .append(printComponent(vec.z() * mult, selected && element == 2))
                .append(Component.literal(" ]").setStyle(STYLE_DEFAULT));
    }

    private static Component printComponent(float val, boolean selected) {
        MutableComponent result = Component.empty();
        String text = "%7.3f".formatted(val);
        int idx = text.lastIndexOf(' ') + 1;
        if (idx > 0) {
            result.append(Component.literal(text.substring(0, idx)).setStyle(FULL_SPACE_FONT));
            text = text.substring(idx);
        }
        return result.append(Component.literal(text).setStyle(selected ? STYLE_SELECTED : STYLE_DEFAULT));
    }

    public static void releaseAllKeys(InputConstants.Key key) {
        KeyMappingLookup map = AccessorKeyMapping.itemmodeltransformer$getKeyMap();
        map.getAll(key).forEach(keyMapping -> ((AccessorKeyMapping) keyMapping).itemmodeltransformer$release());
    }

    public static void releaseKey(KeyMapping keybind) {
        ((AccessorKeyMapping) keybind).itemmodeltransformer$release();
    }

    public static Component formatKeybind(Lazy<KeyMapping> key) {
        return Component.literal("[")
                .append(key.get().getTranslatedKeyMessage())
                .append(Component.literal("]"))
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.GOLD));
    }

    public static Component formatKeyCombination(Component... keyNames) {
        MutableComponent result = Component.literal("[");
        for (int i = 0; i < keyNames.length; i++) {
            if (i > 0) {
                result.append(Component.literal(" + "));
            }
            result.append(keyNames[i]);
        }
        return result.append(Component.literal("]")).setStyle(Style.EMPTY.applyFormat(ChatFormatting.GOLD));
    }

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(ItemModelTransformer.MOD_ID, path);
    }

    public static boolean equals(ItemTransform xformOne, ItemTransform xformTwo) {
        return equals(xformOne.rotation(), xformTwo.rotation()) &&
                equals(xformOne.translation(), xformTwo.translation()) &&
                equals(xformOne.scale(), xformTwo.scale()) &&
                equals(xformOne.rightRotation(), xformTwo.rightRotation());
    }

    public static boolean equals(Vector3fc vecOne, Vector3fc vecTwo) {
        return Mth.equal(vecOne.x(), vecTwo.x()) && Mth.equal(vecOne.y(), vecTwo.y()) && Mth.equal(vecOne.z(), vecTwo.z());
    }

    private Utils() { }
}
