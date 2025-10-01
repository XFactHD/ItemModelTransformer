package xfacthd.itemmodeltransformer.client.mixin;

import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
            @Nullable ItemOwner entity,
            int seed,
            CallbackInfo ci
    )
    {
        ((ItemAwareItemStackRenderState) renderState).imt$setItem(stack.getItem());

        if (ctx == ItemDisplayContext.GUI)
        {
            // Force items in UIs to re-render every frame to properly capture the potentially modified transform
            renderState.setAnimated();
        }
    }
}
