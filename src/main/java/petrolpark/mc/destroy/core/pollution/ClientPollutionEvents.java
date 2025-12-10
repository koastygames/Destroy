package petrolpark.mc.destroy.core.pollution;

import com.petrolpark.client.rendering.world.BlendedBlockColorEvent;
import com.petrolpark.util.ColorHelper;

import net.createmod.catnip.theme.Color;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import petrolpark.mc.destroy.DestroyPollutionTypes;
import petrolpark.mc.destroy.config.DestroyConfigs;

@EventBusSubscriber(Dist.CLIENT)
public class ClientPollutionEvents {

    private static SmogCache SMOG_CACHE = null;

    public static final int BROWN = 0xFF3F3832;
    
    @SubscribeEvent
    public static void onBlendedBlockColors(BlendedBlockColorEvent event) {
        if (
            !(DestroyConfigs.client().pollution.smogAffectsBlockColors.get()) || (
            event.getColorResolver() != BiomeColors.GRASS_COLOR_RESOLVER
            && event.getColorResolver() != BiomeColors.FOLIAGE_COLOR_RESOLVER
            && event.getColorResolver() != BiomeColors.WATER_COLOR_RESOLVER
        )) return;
        final long chunkPos = new ChunkPos(event.getPos()).toLong();
        if (SMOG_CACHE == null || SMOG_CACHE.chunkPos() != chunkPos) {
            SMOG_CACHE = new SmogCache(chunkPos, PollutionHelper.getPollutionProportion(event.getLevel(), event.getPos(), DestroyPollutionTypes.SMOG.get()));
        };
        event.setColor(Color.mixColors(event.getColor(), BROWN, SMOG_CACHE.smogPollutionProportion()));
    };

    static record SmogCache(long chunkPos, float smogPollutionProportion) {};

    public static final void refreshSmog(ChunkPos pos) {
        SMOG_CACHE = null;
        ColorHelper.refreshChunkColors(pos);
    };
};
