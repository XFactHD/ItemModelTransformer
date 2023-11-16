package xfacthd.itemmodeltransformer.client.util;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import xfacthd.itemmodeltransformer.client.handler.TransformHandler;

public final class TransformerKeyConflictContext implements IKeyConflictContext
{
    public static final TransformerKeyConflictContext INSTANCE = new TransformerKeyConflictContext();

    private TransformerKeyConflictContext() { }

    @Override
    public boolean isActive()
    {
        return TransformHandler.isEnabled() && Minecraft.getInstance().screen == null;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other)
    {
        return other == INSTANCE;
    }
}
