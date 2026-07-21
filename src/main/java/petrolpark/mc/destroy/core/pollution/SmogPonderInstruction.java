package petrolpark.mc.destroy.core.pollution;

import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import petrolpark.mc.destroy.DestroyPollutionTypes;

public class SmogPonderInstruction extends PonderInstruction {

    public final int value;

    public SmogPonderInstruction(int value) {
        this.value = Mth.clamp(value, 0, PollutionHelper.getChunkPollutionTypeProperties(DestroyPollutionTypes.SMOG.get()).max());
    };

    @Override
    public boolean isComplete() {
        return true;
    };

    @Override
    public void tick(PonderScene scene) {
        PollutionHelper.setPollution(scene.getWorld(), BlockPos.ZERO, DestroyPollutionTypes.SMOG.get(), value);
        scene.forEach(WorldSectionElement.class, e -> {
            e.queueRedraw();
        });
    };
    
};
