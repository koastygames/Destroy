package petrolpark.mc.destroy.core.seismology;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import petrolpark.mc.destroy.DestroyDataComponentTypes;
import petrolpark.mc.destroy.client.DestroyGuiTexture;
import petrolpark.mc.destroy.core.seismology.Seismograph.Mark;

@EventBusSubscriber(Dist.CLIENT)
public class SeismographRenderer {

    @SubscribeEvent
    public static final void onRenderHand(RenderHandEvent event) {
        if (event.getItemStack().getItem() instanceof SeismographItem) {
            final Minecraft mc = Minecraft.getInstance();
            final LocalPlayer player = mc.player;
            if (player == null) return;
            if (!(mc.screen instanceof SeismographScreen)) renderOneHandedSeismograph(
                mc.level, mc.player,
                event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(),
                event.getEquipProgress(), event.getHand() == InteractionHand.MAIN_HAND
                    ? player.getMainArm()
                    : player.getMainArm().getOpposite(),
                event.getSwingProgress(), event.getItemStack(),
                mc.getEntityRenderDispatcher().getItemInHandRenderer()
            );
            event.setCanceled(true);
        };
    };

    // @Override
    // protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
    //     final Minecraft mc = Minecraft.getInstance();
    //     final LocalPlayer player = mc.player;
    //     if (player == null) return;
    //     final ItemRenderer itemRenderer = mc.getItemRenderer();
    //     final ItemInHandRenderer handItemRenderer = mc.getEntityRenderDispatcher().getItemInHandRenderer();
    //     final float partialTicks = AnimationTickHolder.getPartialTicks();

    //     if (transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
    //         if (mc.screen instanceof SeismographScreen) return; // Don't render if it's already open in GUI form
            
    //         // Logic replicated from ItemInHandRenderer
    //         InteractionHand swingingHand = player.swingingArm;
    //         HumanoidArm arm = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    //         InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    //         if (swingingHand == null) swingingHand = InteractionHand.MAIN_HAND;
    //         float equippedProgress = 1f - (hand == InteractionHand.MAIN_HAND ? Mth.lerp(partialTicks, handItemRenderer.oMainHandHeight, handItemRenderer.mainHandHeight) : Mth.lerp(partialTicks, handItemRenderer.oOffHandHeight, handItemRenderer.offHandHeight));
    //         float swingProgress = swingingHand == hand ? player.getAttackAnim(partialTicks) : 0f;

    //         // Undo the Item Stack model transforms
    //         ms.popPose();
    //         ms.popPose();
    //         ms.popPose();
    //         renderOneHandedSeismograph(mc.level, player, ms, buffer, light, equippedProgress, arm, swingProgress, stack, handItemRenderer);
    //         ms.pushPose();
    //         ms.pushPose();
    //         ms.pushPose();
    //     } else {
    //         itemRenderer.render(stack, ItemDisplayContext.NONE, false, ms, buffer, light, overlay, model.getOriginalModel());
    //     };
    // };

    /**
     * Largely copied from the {@link net.minecraft.client.renderer.ItemInHandRenderer#renderOneHandedMap Minecraft source code}.
     */
    public static void renderOneHandedSeismograph(ClientLevel level, LocalPlayer player, PoseStack ms, MultiBufferSource buffer, int light, float equippedProgress, HumanoidArm hand, float swingProgress, ItemStack stack, ItemInHandRenderer itemRenderer) {
        
        // Copied from vanilla - hand
        ms.pushPose();
        float handRotation = hand == HumanoidArm.RIGHT ? 1f : -1f;
        ms.translate(handRotation * 0.125f, -0.125f, 0f);
        if (!player.isInvisible()) {
            ms.pushPose();
            ms.mulPose(Axis.ZP.rotationDegrees(10f));
            itemRenderer.renderPlayerArm(ms, buffer, light, equippedProgress, swingProgress, hand);
            ms.popPose();
        };

        // Copied from vanilla - transformation for map
        ms.translate(handRotation * 0.51f, -0.08f + equippedProgress * ItemInHandRenderer.MAP_HANDS_HEIGHT_SCALE, -0.75f);
        final float sqrtSwing = Mth.sqrt(swingProgress);
        float swingAngle = Mth.sin(sqrtSwing * (float)Math.PI);
        ms.translate(handRotation * ItemInHandRenderer.MAP_HANDS_TILT_SCALE * swingAngle, 0.4f * Mth.sin(sqrtSwing * ((float)Math.PI * 2f)) - 0.3f * swingAngle, -0.3f * Mth.sin(swingProgress * (float)Math.PI));
        ms.mulPose(Axis.XP.rotationDegrees(swingAngle * -45f));
        ms.mulPose(Axis.YP.rotationDegrees(sqrtSwing * swingAngle * -30f));

        ms.mulPose(Axis.YP.rotationDegrees(180f));
        ms.mulPose(Axis.ZP.rotationDegrees(180f));
        ms.scale(ItemInHandRenderer.MAP_PRE_ROT_SCALE, ItemInHandRenderer.MAP_PRE_ROT_SCALE, ItemInHandRenderer.MAP_PRE_ROT_SCALE);
        ms.translate(ItemInHandRenderer.MAP_GLOBAL_X_POS, ItemInHandRenderer.MAP_GLOBAL_Y_POS, ItemInHandRenderer.MAP_GLOBAL_Z_POS);

        // Scale to the size of the vanilla map
        ms.scale(ItemInHandRenderer.MAP_FINAL_SCALE, ItemInHandRenderer.MAP_FINAL_SCALE, ItemInHandRenderer.MAP_FINAL_SCALE);
        // Rescale as the Seismograph map does not fill the whole area
        ms.translate(-7f, -7f, 0f);
        ms.scale(142 / 64f, 142 / 64f, 1f);

        // Get relevant data
        final MapId mapId = stack.get(DataComponents.MAP_ID);
        final MapItemSavedData mapData = MapItem.getSavedData(stack, level);
        final Seismograph seismograph = stack.getOrDefault(DestroyDataComponentTypes.SEISMOGRAPH, Seismograph.EMPTY);

        // Render as normal
        renderSeismograph(ms, buffer, light, mapId, mapData, seismograph, (t, x, y) -> t.renderText(ms, buffer, light, x, y), true);

        ms.popPose();
    };

    public static final DestroyGuiTexture[] numberSymbols = new DestroyGuiTexture[]{DestroyGuiTexture.SEISMOGRAPH_1, DestroyGuiTexture.SEISMOGRAPH_1, DestroyGuiTexture.SEISMOGRAPH_2, DestroyGuiTexture.SEISMOGRAPH_3, DestroyGuiTexture.SEISMOGRAPH_4, DestroyGuiTexture.SEISMOGRAPH_5, DestroyGuiTexture.SEISMOGRAPH_6, DestroyGuiTexture.SEISMOGRAPH_7, DestroyGuiTexture.SEISMOGRAPH_8};

    @FunctionalInterface
    public static interface SeismographGuiTextureRenderer {
        public void render(DestroyGuiTexture texture, float x, float y);
    };
    
    public static void renderSeismograph(PoseStack ms, MultiBufferSource buffer, int light, MapId mapId, MapItemSavedData mapData, Seismograph seismograph, SeismographGuiTextureRenderer renderer, boolean invertZ) {
        if (seismograph.getMarks().size() != 64) return;
        
        ms.pushPose(); 

        float zm = invertZ ? -2f : 1f;

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();

        // Background
        renderer.render(DestroyGuiTexture.SEISMOGRAPH_BACKGROUND, 0f, 0f);
    
        // Marks
        ms.pushPose();
        ms.translate(13f, 13f, 0.02f * zm);
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                Mark mark = seismograph.getMark(x, z);
                if (mark != Mark.NONE) renderer.render(mark.icon, x * 6f, z * 6f);
            };
        };
        ms.popPose();

        // Rows
        ms.pushPose();;
        ms.translate(8f, 13f, 0.03f * zm);
        for (int z = 0; z < 8; z++) {
            ms.pushPose();
            ms.translate(0f, z * 6f, 0f);
            if (seismograph.isRowDiscovered(z)) {
                int[] numbers = seismograph.getRowDisplayed(z);
                for (int i = numbers.length - 1; i >= 0; i--) {
                    if (numbers[i] != 0) {
                        renderer.render(numberSymbols[numbers[i]], 0f, 0f);
                        ms.translate((numbers[i] <= 2) ? -2f : -3f, 0f, 0f);
                    };
                };
            } else {
                renderer.render(DestroyGuiTexture.SEISMOGRAPH_UNKNOWN, 0f, 0f);
            };
            ms.popPose();
        };
        ms.popPose();

        // Columns
        ms.pushPose();;
        ms.translate(18f, 8f, 0.03f * zm);
        TransformStack.of(ms)
            .rotateZDegrees(90);
        for (int x = 0; x < 8; x++) {
            ms.pushPose();
            ms.translate(0f, x * -6f, 0f);
            if (seismograph.isColumnDiscovered(x)) {
                int[] numbers = seismograph.getColumnDisplayed(x);
                for (int i = numbers.length - 1; i >= 0; i--) {
                    if (numbers[i] != 0) {
                        renderer.render(numberSymbols[numbers[i]], 0f, 0f);
                        ms.translate((numbers[i] <= 2) ? -2f : -3f, 0f, 0f);
                    };
                };
            } else {
                renderer.render(DestroyGuiTexture.SEISMOGRAPH_UNKNOWN, 0f, 0f);
            };
            ms.popPose();
        };
        ms.popPose();

        // Map colors
        ms.pushPose();
        ms.translate(13f, 13f,  0f);
        ms.scale(47 / 128f, 47 / 128f, 1f);
        ms.translate(0f, 0f, 0.01f * zm);
        if (mapId != null && mapData != null) Minecraft.getInstance().gameRenderer.getMapRenderer().render(ms, buffer, mapId, mapData, false, light);
        ms.popPose();

        // Overlay
        ms.pushPose();
        ms.translate(0f, 0f, 0.04f * zm);
        renderer.render(DestroyGuiTexture.SEISMOGRAPH_OVERLAY, 0f, 0f);
        ms.popPose();
        ms.popPose();
    };

};
