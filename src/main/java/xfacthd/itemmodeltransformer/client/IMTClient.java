package xfacthd.itemmodeltransformer.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiOverlaysEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;
import xfacthd.itemmodeltransformer.ItemModelTransformer;
import xfacthd.itemmodeltransformer.client.screen.TransformOverlay;
import xfacthd.itemmodeltransformer.client.util.TransformerKeyConflictContext;
import xfacthd.itemmodeltransformer.client.util.Utils;

@Mod.EventBusSubscriber(modid = ItemModelTransformer.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class IMTClient
{
    private static final Lazy<KeyMapping> KEY_TOGGLE_TRANSFORMER = makeKeybind("toggle", GLFW.GLFW_KEY_I, false, false);
    public static final Lazy<KeyMapping> KEY_PREV_CATEGORY = makeKeybind("prev_category", GLFW.GLFW_KEY_UP, true, false);
    public static final Lazy<KeyMapping> KEY_NEXT_CATEGORY = makeKeybind("next_category", GLFW.GLFW_KEY_DOWN, true, false);
    public static final Lazy<KeyMapping> KEY_PREV_ELEMENT = makeKeybind("prev_element", GLFW.GLFW_KEY_LEFT, true, false);
    public static final Lazy<KeyMapping> KEY_NEXT_ELEMENT = makeKeybind("next_element", GLFW.GLFW_KEY_RIGHT, true, false);
    public static final Lazy<KeyMapping> KEY_DECREMENT = makeKeybind("decrement", GLFW.GLFW_KEY_KP_SUBTRACT, true, true);
    public static final Lazy<KeyMapping> KEY_INCREMENT = makeKeybind("increment", GLFW.GLFW_KEY_KP_ADD, true, true);
    public static final Lazy<KeyMapping> KEY_CLEAR = makeKeybind("clear", GLFW.GLFW_KEY_C, true, false);
    public static final Lazy<KeyMapping> KEY_LOAD = makeKeybind("load", GLFW.GLFW_KEY_L, true, false);
    public static final Lazy<KeyMapping> KEY_PRINT_JSON = makeKeybind("print_json", GLFW.GLFW_KEY_P, true, false);
    public static final Lazy<KeyMapping> KEY_PRINT_DATAGEN = makeKeybind("print_datagen", GLFW.GLFW_KEY_G, true, false);
    public static final Lazy<KeyMapping> KEY_TOGGLE_USAGE = makeKeybind("toggle_usage", GLFW.GLFW_KEY_H, true, false);

    @SubscribeEvent
    public static void onRegisterKeyMappings(final RegisterKeyMappingsEvent event)
    {
        event.register(KEY_TOGGLE_TRANSFORMER.get());
        event.register(KEY_PREV_CATEGORY.get());
        event.register(KEY_NEXT_CATEGORY.get());
        event.register(KEY_PREV_ELEMENT.get());
        event.register(KEY_NEXT_ELEMENT.get());
        event.register(KEY_DECREMENT.get());
        event.register(KEY_INCREMENT.get());
        event.register(KEY_CLEAR.get());
        event.register(KEY_LOAD.get());
        event.register(KEY_PRINT_JSON.get());
        event.register(KEY_PRINT_DATAGEN.get());
        event.register(KEY_TOGGLE_USAGE.get());

        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, IMTClient::onClientTick);
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(final RegisterGuiOverlaysEvent event)
    {
        event.registerAboveAll(Utils.rl("transform_editor"), new TransformOverlay());
    }

    private static void onClientTick(final TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (KEY_TOGGLE_TRANSFORMER.get().consumeClick())
        {
            TransformOverlay.toggleEnabled();
        }
        TransformOverlay.handleInput();
    }

    private static Lazy<KeyMapping> makeKeybind(String name, int key, boolean useConflictCtx, boolean incDec)
    {
        return Lazy.of(() ->
        {
            KeyMapping keybind = new KeyMapping(
                    "key." + ItemModelTransformer.MODID +"." + name,
                    key,
                    "key.categories." + ItemModelTransformer.MODID
            );
            if (useConflictCtx)
            {
                if (incDec)
                {
                    keybind.setKeyConflictContext(TransformerKeyConflictContext.INSTANCE_INC_DEC);
                }
                else
                {
                    keybind.setKeyConflictContext(TransformerKeyConflictContext.INSTANCE);
                }
            }
            return keybind;
        });
    }



    private IMTClient() { }
}
