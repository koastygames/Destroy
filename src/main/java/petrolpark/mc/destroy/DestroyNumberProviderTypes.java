package petrolpark.mc.destroy;

import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import petrolpark.mc.destroy.core.pollution.PollutionNumberProvider;

public class DestroyNumberProviderTypes {
    
    public static final RegistryEntry<LootNumberProviderType, LootNumberProviderType>
    
    POLLUTION = Destroy.REGISTRATE.lootNumberProviderType("pollution", PollutionNumberProvider.CODEC);

    public static final void register() {};
};
