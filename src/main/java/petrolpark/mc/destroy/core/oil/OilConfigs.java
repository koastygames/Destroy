package petrolpark.mc.destroy.core.oil;

import net.createmod.catnip.config.ConfigBase;

public class OilConfigs extends ConfigBase {

    public final ConfigFloat seismometerErrorRate = f(0.25f, 0f, 1f, "seismometerErrorRate", "The rate of false positives Seismometers produce when searching for Destroy Crude Oil");

    @Override
    public String getName() {
        return "seismology";
    };
    
};
