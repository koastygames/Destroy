package petrolpark.mc.destroy.core.oil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import petrolpark.mc.destroy.core.seismology.ISeismologyProvider;
 
@ParametersAreNonnullByDefault
public class ChunkCrudeOil {

    public static final ChunkCrudeOil.Serializer SERIALIZER = new ChunkCrudeOil.Serializer();

    public static final ChunkCrudeOil create(IAttachmentHolder holder) {
        if (holder instanceof ChunkAccess) return new ChunkCrudeOil();
        throw new IllegalArgumentException();
    };

    protected boolean generated = false; // Whether the amount of Crude Oil in this Chunk has already been determined
    protected int amount; // Amount of Crude Oil in the Chunk in mB

    /**
     * Get the amount of oil generated in this chunk. This does not account for if oil has been pumped out.
     * @param level
     * @param chunkX
     * @param chunkZ
     */
    public static int getTheoreticalOil(ServerLevel level, int chunkX, int chunkZ) {
        // Get the seeded randomizer for this level
        final RandomSource random = RandomSource.create(level.getSeed() ^ ISeismologyProvider.HASH_SALT);
        // Generate the noise value for this Chunk
        final double value = (PerlinNoise.create(random, -2, 1d).getValue(chunkX * 1.5d, chunkZ * 1.5d, 0));
        // Don't generate any oil if the value is less than a threshold
        return value < 0.3d ? 0 : (int)(value * 10000000d);
    };

    public void generate(LevelChunk chunk, @javax.annotation.Nullable Player player) {
        if (generated) return;
        if (chunk.getLevel() instanceof ServerLevel level) {
            ChunkPos pos = chunk.getPos();
            amount = getTheoreticalOil(level, pos.x, pos.z);
            //TODO check for Player luck
            generated = true;
        };
    };

    public boolean isGenerated() {
        return generated;
    };

    public int getAmount() {
        return amount;
    };

    public int setAmount(int amount) {
        this.amount = Math.max(0, amount);
        return amount;
    };

    public int decreaseAmount(int decrease) {
        amount = (int)Math.max(0, amount - decrease);
        return amount;
    };
    
    public static class Serializer implements IAttachmentSerializer<CompoundTag, ChunkCrudeOil> {

        @Override
        public ChunkCrudeOil read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
            final ChunkCrudeOil oil = new ChunkCrudeOil();
            oil.generated = tag.getBoolean("Generated");
            oil.amount = tag.getInt("Amount");
            return oil;
        };

        @Override
        public @Nullable CompoundTag write(ChunkCrudeOil attachment, HolderLookup.Provider provider) {
            final CompoundTag tag = new CompoundTag();
            tag.putBoolean("Generated", attachment.generated);
            tag.putInt("Amount", attachment.amount);
            return tag;
        };

    };
};
