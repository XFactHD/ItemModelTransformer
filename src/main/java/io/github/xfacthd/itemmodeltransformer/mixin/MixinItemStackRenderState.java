package io.github.xfacthd.itemmodeltransformer.mixin;

import io.github.xfacthd.itemmodeltransformer.util.ItemAwareItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStackRenderState.class)
public final class MixinItemStackRenderState implements ItemAwareItemStackRenderState {
    @Shadow
    ItemDisplayContext displayContext;

    private Item imt$item = Items.AIR;

    @Override
    public void imt$setItem(Item item) {
        imt$item = item;
    }

    @Override
    public Item imt$getItem() {
        return imt$item;
    }

    @Override
    public ItemDisplayContext imt$getDisplayContext() {
        return displayContext;
    }
}
