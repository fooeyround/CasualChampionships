package net.casualuhc.uhcmod.mixin;

import net.casualuhc.uhcmod.UHCMod;
import net.casualuhc.uhcmod.managers.GameManager;
import net.casualuhc.uhcmod.utils.Networking.UHCDataBase;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "loadWorld", at = @At("HEAD"))
    private void serverLoaded(CallbackInfo ci) {
        UHCMod.onServerStart((MinecraftServer) (Object) this);
    }

    @Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerMetadata;setDescription(Lnet/minecraft/text/Text;)V", shift = At.Shift.AFTER))
    private void afterDescriptionSet(CallbackInfo ci) {
        GameManager.setDescriptor((MinecraftServer) (Object) this);
    }

    @Inject(method = "shutdown", at = @At("TAIL"))
    private void onShutdown(CallbackInfo ci) {
        UHCMod.UHCLogger.info("Stopping threads...");
        UHCDataBase.INSTANCE.shutdown();
    }
}
