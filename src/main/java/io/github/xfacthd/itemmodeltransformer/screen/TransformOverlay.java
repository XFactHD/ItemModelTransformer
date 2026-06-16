package io.github.xfacthd.itemmodeltransformer.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import io.github.xfacthd.itemmodeltransformer.ItemModelTransformer;
import io.github.xfacthd.itemmodeltransformer.mixin.AccessorItemStackRenderStateLayer;
import io.github.xfacthd.itemmodeltransformer.util.Attribute;
import io.github.xfacthd.itemmodeltransformer.util.TransformHolder;
import io.github.xfacthd.itemmodeltransformer.util.TransformPrinter;
import io.github.xfacthd.itemmodeltransformer.util.Utils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.common.util.Lazy;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public final class TransformOverlay implements GuiLayer {
    private static final ItemDisplayContext[] CONTEXTS = ItemDisplayContext.values();
    private static final ItemStackRenderState SCRATCH_RENDER_STATE = new ItemStackRenderState();
    private static final int LINE_COUNT = 5;
    private static final int ELEMENT_COUNT = 3;
    private static final int LINE_HEIGHT = 10;
    private static final int LINE_PADDING = 5;
    private static final int HEIGHT_BASE = (LINE_HEIGHT * 2 + LINE_PADDING) * LINE_COUNT + LINE_HEIGHT;
    private static final int HEIGHT_USAGE = HEIGHT_BASE + (LINE_HEIGHT * 15);
    private static final int TOOLTIP_DIFF = 4;
    private static final int KEY_LEFT_SHIFT = GLFW.GLFW_KEY_LEFT_SHIFT;
    private static final int KEY_RIGHT_SHIFT = GLFW.GLFW_KEY_RIGHT_SHIFT;
    private static final int KEY_LEFT_CTRL = GLFW.GLFW_KEY_LEFT_CONTROL;
    private static final int KEY_RIGHT_CTRL = GLFW.GLFW_KEY_RIGHT_CONTROL;
    private static final int KEY_LEFT_ALT = GLFW.GLFW_KEY_LEFT_ALT;
    private static final int KEY_RIGHT_ALT = GLFW.GLFW_KEY_RIGHT_ALT;
    private static final Component DUMMY_VECTOR_PRINT = Utils.printVector(new Vector3f(), false, 0);
    private static final Component DESC_CAT_TYPE = Component.translatable("desc.itemmodeltransformer.category.type");
    private static final Component DESC_CAT_ROTATION = Component.translatable("desc.itemmodeltransformer.category.rotation");
    private static final Component DESC_CAT_TRANSLATION = Component.translatable("desc.itemmodeltransformer.category.translation");
    private static final Component DESC_CAT_SCALE = Component.translatable("desc.itemmodeltransformer.category.scale");
    private static final Component DESC_CAT_POST_ROTATION = Component.translatable("desc.itemmodeltransformer.category.post_rotation");
    private static final Component MSG_CLEARED = Component.translatable("msg.itemmodeltransformer.cleared");
    private static final Component MSG_CLEARED_ALL = Component.translatable("msg.itemmodeltransformer.cleared_all");
    private static final Component MSG_LOADED = Component.translatable("msg.itemmodeltransformer.loaded_from_item");
    private static final Component MSG_LOADED_PARTIAL = Component.translatable("msg.itemmodeltransformer.loaded_from_item_partial");
    private static final Component MSG_COPIED_JSON = Component.translatable("msg.itemmodeltransformer.copied_json_to_clipboard");
    private static final Component MSG_COPIED_CODE = Component.translatable("msg.itemmodeltransformer.copied_code_to_clipboard");
    private static final Component DESC_KEY_CTRL = Component.translatable("desc.itemmodeltransformer.key.ctrl");
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
            Utils.formatKeyCombination(DESC_KEY_CTRL)
    );
    private static final Component DESC_INC_DEC_X0_001 = Component.translatable(
            "desc.itemmodeltransformer.usage.inc_dec.x0_001",
            Utils.formatKeyCombination(DESC_KEY_SHIFT, DESC_KEY_CTRL)
    );
    private static final Component DESC_CLEAR_ALL = Component.translatable(
            "desc.itemmodeltransformer.usage.clear.all",
            Utils.formatKeyCombination(DESC_KEY_SHIFT)
    );
    private static final Component DESC_LOAD_OVERWRITE = Component.translatable(
            "desc.itemmodeltransformer.usage.load.overwrite",
            Utils.formatKeyCombination(DESC_KEY_SHIFT)
    );

    private static final TransformHolder[] SCRATCH_TRANSFORMS = Util.make(
            new TransformHolder[CONTEXTS.length - 1],
            arr -> Arrays.setAll(arr, i -> new TransformHolder(CONTEXTS[i + 1]))
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
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!enabled) {
            return;
        }

        Component[] usageLines = makeUsageLines();
        Font font = Minecraft.getInstance().font;

        int width = calculateWidth(font, usageLines) - TOOLTIP_DIFF;
        int height = (showUsage ? HEIGHT_USAGE : HEIGHT_BASE) - TOOLTIP_DIFF;
        TooltipRenderUtil.extractTooltipBackground(graphics, 4, 4, width, height, null);

        TransformHolder holder = getScratchTransform();
        ItemTransform xform = holder.getTransform();

        boolean selected = line == 0;
        graphics.text(font, DESC_CAT_TYPE, 3, 3, selected ? 0xFF66FF66 : 0xFFFFFFFF, false);
        graphics.text(font, holder.printType(), 3, 13, 0xFFFFFFFF, false);

        selected = line == 1;
        graphics.text(font, DESC_CAT_ROTATION, 3, 28, selected ? 0xFF66FF66 : 0xFFFFFFFF, false);
        graphics.text(font, Utils.printVector(xform.rotation(), selected, element), 3, 38, 0xFFFFFFFF, false);

        selected = line == 2;
        graphics.text(font, DESC_CAT_TRANSLATION, 3, 53, selected ? 0xFF66FF66 : 0xFFFFFFFF, false);
        // Translation is a special snowflake and gets divided by 16, see ItemTransform.Deserializer
        graphics.text(font, Utils.printVector(xform.translation(), selected, element, 16F), 3, 63, 0xFFFFFFFF, false);

        selected = line == 3;
        graphics.text(font, DESC_CAT_SCALE, 3, 78, selected ? 0xFF66FF66 : 0xFFFFFFFF, false);
        graphics.text(font, Utils.printVector(xform.scale(), selected, element), 3, 88, 0xFFFFFFFF, false);

        selected = line == 4;
        graphics.text(font, DESC_CAT_POST_ROTATION, 3, 103, selected ? 0xFF66FF66 : 0xFFFFFFFF, false);
        graphics.text(font, Utils.printVector(xform.rightRotation(), selected, element), 3, 113, 0xFFFFFFFF, false);

        for (int i = 0; i < usageLines.length; i++) {
            graphics.text(font, usageLines[i], 3, 128 + (LINE_HEIGHT * i), 0xFFFFFFFF, false);
        }
    }

    public static void toggleEnabled() {
        enabled = !enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    private static TransformHolder getScratchTransform() {
        return SCRATCH_TRANSFORMS[currContext.ordinal() - 1];
    }

    private static TransformHolder getScratchTransform(ItemDisplayContext context) {
        return SCRATCH_TRANSFORMS[context.ordinal() - 1];
    }

    public static boolean isItemAffected(Item item, ItemDisplayContext context) {
        Player player = Objects.requireNonNull(Minecraft.getInstance().player);
        return enabled && context == currContext && switch (context) {
            case NONE -> false;
            case THIRD_PERSON_LEFT_HAND, FIRST_PERSON_LEFT_HAND -> getHeldItem(player, false).is(item);
            case THIRD_PERSON_RIGHT_HAND, FIRST_PERSON_RIGHT_HAND -> getHeldItem(player, true).is(item);
            case HEAD -> player.getItemBySlot(EquipmentSlot.HEAD).is(item);
            case GUI, GROUND, FIXED, ON_SHELF -> player.getMainHandItem().is(item);
        };
    }

    private static ItemStack getHeldItem(Player player, boolean rightHand)
    {
        boolean rightMain = player.getMainArm() == HumanoidArm.RIGHT;
        InteractionHand hand = rightMain == rightHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return player.getItemInHand(hand);
    }

    public static ItemTransform getActiveTransform(Item item, ItemDisplayContext context, ItemTransform originalXform) {
        return isItemAffected(item, context) ? getScratchTransform().getTransform() : originalXform;
    }

    private static Component[] makeUsageLines() {
        if (!showUsage) {
            return new Component[] {
                    Component.translatable(
                            "desc.itemmodeltransformer.usage.show",
                            Utils.formatKeybind(ItemModelTransformer.KEY_TOGGLE_USAGE)
                    )
            };
        }

        return new Component[] {
                Component.translatable(
                        "desc.itemmodeltransformer.usage.hide",
                        Utils.formatKeybind(ItemModelTransformer.KEY_TOGGLE_USAGE)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.prev_category",
                        Utils.formatKeybind(ItemModelTransformer.KEY_PREV_CATEGORY)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.next_category",
                        Utils.formatKeybind(ItemModelTransformer.KEY_NEXT_CATEGORY)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.prev_element",
                        Utils.formatKeybind(ItemModelTransformer.KEY_PREV_ELEMENT)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.next_element",
                        Utils.formatKeybind(ItemModelTransformer.KEY_NEXT_ELEMENT)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.inc_dec",
                        Utils.formatKeybind(ItemModelTransformer.KEY_INCREMENT),
                        Utils.formatKeybind(ItemModelTransformer.KEY_DECREMENT)
                ),
                DESC_INC_DEC_X10_0,
                DESC_INC_DEC_X0_1,
                DESC_INC_DEC_X0_01,
                DESC_INC_DEC_X0_001,
                Component.translatable(
                        "desc.itemmodeltransformer.usage.clear",
                        Utils.formatKeybind(ItemModelTransformer.KEY_CLEAR)
                ),
                DESC_CLEAR_ALL,
                Component.translatable(
                        "desc.itemmodeltransformer.usage.load",
                        Utils.formatKeybind(ItemModelTransformer.KEY_LOAD)
                ),
                DESC_LOAD_OVERWRITE,
                Component.translatable(
                        "desc.itemmodeltransformer.usage.print_json",
                        Utils.formatKeybind(ItemModelTransformer.KEY_PRINT_JSON)
                ),
                Component.translatable(
                        "desc.itemmodeltransformer.usage.print_datagen",
                        Utils.formatKeybind(ItemModelTransformer.KEY_PRINT_DATAGEN)
                )
        };
    }

    private static int calculateWidth(Font font, Component[] lines) {
        int width = font.width(DUMMY_VECTOR_PRINT);
        for (Component line : lines) {
            width = Math.max(width, font.width(line));
        }
        return width + 1;
    }

    public static void handleInput() {
        if (!enabled) {
            releaseAllKeys();
            return;
        }

        Window window = Minecraft.getInstance().getWindow();
        shift = InputConstants.isKeyDown(window, KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, KEY_RIGHT_SHIFT);
        ctrl = InputConstants.isKeyDown(window, KEY_LEFT_CTRL) || InputConstants.isKeyDown(window, KEY_RIGHT_CTRL);
        alt = InputConstants.isKeyDown(window, KEY_LEFT_ALT) || InputConstants.isKeyDown(window, KEY_RIGHT_ALT);

        if (wasClicked(ItemModelTransformer.KEY_PREV_CATEGORY)) {
            line = Mth.positiveModulo(line - 1, LINE_COUNT);
        } else if (wasClicked(ItemModelTransformer.KEY_NEXT_CATEGORY)) {
            line = Mth.positiveModulo(line + 1, LINE_COUNT);
        } else if (line > 0 && wasClicked(ItemModelTransformer.KEY_PREV_ELEMENT)) {
            element = Mth.positiveModulo(element - 1, ELEMENT_COUNT);
        } else if (line > 0 && wasClicked(ItemModelTransformer.KEY_NEXT_ELEMENT)) {
            element = Mth.positiveModulo(element + 1, ELEMENT_COUNT);
        } else if (wasClicked(ItemModelTransformer.KEY_DECREMENT)) {
            TransformHolder holder = getScratchTransform();
            float magnitude = getMagnitude(-1F);
            switch (line) {
                case 0 -> cycleContext(-1);
                case 1 -> holder.modify(Attribute.ROTATION, element, magnitude);
                case 2 -> holder.modify(Attribute.TRANSLATION, element, magnitude);
                case 3 -> holder.modify(Attribute.SCALE, element, magnitude);
                case 4 -> holder.modify(Attribute.RIGHT_ROTATION, element, magnitude);
            }
        } else if (wasClicked(ItemModelTransformer.KEY_INCREMENT)) {
            TransformHolder holder = getScratchTransform();
            float magnitude = getMagnitude(1F);
            switch (line) {
                case 0 -> cycleContext(1);
                case 1 -> holder.modify(Attribute.ROTATION, element, magnitude);
                case 2 -> holder.modify(Attribute.TRANSLATION, element, magnitude);
                case 3 -> holder.modify(Attribute.SCALE, element, magnitude);
                case 4 -> holder.modify(Attribute.RIGHT_ROTATION, element, magnitude);
            }
        } else if (wasClicked(ItemModelTransformer.KEY_CLEAR)) {
            boolean clearAll = shift;
            Stream<TransformHolder> transforms;
            if (clearAll) {
                transforms = Stream.of(SCRATCH_TRANSFORMS);
            } else {
                transforms = Stream.of(getScratchTransform());
            }
            transforms.forEach(TransformHolder::clear);

            //noinspection ConstantConditions
            Minecraft.getInstance().player.sendOverlayMessage(clearAll ? MSG_CLEARED_ALL : MSG_CLEARED);
        } else if (wasClicked(ItemModelTransformer.KEY_LOAD)) {
            Player player = Minecraft.getInstance().player;
            //noinspection ConstantConditions
            ItemStack stack = player.getMainHandItem();
            if (!stack.isEmpty()) {
                ItemModelResolver resolver = Minecraft.getInstance().getItemModelResolver();
                boolean overwrite = shift;
                boolean partial = false;
                for (ItemDisplayContext context : CONTEXTS) {
                    if (context == ItemDisplayContext.NONE) {
                        continue;
                    }

                    resolver.updateForTopItem(SCRATCH_RENDER_STATE, stack, context, player.level(), player, 0);
                    ItemTransform srcXform = ((AccessorItemStackRenderStateLayer) SCRATCH_RENDER_STATE.firstLayer()).imt$getItemTransform();
                    partial |= !getScratchTransform(context).load(srcXform, overwrite);
                    SCRATCH_RENDER_STATE.clear();
                }
                player.sendOverlayMessage(partial ? MSG_LOADED_PARTIAL : MSG_LOADED);
            }
        } else if (wasClicked(ItemModelTransformer.KEY_PRINT_JSON)) {
            String out = TransformPrinter.printJson(SCRATCH_TRANSFORMS);
            Minecraft.getInstance().keyboardHandler.setClipboard(out);
            //noinspection ConstantConditions
            Minecraft.getInstance().player.sendOverlayMessage(MSG_COPIED_JSON);
        } else if (wasClicked(ItemModelTransformer.KEY_PRINT_DATAGEN)) {
            String out = TransformPrinter.printDatagen(SCRATCH_TRANSFORMS);
            Minecraft.getInstance().keyboardHandler.setClipboard(out);
            //noinspection ConstantConditions
            Minecraft.getInstance().player.sendOverlayMessage(MSG_COPIED_CODE);
        } else if (wasClicked(ItemModelTransformer.KEY_TOGGLE_USAGE)) {
            showUsage = !showUsage;
        }

        // Ensure keys pressed while not supported (i.e. inc/dec in type selection) don't trigger after switching line to one supporting said keys
        releaseAllKeys();
    }

    private static void cycleContext(int dir) {
        int idx = currContext.ordinal() - 1;
        int newIdx = Mth.positiveModulo(idx + dir, CONTEXTS.length - 1);
        currContext = CONTEXTS[newIdx + 1];
    }

    private static float getMagnitude(float dir) {
        if (ctrl && shift) {
            dir *= .001F;
        } else if (ctrl) {
            dir *= .01F;
        } else if (shift) {
            dir *= .1F;
        } else if (alt) {
            dir *= 10F;
        }
        return dir;
    }

    private static boolean wasClicked(Lazy<KeyMapping> keybind) {
        KeyMapping key = keybind.get();
        boolean clicked = false;
        while (key.consumeClick()) {
            clicked = true;
        }
        if (clicked) {
            // Prevent vanilla keybinds with the same key from firing
            Utils.releaseAllKeys(key.getKey());
        }
        return clicked;
    }

    private static void releaseAllKeys() {
        Utils.releaseKey(ItemModelTransformer.KEY_PREV_CATEGORY.get());
        Utils.releaseKey(ItemModelTransformer.KEY_NEXT_CATEGORY.get());
        Utils.releaseKey(ItemModelTransformer.KEY_PREV_ELEMENT.get());
        Utils.releaseKey(ItemModelTransformer.KEY_NEXT_ELEMENT.get());
        Utils.releaseKey(ItemModelTransformer.KEY_DECREMENT.get());
        Utils.releaseKey(ItemModelTransformer.KEY_INCREMENT.get());
        Utils.releaseKey(ItemModelTransformer.KEY_CLEAR.get());
        Utils.releaseKey(ItemModelTransformer.KEY_LOAD.get());
        Utils.releaseKey(ItemModelTransformer.KEY_PRINT_JSON.get());
    }
}
