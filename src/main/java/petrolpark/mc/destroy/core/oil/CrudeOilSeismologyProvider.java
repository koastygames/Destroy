package petrolpark.mc.destroy.core.oil;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.core.seismology.ISeismologyProvider;

@ParametersAreNonnullByDefault
public class CrudeOilSeismologyProvider implements ISeismologyProvider {

    public static final Component NAME = ISeismologyProvider.createName(Destroy.asResource("crude_oil"))
        .withStyle(ChatFormatting.WHITE);

    @Override
    public Component getName() {
        return NAME;
    };

    @Override
    public boolean isChunkActive(ServerLevel level, int chunkX, int chunkZ) {
        return ChunkCrudeOil.getTheoreticalOil(level, chunkX, chunkZ) > 0;
    };
    
};
