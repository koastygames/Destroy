package petrolpark.mc.destroy;

import static petrolpark.mc.destroy.Destroy.REGISTRATE;

import com.tterrag.registrate.util.entry.RegistryEntry;

import petrolpark.mc.destroy.core.oil.CrudeOilSeismologyProvider;
import petrolpark.mc.destroy.core.seismology.ISeismologyProvider;

public class DestroySeismologyProviders {
    
    public static final RegistryEntry<ISeismologyProvider, ISeismologyProvider> NONE = REGISTRATE.simple("none", DestroyRegistries.Keys.SEISMOLOGY_PROVIDER, ISeismologyProvider::none);
    public static final RegistryEntry<ISeismologyProvider, CrudeOilSeismologyProvider> CRUDE_OIL = REGISTRATE.simple("crude_oil", DestroyRegistries.Keys.SEISMOLOGY_PROVIDER, CrudeOilSeismologyProvider::new);

    public static final void register() {};
};
