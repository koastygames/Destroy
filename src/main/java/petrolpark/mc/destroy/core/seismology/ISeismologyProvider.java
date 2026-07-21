package petrolpark.mc.destroy.core.seismology;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public interface ISeismologyProvider {
  
    public boolean isChunkSeismicallyActive(Level level, ChunkPos pos);
};
