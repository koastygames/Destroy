package petrolpark.mc.destroy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CartographyTableMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import petrolpark.mc.destroy.DestroyDataComponentTypes;
import petrolpark.mc.destroy.DestroyItems;

@Mixin(CartographyTableMenu.class)
public abstract class CartographyTableMenuMixin extends AbstractContainerMenu {

    protected CartographyTableMenuMixin(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
        throw new AssertionError();
    };

    @Shadow
    private ResultContainer resultContainer;
    
    @Inject(
        method = "lambda$setupResultSlot$0",
        at = @At(
            value = "INVOKE",
            target = "is",
            ordinal = 0
        ),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILEXCEPTION
    )
    public void destroy$createSeismograph(ItemStack map, ItemStack firstSlotStack, ItemStack resultOutput, Level level, BlockPos pos, CallbackInfo ci, MapItemSavedData mapData) {
        if (mapData.scale == 0 && !mapData.locked && DestroyItems.SEISMOMETER.isIn(firstSlotStack)) {
            final ItemStack seismograph = DestroyItems.SEISMOGRAPH.asStack();
            seismograph.set(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER, firstSlotStack.get(DestroyDataComponentTypes.SEISMOLOGY_PROVIDER));
            seismograph.set(DataComponents.MAP_ID, map.get(DataComponents.MAP_ID));
            resultContainer.setItem(2, seismograph);
            broadcastChanges();
            ci.cancel();
        };
    };

    @WrapOperation(
        method = "quickMoveStack",
        at = @At(
            value = "INVOKE",
            target = "is",
            ordinal = 1
        )
    )
    public boolean destroy$quickMoveSeismometer(ItemStack stack, Item item, Operation<Boolean> original) {
        return original.call(stack, item) || original.call(stack, DestroyItems.SEISMOMETER.get());
    };
};
