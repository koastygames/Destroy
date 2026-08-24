package petrolpark.mc.destroy.core.seismology;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import petrolpark.mc.destroy.DestroyPackets;

public class SeismometerSpikePacket implements ClientboundPacketPayload {

    public static final SeismometerSpikePacket INSTANCE = new SeismometerSpikePacket();

    private SeismometerSpikePacket() {};

    // @Override
    // public void toBytes(FriendlyByteBuf buffer) {};

    // @Override
    // public boolean handle(Supplier<Context> supplier) {
    //     NetworkEvent.Context context = supplier.get();
    //     context.enqueueWork(() -> {
    //         SeismometerItemRenderer.spike();
    //     });
    //     return true;
    // };

    @Override
    public void handle(LocalPlayer player) {
        SeismometerItemRenderer.spike();
    };

    @Override
    public PacketTypeProvider getTypeProvider() {
        return DestroyPackets.SEISMOMETER_SPIKE;
    };
    
};
