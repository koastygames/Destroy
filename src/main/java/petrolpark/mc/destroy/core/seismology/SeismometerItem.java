package petrolpark.mc.library.destroy.content.oil.seismology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import petrolpark.mc.destroy.DestroyCriteriaTriggers;
import petrolpark.mc.destroy.DestroyDataComponentTypes;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.library.compat.pquality.OptionalQuality;

@EventBusSubscriber
@ParametersAreNonnullByDefault
public class SeismometerItem extends Item {

    public SeismometerItem(Properties properties) {
        super(properties);
    };

    @Override
    @OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(SimpleCustomRenderer.create(this, new SeismometerItemRenderer()));
	};

    public static void trigger(ServerPlayer player) {

        if (!(player.level() instanceof ServerLevel level)) return;

        final int chunkX = SectionPos.blockToSectionCoord(player.getOnPos().getX());
        final int chunkZ = SectionPos.blockToSectionCoord(player.getOnPos().getZ());

        final Object2FloatMap<ISeismologyProvider> seismometerErrorRates = new Object2FloatOpenHashMap<>();
        final Map<ISeismologyProvider, List<ItemStack>> seismographs = new HashMap<>();

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            final ItemStack stack = player.getInventory().getItem(slot);

            if (DestroyItems.SEISMOMETER.isIn(stack)) {
                final ISeismologyProvider seismologyProvider = stack.get(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER);
                if (seismologyProvider != null && seismologyProvider != ISeismologyProvider.none())
                    seismometerErrorRates.merge(
                        seismologyProvider,
                        OptionalQuality.reduce(stack, DestroyConfigs.server().oil.seismometerErrorRate.getF()),
                        Float::min // Take the minimum error of any Seismometers
                    );
            };

            if (DestroyItems.SEISMOMETER.isIn(stack)) {
                
                final MapItemSavedData mapData = MapItem.getSavedData(stack, level);
                if (mapData == null ||
                    SeismographItem.mapChunkCenter(chunkX) * 16 != mapData.centerX || // Must be in the range of the Seismograph
                    SeismographItem.mapChunkCenter(chunkZ) * 16 != mapData.centerZ
                ) continue;

                final ISeismologyProvider seismologyProvider = stack.getOrDefault(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER, ISeismologyProvider.none());
                seismographs.computeIfAbsent(seismologyProvider, $ -> new ArrayList<>()).add(stack);
            };
        };

        if (seismometerErrorRates.isEmpty()) return;

        // Add Seismology Providers to Seismographs without one
        seismographs.getOrDefault(ISeismologyProvider.none(), Collections.emptyList()).forEach(stack -> stack.set(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER, seismometerErrorRates.iterator().next()));


        int modX = chunkX - SeismographItem.mapChunkLowerCorner(chunkX);
        int modZ = chunkZ - SeismographItem.mapChunkLowerCorner(chunkZ);
        boolean newInfo = false; // Whether new information was added to any Seismographs

        for (Object2FloatMap.Entry<ISeismologyProvider> entry : seismometerErrorRates.object2FloatEntrySet()) {
            final ISeismologyProvider provider = entry.getKey();
            final float errorRate = entry.getFloatValue();

            final byte xSignals = ISeismologyProvider.getSignals(level, provider, errorRate, chunkX, chunkZ, true);
            final byte zSignals = ISeismologyProvider.getSignals(level, provider, errorRate, chunkX, chunkZ, false);

            for (ItemStack stack : seismographs.get(provider)) {
                final Seismograph.Mutable seismograph = stack.getOrDefault(DestroyDataComponentTypes.SEISMOGRAPH, Seismograph.empty()).mutable();
                
                newInfo |= seismograph.setMark(modX, modZ, (zSignals & 1 << modZ) != 0 ? Seismograph.Mark.ACTIVE : Seismograph.Mark.INACTIVE);
                newInfo |= seismograph.discoverRow(modZ, player);
                newInfo |= seismograph.discoverColumn(modX, player);
                seismograph.getColumns()[modX] = zSignals;
                seismograph.getRows()[modZ] = xSignals;

                stack.set(DestroyDataComponentTypes.SEISMOGRAPH, seismograph.immutable());
            };
        };

        if (seismographs.isEmpty()) player.displayClientMessage(DestroyLang.translate("tooltip.seismometer.no_seismograph").style(ChatFormatting.RED).component(), true);
            else if (newInfo) player.displayClientMessage(DestroyLang.translate("tooltip.seismometer.added_info").component(), true);
            else player.displayClientMessage(DestroyLang.translate("tooltip.seismometer.no_new_info").style(ChatFormatting.RED).component(), true);
        
        

        // Update the animation of the Seismometer(s)
        CatnipServices.NETWORK.sendToClient(player, SeismometerSpikePacket.INSTANCE);
        // Award Advancement if some Seismograph info was filled in
        if (newInfo) DestroyCriteriaTriggers.USE_SEISMOMETER.get().trigger(player);
    };

    /**
     * Trigger Handheld Seismometers when there are nearby Explosions.
     */
    @SubscribeEvent
    public static final void onExplosion(ExplosionEvent.Start event) {
        Level level = event.getLevel();
        level.getEntitiesOfClass(Player.class, AABB.ofSize(event.getExplosion().getPosition(), 16, 16, 16), $ -> true).forEach(SeismometerItem::trigger);
    };
    
};
