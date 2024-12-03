package xfacthd.itemmodeltransformer.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xfacthd.itemmodeltransformer.client.screen.TransformOverlay;
import xfacthd.itemmodeltransformer.client.util.ItemAwareItemStackRenderState;

@Mixin(ItemStackRenderState.LayerRenderState.class)
@SuppressWarnings("MethodMayBeStatic")
public final class MixinItemStackLayerRenderState
{
    @Shadow
    @Final
    private ItemStackRenderState this$0;

    @Inject(method = "render", at = @At("HEAD"))
    private void itemmodeltransformer$preApplyTransform(PoseStack poseStack, MultiBufferSource buffers, int light, int overlay, CallbackInfo ci)
    {
        TransformOverlay.activateTransformer(((ItemAwareItemStackRenderState) this$0).imt$getItem());
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"
            )
    )
    private void itemmodeltransformer$postApplyTransform(PoseStack poseStack, MultiBufferSource buffers, int light, int overlay, CallbackInfo ci)
    {
        TransformOverlay.deactivateTransformer();
    }
}
