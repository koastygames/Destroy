package petrolpark.mc.destroy.core.pollution;

import java.util.HashMap;
import java.util.Map;

import com.petrolpark.util.CodecHelper;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.DestroyRegistries;

public record ChunkPollutionPacket(ChunkPos pos, Map<PollutionType<ChunkAccess>, Integer> values) implements ClientboundPacketPayload {
    
    public static final StreamCodec<RegistryFriendlyByteBuf, ChunkPollutionPacket> STREAM_CODEC = StreamCodec.composite(
        CodecHelper.CHUNK_POS_STREAM_CODEC, ChunkPollutionPacket::pos,
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(DestroyRegistries.Keys.CHUNK_POLLUTION_TYPE), ByteBufCodecs.VAR_INT), ChunkPollutionPacket::values,
        ChunkPollutionPacket::new
    );

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.CHUNK_POLLUTION;
    };

    @Override
    public void handle(LocalPlayer player) {
        player.level().getChunk(pos.x, pos.z).getData(DestroyAttachmentTypes.CHUNK_POLLUTION).setValues(values());
        final Minecraft mc = Minecraft.getInstance();

        // Re-render chunk
        for (int y = mc.level.getMinSection(); y < mc.level.getMaxSection(); y++) mc.levelRenderer.setSectionDirty(pos.x, y, pos.z);
        mc.level.clearTintCaches();
    };
};
