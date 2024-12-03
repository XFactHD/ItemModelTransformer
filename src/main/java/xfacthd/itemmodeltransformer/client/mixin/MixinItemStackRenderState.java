package xfacthd.itemmodeltransformer.client.mixin;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import xfacthd.itemmodeltransformer.client.util.ItemAwareItemStackRenderState;

@Mixin(ItemStackRenderState.class)
public final class MixinItemStackRenderState implements ItemAwareItemStackRenderState
{
    private Item imt$item = Items.AIR;

    @Override
    public void imt$setItem(Item item)
    {
        imt$item = item;
    }

    @Override
    public Item imt$getItem()
    {
        return imt$item;
    }
}
