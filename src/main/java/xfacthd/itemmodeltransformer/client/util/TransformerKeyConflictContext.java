package xfacthd.itemmodeltransformer.client.util;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import xfacthd.itemmodeltransformer.client.handler.TransformHandler;

public sealed class TransformerKeyConflictContext implements IKeyConflictContext
{
    public static final IKeyConflictContext INSTANCE = new TransformerKeyConflictContext();
    public static final IKeyConflictContext INSTANCE_INC_DEC = new TransformerKeyConflictContext.IncDec();

    private TransformerKeyConflictContext() { }

    @Override
    public boolean isActive()
    {
        return TransformHandler.isEnabled() && Minecraft.getInstance().screen == null;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other)
    {
        return other == INSTANCE || other == INSTANCE_INC_DEC;
    }

    private static final class IncDec extends TransformerKeyConflictContext
    {
        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            // Must conflict with IN_GAME to allow modifiers to be used for range adjustment
            return super.conflicts(other) || other == KeyConflictContext.IN_GAME;
        }
    }
}
