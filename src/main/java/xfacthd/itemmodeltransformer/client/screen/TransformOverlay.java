package xfacthd.itemmodeltransformer.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.common.util.Lazy;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.glfw.GLFW;
import xfacthd.itemmodeltransformer.client.IMTClient;
import xfacthd.itemmodeltransformer.client.mixin.AccessorItemStackRenderStateLayer;
import xfacthd.itemmodeltransformer.client.util.Utils;

import java.util.Arrays;

public final class TransformOverlay implements GuiLayer
{
    private static final ItemDisplayContext[] CONTEXTS = ItemDisplayContext.values();
    private static final ItemStackRenderState SCRATCH_RENDER_STATE = new ItemStackRenderState();
    private static final int LINE_COUNT = 5;
    private static final int ELEMENT_COUNT = 3;
    private static final int LINE_HEIGHT = 10;
    private static final int LINE_PADDING = 5;
    private static final int HEIGHT_BASE = (LINE_HEIGHT * 2 + LINE_PADDING) * LINE_COUNT + LINE_HEIGHT;
    private static final int HEIGHT_USAGE = HEIGHT_BASE + (LINE_HEIGHT * 13);
    private static final int TOOLTIP_DIFF = 4;
    private static final int KEY_LEFT_SHIFT = GLFW.GLFW_KEY_LEFT_SHIFT;
    private static final int KEY_RIGHT_SHIFT = GLFW.GLFW_KEY_RIGHT_SHIFT;
    private static final int KEY_LEFT_CTRL = InputQuirks.EDIT_SHORTCUT_KEY_LEFT;
    private static final int KEY_RIGHT_CTRL = InputQuirks.EDIT_SHORTCUT_KEY_RIGHT;
    private static final int KEY_LEFT_ALT = GLFW.GLFW_KEY_LEFT_ALT;
    private static final int KEY_RIGHT_ALT = GLFW.GLFW_KEY_RIGHT_ALT;
    private static final Component DUMMY_VECTOR_PRINT = Utils.printVector(new Vector3f(), false, 0);
    private static final Component DESC_CAT_TYPE = Component.translatable("desc.itemmodeltransformer.category.type");
    private static final Component DESC_CAT_ROTATION = Component.translatable("desc.itemmodeltransformer.category.rotation");
    private static final Component DESC_CAT_TRANSLATION = Component.translatable("desc.itemmodeltransformer.category.translation");
    private static final Component DESC_CAT_SCALE = Component.translatable("desc.itemmodeltransformer.category.scale");
    private static final Component DESC_CAT_POST_ROTATION = Component.translatable("desc.itemmodeltransformer.category.post_rotation");
    private static final Component MSG_CLEARED = Component.translatable("msg.itemmodeltransformer.cleared");
    private static final Component MSG_LOADED = Component.translatable("msg.itemmodeltransformer.loaded_from_item");
    private static final Component MSG_COPIED_JSON = Component.translatable("msg.itemmodeltransformer.copied_json_to_clipboard");
    private static final Component MSG_COPIED_CODE = Component.translatable("msg.itemmodeltransformer.copied_code_to_clipboard");
    private static final Component DESC_KEY_CTRL = Component.translatable("desc.itemmodeltransformer.key.ctrl");
    private static final Component DESC_KEY_CMD = Component.translatable("desc.itemmodeltransformer.key.cmd");
    private static final Component DESC_KEY_SHIFT = Component.translatable("desc.itemmodeltransformer.key.shift");
    private static final Component DESC_KEY_ALT = Component.translatable("desc.itemmodeltransformer.key.alt");
    private static final Component DESC_INC_DEC_X10_0 = Component.translatable(
            "desc.itemmodeltransformer.usage.inc_dec.x10_0",
            Utils.formatKeyCombination(DESC_KEY_ALT)
    );
    private static final Component DESC_INC_DEC_X0_1 = Component.translatable(
            "desc.itemmodeltransformer.usage.inc_dec.x0_1",
            Utils.formatKeyCombination(DESC_KEY_SHIFT)
    );
    private static final Component DESC_INC_DEC_X0_01 = Component.translatable(
            "desc.itemmodeltransformer.usage.inc_dec.x0_01",
            Utils.formatKeyCombination(InputQuirks.ON_OSX ? DESC_KEY_CMD : DESC_KEY_CTRL)
    );
    private static final Component DESC_INC_DEC_X0_001 = Component.translatable(
            "desc.itemmodeltransformer.usage.inc_dec.x0_001",
            Utils.formatKeyCombination(DESC_KEY_SHIFT, InputQuirks.ON_OSX ? DESC_KEY_CMD : DESC_KEY_CTRL)
    );

    private static final ItemTransform[] SCRATCH_TRANSFORMS = Util.make(
            new ItemTransform[CONTEXTS.length - 1],
            arr -> Arrays.setAll(arr, i -> new ItemTransform(
                    new Vector3f(ItemTransform.Deserializer.DEFAULT_ROTATION),
                    new Vector3f(ItemTransform.Deserializer.DEFAULT_TRANSLATION),
                    new Vector3f(ItemTransform.Deserializer.DEFAULT_SCALE),
                    new Vector3f(ItemTransform.Deserializer.DEFAULT_ROTATION)
            ))
    );
    private static boolean enabled = false;
    private static ItemDisplayContext currContext = ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    private static int line = 0;
    private static int element = 0;
    private static boolean showUsage = false;
    private static boolean ctrl = false;
    private static boolean shift = false;
    private static boolean alt = false;

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker)
    {
        if (!enabled) return;

        Component[] usageLines = makeUsageLines();
        Font font = Minecraft.getInstance().font;

        int width = calculateWidth(font, usageLines) - TOOLTIP_DIFF;
        int height = (showUsage ? HEIGHT_USAGE : HEIGHT_BASE) - TOOLTIP_DIFF;
        TooltipRenderUtil.renderTooltipBackground(graphics, 4, 4, width, height, null);

        ItemTransform xform = getScratchTransform();

        boolean selected = line == 0;
        graphics.drawString(font, DESC_CAT_TYPE, 3, 3, selected ? 0xFF66FF66 : 0xFFFFFFFF, false);
        graphics.drawString(font, currContext.getSerializedName(), 3, 13, 0xFFFFFFFF, false);

        selected = line == 1;
        graphics.drawString(font, DESC_CAT_ROTATION, 3, 28, selected ? 0xFF66FF66 : 0xFFFFFFFF, false);
        graphics.drawString(font, Utils.printVector(xform.rotation(), selected, element), 3, 38, 0xFFFFFFFF, false);

        selected = line == 2;
        graphics.drawString(font, DESC_CAT_TRANSLATION, 3, 53, selected ? 0xFF66FF66 : 0xFFFFFFFF, false);
        // Translation is a special snowflake and gets divided by 16, see ItemTransform.Deserializer
        graphics.drawString(font, Utils.printVector(xform.translation(), selected, element, 16F), 3, 63, 0xFFFFFFFF, false);

        selected = line == 3;
        graphics.drawString(font, DESC_CAT_SCALE, 3, 78, selected ? 0xFF66FF66 : 0xFFFFFFFF, false);
        graphics.drawString(font, Utils.printVector(xform.scale(), selected, element), 3, 88, 0xFFFFFFFF, false);

        selected = line == 4;
        graphics.drawString(font, DESC_CAT_POST_ROTATION, 3, 103, selected ? 0xFF66FF66 : 0xFFFFFFFF, false);
        graphics.drawString(font, Utils.printVector(xform.rightRotation(), selected, element), 3, 113, 0xFFFFFFFF, false);

        for (int i = 0; i < usageLines.length; i++)
        {
            graphics.drawString(font, usageLines[i], 3, 128 + (LINE_HEIGHT * i), 0xFFFFFFFF, false);
        }
    }

    public static void toggleEnabled()
    {
        enabled = !enabled;
    }

    public static boolean isEnabled()
    {
        return enabled;
    }

    private static ItemTransform getScratchTransform()
    {
        return SCRATCH_TRANSFORMS[currContext.ordinal() - 1];
    }

    public static boolean isItemAffected(Item item, ItemDisplayContext context)
    {
        // noinspection ConstantConditions
        return enabled && context == currContext && item == Minecraft.getInstance().player.getMainHandItem().getItem();
    }

    public static ItemTransform getActiveTransform(Item item, ItemDisplayContext context, ItemTransform originalXform)
    {
        return isItemAffected(item, context) ? getScratchTransform() : originalXform;
    }

    private static Component[] makeUsageLines()
    {
        if (!showUsage)
        {
            return new Component[] {
                    Component.translatable(
                            "desc.itemmodeltransformer.usage.show",
                            Utils.formatKeybind(IMTClient.KEY_TOGGLE_USAGE)
                    )
            };
        }

        return new Component[] {
                Component.translatable(
                        "desc.itemmodeltransformer.usage.hide",
                        Utils.formatKeybind(IMTClient.KEY_TOGGLE_USAGE)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.prev_category",
                        Utils.formatKeybind(IMTClient.KEY_PREV_CATEGORY)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.next_category",
                        Utils.formatKeybind(IMTClient.KEY_NEXT_CATEGORY)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.prev_element",
                        Utils.formatKeybind(IMTClient.KEY_PREV_ELEMENT)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.next_element",
                        Utils.formatKeybind(IMTClient.KEY_NEXT_ELEMENT)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.inc_dec",
                        Utils.formatKeybind(IMTClient.KEY_INCREMENT),
                        Utils.formatKeybind(IMTClient.KEY_DECREMENT)
                ),
                DESC_INC_DEC_X10_0,
                DESC_INC_DEC_X0_1,
                DESC_INC_DEC_X0_01,
                DESC_INC_DEC_X0_001,
                Component.translatable(
                        "desc.itemmodeltransformer.usage.clear",
                        Utils.formatKeybind(IMTClient.KEY_CLEAR)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.load",
                        Utils.formatKeybind(IMTClient.KEY_LOAD)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.print_json",
                        Utils.formatKeybind(IMTClient.KEY_PRINT_JSON)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.print_datagen",
                        Utils.formatKeybind(IMTClient.KEY_PRINT_DATAGEN)
                )
        };
    }

    private static int calculateWidth(Font font, Component[] lines)
    {
        int width = font.width(DUMMY_VECTOR_PRINT);
        for (Component line : lines)
        {
            width = Math.max(width, font.width(line));
        }
        return width + 1;
    }

    public static void handleInput()
    {
        if (!enabled)
        {
            releaseAllKeys();
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        shift = InputConstants.isKeyDown(window, KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, KEY_RIGHT_SHIFT);
        ctrl = InputConstants.isKeyDown(window, KEY_LEFT_CTRL) || InputConstants.isKeyDown(window, KEY_RIGHT_CTRL);
        alt = InputConstants.isKeyDown(window, KEY_LEFT_ALT) || InputConstants.isKeyDown(window, KEY_RIGHT_ALT);

        if (wasClicked(IMTClient.KEY_PREV_CATEGORY))
        {
            line = Mth.positiveModulo(line - 1, LINE_COUNT);
        }
        else if (wasClicked(IMTClient.KEY_NEXT_CATEGORY))
        {
            line = Mth.positiveModulo(line + 1, LINE_COUNT);
        }
        else if (line > 0 && wasClicked(IMTClient.KEY_PREV_ELEMENT))
        {
            element = Mth.positiveModulo(element - 1, ELEMENT_COUNT);
        }
        else if (line > 0 && wasClicked(IMTClient.KEY_NEXT_ELEMENT))
        {
            element = Mth.positiveModulo(element + 1, ELEMENT_COUNT);
        }
        else if (wasClicked(IMTClient.KEY_DECREMENT))
        {
            ItemTransform xform = getScratchTransform();
            switch (line)
            {
                case 0 -> cycleContext(-1);
                case 1 -> modifyVector(xform.rotation(), -1F, true, 360F);
                // Translation is a special snowflake and gets divided by 16, see ItemTransform.Deserializer
                case 2 -> modifyVector(xform.translation(), -.0625F, false, ItemTransform.Deserializer.MAX_TRANSLATION);
                case 3 -> modifyVector(xform.scale(), -1F, false, ItemTransform.Deserializer.MAX_SCALE);
                case 4 -> modifyVector(xform.rightRotation(), -1F, true, 360F);
            }
        }
        else if (wasClicked(IMTClient.KEY_INCREMENT))
        {
            ItemTransform xform = getScratchTransform();
            switch (line)
            {
                case 0 -> cycleContext(1);
                case 1 -> modifyVector(xform.rotation(), 1F, true, 360F);
                // Translation is a special snowflake and gets divided by 16, see ItemTransform.Deserializer
                case 2 -> modifyVector(xform.translation(), .0625F, false, ItemTransform.Deserializer.MAX_TRANSLATION);
                case 3 -> modifyVector(xform.scale(), 1F, false, ItemTransform.Deserializer.MAX_SCALE);
                case 4 -> modifyVector(xform.rightRotation(), 1F, true, 360F);
            }
        }
        else if (wasClicked(IMTClient.KEY_CLEAR))
        {
            ItemTransform xform = getScratchTransform();
            setVector(xform.rotation(), ItemTransform.Deserializer.DEFAULT_ROTATION);
            setVector(xform.translation(), ItemTransform.Deserializer.DEFAULT_TRANSLATION);
            setVector(xform.scale(), ItemTransform.Deserializer.DEFAULT_SCALE);
            setVector(xform.rightRotation(), ItemTransform.Deserializer.DEFAULT_ROTATION);

            //noinspection ConstantConditions
            Minecraft.getInstance().player.displayClientMessage(MSG_CLEARED, true);
        }
        else if (wasClicked(IMTClient.KEY_LOAD))
        {
            Player player = Minecraft.getInstance().player;
            //noinspection ConstantConditions
            ItemStack stack = player.getMainHandItem();
            if (!stack.isEmpty())
            {
                ItemModelResolver resolver = Minecraft.getInstance().getItemModelResolver();
                resolver.updateForTopItem(SCRATCH_RENDER_STATE, stack, currContext, player.level(), player, 0);

                ItemTransform srcXform = ((AccessorItemStackRenderStateLayer) SCRATCH_RENDER_STATE.firstLayer()).imt$getTransform();
                if (srcXform != ItemTransform.NO_TRANSFORM)
                {
                    ItemTransform xform = getScratchTransform();
                    setVector(xform.rotation(), srcXform.rotation());
                    setVector(xform.translation(), srcXform.translation());
                    setVector(xform.scale(), srcXform.scale());
                    setVector(xform.rightRotation(), srcXform.rightRotation());
                    player.displayClientMessage(MSG_LOADED, true);
                }

                SCRATCH_RENDER_STATE.clear();
            }
        }
        else if (wasClicked(IMTClient.KEY_PRINT_JSON))
        {
            String out = Utils.encodeItemTransform(SCRATCH_TRANSFORMS);
            Minecraft.getInstance().keyboardHandler.setClipboard(out);
            //noinspection ConstantConditions
            Minecraft.getInstance().player.displayClientMessage(MSG_COPIED_JSON, true);
        }
        else if (wasClicked(IMTClient.KEY_PRINT_DATAGEN))
        {
            String out = Utils.printDatagenCode(SCRATCH_TRANSFORMS);
            Minecraft.getInstance().keyboardHandler.setClipboard(out);
            //noinspection ConstantConditions
            Minecraft.getInstance().player.displayClientMessage(MSG_COPIED_CODE, true);
        }
        else if (wasClicked(IMTClient.KEY_TOGGLE_USAGE))
        {
            showUsage = !showUsage;
        }
    }

    private static void cycleContext(int dir)
    {
        int idx = currContext.ordinal() - 1;
        int newIdx = Mth.positiveModulo(idx + dir, CONTEXTS.length - 1);
        currContext = CONTEXTS[newIdx + 1];
    }

    private static void modifyVector(Vector3fc vec, float dir, boolean wrap, float range)
    {
        if (ctrl && shift)
        {
            dir *= .001F;
        }
        else if (ctrl)
        {
            dir *= .01F;
        }
        else if (shift)
        {
            dir *= .1F;
        }
        else if (alt)
        {
            dir *= 10F;
        }

        float component = vec.get(element) + dir;
        if (wrap)
        {
            component = Mth.positiveModulo(component, range);
        }
        else
        {
            component = Mth.clamp(component, -range, range);
        }
        ((Vector3f) vec).setComponent(element, component);
    }

    private static void setVector(Vector3fc target, Vector3fc source)
    {
        ((Vector3f) target).set(source);
    }

    private static boolean wasClicked(Lazy<KeyMapping> keybind)
    {
        KeyMapping key = keybind.get();
        boolean clicked = false;
        while (key.consumeClick())
        {
            clicked = true;
        }
        if (clicked)
        {
            // Prevent vanilla keybinds with the same key from firing
            Utils.releaseAllKeys(key.getKey());
        }
        return clicked;
    }

    private static void releaseAllKeys()
    {
        Utils.releaseKey(IMTClient.KEY_PREV_CATEGORY.get());
        Utils.releaseKey(IMTClient.KEY_NEXT_CATEGORY.get());
        Utils.releaseKey(IMTClient.KEY_PREV_ELEMENT.get());
        Utils.releaseKey(IMTClient.KEY_NEXT_ELEMENT.get());
        Utils.releaseKey(IMTClient.KEY_DECREMENT.get());
        Utils.releaseKey(IMTClient.KEY_INCREMENT.get());
        Utils.releaseKey(IMTClient.KEY_CLEAR.get());
        Utils.releaseKey(IMTClient.KEY_LOAD.get());
        Utils.releaseKey(IMTClient.KEY_PRINT_JSON.get());
    }
}
