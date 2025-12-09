package petrolpark.mc.destroy.core.pollution;

import java.util.HashMap;
import java.util.Map;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyPackets;
import petrolpark.mc.destroy.DestroyRegistries;

public record LevelPollutionPacket(Map<PollutionType<Level>, Integer> values) implements ClientboundPacketPayload {
    
    public static final StreamCodec<RegistryFriendlyByteBuf, LevelPollutionPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.registry(DestroyRegistries.Keys.LEVEL_POLLUTION_TYPE), ByteBufCodecs.VAR_INT), LevelPollutionPacket::values,
        LevelPollutionPacket::new
    );

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.LEVEL_POLLUTION;
    };

    @Override
    public void handle(LocalPlayer player) {
        player.level().getData(DestroyAttachmentTypes.LEVEL_POLLUTION).setValues(values());
    };
};
