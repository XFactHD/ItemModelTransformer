package xfacthd.itemmodeltransformer.client.util;

import net.minecraft.world.item.Item;

public interface ItemAwareItemStackRenderState
{
    void imt$setItem(Item item);

    Item imt$getItem();
}
