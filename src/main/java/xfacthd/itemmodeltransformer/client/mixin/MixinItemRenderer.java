package xfacthd.itemmodeltransformer.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xfacthd.itemmodeltransformer.client.handler.TransformHandler;

@Mixin(ItemRenderer.class)
@SuppressWarnings("MethodMayBeStatic")
public final class MixinItemRenderer
{
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/ClientHooks;handleCameraTransforms(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemDisplayContext;Z)Lnet/minecraft/client/resources/model/BakedModel;"
            )
    )
    private void itemmodeltransformer$preApplyTransform(
            ItemStack stack,
            ItemDisplayContext ctx,
            boolean leftHand,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay,
            BakedModel model,
            CallbackInfo ci
    )
    {
        TransformHandler.activateTransformer(stack);
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/ClientHooks;handleCameraTransforms(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemDisplayContext;Z)Lnet/minecraft/client/resources/model/BakedModel;",
                    shift = At.Shift.AFTER
            )
    )
    private void itemmodeltransformer$postApplyTransform(
            ItemStack stack,
            ItemDisplayContext ctx,
            boolean leftHand,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light,
            int overlay,
            BakedModel model,
            CallbackInfo ci
    )
    {
        TransformHandler.deactivateTransformer();
    }
}
