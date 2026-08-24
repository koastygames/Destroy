package petrolpark.mc.destroy;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import petrolpark.mc.destroy.core.pollution.PollutionType;
import petrolpark.mc.destroy.core.seismology.ISeismologyProvider;

@EventBusSubscriber
public class DestroyRegistries {

    private static final Set<Registry<?>> REGISTRIES = new HashSet<>(1);

    public static final Registry<PollutionType<ChunkAccess>> CHUNK_POLLUTION_TYPES = simple(Keys.CHUNK_POLLUTION_TYPE);
    public static final Registry<PollutionType<Level>> LEVEL_POLLUTION_TYPES = simple(Keys.LEVEL_POLLUTION_TYPE);

    public static final Registry<ISeismologyProvider> SEISMOLOGY_PROVIDERS = simple(Keys.SEISMOLOGY_PROVIDER);

    @ApiStatus.Internal
    public static <T> Registry<T> simple(ResourceKey<Registry<T>> key) {
        return register(key, false);
    };

    @ApiStatus.Internal
    @SuppressWarnings("deprecation")
	public static <T> Registry<T> register(ResourceKey<Registry<T>> key, boolean hasIntrusiveHolders) {
		RegistryBuilder<T> builder = new RegistryBuilder<>(key).sync(true);

		if (hasIntrusiveHolders) builder.withIntrusiveHolders();

		Registry<T> registry = builder.create();
		REGISTRIES.add(registry);

		return registry;
	};
  
    public static final class Keys {

        public static final ResourceKey<Registry<PollutionType<ChunkAccess>>> CHUNK_POLLUTION_TYPE = key("chunk_pollution_type");
        public static final ResourceKey<Registry<PollutionType<Level>>> LEVEL_POLLUTION_TYPE = key("level_pollution_type");

        public static final ResourceKey<Registry<ISeismologyProvider>> SEISMOLOGY_PROVIDER = key("seismology_provider");

        private static <T> ResourceKey<Registry<T>> key(String name) {
		    return ResourceKey.createRegistryKey(Destroy.asResource(name));
	    };
    };

    @SubscribeEvent
    public static final void onNewRegistries(NewRegistryEvent event) {
        REGISTRIES.forEach(event::register);
    };

	@ApiStatus.Internal
	public static void init() {
		// make sure the class is loaded.
		// this method is called at the tail of BuiltInRegistries, injected by BuiltInRegistriesMixin.
	};
};
