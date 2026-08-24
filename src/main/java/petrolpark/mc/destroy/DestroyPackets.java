package petrolpark.mc.destroy;

import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import petrolpark.mc.destroy.core.pollution.ChunkPollutionPacket;
import petrolpark.mc.destroy.core.pollution.LevelPollutionPacket;
import petrolpark.mc.destroy.core.seismology.MarkSeismographPacket;
import petrolpark.mc.destroy.core.seismology.SeismometerSpikePacket;

public enum DestroyPackets implements BasePacketPayload.PacketTypeProvider {

    // Server -> client
    CHUNK_POLLUTION(ChunkPollutionPacket.class, ChunkPollutionPacket.STREAM_CODEC),
    LEVEL_POLLUTION(LevelPollutionPacket.class, LevelPollutionPacket.STREAM_CODEC),
    SEISMOMETER_SPIKE(SeismometerSpikePacket.class, StreamCodec.unit(SeismometerSpikePacket.INSTANCE)),

    // Client -> server
    MARK_SEISMOGRAPH(MarkSeismographPacket.class, MarkSeismographPacket.STREAM_CODEC),
    ;

    private final CatnipPacketRegistry.PacketType<?> type;

    <T extends BasePacketPayload> DestroyPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
		type = new CatnipPacketRegistry.PacketType<>(
			new CustomPacketPayload.Type<>(Destroy.asResource(name().toLowerCase())),
			clazz, codec
		);
	};

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>)type.type();
    };

    public static final void register() {
		final CatnipPacketRegistry packetRegistry = new CatnipPacketRegistry(Destroy.MOD_ID, 1);
		for (DestroyPackets packet : DestroyPackets.values()) {
			packetRegistry.registerPacket(packet.type);
		};
		packetRegistry.registerAllPackets();
	};
};
