package petrolpark.mc.destroy.mixin.client;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.core.seismology.Seismograph;
import petrolpark.mc.destroy.core.seismology.SeismographRenderer;

@Mixin(CartographyTableScreen.class)
public abstract class CartographyTableScreenMixin extends AbstractContainerScreen<CartographyTableMenu> {
    
    public CartographyTableScreenMixin(CartographyTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        throw new AssertionError();
    };

    @Shadow
    private static ResourceLocation ERROR_SPRITE;

    @Shadow
    private static ResourceLocation DUPLICATED_MAP_SPRITE;

    @Shadow
    abstract void renderMap(GuiGraphics guiGraphics, @Nullable MapId mapId, @Nullable MapItemSavedData mapData, int x, int y, float scale);

    @Inject(
        method = "renderBg",
        at = @At(
            value = "INVOKE",
            target = "getSavedData",
            shift = Shift.BY,
            by = 5
        ),
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    public void destroy$seismographError(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci, int i, int j, ItemStack modifier, boolean hasMap, boolean hasPaper, boolean hasGlassPane, ItemStack map, MapId mapId, boolean isMaxSize, MapItemSavedData mapData) {
        if (DestroyItems.SEISMOMETER.isIn(modifier) && (mapData.scale != 0 || mapData.locked)) {
            guiGraphics.blitSprite(ERROR_SPRITE, i + 35, j + 31, 28, 21);
        };
    };

    @Inject(
        method = "renderResultingMap",
        at = @At("HEAD"),
        cancellable = true
    )
    public void destroy$renderSeismograph(
        GuiGraphics guiGraphics,
        @Nullable MapId mapId,
        @Nullable MapItemSavedData mapData,
        boolean hasMap,
        boolean hasPaper,
        boolean hasGlassPane,
        boolean isMaxSize,
        CallbackInfo ci
    ) {
        if (DestroyItems.SEISMOMETER.isIn(menu.getSlot(1).getItem()) && mapData != null && mapData.scale == 0 && !mapData.locked) {

            guiGraphics.blitSprite(DUPLICATED_MAP_SPRITE, leftPos + 67 + 16, topPos + 13, 50, 66);
            renderMap(guiGraphics, mapId, mapData, leftPos + 86, topPos + 16, 0.34f);

            guiGraphics.pose().pushPose(); {
                guiGraphics.pose().translate(0f, 0f, 1f);

                guiGraphics.pose().pushPose(); {
                    guiGraphics.pose().translate(leftPos + 67, topPos + 29, 1f);
                    guiGraphics.pose().scale(0.754375f, 0.754375f, 1f);
                    SeismographRenderer.renderSeismograph(guiGraphics.pose(), guiGraphics.bufferSource(), 15728880, mapId, mapData, Seismograph.EMPTY, (t, x, y) -> t.render(guiGraphics, (int)x, (int)y), false);
                    guiGraphics.flush();
                }; guiGraphics.pose().popPose();

            }; guiGraphics.pose().pushPose();

            ci.cancel();
        };
    };
};
