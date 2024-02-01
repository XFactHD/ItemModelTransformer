package xfacthd.itemmodeltransformer.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.overlay.ExtendedGui;
import net.neoforged.neoforge.client.gui.overlay.IGuiOverlay;
import net.neoforged.neoforge.common.util.Lazy;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import xfacthd.itemmodeltransformer.client.IMTClient;
import xfacthd.itemmodeltransformer.client.handler.TransformHandler;
import xfacthd.itemmodeltransformer.client.util.Utils;

@SuppressWarnings("deprecation")
public final class TransformOverlay implements IGuiOverlay
{
    private static final ItemDisplayContext[] CONTEXTS = ItemDisplayContext.values();
    private static final int LINE_COUNT = 5;
    private static final int ELEMENT_COUNT = 3;
    private static final int LINE_HEIGHT = 10;
    private static final int LINE_PADDING = 5;
    private static final int HEIGHT_BASE = (LINE_HEIGHT * 2 + LINE_PADDING) * LINE_COUNT + LINE_HEIGHT;
    private static final int HEIGHT_USAGE = HEIGHT_BASE + (LINE_HEIGHT * 12);
    private static final int TOOLTIP_DIFF = 4;
    private static final int KEY_LEFT_SHIFT = GLFW.GLFW_KEY_LEFT_SHIFT;
    private static final int KEY_RIGHT_SHIFT = GLFW.GLFW_KEY_RIGHT_SHIFT;
    private static final int KEY_LEFT_CTRL = Minecraft.ON_OSX ? GLFW.GLFW_KEY_LEFT_SUPER : GLFW.GLFW_KEY_LEFT_CONTROL;
    private static final int KEY_RIGHT_CTRL = Minecraft.ON_OSX ? GLFW.GLFW_KEY_RIGHT_SUPER : GLFW.GLFW_KEY_RIGHT_CONTROL;
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
    private static final Component MSG_COPIED = Component.translatable("msg.itemmodeltransformer.copied_to_clipboard");
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
            Utils.formatKeyCombination(Minecraft.ON_OSX ? DESC_KEY_CMD : DESC_KEY_CTRL)
    );
    private static final Component DESC_INC_DEC_X0_001 = Component.translatable(
            "desc.itemmodeltransformer.usage.inc_dec.x0_001",
            Utils.formatKeyCombination(DESC_KEY_SHIFT, Minecraft.ON_OSX ? DESC_KEY_CMD : DESC_KEY_CTRL)
    );

    private static int line = 0;
    private static int element = 0;
    private static boolean showUsage = false;
    private static boolean ctrl = false;
    private static boolean shift = false;
    private static boolean alt = false;

    @Override
    public void render(ExtendedGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight)
    {
        if (!TransformHandler.isEnabled()) return;

        Component[] usageLines = makeUsageLines();

        RenderSystem.enableBlend();
        int width = calculateWidth(gui.getFont(), usageLines) - TOOLTIP_DIFF;
        int height = (showUsage ? HEIGHT_USAGE : HEIGHT_BASE) - TOOLTIP_DIFF;
        TooltipRenderUtil.renderTooltipBackground(graphics, 4, 4, width, height, 0);
        RenderSystem.disableBlend();

        graphics.drawManaged(() ->
        {
            Font font = gui.getFont();
            ItemDisplayContext ctx = TransformHandler.getCurrContext();
            ItemTransform xform = TransformHandler.getScratchTransform();

            boolean selected = line == 0;
            graphics.drawString(font, DESC_CAT_TYPE, 3, 3, selected ? 0x66FF66 : 0xFFFFFF, false);
            graphics.drawString(font, ctx.getSerializedName(), 3, 13, 0xFFFFFF, false);

            selected = line == 1;
            graphics.drawString(font, DESC_CAT_ROTATION, 3, 28, selected ? 0x66FF66 : 0xFFFFFF, false);
            graphics.drawString(font, Utils.printVector(xform.rotation, selected, element), 3, 38, 0xFFFFFF, false);

            selected = line == 2;
            graphics.drawString(font, DESC_CAT_TRANSLATION, 3, 53, selected ? 0x66FF66 : 0xFFFFFF, false);
            // Translation is a special snowflake and gets divided by 16, see ItemTransform.Deserializer
            graphics.drawString(font, Utils.printVector(xform.translation, selected, element, 16F), 3, 63, 0xFFFFFF, false);

            selected = line == 3;
            graphics.drawString(font, DESC_CAT_SCALE, 3, 78, selected ? 0x66FF66 : 0xFFFFFF, false);
            graphics.drawString(font, Utils.printVector(xform.scale, selected, element), 3, 88, 0xFFFFFF, false);

            selected = line == 4;
            graphics.drawString(font, DESC_CAT_POST_ROTATION, 3, 103, selected ? 0x66FF66 : 0xFFFFFF, false);
            graphics.drawString(font, Utils.printVector(xform.rightRotation, selected, element), 3, 113, 0xFFFFFF, false);

            for (int i = 0; i < usageLines.length; i++)
            {
                graphics.drawString(font, usageLines[i], 3, 128 + (LINE_HEIGHT * i), 0xFFFFFF, false);
            }
        });
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
                        "desc.itemmodeltransformer.usage.print",
                        Utils.formatKeybind(IMTClient.KEY_PRINT)
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
        if (!TransformHandler.isEnabled())
        {
            releaseAllKeys();
            return;
        }

        long window = Minecraft.getInstance().getWindow().getWindow();
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
            ItemTransform xform = TransformHandler.getScratchTransform();
            switch (line)
            {
                case 0 -> cycleContext(-1);
                case 1 -> modifyVector(xform.rotation, -1F, false, true, 360F);
                // Translation is a special snowflake and gets divided by 16, see ItemTransform.Deserializer
                case 2 -> modifyVector(xform.translation, -.0625F, false, false, 0F);
                case 3 -> modifyVector(xform.scale, -1F, true, false, 0F);
                case 4 -> modifyVector(xform.rightRotation, -1F, false, true, 360F);
            }
        }
        else if (wasClicked(IMTClient.KEY_INCREMENT))
        {
            ItemTransform xform = TransformHandler.getScratchTransform();
            switch (line)
            {
                case 0 -> cycleContext(1);
                case 1 -> modifyVector(xform.rotation, 1F, false, true, 360F);
                // Translation is a special snowflake and gets divided by 16, see ItemTransform.Deserializer
                case 2 -> modifyVector(xform.translation, .0625F, false, false, 0F);
                case 3 -> modifyVector(xform.scale, 1F, true, false, 0F);
                case 4 -> modifyVector(xform.rightRotation, 1F, false, true, 360F);
            }
        }
        else if (wasClicked(IMTClient.KEY_CLEAR))
        {
            ItemTransform xform = TransformHandler.getScratchTransform();
            xform.rotation.set(0F, 0F, 0F);
            xform.translation.set(0F, 0F, 0F);
            xform.scale.set(1F, 1F, 1F);
            xform.rightRotation.set(0F, 0F, 0F);

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
                BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, player.level(), player, 0);
                ItemTransform srcXform = model.getTransforms().getTransform(TransformHandler.getCurrContext());
                if (srcXform != ItemTransform.NO_TRANSFORM)
                {
                    ItemTransform xform = TransformHandler.getScratchTransform();
                    xform.rotation.set(srcXform.rotation);
                    xform.translation.set(srcXform.translation);
                    xform.scale.set(srcXform.scale);
                    xform.rightRotation.set(srcXform.rightRotation);
                    player.displayClientMessage(MSG_LOADED, true);
                }
            }
        }
        else if (wasClicked(IMTClient.KEY_PRINT))
        {
            String out = Utils.encodeItemTransform(
                    TransformHandler.getCurrContext(), TransformHandler.getScratchTransform()
            );
            Minecraft.getInstance().keyboardHandler.setClipboard(out);
            //noinspection ConstantConditions
            Minecraft.getInstance().player.displayClientMessage(MSG_COPIED, true);
        }
        else if (wasClicked(IMTClient.KEY_TOGGLE_USAGE))
        {
            showUsage = !showUsage;
        }
    }

    private static void cycleContext(int dir)
    {
        int idx = TransformHandler.getCurrContext().ordinal() - 1;
        int newIdx = Mth.positiveModulo(idx + dir, CONTEXTS.length - 1);
        TransformHandler.setCurrContext(CONTEXTS[newIdx + 1]);
    }

    private static void modifyVector(Vector3f vec, float dir, boolean noNegative, boolean wrap, float range)
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
        else if (noNegative && component < 0F)
        {
            component = 0F;
        }
        vec.setComponent(element, component);
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
        Utils.releaseKey(IMTClient.KEY_PRINT.get());
    }
}
