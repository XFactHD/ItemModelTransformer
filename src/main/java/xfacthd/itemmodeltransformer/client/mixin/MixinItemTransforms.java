package xfacthd.itemmodeltransformer.client.mixin;

import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xfacthd.itemmodeltransformer.client.screen.TransformOverlay;

@Mixin(ItemTransforms.class)
@SuppressWarnings("MethodMayBeStatic")
public final class MixinItemTransforms
{
    @Inject(
            method = "getTransform",
            at = @At("HEAD"),
            cancellable = true
    )
    private void itemmodeltransformer$replaceItemTransform(
            ItemDisplayContext context, CallbackInfoReturnable<ItemTransform> cir
    )
    {
        if (TransformOverlay.matchesCurrentContext(context))
        {
            cir.setReturnValue(TransformOverlay.getScratchTransform());
        }
    }
}
