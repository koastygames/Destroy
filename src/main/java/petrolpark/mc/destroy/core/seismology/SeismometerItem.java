package petrolpark.mc.destroy.core.seismology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.TooltipFlag;
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
import petrolpark.mc.destroy.DestroyRegistries;
import petrolpark.mc.destroy.DestroyTags;
import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.destroy.util.DestroyLang;
import petrolpark.mc.library.compat.create.core.world.item.valueSettings.IValueSettingsItem;
import petrolpark.mc.library.compat.pquality.OptionalQuality;

@EventBusSubscriber
@ParametersAreNonnullByDefault
public class SeismometerItem extends Item implements IValueSettingsItem {

    public static List<Holder<ISeismologyProvider>> getProviders() {
        final List<Holder<ISeismologyProvider>> providers = DestroyRegistries.SEISMOLOGY_PROVIDERS.getTag(DestroyTags.SeismologyProviders.FOR_SEISMOMETER.tag).stream()
            .flatMap(HolderSet.Named::stream)
            .toList();
        return providers.isEmpty() ? Collections.singletonList(ISeismologyProvider.noneHolder()) : providers;
    };

    public SeismometerItem(Item.Properties properties) {
        super(properties);
    };

    @Override
    @OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(SimpleCustomRenderer.create(this, new SeismometerItemRenderer()));
	};

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return useValueSettingsItem(level, player, usedHand);
    };

    @Override
    public ValueSettingsBoard createValueSettingsBoard(Player player, InteractionHand hand, ItemStack stack) {
        final List<Holder<ISeismologyProvider>> providers = getProviders();
        return new ValueSettingsBoard(
            DestroyLang.tooltip("seismology_provider", ""),
            0,
            providers.size(),
            providers.stream().map(Holder::value).map(ISeismologyProvider::getName).toList(),
            new ValueSettingsFormatter(settings -> settings.row() >= 0 && settings.row() < providers.size() ? providers.get(settings.row()).value().getName().copy() : Component.empty())
        );
    };

    @Override
    public ValueSettings getValueSettings(ItemStack stack) {
        return new ValueSettings(getProviders().indexOf(stack.getOrDefault(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER, ISeismologyProvider.noneHolder())), 0);
    };

    @Override
    public void setValueSettings(ItemStack stack, ValueSettings valueSettings, boolean ctrlDown) {
        final int index = valueSettings.row();
        final List<Holder<ISeismologyProvider>> providers = getProviders();
        if (index >= 0 && index < providers.size()) stack.set(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER, providers.get(index));
    };

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        stack.getOrDefault(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER, ISeismologyProvider.noneHolder()).value().addToTooltip(context, tooltipComponents::add, tooltipFlag);
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    };

    public static void trigger(ServerPlayer player) {

        if (!(player.level() instanceof ServerLevel level)) return;

        final int chunkX = SectionPos.blockToSectionCoord(player.getOnPos().getX());
        final int chunkZ = SectionPos.blockToSectionCoord(player.getOnPos().getZ());

        final Object2FloatMap<Holder<ISeismologyProvider>> seismometerErrorRates = new Object2FloatOpenHashMap<>();
        final Map<ISeismologyProvider, List<ItemStack>> seismographs = new HashMap<>();

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            final ItemStack stack = player.getInventory().getItem(slot);

            if (DestroyItems.SEISMOMETER.isIn(stack)) {
                final Holder<ISeismologyProvider> seismologyProvider = stack.getOrDefault(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER, ISeismologyProvider.noneHolder());
                if (seismologyProvider.value() != ISeismologyProvider.none())
                    seismometerErrorRates.merge(
                        seismologyProvider,
                        OptionalQuality.reduce(stack, DestroyConfigs.server().oil.seismometerErrorRate.getF()),
                        Float::min // Take the minimum error of any Seismometers
                    );
            };

            if (DestroyItems.SEISMOGRAPH.isIn(stack)) {
                
                final MapItemSavedData mapData = MapItem.getSavedData(stack, level);
                if (mapData == null ||
                    SeismographItem.mapChunkCenter(chunkX) * 16 != mapData.centerX || // Must be in the range of the Seismograph
                    SeismographItem.mapChunkCenter(chunkZ) * 16 != mapData.centerZ
                ) continue;

                final ISeismologyProvider seismologyProvider = stack.getOrDefault(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER, ISeismologyProvider.noneHolder()).value();
                seismographs.computeIfAbsent(seismologyProvider, $ -> new ArrayList<>()).add(stack);
            };
        };

        if (seismometerErrorRates.isEmpty()) return;

        // Add Seismology Providers to Seismographs without one
        seismographs.getOrDefault(ISeismologyProvider.none(), Collections.emptyList()).forEach(stack -> stack.set(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER, seismometerErrorRates.keySet().iterator().next()));

        int modX = chunkX - SeismographItem.mapChunkLowerCorner(chunkX);
        int modZ = chunkZ - SeismographItem.mapChunkLowerCorner(chunkZ);
        boolean newInfo = false; // Whether new information was added to any Seismographs

        for (Object2FloatMap.Entry<Holder<ISeismologyProvider>> entry : seismometerErrorRates.object2FloatEntrySet()) {
            final ISeismologyProvider provider = entry.getKey().value();
            final float errorRate = entry.getFloatValue();

            final List<ItemStack> stacks = seismographs.get(provider);
            if (stacks == null) continue;

            final byte xSignals = ISeismologyProvider.getSignals(level, provider, errorRate, chunkX, chunkZ, true);
            final byte zSignals = ISeismologyProvider.getSignals(level, provider, errorRate, chunkX, chunkZ, false);

            for (ItemStack stack : stacks) {
                final Seismograph.Mutable seismograph = stack.getOrDefault(DestroyDataComponentTypes.SEISMOGRAPH, Seismograph.EMPTY).mutable();
                
                newInfo |= seismograph.setMark(modX, modZ, (zSignals & 1 << modZ) != 0 ? Seismograph.Mark.ACTIVE : Seismograph.Mark.INACTIVE);
                newInfo |= seismograph.discoverRow(modZ, player);
                newInfo |= seismograph.discoverColumn(modX, player);
                seismograph.getColumns()[modX] = zSignals;
                seismograph.getRows()[modZ] = xSignals;

                stack.set(DestroyDataComponentTypes.SEISMOGRAPH, seismograph.immutable());
            };
        };

        // if (seismographs.isEmpty()) player.displayClientMessage(DestroyLang.translate("tooltip.seismometer.no_seismograph").style(ChatFormatting.RED).component(), true);
        //     else if (newInfo) player.displayClientMessage(DestroyLang.translate("tooltip.seismometer.added_info").component(), true);
        //     else player.displayClientMessage(DestroyLang.translate("tooltip.seismometer.no_new_info").style(ChatFormatting.RED).component(), true);
        
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
        event.getLevel().getEntitiesOfClass(ServerPlayer.class, AABB.ofSize(event.getExplosion().center(), 16, 16, 16), $ -> true).forEach(SeismometerItem::trigger);
    };
    
};
