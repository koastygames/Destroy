package petrolpark.mc.destroy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.world.item.ItemStack;
import petrolpark.mc.destroy.DestroyItems;

@Mixin(targets = "net.minecraft.world.inventory.CartographyTableMenu$4")
public class CartographyTableMenuInputSlotMixin {
    
    @ModifyReturnValue(
        method = "mayPlace",
        at = @At("RETURN")
    )
    public boolean destroy$allowSeismometer(boolean original, ItemStack stack) {
        return original || DestroyItems.SEISMOMETER.isIn(stack);
    };
};
