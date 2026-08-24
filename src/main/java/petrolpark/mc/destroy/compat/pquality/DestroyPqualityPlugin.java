package petrolpark.mc.destroy.compat.pquality;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.pquality.core.client.effectDescription.IQualityEffectDescription;
import petrolpark.mc.pquality.core.client.effectDescription.SimpleQualityEffectDescription;
import petrolpark.mc.pquality.core.plugin.IPqualityPlugin;
import petrolpark.mc.pquality.core.plugin.PQualityPlugin;

@PQualityPlugin
public class DestroyPqualityPlugin implements IPqualityPlugin {
    
    @Override
    public void registerEffectDescriptions(Consumer<IQualityEffectDescription> adder) {
        register(adder, "seismometer", DestroyItems.SEISMOMETER);
    };

    private void register(Consumer<IQualityEffectDescription> adder, String name, ItemLike item) {
        register(adder, name, Collections.singletonList(new ItemStack(item)));
    };

    private void register(Consumer<IQualityEffectDescription> adder, String name, List<ItemStack> stacks) {
        adder.accept(new SimpleQualityEffectDescription(Destroy.asResource(name), () -> true, stacks));
    };
};
