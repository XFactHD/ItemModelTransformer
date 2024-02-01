package xfacthd.itemmodeltransformer.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public final class TransformHandler
{
    private static final ItemTransform SCRATCH_TRANSFORM = new ItemTransform(new Vector3f(), new Vector3f(), new Vector3f(1, 1, 1));
    private static boolean enabled = false;
    private static boolean active = false;
    private static ItemDisplayContext currContext = ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

    public static void toggleEnabled()
    {
        enabled = !enabled;
    }

    public static boolean isEnabled()
    {
        return enabled;
    }

    public static void setCurrContext(ItemDisplayContext currContext)
    {
        TransformHandler.currContext = currContext;
    }

    public static ItemDisplayContext getCurrContext()
    {
        return currContext;
    }

    public static ItemTransform getScratchTransform()
    {
        return SCRATCH_TRANSFORM;
    }

    public static boolean matchesCurrentContext(ItemDisplayContext context)
    {
        return enabled && active && context == currContext;
    }

    public static void activateTransformer(ItemStack stack)
    {
        // noinspection ConstantConditions
        active = enabled && stack.getItem() == Minecraft.getInstance().player.getMainHandItem().getItem();
    }

    public static void deactivateTransformer()
    {
        active = false;
    }



    private TransformHandler() { }
}
