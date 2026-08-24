package petrolpark.mc.destroy.core.seismology;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import petrolpark.mc.destroy.DestroyRegistries;

@ParametersAreNonnullByDefault
public interface ISeismologyProvider extends TooltipProvider {

    static final long HASH_SALT = 5252525252l;

    public static final Codec<ISeismologyProvider> CODEC = DestroyRegistries.SEISMOLOGY_PROVIDERS.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, ISeismologyProvider> STREAM_CODEC = ByteBufCodecs.registry(DestroyRegistries.Keys.SEISMOLOGY_PROVIDER);
  
    public static ISeismologyProvider none() {
        return ISeismologyProvider.None.INSTANCE;
    };

    public Component getName();

    public boolean isChunkActive(ServerLevel level, int chunkX, int chunkZ);

    /**
     * To make prospecting a bit less straightforward, some random non-seismically active chunks also show as 'seismically active'. This generates,
     * completely randomly, some chunks to show as seismically active
     * @param level
     * @param chunkX
     * @param chunkY
     */
    public default boolean isFalsePositive(ServerLevel level, float errorRate, int chunkX, int chunkZ) {
        final RandomSource random = RandomSource.create(level.getSeed() ^ HASH_SALT ^ chunkX ^ chunkZ);
        random.nextInt();
        return random.nextFloat() < errorRate;
    };

    /**
     * Get the 'signals' in a line (the "long axis") which are used for the seismograph nonogram.
     * @param level
     * @param chunkX Any chunk co-ordinate - the line of chunks will always start on a multiple of eight
     * @param chunkZ See above
     * @param xNotZ {@code true} if the long axis is X, {@code false} if its Z
     * @return A byte where each bit is {@code 1} if we show a signal on that chunk, starting on the multiple of eight and ascending
     */
    public static byte getSignals(ServerLevel level, ISeismologyProvider provider, float errorRate, int chunkX, int chunkZ, boolean xNotZ) {
        final boolean[][] activity = new boolean[10][3];
        final boolean[][] falsePositives = new boolean[10][3];
        final int widthAxis = xNotZ ? chunkZ : chunkX;
        final int lengthAxis = xNotZ ? chunkX : chunkZ;
        for (int width = 0; width < 3; width++) {
            for (int length = 0; length < 10; length++) {
                int lengthCoordinate = SeismographItem.mapChunkLowerCorner(lengthAxis) - 1 + length;
                int widthCoordinate = widthAxis - 1 + width;
                int x = xNotZ ? lengthCoordinate : widthCoordinate;
                int z = xNotZ ? widthCoordinate : lengthCoordinate;
                activity[length][width] = provider.isChunkActive(level, x, z);
                falsePositives[length][width] = provider.isFalsePositive(level, errorRate, x, z);
            };
        };
        byte signals = 0;
        for (int length = 1; length <= 8; length++) {
            boolean oilInSurroundings = false; // Start by assuming we have red herrings on all eight sides
            boolean surroundedByHerrings = true; // Whether we have oil or an adjacent one does
            for (int lengthOffset = -1; lengthOffset <= 1; lengthOffset++) {
                checkAllSides: for (int widthOffset = -1; widthOffset <= 1; widthOffset++) {
                    if (lengthOffset != 0 && widthOffset != 0) continue checkAllSides; // Don't check corners
                    if (activity[length + lengthOffset][1 + widthOffset]) oilInSurroundings = true;
                    if (!falsePositives[length + lengthOffset][1 + widthOffset] && lengthOffset != 0 && widthOffset != 0) surroundedByHerrings = false; // If there's anything which isn't a red herring to our side (not including ourselves)
                    if (oilInSurroundings) break checkAllSides; // If there's nothing more to look for, stop looking
                };
            };

            /* 
             * If there is any oil, either in us or an adjacent chunk, this chunk should show seismic activity.
             * If we are surrounded on four sides by red herrings, we don't want to show this too as that would lead to a false positive (plusses indicate oil).
             * Otherwise, if we are a red herring, show that.
             */
            if (oilInSurroundings || (!surroundedByHerrings && falsePositives[length][1])) {
                signals |= 1 << (length - 1);
            };
            
        };
        // if (debug) {
        //     Destroy.LOGGER.info("Oil: ");
        //     for (int i = 0; i <= 2; i++) {
        //         String string = "";
        //         for (boolean[] slice : oil) {
        //             string += slice[i] ? "O" : "_";
        //         };
        //         Destroy.LOGGER.info((xNotZ ? " X " : " Z ") + string);
        //     };
        //     Destroy.LOGGER.info("Herrings: ");
        //     for (int i = 0; i <= 2; i++) {
        //         String string = "";
        //         for (boolean[] slice : redHerring) {
        //             string += slice[i] ? "O" : "_";
        //         };
        //         Destroy.LOGGER.info((xNotZ ? " X " : " Z ") + string);
        //     };
        //     String string = Integer.toBinaryString(signals);
        //     string = string.substring(Math.max(0, string.length() - 8));
        //     Destroy.LOGGER.info((xNotZ ? " X " : " Z ") + "signals: "+string);
        // };
        return signals;
    };

    static final class None implements ISeismologyProvider {

        private static final ISeismologyProvider.None INSTANCE = new ISeismologyProvider.None();

        private None() {};

        @Override
        public Component getName() {
            return Component.empty();
        };

        @Override
        public boolean isChunkActive(ServerLevel level, int chunkX, int chunkZ) {
            return false;
        };

        @Override
        public boolean isFalsePositive(ServerLevel level, float errorRate, int chunkX, int chunkZ) {
            return false;
        };

        @Override
        public void addToTooltip(TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
            return;
        };

    };
};
