package petrolpark.mc.destroy;

import java.util.function.UnaryOperator;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import petrolpark.mc.destroy.core.seismology.ISeismologyProvider;
import petrolpark.mc.destroy.core.seismology.Seismograph;

public class DestroyDataComponentTypes {
    
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Destroy.MOD_ID);

    public static final DataComponentType<ISeismologyProvider> SEISMOLOGY_PROVIDER = register("seismology_provider", b -> b
        .persistent(ISeismologyProvider.CODEC)
        .networkSynchronized(ISeismologyProvider.STREAM_CODEC)
    );
    public static final DataComponentType<Seismograph> SEISMOGRAPH = register("seismograph", b -> b
        .persistent(Seismograph.CODEC)
        .networkSynchronized(Seismograph.STREAM_CODEC)
    );

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
		DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
		DATA_COMPONENTS.register(name, () -> type);
		return type;
	};

	@ApiStatus.Internal
	public static final void register(IEventBus modEventBus) {
		DATA_COMPONENTS.register(modEventBus);
	};
};
