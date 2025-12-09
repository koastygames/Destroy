package petrolpark.mc.destroy.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class DestroyDatagen {
    
    public static final void gatherDataHighPriority(GatherDataEvent event) {
    
    };

    public static final void gatherData(GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
		final PackOutput output = generator.getPackOutput();
		final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new DestroyBlockTagsProvider(output, lookupProvider, existingFileHelper));
    };
};
