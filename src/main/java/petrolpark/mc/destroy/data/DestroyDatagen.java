package petrolpark.mc.destroy.data;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;

import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.client.ponder.DestroyPonderPlugin;

@EventBusSubscriber(modid = Destroy.MOD_ID)
public class DestroyDatagen {
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static final void gatherDataHighPriority(GatherDataEvent event) {
        if (event.getMods().contains(Destroy.MOD_ID)) addExtraRegistrateData();
    };

    @SubscribeEvent
    public static final void gatherData(GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
		final PackOutput output = generator.getPackOutput();
		final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
		final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new DestroyBlockTagsProvider(output, lookupProvider, existingFileHelper));
    };

    private static final void addExtraRegistrateData() {

        // CreateBistroRegistrateTags.addGenerators();
        
        Destroy.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {

            // Extra manually-defined Lang
            for (Map.Entry<String, JsonElement> entry : FilesHelper.loadJsonResource("assets/" + Destroy.MOD_ID + "/lang/default/extra.json").getAsJsonObject().entrySet()) {
                provider.add(entry.getKey(), entry.getValue().getAsString());
            };

            // Ponder text
            PonderIndex.addPlugin(new DestroyPonderPlugin());
            PonderIndex.getLangAccess().provideLang(Destroy.MOD_ID, provider::add);
        });
    };
};
