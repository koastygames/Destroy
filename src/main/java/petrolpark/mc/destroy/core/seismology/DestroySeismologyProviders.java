package petrolpark.mc.destroy.core.seismology;

import static petrolpark.mc.destroy.Destroy.REGISTRATE;

import com.tterrag.registrate.util.entry.RegistryEntry;

import petrolpark.mc.destroy.DestroyRegistries;

public class DestroySeismologyProviders {
    
    public static final RegistryEntry<ISeismologyProvider, ISeismologyProvider> NONE = REGISTRATE.simple(DestroyRegistries.Keys.SEISMOLOGY_PROVIDER, ISeismologyProvider::none);

    public static final void register() {};
};
