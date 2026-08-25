package petrolpark.mc.destroy.core.pollution;

import java.util.WeakHashMap;

import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import petrolpark.mc.destroy.DestroyPollutionTypes;
import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.library.core.client.rendering.world.BlendedBlockColorEvent;
import petrolpark.mc.library.util.ColorHelper;

@EventBusSubscriber(Dist.CLIENT)
public class ClientPollutionEvents {

    private static final ThreadLocal<SmogCache> SMOG_CACHE = ThreadLocal.withInitial(() -> null);

    public static final int BROWN = 0xFF3F3832;
    public static final Color BROWN_COLOR = new Color(BROWN);
    
    @SubscribeEvent
    public static void onBlendedBlockColors(BlendedBlockColorEvent event) {
        if (
            !DestroyConfigs.client().pollution.smogAffectsBlockColors.get() || (
            event.getColorResolver() != BiomeColors.GRASS_COLOR_RESOLVER
            && event.getColorResolver() != BiomeColors.FOLIAGE_COLOR_RESOLVER
            && event.getColorResolver() != BiomeColors.WATER_COLOR_RESOLVER
        )) return;
        final long chunkPos = new ChunkPos(event.getPos()).toLong();
        if (SMOG_CACHE.get() == null || SMOG_CACHE.get().chunkPos() != chunkPos) {
            SMOG_CACHE.set(new SmogCache(chunkPos, PollutionHelper.getPollutionProportion(event.getLevel(), event.getPos(), DestroyPollutionTypes.SMOG.get())));
        };
        if (SMOG_CACHE.get() != null) event.setColor(Color.mixColors(event.getColor(), BROWN, SMOG_CACHE.get().smogPollutionProportion()));
    };

    static record SmogCache(long chunkPos, float smogPollutionProportion) {};

    public static final void refreshSmog(ChunkPos pos) {
        SMOG_CACHE.set(null);
        ColorHelper.refreshChunkColors(pos);
    };

    public static final WeakHashMap<Camera, LerpedFloat> FOG_MIXES = new WeakHashMap<>();

    public static final float getFogMix(ViewportEvent event) {
        return FOG_MIXES.computeIfAbsent(event.getCamera(), camera -> LerpedFloat.linear()).getValue((float)event.getPartialTick());
    };

    @SubscribeEvent
    public static final void onClientTick(ClientTickEvent.Pre event) {
        FOG_MIXES.forEach((camera, chaser) -> {
            chaser.chase(DestroyConfigs.client().pollution.smogFog.get() && camera.getEntity() != null ? PollutionHelper.getPollutionProportion(camera.getEntity().level(), camera.getBlockPosition(), DestroyPollutionTypes.SMOG.get()) : 0f, 0.02f, LerpedFloat.Chaser.LINEAR);
            chaser.tickChaser();
        });
    };

    @SubscribeEvent
    public static final void onFogColor(ViewportEvent.ComputeFogColor event) {
        if (!DestroyConfigs.client().pollution.smogFog.get()) return;
        final Color mixedColor = Color.mixColors(new Color(event.getRed(), event.getGreen(), event.getBlue(), 1f), BROWN_COLOR, getFogMix(event));
        event.setRed(mixedColor.getRedAsFloat());
        event.setGreen(mixedColor.getGreenAsFloat());
        event.setBlue(mixedColor.getBlueAsFloat());
    };

    @SubscribeEvent
    public static final void onRenderFog(ViewportEvent.RenderFog event) {
        if (!DestroyConfigs.client().pollution.smogFog.get() || getFluidFog(event.getCamera()) != FogType.NONE) return;

        event.scaleNearPlaneDistance(1f - (0.9f * getFogMix(event)));
        event.scaleFarPlaneDistance(1f - (0.7f * getFogMix(event)));
        event.setCanceled(true);
    };

    /**
     * {@link Camera#getFluidInCamera()} doesn't account for modded fluids so we need a more general check.
     */
    @SuppressWarnings("null")
    private static final FogType getFluidFog(Camera camera) {
        final Minecraft mc = Minecraft.getInstance();
        final FluidState state = mc.level.getFluidState(camera.getBlockPosition());
        if (camera.getPosition().y < (double)((float)camera.getBlockPosition().getY() + state.getHeight(mc.level, camera.getBlockPosition()))) return FogType.WATER;
        return camera.getFluidInCamera();
    };
};
