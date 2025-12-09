package petrolpark.mc.destroy.config;

import net.createmod.catnip.config.ConfigBase;
import petrolpark.mc.destroy.core.pollution.PollutionConfig;

public class DestroyServerConfigs extends ConfigBase {

    public final PollutionConfig pollution = nested(0, PollutionConfig::new, "Pollution");

    @Override
    public String getName() {
        return "server";
    };
    
};
