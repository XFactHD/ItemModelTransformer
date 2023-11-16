package xfacthd.itemmodeltransformer;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ItemModelTransformer.MODID)
@SuppressWarnings("UtilityClassWithPublicConstructor")
public final class ItemModelTransformer
{
    public static final String MODID = "itemmodeltransformer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ItemModelTransformer() { }
}
