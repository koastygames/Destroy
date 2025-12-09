package petrolpark.mc.destroy.core.pollution;

import java.util.Optional;
import java.util.function.Supplier;

import org.joml.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyDataMapTypes;
import petrolpark.mc.destroy.DestroyRegistries;
import petrolpark.mc.destroy.config.DestroyConfigs;

public class PollutionHelper {

    public static final boolean isPollutionEnabled() {
        return DestroyConfigs.server().pollution.enablePollution.get();
    };

    public static final int getPollution(Level level, PollutionType<Level> pollutionType) {
        return level.getData(DestroyAttachmentTypes.LEVEL_POLLUTION).getPollution(pollutionType);
    };

    public static final float getPollutionProportion(Level level, PollutionType<Level> pollutionType) {
        return (float)getPollution(level, pollutionType) / (float)getLevelPollutionTypeProperties(pollutionType).max();
    };
  
    public static final int setPollution(Level level, PollutionType<Level> pollutionType, int value) {
        return level.getData(DestroyAttachmentTypes.LEVEL_POLLUTION).setPollution(pollutionType, value);
    };

    public static final int changePollution(Level level, PollutionType<Level> pollutionType, int change) {
        return level.getData(DestroyAttachmentTypes.LEVEL_POLLUTION).changePollution(pollutionType, change);
    };

    public static final int getPollution(Level level, Supplier<PollutionType<Level>> pollutionType) {
        return level.getData(DestroyAttachmentTypes.LEVEL_POLLUTION).getPollution(pollutionType.get());
    };

    public static final float getPollutionProportion(Level level, Supplier<PollutionType<Level>> pollutionType) {
        return getPollutionProportion(level, pollutionType.get());
    };
  
    public static final int setPollution(Level level, Supplier<PollutionType<Level>> pollutionType, int value) {
        return level.getData(DestroyAttachmentTypes.LEVEL_POLLUTION).setPollution(pollutionType.get(), value);
    };

    public static final int changePollution(Level level, Supplier<PollutionType<Level>> pollutionType, int change) {
        return level.getData(DestroyAttachmentTypes.LEVEL_POLLUTION).changePollution(pollutionType.get(), change);
    };

    public static final int getPollution(ChunkAccess chunk, PollutionType<ChunkAccess> pollutionType) {
        return chunk.getData(DestroyAttachmentTypes.CHUNK_POLLUTION).getPollution(pollutionType);
    };

    public static final float getPollutionProportion(ChunkAccess chunk, PollutionType<ChunkAccess> pollutionType) {
        return (float)getPollution(chunk, pollutionType) / (float)getChunkPollutionTypeProperties(pollutionType).max();
    };
  
    public static final int setPollution(ChunkAccess chunk, PollutionType<ChunkAccess> pollutionType, int value) {
        return chunk.getData(DestroyAttachmentTypes.CHUNK_POLLUTION).setPollution(pollutionType, value);
    };

    public static final int changePollution(ChunkAccess chunk, PollutionType<ChunkAccess> pollutionType, int change) {
        return chunk.getData(DestroyAttachmentTypes.CHUNK_POLLUTION).changePollution(pollutionType, change);
    };

    @SuppressWarnings("unchecked")
    public static final int getPollution(Level level, BlockPos pos, PollutionType<?> pollutionType) {
        if (!pollutionType.chunk) try {
            final PollutionType<Level> levelPollutionType = (PollutionType<Level>)pollutionType;
            return getPollution(level, levelPollutionType);
        } catch (ClassCastException e) {};
        try {
            final PollutionType<ChunkAccess> chunkPollutionType = (PollutionType<ChunkAccess>)pollutionType;
            return getPollution(level.getChunk(pos), chunkPollutionType);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("PollutionType must be of Level or Chunk");
        }
    };

    @SuppressWarnings("unchecked")
    public static final float getPollutionProportion(Level level, BlockPos pos, PollutionType<?> pollutionType) {
        if (!pollutionType.chunk) try {
            final PollutionType<Level> levelPollutionType = (PollutionType<Level>)pollutionType;
            return getPollutionProportion(level, levelPollutionType);
        } catch (ClassCastException e) {};
        try {
            final PollutionType<ChunkAccess> chunkPollutionType = (PollutionType<ChunkAccess>)pollutionType;
            return getPollutionProportion(level.getChunk(pos), chunkPollutionType);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("PollutionType must be of Level or Chunk");
        }
        
    };
  
    @SuppressWarnings("unchecked")
    public static final int setPollution(Level level, BlockPos pos, PollutionType<?> pollutionType, int value) {
        if (!pollutionType.chunk) try {
            final PollutionType<Level> levelPollutionType = (PollutionType<Level>)pollutionType;
            return setPollution(level, levelPollutionType, value);
        } catch (ClassCastException e) {};
        try {
            final PollutionType<ChunkAccess> chunkPollutionType = (PollutionType<ChunkAccess>)pollutionType;
            return setPollution(level.getChunk(pos), chunkPollutionType, value);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("PollutionType must be of Level or Chunk");
        }
    };

    @SuppressWarnings("unchecked")
    public static final int changePollution(Level level, BlockPos pos, PollutionType<?> pollutionType, int change) {
        if (!pollutionType.chunk) try {
            final PollutionType<Level> levelPollutionType = (PollutionType<Level>)pollutionType;
            return changePollution(level, levelPollutionType, change);
        } catch (ClassCastException e) {};
        try {
            final PollutionType<ChunkAccess> chunkPollutionType = (PollutionType<ChunkAccess>)pollutionType;
            return changePollution(level.getChunk(pos), chunkPollutionType, change);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("PollutionType must be of Level or Chunk");
        }
    };

    public static final int getPollution(Level level, BlockPos pos, Supplier<PollutionType<ChunkAccess>> pollutionType) {
        return getPollution(level, pos, pollutionType.get());
    };

    public static final float getPollutionProportion(Level level, BlockPos pos, Supplier<PollutionType<ChunkAccess>> pollutionType) {
        return getPollutionProportion(level, pos, pollutionType.get());
    };
  
    public static final int setPollution(Level level, BlockPos pos, Supplier<PollutionType<ChunkAccess>> pollutionType, int value) {
        return setPollution(level, pos, pollutionType, value);
    };

    public static final int changePollution(Level level, BlockPos pos, Supplier<PollutionType<ChunkAccess>> pollutionType, int change) {
        return changePollution(level, pos, pollutionType.get(), change);
    };

    public static final PollutionType.Properties getLevelPollutionTypeProperties(PollutionType<Level> pollutionType) {
        return getLevelPollutionTypeProperties(DestroyRegistries.LEVEL_POLLUTION_TYPES.wrapAsHolder(pollutionType));  
    };

    public static final PollutionType.Properties getLevelPollutionTypeProperties(Holder<PollutionType<Level>> pollutionTypeHolder) {
        return Optional.ofNullable(pollutionTypeHolder.getData(DestroyDataMapTypes.LEVEL_POLLUTION_PROPERTIES)).orElse(PollutionType.Properties.DEFAULT);
    };

    public static final PollutionType.Properties getChunkPollutionTypeProperties(PollutionType<ChunkAccess> pollutionType) {
        return getChunkPollutionTypeProperties(DestroyRegistries.CHUNK_POLLUTION_TYPES.wrapAsHolder(pollutionType));  
    };

    public static final PollutionType.Properties getChunkPollutionTypeProperties(Holder<PollutionType<ChunkAccess>> pollutionTypeHolder) {
        return Optional.ofNullable(pollutionTypeHolder.getData(DestroyDataMapTypes.CHUNK_POLLUTION_PROPERTIES)).orElse(PollutionType.Properties.DEFAULT);
    };

    @SuppressWarnings("unchecked")
    public static final PollutionType.Properties getPollutionTypeProperties(PollutionType<?> pollutionType) {
        if (!pollutionType.chunk) try {
            final PollutionType<Level> levelPollutionType = (PollutionType<Level>)pollutionType;
            return getLevelPollutionTypeProperties(levelPollutionType);
        } catch (ClassCastException e) {}
        try {
            final PollutionType<ChunkAccess> chunkPollutionType = (PollutionType<ChunkAccess>)pollutionType;
            return getChunkPollutionTypeProperties(chunkPollutionType);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("PollutionType must be of Level or Chunk");
        }
    };

    public static final DustParticleOptions getCropGrowthFailureParticles() {
        return new DustParticleOptions(new Vector3f(109 / 256f, 77 / 256f, 14 / 256f), 1f);
    };


};
