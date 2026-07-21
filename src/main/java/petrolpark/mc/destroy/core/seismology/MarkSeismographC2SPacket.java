package petrolpark.mc.library.destroy.content.oil.seismology;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import petrolpark.mc.destroy.core.seismology.SeismographItem.Seismograph;
import petrolpark.mc.destroy.core.seismology.SeismographItem.Seismograph.Mark;
import petrolpark.mc.library.network.packet.C2SPacket;

public class MarkSeismographC2SPacket extends C2SPacket {

    private final byte x;
    private final byte z;
    private final Mark mark;
    private final boolean mainHand;
    
    public MarkSeismographC2SPacket(FriendlyByteBuf buffer) {
        x = buffer.readByte();
        z = buffer.readByte();
        mark = buffer.readEnum(Seismograph.Mark.class);
        mainHand = buffer.readBoolean();
    };

    public MarkSeismographC2SPacket(byte x, byte z, Seismograph.Mark mark, boolean mainHand) {
        this.x = x;
        this.z = z;
        this.mark = mark;
        this.mainHand = mainHand;
    };

    @Override
    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeByte(x);
        buffer.writeByte(z);
        buffer.writeEnum(mark);
        buffer.writeBoolean(mainHand);
    };

    @Override
    public boolean handle(Supplier<Context> supplier) {
        Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            ItemStack stack = player.getItemInHand(mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
            if (mark != Seismograph.Mark.CROSS && mark != Seismograph.Mark.TICK && stack.getItem() instanceof SeismographItem) {
                Seismograph seismograph = SeismographItem.readSeismograph(stack);
                seismograph.mark(x, z, mark);
                seismograph.triggerSolveSeismographAdvancement(player.level(), player);
                SeismographItem.writeSeismograph(stack, seismograph);
            };
        });
        return true;
    };
    
};
