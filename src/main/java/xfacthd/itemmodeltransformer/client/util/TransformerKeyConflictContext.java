package xfacthd.itemmodeltransformer.client.util;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import xfacthd.itemmodeltransformer.client.screen.TransformOverlay;

public sealed class TransformerKeyConflictContext implements IKeyConflictContext {
    public static final IKeyConflictContext INSTANCE = new TransformerKeyConflictContext();
    public static final IKeyConflictContext INSTANCE_MODIFIERS = new WithModifiers();

    private TransformerKeyConflictContext() { }

    @Override
    public boolean isActive() {
        return TransformOverlay.isEnabled() && Minecraft.getInstance().gui.screen() == null;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return other == INSTANCE || other == INSTANCE_MODIFIERS;
    }

    private static final class WithModifiers extends TransformerKeyConflictContext {
        @Override
        public boolean conflicts(IKeyConflictContext other) {
            // Must conflict with IN_GAME to allow modifiers to be used for range adjustment, load and clear
            return super.conflicts(other) || other == KeyConflictContext.IN_GAME;
        }
    }
}
