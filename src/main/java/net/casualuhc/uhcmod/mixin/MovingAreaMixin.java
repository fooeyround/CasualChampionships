package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.utils.event.EventHandler;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldBorder.MovingArea.class)
public abstract class MovingAreaMixin implements WorldBorder.Area {
	/**
	 * This is like a super jank way of doing it,
	 * but since there are 3 worlds (in vanilla)
	 * we just trigger once every 3 times...
	 */
	@Unique
	private static int counter = 0;

	@Inject(method = "getAreaInstance", at = @At("RETURN"))
	private void onGetInstance(CallbackInfoReturnable<WorldBorder.Area> cir) {
		if (cir.getReturnValue() != this && ++counter == 3) {
			EventHandler.onWorldBorderFinishShrinking();
			counter = 0;
		}
	}
}
