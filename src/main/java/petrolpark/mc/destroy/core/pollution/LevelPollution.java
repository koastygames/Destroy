package petrolpark.mc.destroy.core.pollution;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.ParametersAreNonnullByDefault;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import petrolpark.mc.destroy.DestroyAttachmentTypes;
import petrolpark.mc.destroy.DestroyRegistries;

@EventBusSubscriber
@ParametersAreNonnullByDefault
public class LevelPollution extends Pollution<Level> {

    public static final Serializer SERIALIZER = new Serializer();

    public LevelPollution(Level level, Map<PollutionType<Level>, Integer> values) {
        super(level, values);
    };

    public static LevelPollution create(IAttachmentHolder holder) {
        if (holder instanceof Level level) return new LevelPollution(level, DestroyRegistries.LEVEL_POLLUTION_TYPES.stream().collect(Collectors.toMap(Function.identity(), t -> 0)));
        throw new IllegalArgumentException();
    };

    @Override
    protected void syncInternal() {
        if (!holder.isClientSide()) CatnipServices.NETWORK.sendToAllClients(new LevelPollutionPacket(getValues()));
    };

    public void syncTo(ServerPlayer player) {
        CatnipServices.NETWORK.sendToClient(player, new LevelPollutionPacket(getValues()));
    };

    public static final void syncTo(Player entity) {
        if (entity instanceof ServerPlayer player) player.level().getData(DestroyAttachmentTypes.LEVEL_POLLUTION).syncTo(player);
    };

    @Override
    public PollutionType.Properties getProperties(PollutionType<Level> pollutionType) {
        return PollutionHelper.getLevelPollutionTypeProperties(pollutionType);
    };

    public static class Serializer extends Pollution.Serializer<Level, LevelPollution> {

        public Serializer() {
            super(DestroyRegistries.LEVEL_POLLUTION_TYPES);
        };

        @Override
        protected LevelPollution create(Level holder, Map<PollutionType<Level>, Integer> values) {
            return new LevelPollution(holder, values);
        };

        @Override
        public Level castHolder(IAttachmentHolder holder) {
            if (holder instanceof Level level) return level;
            throw new IllegalArgumentException();
        };

    };

    @SubscribeEvent
    public static final void onPlayerLoggedIn(PlayerLoggedInEvent event) {
        syncTo(event.getEntity());
    };

    @SubscribeEvent
    public static final void onPlayerChangeDimensions(PlayerChangedDimensionEvent event) {
        syncTo(event.getEntity());
    };

    @SubscribeEvent
    public static final void onLevelTick(LevelTickEvent.Post event) {
        event.getLevel().getData(DestroyAttachmentTypes.LEVEL_POLLUTION).tick(event.getLevel().getRandom());
    };
    
};
