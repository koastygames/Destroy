package petrolpark.mc.destroy.core.pollution;

import net.createmod.catnip.config.ConfigBase;

public class ClientPollutionConfigs extends ConfigBase {

    public final ConfigBool smogAffectsBlockColors = b(true, "smogAffectsBlockColors", "Whether increased levels of Smog causes foliage to turn browner");

    @Override
    public String getName() {
        return "pollution";
    };
    
};
