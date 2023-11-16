package xfacthd.itemmodeltransformer.client.util;

import com.google.gson.JsonElement;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;
import net.neoforged.neoforge.common.util.Lazy;
import org.joml.Vector3f;
import xfacthd.itemmodeltransformer.ItemModelTransformer;
import xfacthd.itemmodeltransformer.client.mixin.AccessorKeyMapping;

import java.io.IOException;
import java.io.StringWriter;

public final class Utils
{
    private static final Codec<ItemTransform> TRANSFORM_CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ExtraCodecs.VECTOR3F.fieldOf("rotation").forGetter(xform -> xform.rotation),
            ExtraCodecs.VECTOR3F.fieldOf("translation").xmap(
                    // Translation is a special snowflake and gets divided by 16, see ItemTransform.Deserializer
                    v -> new Vector3f(v.x / 16F, v.y / 16F, v.y / 16F),
                    v -> new Vector3f(v.x * 16F, v.y * 16F, v.y * 16F)
            ).forGetter(xform -> xform.translation),
            ExtraCodecs.VECTOR3F.fieldOf("scale").forGetter(xform -> xform.scale),
            ExtraCodecs.VECTOR3F.fieldOf("right_rotation").forGetter(xform -> xform.rightRotation)
    ).apply(inst, ItemTransform::new));
    private static final Style FULL_SPACE_FONT = Style.EMPTY.withFont(
            new ResourceLocation(ItemModelTransformer.MODID, "full_space")
    );
    private static final Style STYLE_DEFAULT = Style.EMPTY.applyFormat(ChatFormatting.WHITE);
    private static final Style STYLE_SELECTED = Style.EMPTY.withColor(0xFF6666);

    public static Component printVector(Vector3f vec, boolean selected, int element)
    {
        return printVector(vec, selected, element, 1F);
    }

    public static Component printVector(Vector3f vec, boolean selected, int element, float mult)
    {
        return Component.literal("[ ").setStyle(STYLE_DEFAULT)
                .append(printComponent(vec.x * mult, selected && element == 0))
                .append(Component.literal(" | ").setStyle(STYLE_DEFAULT))
                .append(printComponent(vec.y * mult, selected && element == 1))
                .append(Component.literal(" | ").setStyle(STYLE_DEFAULT))
                .append(printComponent(vec.z * mult, selected && element == 2))
                .append(Component.literal(" ]").setStyle(STYLE_DEFAULT));
    }

    private static Component printComponent(float val, boolean selected)
    {
        MutableComponent result = Component.empty();
        String text = "%7.3f".formatted(val);
        int idx = text.lastIndexOf(' ') + 1;
        if (idx > 0)
        {
            result.append(Component.literal(text.substring(0, idx)).setStyle(FULL_SPACE_FONT));
            text = text.substring(idx);
        }
        return result.append(Component.literal(text).setStyle(selected ? STYLE_SELECTED : STYLE_DEFAULT));
    }

    public static String encodeItemTransform(ItemDisplayContext ctx, ItemTransform xform)
    {
        JsonElement obj = TRANSFORM_CODEC.encodeStart(JsonOps.INSTANCE, xform).getOrThrow(false, err -> {});

        try
        {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            jsonWriter.setLenient(true);
            jsonWriter.setIndent("  ");
            Streams.write(obj, jsonWriter);
            return "\"%s\": %s".formatted(ctx.getSerializedName(), stringWriter);
        }
        catch (IOException e)
        {
            throw new AssertionError(e);
        }
    }

    public static void releaseAllKeys(InputConstants.Key key)
    {
        KeyMappingLookup map = AccessorKeyMapping.itemmodeltransformer$getKeyMap();
        map.getAll(key).forEach(keyMapping -> ((AccessorKeyMapping) keyMapping).itemmodeltransformer$release());
    }

    public static void releaseKey(KeyMapping keybind)
    {
        ((AccessorKeyMapping) keybind).itemmodeltransformer$release();
    }

    public static Component formatKeybind(Lazy<KeyMapping> key)
    {
        return Component.literal("[")
                .append(key.get().getTranslatedKeyMessage())
                .append(Component.literal("]"))
                .setStyle(Style.EMPTY.applyFormat(ChatFormatting.GOLD));
    }



    private Utils() { }
}
