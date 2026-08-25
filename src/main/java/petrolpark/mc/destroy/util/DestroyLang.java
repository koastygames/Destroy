package petrolpark.mc.destroy.util;

import net.minecraft.network.chat.Component;
import petrolpark.mc.destroy.Destroy;

public class DestroyLang {
  
    public static Component translateDirect(String keyEnd, Object ... args) {
        return Component.translatable(Destroy.MOD_ID + "." + keyEnd, args);
    };

    public static Component tooltip(String keyEnd, Object ... args) {
        return translateDirect("tooltip." + keyEnd, args);
    };
};
