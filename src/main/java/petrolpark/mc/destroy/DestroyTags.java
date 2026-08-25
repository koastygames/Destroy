package petrolpark.mc.destroy;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import petrolpark.mc.destroy.core.seismology.ISeismologyProvider;
import petrolpark.mc.library.util.Lang;

public class DestroyTags {

    public enum Blocks {

        ACID_RAIN_DESTROYS,
        ACID_RAIN_SETS_DEAD_BUSH,
        ACID_RAIN_SETS_DIRT,
        ;

        public final TagKey<Block> tag;

        private Blocks() {
            tag = TagKey.create(Registries.BLOCK, Destroy.asResource(Lang.asId(name())));
        };

    };
  
    public enum Items {

        BONEMEAL_BYPASSES_POLLUTION("bonemeal/bypasses_pollution"),
        ;

        public final TagKey<Item> tag;

        private Items() {
            tag = TagKey.create(Registries.ITEM, Destroy.asResource(Lang.asId(name())));
        };

        private Items(String path) {
            tag = TagKey.create(Registries.ITEM, Destroy.asResource(path));
        };
    };

    public enum SeismologyProviders {

        FOR_SEISMOMETER,
        ;

        public final TagKey<ISeismologyProvider> tag;

        private SeismologyProviders() {
            tag = TagKey.create(DestroyRegistries.Keys.SEISMOLOGY_PROVIDER, Destroy.asResource(Lang.asId(name())));
        };
    };
};
