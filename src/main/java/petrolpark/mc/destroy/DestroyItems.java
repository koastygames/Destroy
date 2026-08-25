package petrolpark.mc.destroy;

import static petrolpark.mc.destroy.Destroy.REGISTRATE;

import com.simibubi.create.AllBlocks;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import petrolpark.mc.destroy.core.seismology.Seismograph;
import petrolpark.mc.destroy.core.seismology.SeismographItem;
import petrolpark.mc.destroy.core.seismology.SeismometerItem;
import petrolpark.mc.library.PetrolparkTags;

public class DestroyItems {

    public static final ItemEntry<SeismographItem> SEISMOGRAPH = REGISTRATE.item("seismograph", SeismographItem::new)
        .defaultLang()
        .defaultModel()
        .properties(p -> p
            .stacksTo(1)
            .component(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER, DestroySeismologyProviders.NONE.getDelegate())
            .component(DestroyDataComponentTypes.SEISMOGRAPH, Seismograph.EMPTY)
        ).register();

    public static final ItemEntry<SeismometerItem> SEISMOMETER = REGISTRATE.item("seismometer", SeismometerItem::new)
        .lang("Handheld Seismometer")
        .properties(p -> p
            .component(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER, DestroySeismologyProviders.NONE.getDelegate())
            .stacksTo(1)
        ).recipe((ctx, prov) -> ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ctx.get())
            .define('I', Tags.Items.STORAGE_BLOCKS_IRON)
            .define('F', Tags.Items.FEATHERS)
            .define('P', Items.PAPER)
            .define('C', AllBlocks.BRASS_CASING)
            .pattern("IFP")
            .pattern(" C ")
            .unlockedBy("has_casing", RegistrateRecipeProvider.has(AllBlocks.BRASS_CASING))
            .save(prov)
        ).tag(PetrolparkTags.Items.FLAGGABLE.tag)
        .register();
    
    public static final void register() {};
};
