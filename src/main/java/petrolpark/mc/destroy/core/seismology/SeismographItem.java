package petrolpark.mc.destroy.core.seismology;

import java.util.function.Consumer;

import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderItemInFrameEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import petrolpark.mc.library.Petrolpark;

@EventBusSubscriber
@ParametersAreNonnullByDefault
public class SeismographItem extends MapItem {

    public SeismographItem(Properties properties) {
        super(properties);
    };

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        final ItemStack stack = player.getItemInHand(usedHand);
        Petrolpark.unsafeRunClient(() -> () -> openScreen(stack, usedHand));
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    };

	@OnlyIn(value = Dist.CLIENT)
	protected void openScreen(ItemStack item, InteractionHand hand) {
		ScreenOpener.open(new SeismographScreen(item, hand));
	};

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SimpleCustomRenderer.create(this, new SeismographItemRenderer()));
    };

    public static int mapChunkCenter(int chunkCoordinate) {
        return Mth.floor((chunkCoordinate + 4d) / 8d) * 8;
    };

    public static int mapChunkLowerCorner(int chunkCoordinate) {
        return mapChunkCenter(chunkCoordinate) - 4;
    };

    /** 
     * Render Seismographs like normal Items rather than Maps.
     * @param event
     */
    @SubscribeEvent
    public static void onRenderItemInFrame(RenderItemInFrameEvent event) {
        final ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof SeismographItem) {
            ItemFrame frame = event.getItemFrameEntity();
            int light = frame.getType() == EntityType.GLOW_ITEM_FRAME ? 15728880 : event.getPackedLight();
            event.getPoseStack().scale(0.5f, 0.5f, 0.5f);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, event.getPoseStack(), event.getMultiBufferSource(), frame.level(), frame.getId());
            event.setCanceled(true);
        };
    };
    
};
