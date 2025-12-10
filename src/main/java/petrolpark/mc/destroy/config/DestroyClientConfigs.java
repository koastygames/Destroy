package petrolpark.mc.destroy.config;

import net.createmod.catnip.config.ConfigBase;
import petrolpark.mc.destroy.core.pollution.ClientPollutionConfigs;

public class DestroyClientConfigs extends ConfigBase {

    public final ClientPollutionConfigs pollution = nested(0, ClientPollutionConfigs::new, "Pollution");

    @Override
    public String getName() {
        return "client";
    };
    
};
