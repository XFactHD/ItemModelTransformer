package xfacthd.itemmodeltransformer.client.mixin;

import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xfacthd.itemmodeltransformer.client.screen.TransformOverlay;
import xfacthd.itemmodeltransformer.client.util.ItemAwareItemStackRenderState;

@Mixin(ItemModelResolver.class)
@SuppressWarnings("MethodMayBeStatic")
public final class MixinItemModelResolver
{
    @Inject(method = "appendItemLayers", at = @At("HEAD"))
    private void itemmodeltransformer$captureItem(
            ItemStackRenderState renderState,
            ItemStack stack,
            ItemDisplayContext ctx,
            @Nullable Level level,
            @Nullable LivingEntity entity,
            int seed,
            CallbackInfo ci
    )
    {
        ((ItemAwareItemStackRenderState) renderState).imt$setItem(stack.getItem());

        // TODO: this also needs to evaluate to true exactly once after the context was switched off of GUI or the tool was disabled
        if (ctx == ItemDisplayContext.GUI && TransformOverlay.isItemAffected(stack.getItem(), ctx))
        {
            // Force item being edited to re-render in the UI every frame
            renderState.setAnimated();
        }
    }
}
