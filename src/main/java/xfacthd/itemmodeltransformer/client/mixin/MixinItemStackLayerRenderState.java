package xfacthd.itemmodeltransformer.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xfacthd.itemmodeltransformer.client.screen.TransformOverlay;
import xfacthd.itemmodeltransformer.client.util.ItemAwareItemStackRenderState;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public final class MixinItemStackLayerRenderState
{
    @Shadow
    @Final
    private ItemStackRenderState this$0;

    @WrapOperation(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/block/model/ItemTransform;apply(ZLcom/mojang/blaze3d/vertex/PoseStack$Pose;)V"
            )
    )
    private void itemmodeltransformer$injectModifiedItemTransform(ItemTransform originalXforms, boolean leftHand, PoseStack.Pose pose, Operation<Void> operation)
    {
        Item item = ((ItemAwareItemStackRenderState) this$0).imt$getItem();
        ItemDisplayContext ctx = ((ItemAwareItemStackRenderState) this$0).imt$getDisplayContext();
        operation.call(TransformOverlay.getActiveTransform(item, ctx, originalXforms), leftHand, pose);
    }
}
