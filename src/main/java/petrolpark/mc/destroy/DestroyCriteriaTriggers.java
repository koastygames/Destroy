package petrolpark.mc.destroy;

import static petrolpark.mc.destroy.Destroy.REGISTRATE;

import com.tterrag.registrate.util.entry.RegistryEntry;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;

public class DestroyCriteriaTriggers {

    public static final RegistryEntry<CriterionTrigger<?>, PlayerTrigger>
    
    USE_SEISMOMETER = REGISTRATE.criterionTrigger("use_seismometer", PlayerTrigger::new),
    FILL_SEISMOGRAPH = REGISTRATE.criterionTrigger("fill_seismograph", PlayerTrigger::new),
    COMPLETE_SEISMOGRAPH = REGISTRATE.criterionTrigger("complete_seismograph", PlayerTrigger::new);

  
    public static final void register() {};
};
