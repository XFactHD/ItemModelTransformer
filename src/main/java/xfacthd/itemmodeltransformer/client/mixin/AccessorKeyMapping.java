package xfacthd.itemmodeltransformer.client.mixin;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyMapping.class)
public interface AccessorKeyMapping
{
    @Accessor("MAP")
    static KeyMappingLookup itemmodeltransformer$getKeyMap() { throw new AssertionError(); }

    @Accessor("isDown")
    boolean itemmodeltransformer$isDown();

    @Invoker("release")
    void itemmodeltransformer$release();
}
