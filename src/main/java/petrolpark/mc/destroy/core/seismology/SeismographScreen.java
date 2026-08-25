package petrolpark.mc.destroy.core.seismology;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.vertex.PoseStack;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import petrolpark.mc.destroy.DestroyDataComponentTypes;

@ParametersAreNonnullByDefault
public class SeismographScreen extends AbstractSimiScreen {

    private final Minecraft mc;

    private final Seismograph.Mutable seismograph;
    private final MapId mapId;
    private final MapItemSavedData mapData;
    private final InteractionHand hand;

    public static final int SCALE = 3;

    @SuppressWarnings("null")
    public SeismographScreen(ItemStack stack, InteractionHand hand) {
        mc = Minecraft.getInstance();

        mapId = stack.get(DataComponents.MAP_ID);
        mapData = SeismographItem.getSavedData(mapId, mc.level);
        seismograph = stack.getOrDefault(DestroyDataComponentTypes.SEISMOGRAPH, Seismograph.EMPTY).mutable();

        this.hand = hand;
    };

    @Override
    protected void init() {
        setWindowSize(64 * SCALE, 64 * SCALE);
        super.init();
    };

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        final double x = (mouseX - guiLeft) / (double)SCALE;
        final double y = (mouseY - guiTop) / (double)SCALE;
        if (seismograph != null && x > 13 && y > 13 && x < 60 && y < 60) {
            final int gridX = (int)(x - 13) / 6;
            final int gridY = (int)(y - 13) / 6;
            Seismograph.Mark mark = seismograph.getMark(gridX, gridY);
            Seismograph.Mark newMark = null;
            switch (mark) {
                case PRESENT: case ACTIVE: case INACTIVE:
                    break;
                case NONE: {
                    newMark = Seismograph.Mark.GUESSED_ACTIVE;
                    break;
                } case GUESSED_PRESENT: case GUESSED_ACTIVE: {
                    newMark = Seismograph.Mark.GUESSED_INACTIVE;
                    break;
                } case GUESSED_INACTIVE: {
                    newMark = Seismograph.Mark.NONE;
                    break;
                }
            };
            if (newMark != null) {
                seismograph.setMark(gridX, gridY, newMark);
                seismograph.fillInIfCorrect(null); // Won't actually trigger the advancement, but will fill in the grid if player is correct
                CatnipServices.NETWORK.sendToServer(new MarkSeismographPacket((byte)gridX, (byte)gridY, newMark, hand == InteractionHand.MAIN_HAND));
            };
        };
        return super.mouseClicked(mouseX, mouseY, button);
    };

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        PoseStack ms = graphics.pose();
        ms.pushPose();
        ms.translate(guiLeft, guiTop, 0f);
        ms.scale(3f, 3f, 3f);
        SeismographRenderer.renderSeismograph(ms, graphics.bufferSource(), 15728880, mapId, mapData, seismograph, (t, x, y) -> t.render(graphics, (int)x, (int)y), false);
        ms.popPose();
    };
    
};
