package xfacthd.itemmodeltransformer;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ItemModelTransformer.MOD_ID)
@SuppressWarnings("UtilityClassWithPublicConstructor")
public final class ItemModelTransformer {
    public static final String MOD_ID = "itemmodeltransformer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ItemModelTransformer() { }
}
