package xfacthd.itemmodeltransformer.client.mixin;

import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public interface AccessorItemStackRenderStateLayer
{
    @Accessor("transform")
    ItemTransform imt$getTransform();
}
