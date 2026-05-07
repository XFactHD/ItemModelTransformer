package xfacthd.itemmodeltransformer.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InputConstants.Type.class)
public class MixinInputConstantsType {
    @ModifyExpressionValue(
            method = "lambda$static$0",
            at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z")
    )
    private static boolean itemmodeltransformer$preventSystemNameForKeypadPlusMinus(boolean stringEquals, Integer key) {
        return stringEquals || key == GLFW.GLFW_KEY_KP_ADD || key == GLFW.GLFW_KEY_KP_SUBTRACT;
    }
}
