package petrolpark.mc.destroy.core.seismology;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.createmod.catnip.data.Iterate;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import petrolpark.mc.destroy.DestroyCriteriaTriggers;
import petrolpark.mc.destroy.client.DestroyGuiTexture;

public class Seismograph {

    protected final byte[] rows, columns;
    protected byte rowsDiscovered, columnsDiscovered;
    protected final List<Seismograph.Mark> marks;

    private static final Codec<byte[]> BYTE_ARRAY_CODEC = Codec.BYTE_BUFFER.xmap(
        buffer -> {
            final ByteBuffer copy = buffer.duplicate();
            final byte[] bytes = new byte[copy.remaining()];
            copy.get(bytes);
            return bytes;
        },
        bytes -> ByteBuffer.wrap(bytes)
    );

    public static final Codec<Seismograph> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BYTE_ARRAY_CODEC.fieldOf("rows").forGetter(s -> s.rows),
        BYTE_ARRAY_CODEC.fieldOf("columns").forGetter(s -> s.columns),
        Codec.BYTE.fieldOf("rows_discovered").forGetter(Seismograph::getRowsDiscovered),
        Codec.BYTE.fieldOf("columns_discovered").forGetter(Seismograph::getColumnsDiscovered),
        Seismograph.Mark.LIST_CODEC.fieldOf("marks").forGetter(Seismograph::getMarks)
    ).apply(instance, Seismograph::new));

    public static final StreamCodec<FriendlyByteBuf, Seismograph> STREAM_CODEC = StreamCodec.of(Seismograph::write, Seismograph::read);

    public static Seismograph read(FriendlyByteBuf buf) {
        return new Seismograph(buf.readByteArray(), buf.readByteArray(), buf.readByte(), buf.readByte(), buf.readList(Seismograph.Mark::read));
    };

    public static void write(FriendlyByteBuf buf, Seismograph seismograph) {
        buf.writeByteArray(seismograph.getRows());
        buf.writeByteArray(seismograph.getColumns());
        buf.writeByte(seismograph.getRowsDiscovered());
        buf.writeByte(seismograph.getColumnsDiscovered());
        buf.writeCollection(seismograph.getMarks(), Seismograph.Mark::write);
    };

    public static final Seismograph EMPTY = new Seismograph(new byte[8], new byte[8], (byte)0, (byte)0, Collections.nCopies(64, Seismograph.Mark.NONE));
    
    protected Seismograph(byte[] rows, byte[] columns, byte rowsDiscovered, byte columnsDiscovered, List<Seismograph.Mark> marks) {
        this.rows = rows;
        this.columns = columns;
        this.rowsDiscovered = rowsDiscovered;
        this.columnsDiscovered = columnsDiscovered;
        this.marks = marks;
    };

    public byte[] getRows() {
        return Arrays.copyOf(rows, 8);
    };

    public byte[] getColumns() {
        return Arrays.copyOf(columns, 8);
    };

    public int[] getColumnDisplayed(int column) {
        return getDisplayed(columns, column);
    };

    public int[] getRowDisplayed(int row) {
        return getDisplayed(rows, row);
    };

    public int[] getDisplayed(byte[] array, int index) {
        if (index < 0 || index > 7) return new int[0];
        final int[] numbers = new int[4];
        int numbersAdded = 0;
        int count = 0;
        for (int i = 0; i <= 8; i++) { // Iterate one more time than needed so the last count gets added on
            if (i != 8 && (array[index] & (1 << i)) != 0) { // If there is something here
                count++;
            } else if (count > 0) {
                numbers[numbersAdded] = count;
                numbersAdded++;
                count = 0;
            };
        };
        return numbers;
    };

    public byte getRowsDiscovered() {
        return rowsDiscovered;
    };

    public byte getColumnsDiscovered() {
        return columnsDiscovered;
    };

    public boolean isRowDiscovered(int row) {
        if (row < 0 || row > 7) return false;
        return (rowsDiscovered & 1 << row) != 0;
    };

    public boolean isColumnDiscovered(int column) {
        if (column < 0 || column > 7) return false;
        return (columnsDiscovered & 1 << column) != 0;
    };

    protected List<Seismograph.Mark> getMarks() {
        return marks;
    };

    public Seismograph.Mark getMark(int x, int z) {
        if (x < 0 || x > 7 || z < 0 || z > 7) return Seismograph.Mark.NONE;
        return getMarks().get(x * 8 + z);
    };

    public Seismograph.Mutable mutable() {
        return new Mutable(getRows(), getColumns(), getRowsDiscovered(), getColumnsDiscovered(), new ArrayList<>(getMarks()));
    };

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        return obj instanceof Seismograph seismograph
            && Arrays.equals(rows, seismograph.rows)
            && Arrays.equals(columns, seismograph.columns)
            && rowsDiscovered == seismograph.rowsDiscovered
            && columnsDiscovered == seismograph.columnsDiscovered
            && getMarks().equals(seismograph.getMarks());
    };

    @Override
    public int hashCode() {
        return Objects.hash(rows, columns, rowsDiscovered, columnsDiscovered, marks);
    };

    public static class Mutable extends Seismograph {

        protected Mutable(byte[] rows, byte[] columns, byte rowsDiscovered, byte columnsDiscovered, List<Mark> marks) {
            super(rows, columns, rowsDiscovered, columnsDiscovered, marks);
        };

        @Override
        public byte[] getRows() {
            return rows;
        };

        @Override
        public byte[] getColumns() {
            return columns;
        };

        /**
         * @param x
         * @param z
         * @param mark
         * @return Whether the mark changed
         */
        public boolean setMark(int x, int z, Seismograph.Mark mark) {
            if (x < 0 || x > 7 || z < 0 || z > 7) return false;

            final Seismograph.Mark markBefore = getMark(x, z);
            getMarks().set(x * 8 + z, mark);

            boolean changed = false;
            boolean actuallyPresent = mark.actuallyActive();
            boolean guessedPresent = mark.guessedActive();
            for (int xx = x - 1; xx <= x + 1; xx++) {
                if (xx < 0 || xx >= 8) {
                    actuallyPresent = guessedPresent = false;
                    continue;
                };
                for (int zz = z - 1; zz <= z + 1; zz++) {
                    if ((xx == 0) == (zz == 0)) continue;
                    if (zz < 0 || zz >= 8) {
                        actuallyPresent = guessedPresent = false;
                        continue;
                    };

                    final Seismograph.Mark adjMark = getMark(xx, zz);
                    boolean adjActuallyPresent = adjMark.actuallyActive();
                    boolean adjGuessedPresent = adjMark.guessedActive();
                    if (!adjActuallyPresent) actuallyPresent = false;
                    if (!adjGuessedPresent) guessedPresent = false;

                    if (!adjActuallyPresent || !adjGuessedPresent) continue;
  
                    adj: for (int xxx = xx - 1; xxx <= xx + 1; xxx++) {
                        if (xxx < 0 || xxx >= 8) {
                            adjActuallyPresent = adjGuessedPresent = false;
                            continue;
                        }
                        for (int zzz = zz - 1; zzz <= zz + 1; zzz++) {
                            if ((xxx == 0) == (zzz == 0)) continue;
                            if (zzz < 0 || zzz >= 8) {
                                adjActuallyPresent = adjGuessedPresent = false;
                                continue;
                            }

                            final Seismograph.Mark adjAdjMark = getMark(xxx, zzz);
                            if (!adjAdjMark.actuallyActive()) {
                                adjActuallyPresent = false;
                            }
                            if (!adjAdjMark.guessedActive()) {
                                adjGuessedPresent = false;
                            }

                            if (!adjActuallyPresent && !adjGuessedPresent) break adj;
                        };
                    };

                    if (adjActuallyPresent) {
                        getMarks().set(xx * 8 + zz, Seismograph.Mark.PRESENT);
                        changed = true;
                    } else if (adjGuessedPresent) {
                        getMarks().set(xx * 8 + zz, Seismograph.Mark.GUESSED_PRESENT);
                        changed = true;
                    };
                };
            };

            final Seismograph.Mark newMark;
            if (actuallyPresent) {
                newMark = Seismograph.Mark.PRESENT;
            } else if (guessedPresent) {
                newMark = Seismograph.Mark.GUESSED_PRESENT;
            } else {
                newMark = mark;
            };

            getMarks().set(x * 8 + z, newMark);

            return changed || markBefore != newMark;
        };

        /**
         * Let the Seismograph know we've collected data and filled in this row
         * @param row
         * @return {@code true} if that row had not been filled in already
         */
        public boolean discoverRow(int row, Player player) {
            if (row < 0 || row > 7) return false;
            byte oldRowsDiscovered = rowsDiscovered;
            rowsDiscovered |= 1 << row;
            if (player instanceof ServerPlayer sp) triggerFillSeismographAdvancement(sp);
            return oldRowsDiscovered != rowsDiscovered;
        };

        /**
         * Let the Seismograph know we've collected data and filled in this column
         * @param column
         * @return {@code true} if that column had not been filled in already
         */
        public boolean discoverColumn(int column, Player player) {
            if (column < 0 || column > 7) return false;
            byte oldColumnsDiscovered = columnsDiscovered;
            columnsDiscovered |= 1 << column;
            if (player instanceof ServerPlayer sp) fillInIfCorrect(sp);
            return oldColumnsDiscovered != columnsDiscovered;
        };

        public boolean isRowDiscovered(int row) {
            if (row < 0 || row > 7) return false;
            return (rowsDiscovered & 1 << row) != 0;
        };

        public boolean isColumnDiscovered(int column) {
            if (column < 0 || column > 7) return false;
            return (columnsDiscovered & 1 << column) != 0;
        };

        /**
         * Check if all columns and rows have been discovered and award an advancement if they have.
         * @param level
         * @param player
         * @return {@code true} if all columns and rows have been discovered, whether or not the player already has the advancement
         */
        private boolean triggerFillSeismographAdvancement(@Nullable ServerPlayer player) {
            if (rowsDiscovered == Byte.MAX_VALUE && columnsDiscovered == Byte.MAX_VALUE) {
                if (player != null) DestroyCriteriaTriggers.FILL_SEISMOGRAPH.get().trigger(player);
                return true;
            };
            return false;
        };

        public void fillInIfCorrect(@Nullable ServerPlayer player) {
            if (triggerFillSeismographAdvancement(player)) {
                for (boolean test : Iterate.trueAndFalse) {
                    for (int x = 0; x < 8; x++) {
                        for (int z = 0; z < 8; z++) {
                            boolean shouldBeActive = (rows[z] & 1 << x) != 0;
                            if (test) {
                                Mark mark = getMark(x, z);
                                if (mark == Mark.NONE) return;
                                if (mark.looksActive() != shouldBeActive) return;
                            } else {
                                setMark(x, z, shouldBeActive ? Seismograph.Mark.ACTIVE : Seismograph.Mark.INACTIVE);
                            };
                        }
                    }
                }
                if (player != null) DestroyCriteriaTriggers.COMPLETE_SEISMOGRAPH.get().trigger(player);
            };
        }

        @Override
        public Seismograph.Mutable mutable() {
            return this;
        }

        public Seismograph immutable() {
            return new Seismograph(rows, columns, rowsDiscovered, columnsDiscovered, marks);
        };
    };
    
    public enum Mark {

        NONE(null),
        PRESENT(DestroyGuiTexture.SEISMOGRAPH_TICK),
        ACTIVE(DestroyGuiTexture.SEISMOGRAPH_TICK), //TODO change
        INACTIVE(DestroyGuiTexture.SEISMOGRAPH_CROSS),
        GUESSED_PRESENT(DestroyGuiTexture.SEISMOGRAPH_GUESSED_TICK),
        GUESSED_ACTIVE(DestroyGuiTexture.SEISMOGRAPH_GUESSED_TICK), //TODO change
        GUESSED_INACTIVE(DestroyGuiTexture.SEISMOGRAPH_GUESSED_CROSS);

        public static final Codec<List<Seismograph.Mark>> LIST_CODEC = Codec.BYTE_BUFFER.comapFlatMap(
            buf -> {
                final List<Seismograph.Mark> marks = new ArrayList<>(64);
                for (int i = 0; i < 64; i++) {
                    final int j = i;
                    if (!buf.hasRemaining()) return DataResult.error(() -> "Fewer Marks (" + j + ") than expected (64)");
                    final byte b = buf.get();
                    if (b < 0 || b > 7) return DataResult.error(() -> "Unknown ordinal " + b);
                    marks.add(values()[b]);
                }
                return DataResult.success(marks);
            },
            list -> {
                final ByteBuffer buf = ByteBuffer.allocate(64);
                list.forEach(mark -> buf.put(mark.ordinalByte()));
                return buf;
            }
        );

        @Nullable
        public final DestroyGuiTexture icon;

        Mark(DestroyGuiTexture icon) {
            this.icon = icon;
        };

        public boolean actuallyActive() {
            return this == PRESENT || this == ACTIVE;
        };

        public boolean guessedActive() {
            return actuallyActive() || this == GUESSED_PRESENT || this == GUESSED_ACTIVE;
        }

        public boolean looksActive() {
            return this == PRESENT || this == ACTIVE || this == GUESSED_ACTIVE || this == GUESSED_PRESENT;
        }

        public static Seismograph.Mark read(FriendlyByteBuf buf) {
            return values()[buf.readByte()];
        };

        public static void write(FriendlyByteBuf buf, Seismograph.Mark mark) {
            buf.writeByte(mark.ordinalByte());
        };

        public byte ordinalByte() {
            return (byte)ordinal();
        };

    };
};
