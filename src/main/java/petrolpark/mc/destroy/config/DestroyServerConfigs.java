package petrolpark.mc.destroy.config;

import net.createmod.catnip.config.ConfigBase;
import petrolpark.mc.destroy.core.oil.OilConfigs;
import petrolpark.mc.destroy.core.pollution.PollutionConfigs;

public class DestroyServerConfigs extends ConfigBase {

    public final PollutionConfigs pollution = nested(0, PollutionConfigs::new, "Pollution");
    public final OilConfigs oil = nested(0, OilConfigs::new, "Oil");

    @Override
    public String getName() {
        return "server";
    };
    
};
