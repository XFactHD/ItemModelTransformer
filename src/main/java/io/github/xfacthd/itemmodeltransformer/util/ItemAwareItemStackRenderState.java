package io.github.xfacthd.itemmodeltransformer.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;

public interface ItemAwareItemStackRenderState {
    void imt$setItem(Item item);

    Item imt$getItem();

    ItemDisplayContext imt$getDisplayContext();
}
