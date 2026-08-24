package petrolpark.mc.destroy.core.oil;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import petrolpark.mc.destroy.core.seismology.ISeismologyProvider;

@ParametersAreNonnullByDefault
public class CrudeOilSeismologyProvider implements ISeismologyProvider {

    @Override
    public void addToTooltip(TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addToTooltip'");
    };

    @Override
    public boolean isChunkActive(ServerLevel level, int chunkX, int chunkZ) {
        return ChunkCrudeOil.getTheoreticalOil(level, chunkX, chunkZ) > 0;
    };
    
};
