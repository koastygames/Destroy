package petrolpark.mc.destroy.core.seismology;

import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import petrolpark.mc.destroy.DestroyDataComponentTypes;
import petrolpark.mc.destroy.DestroyPackets;

public record MarkSeismographPacket(byte x, byte z, Seismograph.Mark mark, boolean mainHand) implements ServerboundPacketPayload {

    public static final StreamCodec<FriendlyByteBuf, MarkSeismographPacket> STREAM_CODEC = StreamCodec.of(MarkSeismographPacket::write, MarkSeismographPacket::read);

    public static MarkSeismographPacket read(FriendlyByteBuf buf) {
        return new MarkSeismographPacket(buf.readByte(), buf.readByte(), Seismograph.Mark.values()[buf.readByte()], buf.readBoolean());
    };

    public static void write(FriendlyByteBuf buf, MarkSeismographPacket packet) {
        buf.writeByte(packet.x());
        buf.writeByte(packet.z());
        buf.writeByte(packet.mark().ordinalByte());
        buf.writeBoolean(packet.mainHand());
    };

    @Override
    @SuppressWarnings("null")
    public void handle(ServerPlayer player) {
        final ItemStack stack = player.getItemInHand(mainHand() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        if (stack.has(DestroyDataComponentTypes.SEISMOGRAPH)) {
            final Seismograph.Mutable seismograph = stack.get(DestroyDataComponentTypes.SEISMOGRAPH).mutable();
            seismograph.setMark(x(), z(), mark());
            seismograph.fillInIfCorrect(player);
            stack.set(DestroyDataComponentTypes.SEISMOGRAPH, seismograph.immutable());
        };
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.MARK_SEISMOGRAPH;
    };
    
};
