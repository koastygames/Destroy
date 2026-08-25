package petrolpark.mc.destroy.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import net.minecraft.world.inventory.Slot;
import petrolpark.mc.destroy.DestroyItems;

@Mixin(targets = "net.minecraft.world.inventory.CartographyTableMenu$5")
public class CartographyTableMenuOutputSlotMixin {
    
    @WrapWithCondition(
        method = "onTake",
        at = @At(
            value = "INVOKE",
            target = "remove",
            ordinal = 1
        )
    )
    public boolean destroy$dontConsumeSeismometer(Slot slot, int amount) {
        return !DestroyItems.SEISMOMETER.isIn(slot.getItem());
    };
};
